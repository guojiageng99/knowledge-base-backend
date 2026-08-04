package com.knowledge.base.graph.repository;

import com.knowledge.base.graph.dto.ChunkEntityMappingDTO;
import com.knowledge.base.graph.dto.ChunkPropsDTO;
import com.knowledge.base.graph.dto.CommunityResultDTO;
import com.knowledge.base.graph.dto.DocumentPropsDTO;
import com.knowledge.base.graph.dto.EntityMergeDTO;
import com.knowledge.base.graph.dto.GraphPathResultDTO;
import com.knowledge.base.graph.dto.GraphStatsDTO;
import com.knowledge.base.graph.dto.RelationMergeDTO;
import com.knowledge.base.graph.dto.SubgraphResultDTO;
import com.knowledge.base.graph.dto.TraverseResultDTO;

import java.util.List;

public interface CustomGraphRepository {
    GraphPathResultDTO findShortestPath(String sourceId, String targetId, int maxDepth);
    List<TraverseResultDTO> traverseFromEntity(String entityName, int maxHops, int limit);
    GraphStatsDTO getGraphStatistics();
    List<CommunityResultDTO> detectCommunities(int minCommunitySize);
    SubgraphResultDTO searchSubgraph(String keyword, int maxNodes);
    void mergeEntities(List<EntityMergeDTO> entities);
    void mergeRelations(List<RelationMergeDTO> relations);
    void connectChunksToEntities(List<ChunkEntityMappingDTO> mappings);
    void createDocumentNode(DocumentPropsDTO props);
    void createChunkNode(ChunkPropsDTO props);
    void createHasChunkRelation(Long docId, String chunkId, Integer chunkIndex);
}
