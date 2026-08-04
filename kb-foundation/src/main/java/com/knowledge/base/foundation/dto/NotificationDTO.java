package com.knowledge.base.foundation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Notification DTO")
public class NotificationDTO implements Serializable {

    private Long id;
    @NotNull(message = "Recipient user ID is required")
    private Long userId;
    private String userName;
    @NotBlank(message = "Notification type is required")
    private String notificationType;
    @NotBlank(message = "Notification title is required")
    private String title;
    @NotBlank(message = "Notification content is required")
    private String content;
    private String link;
    private String relatedType;
    private Long relatedId;
    private Integer isRead;
}
