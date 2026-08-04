package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

import java.util.List;

@Data
public class GraphBuildRequest {
    private DocumentPropsDTO document;
    private List<ChunkPropsDTO> chunks;
    private List<EntityMergeDTO> entities;
    private List<RelationMergeDTO> relations;
    private List<ChunkEntityMappingDTO> mentions;
}
