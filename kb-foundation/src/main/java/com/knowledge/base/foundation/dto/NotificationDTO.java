package com.knowledge.base.foundation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationDTO implements Serializable {

    private Long id;
    @NotNull
    private Long userId;
    private String userName;
    @NotBlank
    private String notificationType;
    @NotBlank
    private String title;
    private String content;
    private String link;
    private String relatedType;
    private Long relatedId;
}
