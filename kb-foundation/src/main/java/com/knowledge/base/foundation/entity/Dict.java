package com.knowledge.base.foundation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_dict")
public class Dict extends BaseEntity {

    private String dictCode;
    private String dictName;
    private String dictType;
    private String description;
    private Integer sort;
    private Integer status;
}
