package com.knowledge.base.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalLogFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = GlobalLogFilter.class.getName() + ".startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());
        log.info("Request => method={}, uri={}, remoteAddress={}", request.getMethod(), request.getURI(), request.getRemoteAddress());
        return chain.filter(exchange).doFinally(signal -> {
            Long startTime = exchange.getAttribute(START_TIME_ATTR);
            if (startTime != null) {
                log.info("Response => status={}, time={}ms", exchange.getResponse().getStatusCode(),
                        System.currentTimeMillis() - startTime);
            }
        });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
