package com.knowledge.base.graph.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphDataVO {
    private List<GraphNodeVO> nodes;
    private List<GraphEdgeVO> edges;
    private Long nodeCount;
    private Long edgeCount;
}
