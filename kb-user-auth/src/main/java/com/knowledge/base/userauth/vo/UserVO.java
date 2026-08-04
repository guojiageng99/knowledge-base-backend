package com.knowledge.base.userauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String realName;
    private String department;
    private String position;
    private String remark;
    private Integer status;
    private java.time.LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
    private List<String> roles;
    private List<String> permissions;
}
