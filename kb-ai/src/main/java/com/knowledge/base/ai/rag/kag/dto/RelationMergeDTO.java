package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

@Data
public class RelationMergeDTO {
    private String source;
    private String target;
    private String relationType;
    private Double weight;
}
