package com.knowledge.base.userauth.service;

public interface EmailService {
    void sendActivationEmail(String recipient, String username, String token);
    void sendInvitationEmail(String recipient, String username, String token);
    void sendResetCodeEmail(String recipient, String code);
}
