package com.knowledge.base.document.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tag information")
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID")
    private Long id;
    @Schema(description = "标签名称")
    private String tagName;
    @Schema(description = "标签编码")
    private String tagCode;
    @Schema(description = "所属分类ID")
    private Long categoryId;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "标签类型")
    private Integer tagType;
    @Schema(description = "颜色")
    private String color;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "文档数量")
    private Integer docCount;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
