package com.knowledge.base.graph.entity.relationship;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@Data
@RelationshipProperties
public class MentionsRelation {
    @RelationshipId private Long id;
    private Double confidence;
    private String chunkId;
}
