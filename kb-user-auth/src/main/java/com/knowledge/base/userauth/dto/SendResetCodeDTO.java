package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendResetCodeDTO {
    @NotBlank @Email
    private String email;
}
