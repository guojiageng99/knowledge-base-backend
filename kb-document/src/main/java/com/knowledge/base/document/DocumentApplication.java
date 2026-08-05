package com.knowledge.base.document;

import com.knowledge.base.common.config.JwtConfig;
import com.knowledge.base.common.config.InstanceIdentifier;
import com.knowledge.base.common.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.knowledge.base.document",
        "com.knowledge.base.common.exception",
        "com.knowledge.base.common.handler",
        "com.knowledge.base.common.aspect"
})
@EnableFeignClients(basePackages = "com.knowledge.base.document.feign")
@Import({JwtConfig.class, JwtTokenUtil.class, InstanceIdentifier.class})
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
