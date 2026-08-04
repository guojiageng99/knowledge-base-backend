package com.knowledge.base.graph.repository;

import com.knowledge.base.graph.entity.node.KnowledgeDocumentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeDocumentRepository extends Neo4jRepository<KnowledgeDocumentNode, Long> {
    @Query("MATCH (d:KnowledgeDocument) WHERE d.status = 1 RETURN d ORDER BY d.title")
    List<KnowledgeDocumentNode> findAllPublished();

    @Query("MATCH (d:KnowledgeDocument {docId: $docId}) DETACH DELETE d")
    void deleteByDocId(@Param("docId") Long docId);
}
