package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExtractionResult {
    private List<EntityMergeDTO> entities;
    private List<RelationMergeDTO> relations;
}
