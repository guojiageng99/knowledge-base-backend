package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

@Data
public class ChunkEntityMappingDTO {
    private String chunkId;
    private String entityName;
    private Double confidence;
}
