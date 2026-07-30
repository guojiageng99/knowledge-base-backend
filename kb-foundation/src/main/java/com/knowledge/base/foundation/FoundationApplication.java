package com.knowledge.base.foundation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableCaching
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "com.knowledge.base")
public class FoundationApplication {

    @Value("${server.port}")
    private int serverPort;

    public static void main(String[] args) {
        SpringApplication.run(FoundationApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printStartupMessage() {
        System.out.println("========================================");
        System.out.println("   基础服务启动成功！");
        System.out.println("   服务名称: kb-foundation");
        System.out.println("   服务端口: " + serverPort);
        System.out.println("   API文档: http://localhost:" + serverPort + "/api/foundation/doc.html");
        System.out.println("   Druid监控: http://localhost:" + serverPort + "/api/foundation/druid/");
        System.out.println("========================================");
    }
}
