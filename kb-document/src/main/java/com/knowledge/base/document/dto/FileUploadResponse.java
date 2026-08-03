package com.knowledge.base.document.dto;

import lombok.Data;

/** File-service response used by the document import flow. */
@Data
public class FileUploadResponse {
    private Long id;
    private String originalName;
    private String storedName;
    private String fileUrl;
    private String previewUrl;
    private String convertedUrl;
    private Long fileSize;
    private String mimeType;
    private String newUrl;
}
