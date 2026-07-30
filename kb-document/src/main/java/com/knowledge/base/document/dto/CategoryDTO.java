package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "分类信息")
public class CategoryDTO {

    @Schema(description = "分类ID")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Schema(description = "分类名称")
    private String name;

    @Size(max = 500, message = "分类描述不能超过500个字符")
    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "图标")
    private String icon;
}
