package com.knowledge.base.userauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.userauth.dto.TeamCreateDTO;
import com.knowledge.base.userauth.dto.TeamQueryDTO;
import com.knowledge.base.userauth.dto.TeamUpdateDTO;
import com.knowledge.base.userauth.entity.Team;
import com.knowledge.base.userauth.entity.TeamMember;
import com.knowledge.base.userauth.entity.User;
import com.knowledge.base.userauth.mapper.TeamMapper;
import com.knowledge.base.userauth.mapper.TeamMemberMapper;
import com.knowledge.base.userauth.mapper.UserMapper;
import com.knowledge.base.userauth.service.TeamService;
import com.knowledge.base.userauth.vo.TeamMemberVO;
import com.knowledge.base.userauth.vo.TeamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    private static final Long ROOT_TEAM_ID = 0L;

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTeam(TeamCreateDTO dto) {
        if (Boolean.TRUE.equals(teamMapper.checkTeamCodeExists(dto.getTeamCode(), null))) {
            throw new BusinessException("Team code already exists");
        }
        requireEnabledUser(dto.getLeaderId());

        Team team = new Team();
        team.setId(SnowflakeIdGenerator.nextId());
        team.setTeamName(dto.getTeamName().trim());
        team.setTeamCode(dto.getTeamCode().trim());
        team.setDescription(dto.getDescription());
        team.setLeaderId(dto.getLeaderId());
        team.setStatus(1);
        team.setSort(0);
        team.setMemberCount(0);
        team.setDocCount(0);

        Long parentId = dto.getParentId();
        if (parentId == null || ROOT_TEAM_ID.equals(parentId)) {
            team.setParentId(ROOT_TEAM_ID);
            team.setLevel(1);
            team.setPath("/" + team.getId());
        } else {
            Team parent = requireTeam(parentId);
            team.setParentId(parent.getId());
            team.setLevel(parent.getLevel() + 1);
            team.setPath(parent.getPath() + "/" + team.getId());
        }
        teamMapper.insert(team);
        return team.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTeam(TeamUpdateDTO dto) {
        Team team = requireTeam(dto.getId());
        if (StringUtils.hasText(dto.getTeamCode()) && !dto.getTeamCode().equals(team.getTeamCode())
                && Boolean.TRUE.equals(teamMapper.checkTeamCodeExists(dto.getTeamCode(), dto.getId()))) {
            throw new BusinessException("Team code already exists");
        }
        if (dto.getLeaderId() != null) {
            requireEnabledUser(dto.getLeaderId());
        }
        if (StringUtils.hasText(dto.getTeamName())) team.setTeamName(dto.getTeamName().trim());
        if (StringUtils.hasText(dto.getTeamCode())) team.setTeamCode(dto.getTeamCode().trim());
        if (dto.getDescription() != null) team.setDescription(dto.getDescription());
        if (dto.getLeaderId() != null) team.setLeaderId(dto.getLeaderId());
        if (dto.getStatus() != null) team.setStatus(dto.getStatus());
        return teamMapper.updateById(team) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(Long teamId) {
        requireTeam(teamId);
        if (teamMapper.selectCount(new LambdaQueryWrapper<Team>().eq(Team::getParentId, teamId)) > 0) {
            throw new BusinessException("The team has child teams and cannot be deleted");
        }
        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId));
        return teamMapper.deleteById(teamId) > 0;
    }

    @Override
    public TeamVO getTeamDetail(Long teamId) {
        Team team = requireTeam(teamId);
        return toTeamVO(team, parentMap(List.of(team)), usersById(List.of(team.getLeaderId())));
    }

    @Override
    public PageResult<TeamVO> pageTeams(TeamQueryDTO dto) {
        long current = dto.getCurrent() == null ? 1 : Math.max(dto.getCurrent(), 1);
        long size = dto.getSize() == null ? 10 : Math.min(Math.max(dto.getSize(), 1), 100);
        LambdaQueryWrapper<Team> query = new LambdaQueryWrapper<Team>()
                .like(StringUtils.hasText(dto.getTeamName()), Team::getTeamName, dto.getTeamName())
                .eq(StringUtils.hasText(dto.getTeamCode()), Team::getTeamCode, dto.getTeamCode())
                .eq(dto.getParentId() != null, Team::getParentId, dto.getParentId())
                .eq(dto.getStatus() != null, Team::getStatus, dto.getStatus())
                .orderByAsc(Team::getLevel).orderByAsc(Team::getSort).orderByDesc(Team::getCreateTime);
        Page<Team> page = teamMapper.selectPage(new Page<>(current, size), query);
        Map<Long, Team> parents = parentMap(page.getRecords());
        Map<Long, User> leaders = usersById(page.getRecords().stream().map(Team::getLeaderId).toList());
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(team -> toTeamVO(team, parents, leaders)).toList());
    }

    @Override
    public List<TeamVO> getTeamTree() {
        List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .eq(Team::getStatus, 1).orderByAsc(Team::getSort).orderByAsc(Team::getId));
        Map<Long, Team> parents = teams.stream().collect(Collectors.toMap(Team::getId, Function.identity()));
        Map<Long, User> leaders = usersById(teams.stream().map(Team::getLeaderId).toList());
        Map<Long, List<Team>> childrenByParent = teams.stream()
                .collect(Collectors.groupingBy(Team::getParentId));
        return buildTree(childrenByParent, ROOT_TEAM_ID, parents, leaders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addTeamMembers(Long teamId, List<Long> userIds) {
        requireTeam(teamId);
        Set<Long> distinctUserIds = distinctIds(userIds);
        if (distinctUserIds.isEmpty()) throw new BusinessException("User IDs must not be empty");
        Map<Long, User> users = usersById(distinctUserIds);
        if (users.size() != distinctUserIds.size()) throw new BusinessException("A user does not exist");
        for (User user : users.values()) {
            if (!Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException("Disabled users cannot be added to a team");
        }
        Set<Long> existing = teamMemberMapper.selectByTeamId(teamId).stream().map(TeamMember::getUserId).collect(Collectors.toSet());
        for (Long userId : distinctUserIds) {
            if (!existing.contains(userId)) {
                TeamMember member = new TeamMember();
                member.setId(SnowflakeIdGenerator.nextId());
                member.setTeamId(teamId);
                member.setUserId(userId);
                member.setMemberRole("member");
                member.setJoinTime(LocalDateTime.now());
                teamMemberMapper.insert(member);
            }
        }
        refreshMemberCount(teamId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTeamMembers(Long teamId, List<Long> userIds) {
        requireTeam(teamId);
        Set<Long> distinctUserIds = distinctIds(userIds);
        if (distinctUserIds.isEmpty()) throw new BusinessException("User IDs must not be empty");
        teamMemberMapper.deleteByTeamIdAndUserIds(teamId, new ArrayList<>(distinctUserIds));
        refreshMemberCount(teamId);
        return true;
    }

    @Override
    public List<TeamMemberVO> getTeamMembers(Long teamId) {
        requireTeam(teamId);
        List<TeamMember> members = teamMemberMapper.selectByTeamId(teamId);
        Map<Long, User> users = usersById(members.stream().map(TeamMember::getUserId).toList());
        return members.stream().map(member -> {
            User user = users.get(member.getUserId());
            return TeamMemberVO.builder().userId(member.getUserId())
                    .username(user == null ? "Unknown user" : user.getUsername())
                    .realName(user == null ? null : user.getRealName())
                    .avatar(user == null ? null : user.getAvatar())
                    .role(member.getMemberRole()).joinedAt(member.getJoinTime()).build();
        }).toList();
    }

    private Team requireTeam(Long teamId) {
        if (teamId == null) throw new BusinessException("Team ID must not be null");
        Team team = teamMapper.selectById(teamId);
        if (team == null) throw new BusinessException("Team does not exist");
        return team;
    }

    private void requireEnabledUser(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("Team leader does not exist or is disabled");
        }
    }

    private void refreshMemberCount(Long teamId) {
        teamMapper.updateMemberCount(teamId, Math.toIntExact(teamMemberMapper.countByTeamId(teamId)));
    }

    private Set<Long> distinctIds(Collection<Long> ids) {
        if (ids == null) return Set.of();
        return ids.stream().filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }

    private Map<Long, Team> parentMap(List<Team> teams) {
        Set<Long> parentIds = teams.stream().map(Team::getParentId)
                .filter(parentId -> parentId != null && !ROOT_TEAM_ID.equals(parentId)).collect(Collectors.toSet());
        if (parentIds.isEmpty()) return Map.of();
        return teamMapper.selectByIds(parentIds).stream().collect(Collectors.toMap(Team::getId, Function.identity()));
    }

    private Map<Long, User> usersById(Collection<Long> userIds) {
        Set<Long> ids = userIds.stream().filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return userMapper.selectByIds(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private List<TeamVO> buildTree(Map<Long, List<Team>> childrenByParent, Long parentId,
                                   Map<Long, Team> parents, Map<Long, User> leaders) {
        return childrenByParent.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(Team::getSort).thenComparing(Team::getId))
                .map(team -> {
                    TeamVO vo = toTeamVO(team, parents, leaders);
                    vo.setChildren(buildTree(childrenByParent, team.getId(), parents, leaders));
                    return vo;
                }).toList();
    }

    private TeamVO toTeamVO(Team team, Map<Long, Team> parents, Map<Long, User> leaders) {
        Team parent = parents.get(team.getParentId());
        User leader = leaders.get(team.getLeaderId());
        return TeamVO.builder().id(team.getId()).teamName(team.getTeamName()).teamCode(team.getTeamCode())
                .description(team.getDescription()).parentId(team.getParentId()).parentName(parent == null ? null : parent.getTeamName())
                .level(team.getLevel()).memberCount(team.getMemberCount()).docCount(team.getDocCount())
                .leaderId(team.getLeaderId()).leaderName(leader == null ? null : leader.getUsername())
                .status(team.getStatus()).createdAt(team.getCreateTime()).build();
    }
}
