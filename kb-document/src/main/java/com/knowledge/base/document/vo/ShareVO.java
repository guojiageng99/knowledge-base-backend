package com.knowledge.base.document.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareVO {
    private String shareId;
    private String shareUrl;
    private Long documentId;
    private String title;
    private Integer shareType;
    private String shareTypeDesc;
    private Integer expireType;
    private LocalDateTime expireTime;
    private Boolean expired;
    private Boolean requirePassword;
    private String sharerName;
    private LocalDateTime shareTime;
    private Integer accessCount;
    private Integer accessLimit;
    private String description;
}
