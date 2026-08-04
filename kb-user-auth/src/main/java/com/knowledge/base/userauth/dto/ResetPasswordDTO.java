package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank @Email
    private String email;
    @NotBlank @Pattern(regexp = "\\d{6}")
    private String code;
    @NotBlank @Size(min = 6, max = 50)
    private String newPassword;
}
