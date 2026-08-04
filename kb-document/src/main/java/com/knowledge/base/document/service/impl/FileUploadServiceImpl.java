package com.knowledge.base.document.service.impl;

import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.document.dto.FileUploadResponse;
import com.knowledge.base.document.feign.FileServiceFeignClient;
import com.knowledge.base.document.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private static final Set<String> INTERNAL_HOST_MARKERS = Set.of("localhost", "127.0.0.1", "rustfs");
    private final FileServiceFeignClient fileServiceFeignClient;

    @Override
    public String uploadImageFromUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            throw new BusinessException("Image URL must not be empty");
        }
        String url = imageUrl.trim().replaceAll("^`|`$", "");
        if (INTERNAL_HOST_MARKERS.stream().anyMatch(url::contains)) {
            return url;
        }
        ResultHolder result = new ResultHolder(fileServiceFeignClient.uploadFromUrl(url));
        FileUploadResponse file = result.data;
        if (file == null) {
            throw new BusinessException("Image upload failed");
        }
        if (StringUtils.hasText(file.getNewUrl())) return file.getNewUrl();
        if (StringUtils.hasText(file.getFileUrl())) return file.getFileUrl();
        if (StringUtils.hasText(file.getPreviewUrl())) return file.getPreviewUrl();
        throw new BusinessException("Image upload returned no URL");
    }

    @Override
    public String uploadDocumentFile(org.springframework.web.multipart.MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File must not be empty");
        }
        com.knowledge.base.common.result.Result<FileUploadResponse> result =
                fileServiceFeignClient.uploadFile(file, uploaderId, 0);
        FileUploadResponse uploaded = result == null ? null : result.getData();
        if (uploaded == null) {
            throw new BusinessException("File service upload failed");
        }
        if (StringUtils.hasText(uploaded.getFileUrl())) return uploaded.getFileUrl();
        if (StringUtils.hasText(uploaded.getPreviewUrl())) return uploaded.getPreviewUrl();
        if (uploaded.getId() != null) return "/api/file/files/preview/" + uploaded.getId();
        throw new BusinessException("File service returned no file URL");
    }

    private record ResultHolder(FileUploadResponse data) {
        private ResultHolder(com.knowledge.base.common.result.Result<FileUploadResponse> result) {
            this(result == null ? null : result.getData());
        }
    }
}
