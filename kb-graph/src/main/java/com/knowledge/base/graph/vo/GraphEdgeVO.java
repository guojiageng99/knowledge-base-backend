package com.knowledge.base.graph.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphEdgeVO {
    private String id;
    private String source;
    private String target;
    private String relation;
    private String label;
    private Double weight;
}
