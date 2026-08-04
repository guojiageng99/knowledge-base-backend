package com.knowledge.base.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.knowledge.base")
@EnableFeignClients(basePackages = "com.knowledge.base.search.feign")
@MapperScan("com.knowledge.base.search.mapper")
public class SearchApplication {
    public static void main(String[] args) { SpringApplication.run(SearchApplication.class, args); }
}
