package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

import java.util.List;

@Data
public class EntityMergeDTO {
    private String name;
    private String type;
    private String description;
    private List<String> aliases;
}
