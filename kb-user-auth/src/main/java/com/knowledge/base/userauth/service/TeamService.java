package com.knowledge.base.userauth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.userauth.dto.TeamCreateDTO;
import com.knowledge.base.userauth.dto.TeamQueryDTO;
import com.knowledge.base.userauth.dto.TeamUpdateDTO;
import com.knowledge.base.userauth.entity.Team;
import com.knowledge.base.userauth.vo.TeamMemberVO;
import com.knowledge.base.userauth.vo.TeamVO;

import java.util.List;

public interface TeamService extends IService<Team> {
    Long createTeam(TeamCreateDTO dto);
    Boolean updateTeam(TeamUpdateDTO dto);
    Boolean deleteTeam(Long teamId);
    TeamVO getTeamDetail(Long teamId);
    PageResult<TeamVO> pageTeams(TeamQueryDTO dto);
    List<TeamVO> getTeamTree();
    Boolean addTeamMembers(Long teamId, List<Long> userIds);
    Boolean removeTeamMembers(Long teamId, List<Long> userIds);
    List<TeamMemberVO> getTeamMembers(Long teamId);
}
