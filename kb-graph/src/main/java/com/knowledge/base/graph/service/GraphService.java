package com.knowledge.base.graph.service;

import com.knowledge.base.graph.dto.GraphBuildRequest;
import com.knowledge.base.graph.dto.GraphStatsDTO;
import com.knowledge.base.graph.dto.TraverseResultDTO;
import com.knowledge.base.graph.vo.GraphDataVO;
import com.knowledge.base.graph.vo.GraphEdgeVO;
import com.knowledge.base.graph.vo.GraphNodeVO;
import com.knowledge.base.graph.vo.GraphPathVO;
import com.knowledge.base.graph.vo.GraphRelationVO;
import com.knowledge.base.graph.vo.GraphCommunityVO;
import com.knowledge.base.graph.vo.KagContextVO;

import java.util.List;

public interface GraphService {
    GraphDataVO getGraphData(String type);
    List<GraphNodeVO> getNodes(String type);
    List<GraphEdgeVO> getEdges(String sourceType, String targetType);
    GraphDataVO searchGraph(String keyword);
    GraphPathVO analyzePath(String sourceId, String targetId, Integer maxDepth);
    List<GraphRelationVO> getNodeRelations(String nodeId);
    List<TraverseResultDTO> traverseEntity(String entityName, Integer maxHops, Integer limit);
    GraphStatsDTO getGraphStatistics();
    List<GraphCommunityVO> detectCommunity(String algorithm);
    KagContextVO retrieveKagContext(String query);
    void rebuildDocument(GraphBuildRequest request);
    void deleteDocument(Long documentId);
}
