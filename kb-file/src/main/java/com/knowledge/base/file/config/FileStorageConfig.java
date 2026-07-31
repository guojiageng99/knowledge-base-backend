package com.knowledge.base.file.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
public class FileStorageConfig {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @PostConstruct
    void initializeUploadDirectory() {
        try {
            Path directory = Path.of(uploadPath).toAbsolutePath();
            Files.createDirectories(directory);
            log.info("Multipart upload staging directory: {}", directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create upload directory", exception);
        }
    }
}
