package com.knowledge.base.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DocumentDTO implements Serializable {

    private Long id;

    @NotBlank(message = "Document title must not be blank")
    @Size(max = 200, message = "Document title must not exceed 200 characters")
    private String title;

    @Size(max = 500, message = "Document summary must not exceed 500 characters")
    private String summary;
    private String content;
    private Integer documentType;
    private Long categoryId;
    @Size(max = 200, message = "Tags must not exceed 200 characters")
    private String tags;
    private List<Long> tagIds;
    private Integer status;
    private Integer isTop;
    private Integer isRecommend;
    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImage;
    private Integer source;
    @Size(max = 500, message = "Source URL must not exceed 500 characters")
    private String sourceUrl;
    private Integer allowComment;
    private Integer sort;
    @Size(max = 500, message = "Remark must not exceed 500 characters")
    private String remark;
}
