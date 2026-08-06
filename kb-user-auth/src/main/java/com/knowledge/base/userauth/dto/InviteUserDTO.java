package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InviteUserDTO {
    @NotBlank @Size(min = 4, max = 20)
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(max = 50)
    private String realName;
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$")
    private String phone;
    @NotNull
    private Long teamId;
}
