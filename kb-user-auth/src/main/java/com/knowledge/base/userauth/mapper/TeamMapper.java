package com.knowledge.base.userauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.userauth.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    List<Team> selectByParentId(@Param("parentId") Long parentId);
    Team selectByTeamCode(@Param("teamCode") String teamCode);
    List<Team> selectByStatus(@Param("status") Integer status);
    List<Team> selectByLevel(@Param("level") Integer level);
    List<Team> selectRootTeams();
    List<Team> selectByLeaderId(@Param("leaderId") Long leaderId);
    List<Team> selectByPathPrefix(@Param("path") String path);
    List<Team> selectTeamTree();
    int updateMemberCount(@Param("teamId") Long teamId, @Param("count") Integer count);
    int incrementMemberCount(@Param("teamId") Long teamId);
    int decrementMemberCount(@Param("teamId") Long teamId);
    int updateDocumentCount(@Param("teamId") Long teamId, @Param("count") Integer count);
    int incrementDocumentCount(@Param("teamId") Long teamId);
    int decrementDocumentCount(@Param("teamId") Long teamId);
    Boolean checkTeamCodeExists(@Param("teamCode") String teamCode, @Param("id") Long id);
    Long countAll();
    Long countEnabled();
}
