package com.knowledge.base.foundation.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DictVO implements Serializable {

    private Long id;
    private String dictCode;
    private String dictName;
    private String dictType;
    private String description;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
