package com.knowledge.base.document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(scanBasePackages = "com.knowledge.base")
public class DocumentApplication {

    @Value("${server.port}")
    private int serverPort;

    public static void main(String[] args) {
        SpringApplication.run(DocumentApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printStartupMessage() {
        System.out.println("========================================");
        System.out.println("文档服务启动成功！");
        System.out.println("Swagger文档地址: http://localhost:" + serverPort + "/api/document/doc.html");
        System.out.println("========================================");
    }
}
