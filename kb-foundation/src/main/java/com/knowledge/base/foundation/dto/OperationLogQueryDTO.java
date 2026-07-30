package com.knowledge.base.foundation.dto;

import com.knowledge.base.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Operation log query parameters")
public class OperationLogQueryDTO extends PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String module;
    private String operationType;
    private Long userId;
    private String username;
    private Integer status;
    private String startTime;
    private String endTime;
    private String keyword;
}
