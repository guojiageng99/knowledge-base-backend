package com.knowledge.base.document.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category response")
public class CategoryVO {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer sortOrder;
    private String icon;
    private Long documentCount;
    private List<CategoryVO> children;
}
