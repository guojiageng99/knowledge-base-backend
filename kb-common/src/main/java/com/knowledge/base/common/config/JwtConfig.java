package com.knowledge.base.common.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret = "knowledge-base-secret-key-for-jwt-token-generation-must-be-long-enough";
    private Long expiration = 7200L;
    private Long refreshExpiration = 604800L;
    private String issuer = "knowledge-base";

    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
