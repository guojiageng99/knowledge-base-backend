package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import com.knowledge.base.document.enums.TagTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_tag")
@Schema(description = "Tag entity")
public class Tag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签名称")
    private String tagName;
    @Schema(description = "标签编码")
    private String tagCode;
    @Schema(description = "所属分类ID")
    private Long categoryId;
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
    private Integer version;

    public TagTypeEnum getTagTypeEnum() {
        return TagTypeEnum.of(tagType);
    }
}
