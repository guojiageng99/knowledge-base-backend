package com.knowledge.base.graph.entity.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("DocumentChunk")
public class DocumentChunkNode {
    @Id private String chunkId;
    private Long docId;
    private String content;
    private String heading;
    private Integer chunkIndex;
    private Integer totalChunks;
    private Long categoryId;
}
