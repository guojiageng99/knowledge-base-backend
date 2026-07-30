package com.knowledge.base.foundation.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OperationLogDTO implements Serializable {

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
}
