package com.knowledge.base.graph.entity.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("KnowledgeDocument")
public class KnowledgeDocumentNode {
    @Id private Long docId;
    private String title;
    private String summary;
    private Long categoryId;
    private Long authorId;
    private String authorName;
    private Integer status;
    private Integer documentType;
    private String tags;
}
