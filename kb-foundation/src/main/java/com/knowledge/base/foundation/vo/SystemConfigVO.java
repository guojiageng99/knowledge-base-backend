package com.knowledge.base.foundation.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SystemConfigVO implements Serializable {

    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String category;
    private String description;
    private Integer isPublic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
