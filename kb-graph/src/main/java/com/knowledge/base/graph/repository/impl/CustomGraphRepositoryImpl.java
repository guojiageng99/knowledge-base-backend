package com.knowledge.base.graph.repository.impl;

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
import com.knowledge.base.graph.repository.CustomGraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomGraphRepositoryImpl implements CustomGraphRepository {
    private final Neo4jClient neo4jClient;

    @Override
    public GraphPathResultDTO findShortestPath(String sourceId, String targetId, int maxDepth) {
        int depth = Math.clamp(maxDepth, 1, 6);
        String cypher = "MATCH path = shortestPath((source)-[*1.." + depth + "]-(target)) "
                + "WHERE elementId(source) = $sourceId AND elementId(target) = $targetId "
                + "RETURN [n IN nodes(path) | elementId(n)] AS nodeIds, "
                + "[n IN nodes(path) | coalesce(n.name, n.title, n.chunkId)] AS nodeNames, "
                + "[r IN relationships(path) | coalesce(r.type, type(r))] AS relations";
        return neo4jClient.query(cypher).bind(sourceId).to("sourceId").bind(targetId).to("targetId").fetch().one()
                .map(row -> GraphPathResultDTO.builder().nodeIds(strings(row.get("nodeIds"))).nodeNames(strings(row.get("nodeNames")))
                        .relations(strings(row.get("relations"))).hops(Math.max(0, strings(row.get("relations")).size())).build())
                .orElse(null);
    }

    @Override
    public List<TraverseResultDTO> traverseFromEntity(String entityName, int maxHops, int limit) {
        int hops = Math.clamp(maxHops, 1, 4);
        int safeLimit = Math.clamp(limit, 1, 100);
        String cypher = "MATCH path=(source:KnowledgeEntity {name:$entityName})-[*1.." + hops + "]-(target:KnowledgeEntity) "
                + "RETURN source.name AS sourceName, target.name AS targetName, target.type AS targetType, "
                + "[r IN relationships(path) | coalesce(r.type, type(r))] AS relations LIMIT " + safeLimit;
        return neo4jClient.query(cypher).bind(entityName).to("entityName").fetch().all().stream()
                .map(row -> TraverseResultDTO.builder().sourceName(string(row.get("sourceName"))).targetName(string(row.get("targetName")))
                        .targetType(string(row.get("targetType"))).relations(strings(row.get("relations")))
                        .hops(strings(row.get("relations")).size()).build()).toList();
    }

    @Override
    public GraphStatsDTO getGraphStatistics() {
        long nodes = longValue(neo4jClient.query("MATCH (n) RETURN count(n) AS count").fetch().one().orElse(Map.of()).get("count"));
        long relations = longValue(neo4jClient.query("MATCH ()-[r]->() RETURN count(r) AS count").fetch().one().orElse(Map.of()).get("count"));
        Map<String, Long> labels = counts("MATCH (n) UNWIND labels(n) AS label RETURN label AS name, count(*) AS count");
        Map<String, Long> types = counts("MATCH ()-[r]->() RETURN type(r) AS name, count(*) AS count");
        return GraphStatsDTO.builder().nodeCount(nodes).relationCount(relations).nodesByLabel(labels).relationsByType(types).build();
    }

    @Override
    public List<CommunityResultDTO> detectCommunities(int minCommunitySize) {
        int minSize = Math.max(minCommunitySize, 1);
        String cypher = "MATCH (entity:KnowledgeEntity)-[:RELATED_TO]-(neighbor:KnowledgeEntity) "
                + "WITH entity, collect(DISTINCT neighbor.name) AS neighbors WHERE size(neighbors) >= $minSize "
                + "RETURN entity.name AS entityName, neighbors ORDER BY size(neighbors) DESC LIMIT 50";
        return neo4jClient.query(cypher).bind(minSize).to("minSize").fetch().all().stream().map(row -> {
            List<String> names = new ArrayList<>(); names.add(string(row.get("entityName"))); names.addAll(strings(row.get("neighbors")));
            return CommunityResultDTO.builder().communityId(string(row.get("entityName"))).entityNames(names).size(names.size()).build();
        }).toList();
    }

    @Override
    public SubgraphResultDTO searchSubgraph(String keyword, int maxNodes) {
        int limit = Math.clamp(maxNodes, 1, 500);
        String nodesCypher = "MATCH (n) WHERE coalesce(n.name,'') CONTAINS $keyword OR coalesce(n.title,'') CONTAINS $keyword "
                + "RETURN elementId(n) AS id, labels(n) AS labels, properties(n) AS properties LIMIT " + limit;
        List<Map<String, Object>> nodes = new ArrayList<>(neo4jClient.query(nodesCypher).bind(keyword).to("keyword").fetch().all());
        if (nodes.isEmpty()) return SubgraphResultDTO.builder().nodes(List.of()).edges(List.of()).build();
        List<String> ids = nodes.stream().map(row -> string(row.get("id"))).toList();
        List<Map<String, Object>> edges = new ArrayList<>(neo4jClient.query("MATCH (source)-[r]->(target) WHERE elementId(source) IN $ids AND elementId(target) IN $ids "
                        + "RETURN elementId(r) AS id, elementId(source) AS source, elementId(target) AS target, type(r) AS type, properties(r) AS properties")
                .bind(ids).to("ids").fetch().all());
        return SubgraphResultDTO.builder().nodes(nodes).edges(edges).build();
    }

    @Override
    public void mergeEntities(List<EntityMergeDTO> entities) {
        for (EntityMergeDTO entity : values(entities)) {
            if (entity == null || blank(entity.getName())) continue;
            neo4jClient.query("MERGE (e:KnowledgeEntity {name:$name}) ON CREATE SET e.createdAt=datetime() "
                            + "SET e.type=$type, e.description=$description, e.aliases=$aliases, e.updatedAt=datetime()")
                    .bind(entity.getName()).to("name").bind(entity.getType()).to("type").bind(entity.getDescription()).to("description")
                    .bind(entity.getAliases()).to("aliases").run();
        }
    }

    @Override
    public void mergeRelations(List<RelationMergeDTO> relations) {
        for (RelationMergeDTO relation : values(relations)) {
            if (relation == null || blank(relation.getSource()) || blank(relation.getTarget())) continue;
            neo4jClient.query("MATCH (source:KnowledgeEntity {name:$source}), (target:KnowledgeEntity {name:$target}) "
                            + "MERGE (source)-[r:RELATED_TO {type:$type}]->(target) SET r.weight=$weight, r.updatedAt=datetime()")
                    .bind(relation.getSource()).to("source").bind(relation.getTarget()).to("target")
                    .bind(blank(relation.getRelationType()) ? "RELATED_TO" : relation.getRelationType()).to("type")
                    .bind(relation.getWeight() == null ? 1D : relation.getWeight()).to("weight").run();
        }
    }

    @Override
    public void connectChunksToEntities(List<ChunkEntityMappingDTO> mappings) {
        for (ChunkEntityMappingDTO mapping : values(mappings)) {
            if (mapping == null || blank(mapping.getChunkId()) || blank(mapping.getEntityName())) continue;
            neo4jClient.query("MATCH (chunk:DocumentChunk {chunkId:$chunkId}), (entity:KnowledgeEntity {name:$entityName}) "
                            + "MERGE (chunk)-[r:MENTIONS]->(entity) SET r.confidence=$confidence, r.chunkId=$chunkId")
                    .bind(mapping.getChunkId()).to("chunkId").bind(mapping.getEntityName()).to("entityName")
                    .bind(mapping.getConfidence() == null ? 1D : mapping.getConfidence()).to("confidence").run();
        }
    }

    @Override
    public void createDocumentNode(DocumentPropsDTO props) {
        neo4jClient.query("MERGE (d:KnowledgeDocument {docId:$docId}) SET d.title=$title, d.summary=$summary, d.categoryId=$categoryId, "
                        + "d.authorId=$authorId, d.authorName=$authorName, d.status=$status, d.documentType=$documentType, d.tags=$tags, d.updatedAt=datetime()")
                .bind(props.getDocId()).to("docId").bind(props.getTitle()).to("title").bind(props.getSummary()).to("summary")
                .bind(props.getCategoryId()).to("categoryId").bind(props.getAuthorId()).to("authorId").bind(props.getAuthorName()).to("authorName")
                .bind(props.getStatus()).to("status").bind(props.getDocumentType()).to("documentType").bind(props.getTags()).to("tags").run();
    }

    @Override
    public void createChunkNode(ChunkPropsDTO props) {
        neo4jClient.query("MERGE (chunk:DocumentChunk {chunkId:$chunkId}) SET chunk.docId=$docId, chunk.content=$content, chunk.heading=$heading, "
                        + "chunk.chunkIndex=$chunkIndex, chunk.totalChunks=$totalChunks, chunk.categoryId=$categoryId")
                .bind(props.getChunkId()).to("chunkId").bind(props.getDocId()).to("docId").bind(props.getContent()).to("content")
                .bind(props.getHeading()).to("heading").bind(props.getChunkIndex()).to("chunkIndex").bind(props.getTotalChunks()).to("totalChunks")
                .bind(props.getCategoryId()).to("categoryId").run();
    }

    @Override
    public void createHasChunkRelation(Long docId, String chunkId, Integer chunkIndex) {
        neo4jClient.query("MATCH (document:KnowledgeDocument {docId:$docId}), (chunk:DocumentChunk {chunkId:$chunkId}) "
                        + "MERGE (document)-[r:HAS_CHUNK]->(chunk) SET r.chunkIndex=$chunkIndex")
                .bind(docId).to("docId").bind(chunkId).to("chunkId").bind(chunkIndex).to("chunkIndex").run();
    }

    private Map<String, Long> counts(String cypher) { Map<String, Long> result = new LinkedHashMap<>(); neo4jClient.query(cypher).fetch().all().forEach(row -> result.put(string(row.get("name")), longValue(row.get("count")))); return result; }
    private List<String> strings(Object value) { if (!(value instanceof Collection<?> collection)) return List.of(); return collection.stream().map(this::string).toList(); }
    private String string(Object value) { return value == null ? "" : value.toString(); }
    private long longValue(Object value) { return value instanceof Number number ? number.longValue() : 0L; }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private <T> Collection<T> values(Collection<T> values) { return values == null ? List.of() : values; }
}
