package com.knowledge.base.userauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

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
    private Integer status;
}
