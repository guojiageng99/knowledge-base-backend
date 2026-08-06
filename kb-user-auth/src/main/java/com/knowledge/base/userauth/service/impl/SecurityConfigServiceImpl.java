package com.knowledge.base.userauth.service.impl;

import com.knowledge.base.userauth.service.SecurityConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Reads authentication policy independently from the shared settings table. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityConfigServiceImpl implements SecurityConfigService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String getConfig(String configKey) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT config_value FROM kb_foundation.kb_system_config WHERE config_key = ? AND deleted = 0",
                    String.class, configKey);
        } catch (Exception exception) {
            log.debug("Unable to read system setting {}: {}", configKey, exception.getMessage());
            return null;
        }
    }

    @Override
    public boolean isRegistrationEnabled() {
        String value = getConfig("user.registration.enabled");
        return value != null && Boolean.parseBoolean(value);
    }

    @Override
    public void validatePassword(String password) {
        int minLength = getPasswordMinLength();
        if (password == null || password.length() < minLength) {
            throw new com.knowledge.base.common.exception.BusinessException("Password must contain at least " + minLength + " characters");
        }
        String policy = getConfig("system.passwordPolicy");
        if (policy == null || policy.isBlank()) policy = "medium";
        boolean requiresSpecialCharacter = isRequireSpecialChar();
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasSpecial = password.chars().anyMatch(character -> !Character.isLetterOrDigit(character));
        if ("high".equalsIgnoreCase(policy) && !(hasLower && hasUpper && hasDigit && hasSpecial)) {
            throw new com.knowledge.base.common.exception.BusinessException("High password policy requires upper/lowercase letters, a number, and a special character");
        }
        if ("medium".equalsIgnoreCase(policy) && !(hasLetter && hasDigit)) {
            throw new com.knowledge.base.common.exception.BusinessException("Password must contain both letters and numbers");
        }
        if (requiresSpecialCharacter && !hasSpecial) {
            throw new com.knowledge.base.common.exception.BusinessException("Password must contain a special character");
        }
    }

    @Override
    public int getPasswordMinLength() {
        try {
            String value = getConfig("auth.password.min.length");
            return value == null ? 8 : Integer.parseInt(value.trim());
        } catch (Exception exception) {
            log.warn("Unable to read password length setting: {}", exception.getMessage());
            return 8;
        }
    }

    @Override
    public boolean isRequireSpecialChar() {
        try {
            String value = getConfig("auth.password.require.special");
            return value == null || "true".equalsIgnoreCase(value);
        } catch (Exception exception) {
            log.warn("Unable to read password special-character setting: {}", exception.getMessage());
            return true;
        }
    }
}
