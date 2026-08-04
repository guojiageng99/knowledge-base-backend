package com.knowledge.base.document.feign;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.document.dto.FileUploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "kb-file",
        url = "${file.service.url:http://localhost:8084/api/file}",
        path = "/files")
public interface FileServiceFeignClient {

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    Result<FileUploadResponse> uploadFile(@RequestPart("file") MultipartFile file,
                                           @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                           @RequestParam(value = "accessLevel", required = false) Integer accessLevel);

    @PostMapping("/upload-from-url")
    Result<FileUploadResponse> uploadFromUrl(@RequestParam("imageUrl") String imageUrl);
}
