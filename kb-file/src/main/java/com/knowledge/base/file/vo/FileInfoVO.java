package com.knowledge.base.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "File information response")
public class FileInfoVO {

    private Long id;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String fileSizeReadable;
    private String fileType;
    private String mimeType;
    private String fileUrl;
    private String previewUrl;
    private String convertedUrl;
    private String newUrl;
    private Long uploaderId;
    private String uploaderName;
    private Integer accessLevel;
    private Integer downloadCount;
    private String storageType;
    private LocalDateTime createdAt;
}
