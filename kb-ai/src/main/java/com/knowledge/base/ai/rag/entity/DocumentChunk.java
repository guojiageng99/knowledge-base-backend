package com.knowledge.base.ai.rag.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentChunk {
    private String chunkId;
    private Long documentId;
    private String documentTitle;
    private String content;
    private String heading;
    private Integer chunkIndex;
    private Integer totalChunks;
    private float[] embedding;
    private Long categoryId;
    private Long authorId;
    private Long teamId;
    private Integer docStatus;
    private LocalDateTime indexedAt;
}
