package com.knowledge.base.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    private String type = "rustfs";
    private Rustfs rustfs = new Rustfs();
    private Upload upload = new Upload();
    private Url url = new Url();

    @Data
    public static class Rustfs {
        private String endpoint = "localhost";
        private int port = 9000;
        private String accessKey;
        private String secretKey;
        private boolean secure;
        private String bucketName = "knowledge-base";
        private int connectTimeout = 30_000;
        private int readTimeout = 60_000;
        private int writeTimeout = 60_000;
        private int maxConnections = 50;
        private boolean enabled = true;
    }

    @Data
    public static class Upload {
        private long maxSize = 52_428_800L;
        private List<String> allowedTypes = List.of(
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md",
                "png", "jpg", "jpeg", "gif", "bmp", "svg"
        );
        private boolean enableFastUpload = true;
        private boolean calculateHash = true;
        private boolean enableResumableUpload = true;
    }

    @Data
    public static class Url {
        private String prefix = "/api/file/files";
        private String previewPrefix = "/api/file/files/preview";
    }
}
