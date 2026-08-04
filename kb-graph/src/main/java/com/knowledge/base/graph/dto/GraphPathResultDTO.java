package com.knowledge.base.graph.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphPathResultDTO {
    private List<String> nodeIds;
    private List<String> nodeNames;
    private List<String> relations;
    private Integer hops;
}
