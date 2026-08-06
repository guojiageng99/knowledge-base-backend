package com.knowledge.base.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = "com.knowledge.base.ai.config")
@ComponentScan(basePackages = "com.knowledge.base", excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX, pattern = "com\\.knowledge\\.base\\.ai\\.rag\\..*"))
@MapperScan("com.knowledge.base.ai.mapper")
@EnableFeignClients(basePackages = {"com.knowledge.base.ai.rag.client", "com.knowledge.base.ai.rag.kag.client"})
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
