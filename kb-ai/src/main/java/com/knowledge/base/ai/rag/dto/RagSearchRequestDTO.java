package com.knowledge.base.ai.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RagSearchRequestDTO {
    @NotBlank private String query;
    private Integer topK;
    private boolean enableRerank;
}
