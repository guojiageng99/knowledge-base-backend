package com.knowledge.base.search.feign;

import lombok.Data;

@Data
public class RagSearchItemVO {
    private String chunkId;
    private Long documentId;
    private String documentTitle;
    private String content;
    private String heading;
    private double score;
    private double bm25Score;
    private double vectorScore;
}
