package com.knowledge.base.userauth.service.impl;

import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.userauth.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}") private String from;
    @Value("${app.frontend-url:http://localhost:3002}") private String frontendUrl;

    @Override
    public void sendActivationEmail(String recipient, String username, String token) {
        String url = frontendUrl + "/activate?token=" + token;
        sendHtml(recipient, "Knowledge Base account activation", "<p>Hello, " + escape(username)
                + ".</p><p>Please activate your account:</p><p><a href=\"" + url + "\">Activate account</a></p><p>This link expires in 24 hours.</p>");
    }

    @Override
    public void sendResetCodeEmail(String recipient, String code) {
        sendHtml(recipient, "Knowledge Base password reset code", "<p>Your password reset code is:</p><h2>"
                + code + "</h2><p>The code expires in 10 minutes.</p>");
    }

    private void sendHtml(String recipient, String subject, String body) {
        if (!StringUtils.hasText(from)) throw new BusinessException("Email is not configured: set MAIL_FROM and SMTP settings");
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error("Failed to send email to {}", recipient, exception);
            throw new BusinessException("Email delivery failed");
        }
    }

    private String escape(String value) { return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
}
