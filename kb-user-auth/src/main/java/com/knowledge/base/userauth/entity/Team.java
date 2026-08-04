package com.knowledge.base.userauth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_team")
public class Team extends BaseEntity {
    private String teamName;
    private String teamCode;
    private String description;
    private Long parentId;
    private Integer level;
    private String path;
    private Integer memberCount;
    private Integer docCount;
    private Long leaderId;
    private Integer status;
    private Integer sort;
}
