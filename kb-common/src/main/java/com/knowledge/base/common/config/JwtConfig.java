package com.knowledge.base.common.config;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret = "aW52ZW50ZW1lLWtub3dsZWRnZS1iYXNlLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLW11c3QtYmUtbG9uZy1lbm91Z2g=";
    private Long expiration = 7200L;
    private Long refreshExpiration = 604800L;
    private String issuer = "knowledge-base";

    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
