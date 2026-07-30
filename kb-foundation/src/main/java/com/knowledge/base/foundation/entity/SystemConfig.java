package com.knowledge.base.foundation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_system_config")
public class SystemConfig extends BaseEntity {

    private String configKey;
    private String configValue;
    private String configType;
    private String category;
    private String description;
    private Integer isPublic;
}
