package com.knowledge.base.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.base.common.result.Result;
import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Slf4j
@Order(-1)
@Component("gatewayGlobalExceptionHandler")
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) return Mono.error(exception);

        HttpStatus status;
        Result<?> result;
        if (exception instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            result = Result.error(status.value(), responseStatusException.getReason());
        } else if (exception instanceof ConnectTimeoutException || exception instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            result = Result.error(status.value(), "后端服务响应超时，请稍后重试");
        } else if (exception instanceof ConnectException) {
            status = HttpStatus.BAD_GATEWAY;
            result = Result.error(status.value(), "后端服务不可用，请检查服务状态");
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result = Result.error(status.value(), "系统异常：" + exception.getMessage());
            log.error("Gateway error", exception);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            DataBuffer buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(result));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException jsonException) {
            return Mono.error(jsonException);
        }
    }
}
