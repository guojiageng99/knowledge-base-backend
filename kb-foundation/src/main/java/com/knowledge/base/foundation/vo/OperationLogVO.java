package com.knowledge.base.foundation.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OperationLogVO implements Serializable {

    private Long id;
    private String module;
    private String operationType;
    private String operationDesc;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String responseResult;
    private Long userId;
    private String username;
    private String ipAddress;
    private String location;
    private String userAgent;
    private Integer executeTime;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}
