package com.knowledge.base.graph.repository;

import com.knowledge.base.graph.entity.node.KnowledgeEntityNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeEntityRepository extends Neo4jRepository<KnowledgeEntityNode, String> {
    @Query("MATCH (e:KnowledgeEntity) WHERE e.name CONTAINS $keyword OR any(alias IN coalesce(e.aliases, []) WHERE alias CONTAINS $keyword) RETURN e LIMIT $limit")
    List<KnowledgeEntityNode> searchByName(@Param("keyword") String keyword, @Param("limit") int limit);

    @Query("MATCH (e:KnowledgeEntity {type: $type}) RETURN e LIMIT $limit")
    List<KnowledgeEntityNode> findByType(@Param("type") String type, @Param("limit") int limit);
}
