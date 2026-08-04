package com.knowledge.base.graph.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GraphStatsDTO {
    private Long nodeCount;
    private Long relationCount;
    private Map<String, Long> nodesByLabel;
    private Map<String, Long> relationsByType;
}
