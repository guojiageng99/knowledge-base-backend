package com.knowledge.base.document.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadImageFromUrl(String imageUrl);
    String uploadDocumentFile(MultipartFile file, Long uploaderId);
}
