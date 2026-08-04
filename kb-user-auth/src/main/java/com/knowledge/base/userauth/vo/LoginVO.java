package com.knowledge.base.userauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应信息")
public class LoginVO implements Serializable {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo implements Serializable {
        private Long userId;
        private String username;
        private String nickname;
        private String email;
        private String phone;
        private String avatar;
        private List<String> roles;
        private List<String> permissions;
    }
}
