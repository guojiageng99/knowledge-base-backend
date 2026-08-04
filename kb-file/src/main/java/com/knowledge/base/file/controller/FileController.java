package com.knowledge.base.file.controller;

import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.file.dto.FileQueryDTO;
import com.knowledge.base.file.dto.FileUploadDTO;
import com.knowledge.base.file.service.FileService;
import com.knowledge.base.file.vo.FileInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
@Tag(name = "File management", description = "File upload, download, preview, and metadata APIs")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file")
    public Result<FileInfoVO> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploaderId", defaultValue = "1") Long uploaderId,
            @RequestParam(value = "accessLevel", defaultValue = "0") Integer accessLevel,
            @RequestParam(value = "storageType", defaultValue = "rustfs") String storageType) {
        FileUploadDTO dto = new FileUploadDTO();
        dto.setUploaderId(uploaderId);
        dto.setAccessLevel(accessLevel);
        dto.setStorageType(storageType);
        return Result.success(fileService.uploadFile(file, dto));
    }

    @PostMapping("/batch-upload")
    @Operation(summary = "Upload multiple files")
    public Result<List<FileInfoVO>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "uploaderId", defaultValue = "1") Long uploaderId,
            @RequestParam(value = "accessLevel", defaultValue = "0") Integer accessLevel) {
        FileUploadDTO dto = new FileUploadDTO();
        dto.setUploaderId(uploaderId);
        dto.setAccessLevel(accessLevel);
        return Result.success(fileService.uploadFiles(files, dto));
    }

    @PostMapping("/upload-from-url")
    @Operation(summary = "Upload a remote file URL")
    public Result<FileInfoVO> uploadFromUrl(@RequestParam String imageUrl) {
        FileUploadDTO dto = new FileUploadDTO();
        dto.setBusinessType("document-image");
        return Result.success(fileService.uploadFromUrl(imageUrl, dto));
    }

    @GetMapping({"/download/{fileId}", "/download/{fileId}/**"})
    @Operation(summary = "Download a file")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        fileService.downloadFile(fileId, response);
    }

    @GetMapping("/preview/{fileId}")
    @Operation(summary = "Get file preview URL")
    public Result<String> getPreviewUrl(@PathVariable Long fileId) {
        return Result.success(fileService.getPreviewUrl(fileId));
    }

    @GetMapping({"/preview/{fileId}/content", "/preview/{fileId}/**"})
    @Operation(summary = "Preview a file")
    public void previewFile(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        fileService.previewFile(fileId, response);
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Get file metadata")
    public Result<FileInfoVO> getFileInfo(@PathVariable Long fileId) {
        return Result.success(fileService.getFileInfo(fileId));
    }

    @PostMapping("/convert/{fileId}")
    @Operation(summary = "Transcode audio or video to HLS")
    public Result<String> convert(@PathVariable Long fileId, @RequestParam(defaultValue = "hls") String targetFormat) { return Result.success("Transcoding submitted", fileService.convertFileFormat(fileId, targetFormat)); }

    @GetMapping("/stream/{fileId}/master.m3u8")
    public void streamMaster(@PathVariable Long fileId, HttpServletResponse response) throws IOException { fileService.streamMasterPlaylist(fileId, response); }

    @GetMapping("/stream/{fileId}/{*resource}")
    public void streamHls(@PathVariable Long fileId, @PathVariable String resource, HttpServletResponse response) throws IOException {
        fileService.streamHlsResource(fileId, resource, response);
    }

    @GetMapping("/thumbnail/{fileId}")
    public void thumbnail(@PathVariable Long fileId, HttpServletResponse response) throws IOException { fileService.streamThumbnail(fileId, response); }

    @PostMapping("/page")
    @Operation(summary = "Page files")
    public Result<PageResult<FileInfoVO>> pageFiles(@RequestBody FileQueryDTO dto) {
        return Result.success(fileService.pageFiles(dto));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file")
    public Result<Boolean> deleteFile(@PathVariable Long fileId) {
        return Result.success(fileService.deleteFile(fileId));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "Delete multiple files")
    public Result<Boolean> batchDeleteFiles(@RequestBody List<Long> fileIds) {
        return Result.success(fileService.batchDeleteFiles(fileIds));
    }
}
