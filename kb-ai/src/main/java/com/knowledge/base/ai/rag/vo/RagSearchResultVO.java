package com.knowledge.base.ai.rag.vo;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RagSearchResultVO {
    private String chunkId;
    private Long documentId;
    private String documentTitle;
    private String content;
    private String heading;
    private double score;
    private double bm25Score;
    private double vectorScore;
}
