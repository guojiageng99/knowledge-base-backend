package com.knowledge.base.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class UnifiedResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                MediaType contentType = getDelegate().getHeaders().getContentType();
                if (!(body instanceof Flux) || contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                    return super.writeWith(body);
                }
                return DataBufferUtils.join(Flux.from(body)).flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    String source = new String(bytes, StandardCharsets.UTF_8);
                    String wrapped = wrapResponse(source);
                    byte[] output = wrapped.getBytes(StandardCharsets.UTF_8);
                    getDelegate().getHeaders().setContentLength(output.length);
                    log.info("Gateway response => uri={}, status={}, body={}", exchange.getRequest().getURI(), getStatusCode(), wrapped);
                    return super.writeWith(Mono.just(getDelegate().bufferFactory().wrap(output)));
                });
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(publisher -> publisher));
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private String wrapResponse(String responseData) {
        try {
            Object parsed = JSON.parse(responseData);
            if (parsed instanceof JSONObject object && object.containsKey("code") && object.containsKey("message")) {
                return responseData;
            }
            JSONObject result = new JSONObject();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", parsed);
            result.put("timestamp", System.currentTimeMillis());
            return result.toJSONString();
        } catch (Exception exception) {
            JSONObject result = new JSONObject();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", responseData);
            result.put("timestamp", System.currentTimeMillis());
            return result.toJSONString();
        }
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
