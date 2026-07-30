package com.knowledge.base.userauth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Integer userType;
    private String remark;
}
