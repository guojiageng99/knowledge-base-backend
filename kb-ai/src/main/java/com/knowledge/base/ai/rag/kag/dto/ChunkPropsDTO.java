package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

@Data
public class ChunkPropsDTO {
    private String chunkId;
    private Long docId;
    private String content;
    private String heading;
    private Integer chunkIndex;
    private Integer totalChunks;
    private Long categoryId;
}
