package com.knowledge.base.userauth.service;

public interface SecurityConfigService {
    String getConfig(String configKey);
    boolean isRegistrationEnabled();
    void validatePassword(String password);
    int getPasswordMinLength();
    boolean isRequireSpecialChar();
}
