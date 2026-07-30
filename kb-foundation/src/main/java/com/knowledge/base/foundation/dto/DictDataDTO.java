package com.knowledge.base.foundation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class DictDataDTO implements Serializable {

    private Long id;
    @NotNull
    private Long dictId;
    private String dictCode;
    @NotBlank
    private String dictLabel;
    @NotBlank
    private String dictValue;
    @NotNull
    private Integer dictSort;
    private String cssClass;
    private String listClass;
    @NotNull
    private Integer isDefault;
    @NotNull
    private Integer status;
}
