package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document_share")
public class DocumentShare extends BaseEntity {
    private String shareId;
    private Long documentId;
    private String title;
    private Integer shareType;
    private String shareCode;
    private Integer expireType;
    private LocalDateTime expireTime;
    private Integer accessLimit;
    private Integer accessCount;
    private Integer requirePassword;
    private String password;
    private Long sharerId;
    private String sharerName;
    private String description;
    private Integer status;
    private LocalDateTime shareTime;
}
