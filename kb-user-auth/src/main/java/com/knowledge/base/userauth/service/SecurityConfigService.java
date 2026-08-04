package com.knowledge.base.userauth.service;

public interface SecurityConfigService {
    int getPasswordMinLength();
    boolean isRequireSpecialChar();
}
