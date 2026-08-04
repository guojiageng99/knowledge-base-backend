package com.knowledge.base.graph.service.impl;

import com.knowledge.base.graph.dto.ChunkEntityMappingDTO;
import com.knowledge.base.graph.dto.ChunkPropsDTO;
import com.knowledge.base.graph.dto.EntityMergeDTO;
import com.knowledge.base.graph.dto.GraphBuildRequest;
import com.knowledge.base.graph.dto.GraphStatsDTO;
import com.knowledge.base.graph.dto.RelationMergeDTO;
import com.knowledge.base.graph.dto.TraverseResultDTO;
import com.knowledge.base.graph.repository.CustomGraphRepository;
import com.knowledge.base.graph.service.GraphService;
import com.knowledge.base.graph.vo.GraphDataVO;
import com.knowledge.base.graph.vo.GraphEdgeVO;
import com.knowledge.base.graph.vo.GraphNodeVO;
import com.knowledge.base.graph.vo.GraphPathVO;
import com.knowledge.base.graph.vo.GraphRelationVO;
import com.knowledge.base.graph.vo.GraphCommunityVO;
import com.knowledge.base.graph.vo.KagContextVO;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {
    private static final int MAX_GRAPH_NODES = 500;
    private final Neo4jClient neo4jClient;
    private final CustomGraphRepository customGraphRepository;

    @Override
    public GraphDataVO getGraphData(String type) {
        List<GraphNodeVO> nodes = getNodes(type);
        List<GraphEdgeVO> edges = getEdges(type, null);
        return GraphDataVO.builder().nodes(nodes).edges(edges)
                .nodeCount((long) nodes.size()).edgeCount((long) edges.size()).build();
    }

    @Override
    public List<GraphNodeVO> getNodes(String type) {
        String cypher = "MATCH (n) " + (hasText(type) ? "WHERE $type IN labels(n) " : "")
                + "RETURN elementId(n) AS id, labels(n) AS labels, properties(n) AS props "
                + "ORDER BY coalesce(n.name, n.title, n.chunkId) LIMIT " + MAX_GRAPH_NODES;
        Neo4jClient.RunnableSpec query = neo4jClient.query(cypher);
        if (hasText(type)) query = query.bind(type).to("type");
        return query.fetch().all().stream().map(this::node).toList();
    }

    @Override
    public List<GraphEdgeVO> getEdges(String sourceType, String targetType) {
        String cypher = "MATCH (source)-[r]->(target) "
                + "WHERE ($sourceType IS NULL OR $sourceType IN labels(source)) "
                + "AND ($targetType IS NULL OR $targetType IN labels(target)) "
                + "RETURN elementId(r) AS id, elementId(source) AS source, elementId(target) AS target, type(r) AS relation, properties(r) AS props LIMIT 1000";
        return neo4jClient.query(cypher).bind(hasText(sourceType) ? sourceType : null).to("sourceType")
                .bind(hasText(targetType) ? targetType : null).to("targetType").fetch().all().stream().map(this::edge).toList();
    }

    @Override
    public GraphDataVO searchGraph(String keyword) {
        if (!hasText(keyword)) return getGraphData(null);
        String cypher = "MATCH (n) WHERE coalesce(n.name, '') CONTAINS $keyword OR coalesce(n.title, '') CONTAINS $keyword "
                + "OR coalesce(n.content, '') CONTAINS $keyword "
                + "WITH collect(n)[.." + MAX_GRAPH_NODES + "] AS nodes UNWIND nodes AS n "
                + "RETURN elementId(n) AS id, labels(n) AS labels, properties(n) AS props";
        List<GraphNodeVO> nodes = neo4jClient.query(cypher).bind(keyword).to("keyword").fetch().all().stream().map(this::node).toList();
        if (nodes.isEmpty()) return GraphDataVO.builder().nodes(List.of()).edges(List.of()).nodeCount(0L).edgeCount(0L).build();
        List<String> ids = nodes.stream().map(GraphNodeVO::getId).toList();
        String edgesCypher = "MATCH (source)-[r]->(target) WHERE elementId(source) IN $ids AND elementId(target) IN $ids "
                + "RETURN elementId(r) AS id, elementId(source) AS source, elementId(target) AS target, type(r) AS relation, properties(r) AS props";
        List<GraphEdgeVO> edges = neo4jClient.query(edgesCypher).bind(ids).to("ids").fetch().all().stream().map(this::edge).toList();
        return GraphDataVO.builder().nodes(nodes).edges(edges).nodeCount((long) nodes.size()).edgeCount((long) edges.size()).build();
    }

    @Override
    public GraphPathVO analyzePath(String sourceId, String targetId, Integer maxDepth) {
        int depth = Math.min(Math.max(maxDepth == null ? 3 : maxDepth, 1), 6);
        String cypher = "MATCH path = shortestPath((source)-[*1.." + depth + "]-(target)) "
                + "WHERE elementId(source) = $sourceId AND elementId(target) = $targetId "
                + "WITH nodes(path) AS ns, relationships(path) AS rs UNWIND ns AS n "
                + "RETURN elementId(n) AS id, labels(n) AS labels, properties(n) AS props";
        List<GraphNodeVO> nodes = neo4jClient.query(cypher).bind(sourceId).to("sourceId").bind(targetId).to("targetId")
                .fetch().all().stream().map(this::node).toList();
        if (nodes.isEmpty()) return GraphPathVO.builder().nodes(List.of()).edges(List.of()).hops(0).build();
        List<String> ids = nodes.stream().map(GraphNodeVO::getId).toList();
        String edgeCypher = "MATCH (source)-[r]-(target) WHERE elementId(source) IN $ids AND elementId(target) IN $ids "
                + "RETURN elementId(r) AS id, elementId(source) AS source, elementId(target) AS target, type(r) AS relation, properties(r) AS props";
        List<GraphEdgeVO> edges = neo4jClient.query(edgeCypher).bind(ids).to("ids").fetch().all().stream().map(this::edge).toList();
        return GraphPathVO.builder().nodes(nodes).edges(edges).hops(edges.size()).build();
    }

    @Override
    public List<GraphRelationVO> getNodeRelations(String nodeId) {
        String cypher = "MATCH (source)-[r]-(target) WHERE elementId(source) = $nodeId OR elementId(target) = $nodeId "
                + "RETURN elementId(r) AS id, elementId(source) AS sourceId, coalesce(source.name, source.title, source.chunkId) AS sourceName, "
                + "elementId(target) AS targetId, coalesce(target.name, target.title, target.chunkId) AS targetName, type(r) AS relation, properties(r) AS props LIMIT 200";
        return neo4jClient.query(cypher).bind(nodeId).to("nodeId").fetch().all().stream().map(row -> {
            @SuppressWarnings("unchecked") Map<String, Object> properties = (Map<String, Object>) row.getOrDefault("props", Map.of());
            Object weight = properties.get("weight");
            return GraphRelationVO.builder().id(string(row.get("id"))).sourceId(string(row.get("sourceId"))).sourceName(string(row.get("sourceName")))
                    .targetId(string(row.get("targetId"))).targetName(string(row.get("targetName"))).relation(string(row.get("relation")))
                    .label(string(properties.getOrDefault("type", row.get("relation")))).weight(weight instanceof Number number ? number.doubleValue() : 1D).properties(properties).build();
        }).toList();
    }

    @Override
    public List<TraverseResultDTO> traverseEntity(String entityName, Integer maxHops, Integer limit) {
        return customGraphRepository.traverseFromEntity(entityName, maxHops == null ? 2 : maxHops, limit == null ? 20 : limit);
    }

    @Override
    public GraphStatsDTO getGraphStatistics() { return customGraphRepository.getGraphStatistics(); }

    @Override
    public List<GraphCommunityVO> detectCommunity(String algorithm) {
        String selected = hasText(algorithm) ? algorithm : "degree";
        return customGraphRepository.detectCommunities(2).stream().map(community -> GraphCommunityVO.builder()
                .communityId(community.getCommunityId()).entityNames(community.getEntityNames()).size(community.getSize()).algorithm(selected).build()).toList();
    }

    @Override
    public KagContextVO retrieveKagContext(String query) {
        if (!hasText(query)) return KagContextVO.builder().entities(List.of()).paths(List.of()).chunks(List.of()).hasResults(false).build();
        List<Map<String, Object>> entities = new ArrayList<>(neo4jClient.query("MATCH (e:KnowledgeEntity) WHERE e.name CONTAINS $query OR any(alias IN coalesce(e.aliases, []) WHERE alias CONTAINS $query) "
                        + "OPTIONAL MATCH (e)-[r]-() RETURN e.name AS name, e.type AS type, e.description AS description, count(r) AS connectionCount ORDER BY connectionCount DESC LIMIT 10")
                .bind(query).to("query").fetch().all());
        List<Map<String, Object>> paths = new ArrayList<>(neo4jClient.query("MATCH path=(e:KnowledgeEntity)-[:RELATED_TO*1..2]-(neighbor:KnowledgeEntity) "
                        + "WHERE e.name CONTAINS $query RETURN [node IN nodes(path) | node.name] AS nodes, "
                        + "[rel IN relationships(path) | coalesce(rel.type, type(rel))] AS relations LIMIT 20")
                .bind(query).to("query").fetch().all());
        List<Map<String, Object>> chunks = new ArrayList<>(neo4jClient.query("MATCH (c:DocumentChunk)-[:MENTIONS]->(e:KnowledgeEntity) WHERE e.name CONTAINS $query "
                        + "RETURN c.chunkId AS chunkId, c.docId AS docId, c.content AS content, c.heading AS heading LIMIT 10")
                .bind(query).to("query").fetch().all());
        return KagContextVO.builder().entities(entities).paths(paths).chunks(chunks)
                .hasResults(!entities.isEmpty() || !paths.isEmpty() || !chunks.isEmpty()).build();
    }

    @Override
    @Transactional
    public void rebuildDocument(GraphBuildRequest request) {
        if (request == null || request.getDocument() == null || request.getDocument().getDocId() == null) {
            throw new IllegalArgumentException("Document graph data is required");
        }
        deleteDocument(request.getDocument().getDocId());
        var document = request.getDocument();
        customGraphRepository.createDocumentNode(document);
        for (ChunkPropsDTO chunk : values(request.getChunks())) createChunk(document.getDocId(), chunk);
        customGraphRepository.mergeEntities(request.getEntities());
        customGraphRepository.mergeRelations(request.getRelations());
        customGraphRepository.connectChunksToEntities(request.getMentions());
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        if (documentId == null) return;
        neo4jClient.query("MATCH (d:KnowledgeDocument {docId: $docId})-[:HAS_CHUNK]->(c:DocumentChunk) DETACH DELETE c")
                .bind(documentId).to("docId").run();
        neo4jClient.query("MATCH (d:KnowledgeDocument {docId: $docId}) DETACH DELETE d").bind(documentId).to("docId").run();
    }

    private void createChunk(Long docId, ChunkPropsDTO chunk) {
        if (chunk == null || !hasText(chunk.getChunkId())) return;
        chunk.setDocId(docId);
        customGraphRepository.createChunkNode(chunk);
        customGraphRepository.createHasChunkRelation(docId, chunk.getChunkId(), chunk.getChunkIndex());
    }

    private void mergeEntity(EntityMergeDTO entity) {
        if (entity == null || !hasText(entity.getName())) return;
        neo4jClient.query("MERGE (e:KnowledgeEntity {name: $name}) ON CREATE SET e.createdAt=datetime() SET e.type=$type, e.description=$description, e.aliases=$aliases, e.updatedAt=datetime()")
                .bind(entity.getName()).to("name").bind(entity.getType()).to("type").bind(entity.getDescription()).to("description").bind(entity.getAliases()).to("aliases").run();
    }

    private void mergeRelation(RelationMergeDTO relation) {
        if (relation == null || !hasText(relation.getSource()) || !hasText(relation.getTarget())) return;
        neo4jClient.query("MATCH (source:KnowledgeEntity {name: $source}), (target:KnowledgeEntity {name: $target}) "
                        + "MERGE (source)-[r:RELATED_TO {type: $type}]->(target) SET r.weight=$weight, r.updatedAt=datetime()")
                .bind(relation.getSource()).to("source").bind(relation.getTarget()).to("target")
                .bind(hasText(relation.getRelationType()) ? relation.getRelationType() : "RELATED_TO").to("type")
                .bind(relation.getWeight() == null ? 1.0D : relation.getWeight()).to("weight").run();
    }

    private void mergeMention(ChunkEntityMappingDTO mention) {
        if (mention == null || !hasText(mention.getChunkId()) || !hasText(mention.getEntityName())) return;
        neo4jClient.query("MATCH (c:DocumentChunk {chunkId: $chunkId}), (e:KnowledgeEntity {name: $entityName}) "
                        + "MERGE (c)-[r:MENTIONS]->(e) SET r.confidence=$confidence, r.chunkId=$chunkId")
                .bind(mention.getChunkId()).to("chunkId").bind(mention.getEntityName()).to("entityName")
                .bind(mention.getConfidence() == null ? 1.0D : mention.getConfidence()).to("confidence").run();
    }

    private GraphNodeVO node(Map<String, Object> row) {
        @SuppressWarnings("unchecked") List<String> labels = (List<String>) row.getOrDefault("labels", List.of());
        @SuppressWarnings("unchecked") Map<String, Object> properties = (Map<String, Object>) row.getOrDefault("props", Map.of());
        String type = labels.isEmpty() ? "Unknown" : labels.getFirst();
        String name = string(properties.get("name"));
        if (!hasText(name)) name = string(properties.get("title"));
        if (!hasText(name)) name = string(properties.get("chunkId"));
        return GraphNodeVO.builder().id(string(row.get("id"))).name(hasText(name) ? name : type).type(type).label(type)
                .color(color(type)).size(size(type)).properties(properties).build();
    }

    private GraphEdgeVO edge(Map<String, Object> row) {
        @SuppressWarnings("unchecked") Map<String, Object> properties = (Map<String, Object>) row.getOrDefault("props", Map.of());
        Object weight = properties.get("weight");
        return GraphEdgeVO.builder().id(string(row.get("id"))).source(string(row.get("source"))).target(string(row.get("target")))
                .relation(string(row.get("relation"))).label(string(properties.getOrDefault("type", row.get("relation"))))
                .weight(weight instanceof Number number ? number.doubleValue() : 1.0D).build();
    }

    private <T> Collection<T> values(Collection<T> values) { return values == null ? List.of() : values; }
    private String string(Object value) { return value == null ? null : value.toString(); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private String color(String type) { return switch (type) { case "KnowledgeDocument" -> "#1677ff"; case "KnowledgeEntity" -> "#52c41a"; case "DocumentChunk" -> "#faad14"; default -> "#8c8c8c"; }; }
    private int size(String type) { return switch (type) { case "KnowledgeDocument" -> 30; case "KnowledgeEntity" -> 25; case "DocumentChunk" -> 20; default -> 22; }; }
}
