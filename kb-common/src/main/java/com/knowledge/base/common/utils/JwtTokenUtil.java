package com.knowledge.base.common.utils;

import com.knowledge.base.common.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenUtil {

    @Resource
    private JwtConfig jwtConfig;

    public String generateAccessToken(Long userId) {
        return generateToken(userId, jwtConfig.getExpiration() * 1000, "access");
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, jwtConfig.getRefreshExpiration() * 1000, "refresh");
    }

    private String generateToken(Long userId, Long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", tokenType);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtConfig.secretKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtConfig.secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Parse token failed: {}", e.getMessage());
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return userId == null ? null : Long.valueOf(String.valueOf(userId));
    }

    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        return claims != null && claims.getExpiration().after(new Date());
    }

    public boolean isAccessToken(String token) {
        return isTokenOfType(token, "access");
    }

    public boolean isRefreshToken(String token) {
        return isTokenOfType(token, "refresh");
    }

    private boolean isTokenOfType(String token, String expectedType) {
        Claims claims = parseToken(token);
        return claims != null && claims.getExpiration().after(new Date())
                && expectedType.equals(claims.get("type"));
    }

    public boolean isTokenExpiringSoon(String token, int thresholdSeconds) {
        Claims claims = parseToken(token);
        return claims == null || claims.getExpiration().getTime() - System.currentTimeMillis() < thresholdSeconds * 1000L;
    }

    public String refreshToken(String refreshToken) {
        Claims claims = parseToken(refreshToken);
        if (claims == null || !"refresh".equals(claims.get("type"))) {
            throw new IllegalArgumentException("刷新Token无效");
        }
        return generateAccessToken(getUserIdFromToken(refreshToken));
    }
}
