package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Tag creation request")
public class TagCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签名称")
    @NotBlank(message = "Tag name must not be blank")
    @Size(max = 50, message = "Tag name must not exceed 50 characters")
    private String tagName;

    @Schema(description = "标签编码")
    @Size(max = 50, message = "Tag code must not exceed 50 characters")
    private String tagCode;
    @Schema(description = "所属分类ID")
    private Long categoryId;
    @Schema(description = "标签类型")
    private Integer tagType;
    @Schema(description = "颜色")
    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;
    @Schema(description = "图标")
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
}
