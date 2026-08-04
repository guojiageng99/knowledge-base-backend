package com.knowledge.base.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.knowledge.base")
@MapperScan("com.knowledge.base.ai.mapper")
@EnableFeignClients(basePackages = {"com.knowledge.base.ai.rag.client", "com.knowledge.base.ai.rag.kag.client"})
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
