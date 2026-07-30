package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_category")
public class Category extends BaseEntity {

    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private String description;
    private String icon;
    private Integer sort;
    private Integer status;
    private Integer documentCount;
    private String remark;
}
