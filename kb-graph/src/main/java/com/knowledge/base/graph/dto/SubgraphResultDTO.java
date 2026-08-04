package com.knowledge.base.graph.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SubgraphResultDTO {
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> edges;
}
