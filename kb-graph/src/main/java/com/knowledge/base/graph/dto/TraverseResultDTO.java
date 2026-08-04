package com.knowledge.base.graph.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TraverseResultDTO {
    private String sourceName;
    private String targetName;
    private String targetType;
    private List<String> relations;
    private Integer hops;
}
