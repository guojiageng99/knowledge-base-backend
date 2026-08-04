package com.knowledge.base.graph.dto;

import lombok.Data;

@Data
public class RelationMergeDTO {
    private String source;
    private String target;
    private String relationType;
    private Double weight;
}
