package com.knowledge.base.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CorsResponseHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            deduplicate(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
            deduplicate(headers, HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
            deduplicate(headers, HttpHeaders.VARY);
        }));
    }

    private void deduplicate(HttpHeaders headers, String name) {
        List<String> values = headers.get(name);
        if (values != null && values.size() > 1) headers.put(name, values.stream().distinct().toList());
    }

    @Override
    public int getOrder() {
        return org.springframework.cloud.gateway.filter.NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER + 1;
    }
}
