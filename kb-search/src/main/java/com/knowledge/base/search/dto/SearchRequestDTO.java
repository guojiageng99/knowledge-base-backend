package com.knowledge.base.search.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SearchRequestDTO {
    @NotBlank private String keyword;
    private List<Long> categoryIds;
    private Long creatorId;
    private Integer docStatus;
    private String sortField;
    private String sortOrder;
    private Integer current = 1;
    private Integer size = 10;
    private String searchMode = "keyword";
    private int topK = 10;
    private boolean enableRerank = true;
}
