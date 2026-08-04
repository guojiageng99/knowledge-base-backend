package com.knowledge.base.userauth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class VerificationTokenUtil {
    @Value("${app.activation-token-expiry-hours:24}")
    private int tokenExpiryHours;

    public String generateToken() { return UUID.randomUUID().toString(); }
    public LocalDateTime calculateExpiryTime() { return LocalDateTime.now().plusHours(tokenExpiryHours); }
    public boolean isTokenExpired(LocalDateTime expiryTime) { return expiryTime == null || !expiryTime.isAfter(LocalDateTime.now()); }
}
