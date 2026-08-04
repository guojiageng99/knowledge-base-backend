package com.knowledge.base.search.feign;

import lombok.Data;

@Data
public class RagSearchRequest {
    private String query;
    private Integer topK;
    private boolean enableRerank;
}
