package com.knowledge.base.common.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.UUID;

/** Resolves the stable identifier used to isolate local RabbitMQ resources. */
@Slf4j
@Component
public class InstanceIdentifier implements InitializingBean {

    @Value("${app.instance.id:}")
    private String configuredId;

    @Getter
    private String id;

    @Override
    public void afterPropertiesSet() {
        id = resolve();
        log.info("Current RabbitMQ instance id: {}", id);
    }

    private String resolve() {
        if (configuredId != null && !configuredId.trim().isEmpty()) {
            return sanitize(configuredId.trim());
        }
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            if (hostname != null && !hostname.isBlank()) {
                int dotIndex = hostname.indexOf('.');
                return sanitize(dotIndex > 0 ? hostname.substring(0, dotIndex) : hostname);
            }
        } catch (Exception exception) {
            log.warn("Unable to resolve local hostname: {}", exception.getMessage());
        }
        return "local-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String sanitize(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9._-]", "-");
    }
}
