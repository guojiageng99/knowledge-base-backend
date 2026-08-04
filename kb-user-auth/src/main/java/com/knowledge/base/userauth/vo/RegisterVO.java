package com.knowledge.base.userauth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterVO {
    private Long userId;
    private boolean emailVerificationRequired;
    private String message;
    private LoginVO loginInfo;
}
