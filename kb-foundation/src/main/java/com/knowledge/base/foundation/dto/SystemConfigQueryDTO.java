package com.knowledge.base.foundation.dto;

import com.knowledge.base.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "System configuration query parameters")
public class SystemConfigQueryDTO extends PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String configKey;
    private String category;
    private String configType;
    private Integer isPublic;
    private String keyword;
}
