package com.knowledge.base.common.feign;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignInterceptorConfig {

    @Bean
    public RequestInterceptor feignInterceptor() {
        return template -> template.header("INNER-REQUEST", "true");
    }
}
