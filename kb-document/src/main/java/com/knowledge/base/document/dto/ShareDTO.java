package com.knowledge.base.document.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareDTO {
    @NotNull(message = "Document ID is required")
    private Long documentId;
    private Integer shareType = 1;
    private Integer expireType = 1;
    private String expireTime;
    private Integer accessLimit = 0;
    private Integer requirePassword = 0;
    private String password;
    private String description;
}
