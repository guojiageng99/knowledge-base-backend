package com.knowledge.base.graph.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GraphRelationVO {
    private String id;
    private String sourceId;
    private String sourceName;
    private String targetId;
    private String targetName;
    private String relation;
    private String label;
    private Double weight;
    private Map<String, Object> properties;
}
