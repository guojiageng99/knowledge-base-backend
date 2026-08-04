package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

@Data
public class DocumentPropsDTO {
    private Long docId;
    private String title;
    private String summary;
    private Long categoryId;
    private Long authorId;
    private String authorName;
    private Integer status;
    private Integer documentType;
    private String tags;
}
