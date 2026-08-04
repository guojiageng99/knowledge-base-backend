package com.knowledge.base.userauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.userauth.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
    List<TeamMember> selectByTeamId(@Param("teamId") Long teamId);
    List<TeamMember> selectByUserId(@Param("userId") Long userId);
    int deleteByTeamIdAndUserIds(@Param("teamId") Long teamId, @Param("userIds") List<Long> userIds);
    Long countByTeamId(@Param("teamId") Long teamId);
}
