package com.knowledge.base.foundation.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "Notification VO")
public class NotificationVO implements Serializable {

    private Long id;
    private Long userId;
    private String userName;
    private String notificationType;
    private String title;
    private String content;
    private String link;
    private String relatedType;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;

    private LocalDateTime createdAt;
}
