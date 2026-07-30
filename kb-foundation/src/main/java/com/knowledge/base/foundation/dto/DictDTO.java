package com.knowledge.base.foundation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class DictDTO implements Serializable {

    private Long id;
    @NotBlank
    private String dictCode;
    @NotBlank
    private String dictName;
    @NotBlank
    private String dictType;
    private String description;
    @NotNull
    private Integer sort;
    @NotNull
    private Integer status;
}
