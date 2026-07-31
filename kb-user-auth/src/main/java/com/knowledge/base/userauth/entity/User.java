package com.knowledge.base.userauth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_user")
public class User extends BaseEntity {

    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private String realName;
    private String department;
    private String position;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;

    @TableField(exist = false)
    private Integer isDeleted;
}
