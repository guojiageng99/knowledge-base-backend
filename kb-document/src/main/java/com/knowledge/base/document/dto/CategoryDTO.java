package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Category request")
public class CategoryDTO {

    @Schema(description = "Category ID")
    private Long id;

    @NotBlank(message = "Category name must not be blank")
    @Size(max = 50, message = "Category name must not exceed 50 characters")
    @Schema(description = "Category name")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    private Long parentId;
    private Integer sortOrder;
    private String icon;
}
