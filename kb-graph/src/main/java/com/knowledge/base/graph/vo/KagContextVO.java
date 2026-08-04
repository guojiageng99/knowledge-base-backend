package com.knowledge.base.graph.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class KagContextVO {
    private List<Map<String, Object>> entities;
    private List<Map<String, Object>> paths;
    private List<Map<String, Object>> chunks;
    private boolean hasResults;
}
