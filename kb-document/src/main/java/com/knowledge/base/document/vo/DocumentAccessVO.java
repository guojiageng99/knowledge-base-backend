package com.knowledge.base.document.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "Document access record")
public class DocumentAccessVO implements Serializable {
    private Long id;
    private Long userId;
    private Long documentId;
    private String documentTitle;
    private String summary;
    private String categoryName;
    private String authorName;
    private LocalDateTime accessTime;
    private Integer status;
}
