package com.knowledge.base.graph.dto;

import lombok.Data;

@Data
public class ChunkEntityMappingDTO {
    private String chunkId;
    private String entityName;
    private Double confidence;
}
