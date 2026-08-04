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
    public int getPasswordMinLength() {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM kb_foundation.kb_system_config WHERE config_key = 'auth.password.min.length' AND deleted = 0",
                    String.class);
            return value == null ? 8 : Integer.parseInt(value.trim());
        } catch (Exception exception) {
            log.warn("Unable to read password length setting: {}", exception.getMessage());
            return 8;
        }
    }

    @Override
    public boolean isRequireSpecialChar() {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM kb_foundation.kb_system_config WHERE config_key = 'auth.password.require.special' AND deleted = 0",
                    String.class);
            return value == null || "true".equalsIgnoreCase(value);
        } catch (Exception exception) {
            log.warn("Unable to read password special-character setting: {}", exception.getMessage());
            return true;
        }
    }
}
