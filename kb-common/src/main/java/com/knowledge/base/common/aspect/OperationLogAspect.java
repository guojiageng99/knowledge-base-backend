package com.knowledge.base.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.base.common.annotation.OperationLog;
import com.knowledge.base.common.config.InstanceIdentifier;
import com.knowledge.base.common.event.OperationLogEventDTO;
import com.knowledge.base.common.utils.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {
    private static final String EXCHANGE = "kb.operationlog.exchange";

    private final RabbitTemplate rabbitTemplate;
    private final InstanceIdentifier instanceIdentifier;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startedAt = System.currentTimeMillis();
        HttpServletRequest request = currentRequest();
        OperationLogEventDTO.OperationLogEventDTOBuilder event = OperationLogEventDTO.builder()
                .module(operationLog.module())
                .operationType(operationLog.operation())
                .operationDesc(operationLog.description())
                .userId(UserContextUtil.getCurrentUserId())
                .username(UserContextUtil.getCurrentUsername())
                .executeTime(0)
                .status(1);
        if (request != null) {
            event.requestMethod(request.getMethod())
                    .requestUrl(request.getRequestURI())
                    .ipAddress(request.getRemoteAddr())
                    .userAgent(request.getHeader("User-Agent"));
        }
        try {
            Object result = joinPoint.proceed();
            event.responseResult(toJson(result));
            return result;
        } catch (Throwable throwable) {
            event.status(0).errorMsg(throwable.getMessage());
            throw throwable;
        } finally {
            event.executeTime((int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt));
            publish(event.build());
        }
    }

    private void publish(OperationLogEventDTO event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE,
                    "operationlog." + instanceIdentifier.getId() + ".record", event);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish operation log event", exception);
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            String json = objectMapper.writeValueAsString(value);
            return json.length() > 10000 ? json.substring(0, 10000) : json;
        } catch (Exception exception) {
            return String.valueOf(value);
        }
    }
}
