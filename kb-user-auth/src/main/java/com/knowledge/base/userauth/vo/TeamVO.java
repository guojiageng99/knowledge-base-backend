package com.knowledge.base.userauth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamVO implements Serializable {
    private Long id;
    private String teamName;
    private String teamCode;
    private String description;
    private Long parentId;
    private String parentName;
    private Integer level;
    private Integer memberCount;
    private Integer docCount;
    private Long leaderId;
    private String leaderName;
    private Integer status;
    private LocalDateTime createdAt;
    private List<TeamVO> children;
}
