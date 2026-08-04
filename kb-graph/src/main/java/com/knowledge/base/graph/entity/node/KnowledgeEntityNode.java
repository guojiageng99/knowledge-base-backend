package com.knowledge.base.graph.entity.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.List;

@Data
@Node("KnowledgeEntity")
public class KnowledgeEntityNode {
    @Id private String name;
    private String type;
    private String description;
    private List<String> aliases;
}
