package com.knowledge.base.foundation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableCaching
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "com.knowledge.base")
public class FoundationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoundationApplication.class, args);
    }
}
