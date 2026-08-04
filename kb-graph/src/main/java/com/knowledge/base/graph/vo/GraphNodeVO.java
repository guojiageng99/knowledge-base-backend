package com.knowledge.base.graph.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GraphNodeVO {
    private String id;
    private String name;
    private String type;
    private String label;
    private Integer size;
    private String color;
    private Map<String, Object> properties;
}
