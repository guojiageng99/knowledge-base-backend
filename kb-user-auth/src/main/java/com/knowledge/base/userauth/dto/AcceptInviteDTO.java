package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInviteDTO {
    @NotBlank
    private String token;
    @NotBlank @Size(min = 8, max = 50)
    private String password;
    @NotBlank
    private String confirmPassword;
}
