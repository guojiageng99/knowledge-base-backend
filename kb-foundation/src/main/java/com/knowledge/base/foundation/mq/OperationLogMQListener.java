package com.knowledge.base.foundation.mq;

import com.knowledge.base.common.event.OperationLogEventDTO;
import com.knowledge.base.foundation.entity.OperationLog;
import com.knowledge.base.foundation.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogMQListener {
    private final OperationLogMapper operationLogMapper;

    @RabbitListener(queues = "#{@operationLogQueue.name}")
    public void handle(OperationLogEventDTO event) {
        if (event == null) return;
        OperationLog logEntity = new OperationLog();
        logEntity.setModule(event.getModule());
        logEntity.setOperationType(event.getOperationType());
        logEntity.setOperationDesc(event.getOperationDesc());
        logEntity.setRequestMethod(event.getRequestMethod());
        logEntity.setRequestUrl(event.getRequestUrl());
        logEntity.setRequestParams(event.getRequestParams());
        logEntity.setResponseResult(event.getResponseResult());
        logEntity.setUserId(event.getUserId());
        logEntity.setUsername(event.getUsername());
        logEntity.setIpAddress(event.getIpAddress());
        logEntity.setLocation(event.getLocation());
        logEntity.setUserAgent(event.getUserAgent());
        logEntity.setExecuteTime(event.getExecuteTime());
        logEntity.setStatus(event.getStatus());
        logEntity.setErrorMsg(event.getErrorMsg());
        operationLogMapper.insert(logEntity);
    }
}
