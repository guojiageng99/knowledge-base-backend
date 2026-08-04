package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserProfileDTO implements Serializable {
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    private String username;
    @Email(message = "邮箱格式不正确")
    private String email;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    private String avatar;
    private String realName;
    private String department;
    private String position;
    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String remark;
}
