package com.knowledge.base.userauth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kb_team_member")
public class TeamMember implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long teamId;
    private Long userId;
    private String memberRole;
    private LocalDateTime joinTime;
    private Long createBy;
}
