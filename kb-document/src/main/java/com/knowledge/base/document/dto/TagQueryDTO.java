package com.knowledge.base.document.dto;

import com.knowledge.base.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Tag query request")
public class TagQueryDTO extends PageParam {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签名称")
    private String tagName;
    @Schema(description = "标签类型")
    private Integer tagType;
    @Schema(description = "所属分类ID")
    private Long categoryId;
    @Schema(description = "状态")
    private Integer status;
}
