package com.knowledge.base.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(scanBasePackages = "com.knowledge.base")
public class StatisticsApplication {
    public static void main(String[] args) { SpringApplication.run(StatisticsApplication.class, args); }
}
