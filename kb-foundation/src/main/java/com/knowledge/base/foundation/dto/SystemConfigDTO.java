package com.knowledge.base.foundation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class SystemConfigDTO implements Serializable {

    private Long id;
    @NotBlank
    private String configKey;
    private String configValue;
    @NotBlank
    private String configType;
    @NotBlank
    private String category;
    private String description;
    @NotNull
    private Integer isPublic;
}
