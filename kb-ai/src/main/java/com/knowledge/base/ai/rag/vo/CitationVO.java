package com.knowledge.base.ai.rag.vo;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CitationVO {
    private int index;
    private Long documentId;
    private String documentTitle;
    private String excerpt;
    private double relevanceScore;
}
