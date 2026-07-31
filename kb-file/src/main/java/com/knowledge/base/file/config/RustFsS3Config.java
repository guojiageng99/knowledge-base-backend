package com.knowledge.base.file.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "file.storage.type", havingValue = "rustfs")
public class RustFsS3Config {

    @Bean
    public S3Client s3Client(FileStorageProperties properties) {
        FileStorageProperties.Rustfs rustfs = properties.getRustfs();
        String scheme = rustfs.isSecure() ? "https" : "http";
        return S3Client.builder()
                .endpointOverride(URI.create("%s://%s:%d".formatted(scheme, rustfs.getEndpoint(), rustfs.getPort())))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(rustfs.getAccessKey(), rustfs.getSecretKey())))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .chunkedEncodingEnabled(false)
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
