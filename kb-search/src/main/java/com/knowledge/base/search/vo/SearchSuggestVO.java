package com.knowledge.base.search.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchSuggestVO {
    private String text;
    private String type;
    private Long documentId;
    private Double score;
}
