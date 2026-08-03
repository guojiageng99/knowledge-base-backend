package com.knowledge.base.document.entity.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@org.springframework.data.mongodb.core.mapping.Document(collection = "document_content")
public class DocumentContent {

    @Id
    private String id;
    private String content;
    private String htmlContent;
    private Integer wordCount;
    private Long documentId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
