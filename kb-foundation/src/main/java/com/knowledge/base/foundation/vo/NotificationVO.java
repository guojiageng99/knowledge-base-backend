package com.knowledge.base.foundation.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
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
}
