package com.knowledge.base.document.entity.mongodb;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@org.springframework.data.mongodb.core.mapping.Document(collection = "document_autosave_history")
public class AutoSaveHistory {
    @Id
    private String id;
    @Indexed
    private Long documentId;
    private String title;
    private String content;
    private String contentPreview;
    private Integer contentLength;
    private Long authorId;
    @Indexed
    private LocalDateTime savedAt;
    @Builder.Default
    private Boolean deleted = false;
}
