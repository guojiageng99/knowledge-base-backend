package com.knowledge.base.foundation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_operation_log")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_ID)
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

    @TableField("create_time")
    private LocalDateTime createTime;
}
