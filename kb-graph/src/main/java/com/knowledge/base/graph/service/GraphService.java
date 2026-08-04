package com.knowledge.base.graph.service;

import com.knowledge.base.graph.dto.GraphBuildRequest;
import com.knowledge.base.graph.vo.GraphDataVO;
import com.knowledge.base.graph.vo.GraphEdgeVO;
import com.knowledge.base.graph.vo.GraphNodeVO;
import com.knowledge.base.graph.vo.KagContextVO;

import java.util.List;

public interface GraphService {
    GraphDataVO getGraphData(String type);
    List<GraphNodeVO> getNodes(String type);
    List<GraphEdgeVO> getEdges(String sourceType, String targetType);
    GraphDataVO searchGraph(String keyword);
    GraphDataVO analyzePath(String sourceId, String targetId, Integer maxDepth);
    KagContextVO retrieveKagContext(String query);
    void rebuildDocument(GraphBuildRequest request);
    void deleteDocument(Long documentId);
}
