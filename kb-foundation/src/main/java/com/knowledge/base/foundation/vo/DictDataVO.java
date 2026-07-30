package com.knowledge.base.foundation.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DictDataVO implements Serializable {

    private Long id;
    private Long dictId;
    private String dictCode;
    private String dictLabel;
    private String dictValue;
    private Integer dictSort;
    private String cssClass;
    private String listClass;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
}
