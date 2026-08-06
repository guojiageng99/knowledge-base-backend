package com.knowledge.base.file.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "file.storage.type", havingValue = "rustfs")
public class RustFsS3Config {

    @Bean
    public S3Client s3Client(FileStorageProperties properties) {
        FileStorageProperties.Rustfs rustfs = properties.getRustfs();
        String scheme = rustfs.isSecure() ? "https" : "http";
        return S3Client.builder()
                .endpointOverride(URI.create("%s://%s:%d".formatted(scheme, rustfs.getEndpoint(), rustfs.getPort())))
                .httpClient(ApacheHttpClient.builder()
                        .connectionTimeout(Duration.ofMillis(rustfs.getConnectTimeout()))
                        .socketTimeout(Duration.ofMillis(rustfs.getReadTimeout()))
                        .expectContinueEnabled(false)
                        .build())
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

    @Bean
    public MinioClient minioClient(FileStorageProperties properties) {
        FileStorageProperties.Rustfs rustfs = properties.getRustfs();
        String scheme = rustfs.isSecure() ? "https" : "http";
        MinioClient client = MinioClient.builder()
                .endpoint("%s://%s:%d".formatted(scheme, rustfs.getEndpoint(), rustfs.getPort()))
                .credentials(rustfs.getAccessKey(), rustfs.getSecretKey())
                .build();
        client.setTimeout(rustfs.getConnectTimeout(), rustfs.getReadTimeout(), rustfs.getWriteTimeout());
        return client;
    }
}
