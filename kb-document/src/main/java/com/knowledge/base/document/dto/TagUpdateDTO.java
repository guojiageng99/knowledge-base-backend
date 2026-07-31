package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Tag update request")
public class TagUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID")
    @NotNull(message = "Tag ID is required")
    private Long id;
    @Schema(description = "标签名称")
    @Size(max = 50, message = "Tag name must not exceed 50 characters")
    private String tagName;
    @Schema(description = "标签编码")
    @Size(max = 50, message = "Tag code must not exceed 50 characters")
    private String tagCode;
    @Schema(description = "所属分类ID")
    private Long categoryId;
    @Schema(description = "颜色")
    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;
    @Schema(description = "图标")
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
    @Schema(description = "状态")
    private Integer status;
}
