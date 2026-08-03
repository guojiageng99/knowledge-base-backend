package com.knowledge.base.document.entity.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@org.springframework.data.mongodb.core.mapping.Document(collection = "kb_document_content")
public class DocumentContent {

    @Id
    private String id;
    private String content;
    private String htmlContent;
    private Integer wordCount;
    private Long documentId;
    @Field("createdAt")
    private LocalDateTime createdAt;
    @Field("updatedAt")
    private LocalDateTime updatedAt;
}
