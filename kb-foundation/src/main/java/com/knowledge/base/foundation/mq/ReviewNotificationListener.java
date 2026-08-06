package com.knowledge.base.foundation.mq;

import com.knowledge.base.common.event.ReviewEventDTO;
import com.knowledge.base.foundation.dto.NotificationDTO;
import com.knowledge.base.foundation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewNotificationListener {
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = "#{@reviewNotificationQueue.name}")
    public void handleReviewEvent(ReviewEventDTO event) {
        if (event == null || event.getEventType() == null) return;
        String link = "/review/documents/" + event.getDocumentId();
        switch (event.getEventType()) {
            case "SUBMITTED" -> handleSubmitted(event, link);
            case "APPROVED" -> handleAuthor(event, link, "文档审核通过",
                    "您的文档《%s》已通过审核，正式发布。", null);
            case "REJECTED" -> handleAuthor(event, link, "文档审核驳回",
                    "您的文档《%s》未通过审核，驳回原因：%s", event.getReviewComment());
            default -> log.warn("Unknown review event: {}", event.getEventType());
        }
    }

    private void handleSubmitted(ReviewEventDTO event, String link) {
        String title = "新文档待审核";
        String content = String.format("用户「%s」提交了文档《%s》，请及时审核。",
                event.getAuthorName(), event.getDocumentTitle());
        for (Long reviewerId : findReviewerIds()) persistAndPush(reviewerId, title, content, link, event.getDocumentId());
        messagingTemplate.convertAndSend("/topic/reviewers", payload(title, content, link, event.getDocumentId()));
    }

    private void handleAuthor(ReviewEventDTO event, String link, String title, String template, String reason) {
        if (event.getAuthorId() == null) return;
        String content = reason == null
                ? String.format(template, event.getDocumentTitle())
                : String.format(template, event.getDocumentTitle(), reason.isBlank() ? "未填写原因" : reason);
        persistAndPush(event.getAuthorId(), title, content, link, event.getDocumentId());
    }

    private void persistAndPush(Long userId, String title, String content, String link, Long documentId) {
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(userId);
        dto.setNotificationType("review");
        dto.setTitle(title);
        dto.setContent(content);
        dto.setLink(link);
        dto.setRelatedType("document");
        dto.setRelatedId(documentId);
        notificationService.sendNotification(dto);
        if (isWebSocketEnabled()) {
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications",
                    payload(title, content, link, documentId));
        }
    }

    private Map<String, Object> payload(String title, String content, String link, Long documentId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notificationType", "review");
        payload.put("title", title);
        payload.put("content", content);
        payload.put("link", link);
        payload.put("documentId", documentId);
        return payload;
    }

    private List<Long> findReviewerIds() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT ur.user_id FROM kb_user.sys_user_role ur "
                            + "JOIN kb_user.sys_role r ON r.id = ur.role_id "
                            + "JOIN kb_user.kb_user u ON u.id = ur.user_id "
                            + "WHERE r.role_code = 'ROLE_REVIEWER' AND u.status = 1 AND u.deleted = 0",
                    Long.class);
        } catch (Exception e) {
            log.warn("Unable to query reviewers: {}", e.getMessage());
            return List.of();
        }
    }

    private boolean isWebSocketEnabled() {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM kb_system_config WHERE config_key = 'websocket.enabled' AND deleted = 0",
                    String.class);
            return value == null || "true".equalsIgnoreCase(value);
        } catch (Exception exception) {
            log.warn("Unable to read WebSocket notification setting: {}", exception.getMessage());
            return true;
        }
    }
}
