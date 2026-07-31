package com.knowledge.base.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.file.config.FileStorageProperties;
import com.knowledge.base.file.dto.FileQueryDTO;
import com.knowledge.base.file.dto.FileUploadDTO;
import com.knowledge.base.file.entity.FileInfo;
import com.knowledge.base.file.mapper.FileMapper;
import com.knowledge.base.file.service.FileService;
import com.knowledge.base.file.storage.FileStorage;
import com.knowledge.base.file.storage.FileStorageFactory;
import com.knowledge.base.file.vo.FileInfoVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private final FileStorageFactory storageFactory;
    private final FileStorageProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfoVO uploadFile(MultipartFile file, FileUploadDTO dto) {
        validateFile(file);
        String fileHash = calculateHash(file);
        FileInfo existing = findExistingFile(fileHash);
        if (existing != null) {
            return toVO(existing);
        }

        String relativePath = generateRelativePath(fileHash, file.getOriginalFilename());
        FileStorage storage = storageFactory.getStorage();
        try (InputStream inputStream = file.getInputStream()) {
            if (!storage.upload(inputStream, relativePath, file.getSize())) {
                throw new BusinessException("File upload failed");
            }
        } catch (IOException exception) {
            throw new BusinessException("Unable to read uploaded file", exception);
        }

        FileInfo fileInfo = buildFileInfo(file, dto, fileHash, relativePath, storage.getStorageType());
        try {
            if (fileMapper.insert(fileInfo) != 1) {
                throw new BusinessException("Unable to save file metadata");
            }
        } catch (RuntimeException exception) {
            storage.delete(relativePath);
            throw exception;
        }
        return toVO(fileInfo);
    }

    @Override
    public List<FileInfoVO> uploadFiles(MultipartFile[] files, FileUploadDTO dto) {
        if (files == null || files.length == 0) {
            throw new BusinessException("Files must not be empty");
        }
        List<FileInfoVO> results = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            results.add(uploadFile(file, dto));
        }
        return results;
    }

    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) throws IOException {
        streamFile(fileId, response, true);
    }

    @Override
    public void previewFile(Long fileId, HttpServletResponse response) throws IOException {
        streamFile(fileId, response, false);
    }

    @Override
    public FileInfoVO getFileInfo(Long fileId) {
        return toVO(requireFile(fileId));
    }

    @Override
    public PageResult<FileInfoVO> pageFiles(FileQueryDTO dto) {
        long current = dto.getCurrent() == null || dto.getCurrent() < 1 ? 1 : dto.getCurrent();
        long size = dto.getSize() == null || dto.getSize() < 1 ? 10 : Math.min(dto.getSize(), 100);
        LambdaQueryWrapper<FileInfo> query = new LambdaQueryWrapper<FileInfo>()
                .eq(StringUtils.hasText(dto.getFileType()), FileInfo::getFileType, dto.getFileType())
                .eq(dto.getUploaderId() != null, FileInfo::getUploaderId, dto.getUploaderId())
                .eq(StringUtils.hasText(dto.getStorageType()), FileInfo::getStorageType, dto.getStorageType())
                .like(StringUtils.hasText(dto.getKeyword()), FileInfo::getOriginalName, dto.getKeyword())
                .orderByDesc(FileInfo::getCreateTime);
        Page<FileInfo> page = fileMapper.selectPage(new Page<>(current, size), query);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long fileId) {
        FileInfo file = requireFile(fileId);
        if (!storageFactory.getStorage().delete(file.getFilePath())) {
            return false;
        }
        return fileMapper.deleteById(fileId) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException("File IDs must not be empty");
        }
        for (Long fileId : fileIds) {
            if (!deleteFile(fileId)) {
                return false;
            }
        }
        return true;
    }

    private void streamFile(Long fileId, HttpServletResponse response, boolean attachment) throws IOException {
        FileInfo file = requireFile(fileId);
        FileStorage storage = storageFactory.getStorage();
        if (!storage.exists(file.getFilePath())) {
            throw new BusinessException("File object does not exist");
        }
        response.setContentType(StringUtils.hasText(file.getMimeType()) ? file.getMimeType() : "application/octet-stream");
        response.setContentLengthLong(file.getFileSize());
        String encodedName = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", (attachment ? "attachment" : "inline") + "; filename*=UTF-8''" + encodedName);
        storage.download(file.getFilePath(), response.getOutputStream());
        if (attachment) {
            fileMapper.incrementDownloadCount(file.getId());
        }
    }

    private FileInfo requireFile(Long fileId) {
        if (fileId == null) {
            throw new BusinessException("File ID must not be null");
        }
        FileInfo file = fileMapper.selectById(fileId);
        if (file == null || !Integer.valueOf(1).equals(file.getStatus())) {
            throw new BusinessException("File does not exist");
        }
        return file;
    }

    private FileInfo findExistingFile(String fileHash) {
        if (!properties.getUpload().isEnableFastUpload()) {
            return null;
        }
        return fileMapper.selectOne(new LambdaQueryWrapper<FileInfo>()
                .eq(FileInfo::getFileHash, fileHash)
                .eq(FileInfo::getStatus, 1)
                .last("LIMIT 1"));
    }

    private String calculateHash(MultipartFile file) {
        if (!properties.getUpload().isCalculateHash()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtil.sha256Hex(inputStream);
        } catch (IOException exception) {
            throw new BusinessException("Unable to calculate file hash", exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File must not be empty");
        }
        if (file.getSize() > properties.getUpload().getMaxSize()) {
            throw new BusinessException("File size exceeds the limit");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (extension.isEmpty() || (!properties.getUpload().getAllowedTypes().contains("*")
                && !properties.getUpload().getAllowedTypes().contains(extension))) {
            throw new BusinessException("Unsupported file type: " + extension);
        }
    }

    private FileInfo buildFileInfo(MultipartFile file, FileUploadDTO dto, String hash, String path, String storageType) {
        FileInfo info = new FileInfo();
        info.setId(SnowflakeIdGenerator.nextId());
        info.setOriginalName(file.getOriginalFilename());
        info.setStoredName(path.substring(path.lastIndexOf('/') + 1));
        info.setFilePath(path);
        info.setFileSize(file.getSize());
        info.setFileType(detectFileType(extensionOf(file.getOriginalFilename())));
        info.setMimeType(file.getContentType());
        info.setFileHash(hash);
        info.setStorageType(storageType);
        info.setBucketName(properties.getRustfs().getBucketName());
        info.setUploaderId(dto.getUploaderId() == null ? 1L : dto.getUploaderId());
        info.setAccessLevel(dto.getAccessLevel() == null ? 0 : dto.getAccessLevel());
        info.setDownloadCount(0);
        info.setStatus(1);
        info.setDeleted(0);
        info.setCreateTime(LocalDateTime.now());
        info.setUpdateTime(LocalDateTime.now());
        return info;
    }

    private String generateRelativePath(String hash, String originalName) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = extensionOf(originalName);
        return date + "/" + hash.substring(0, 2) + "/" + hash + (extension.isEmpty() ? "" : "." + extension);
    }

    private FileInfoVO toVO(FileInfo file) {
        String base = properties.getUrl().getPrefix();
        return FileInfoVO.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileSize(file.getFileSize())
                .fileSizeReadable(formatSize(file.getFileSize()))
                .fileType(file.getFileType())
                .mimeType(file.getMimeType())
                .fileUrl(base + "/download/" + file.getId())
                .previewUrl(properties.getUrl().getPreviewPrefix() + "/" + file.getId())
                .uploaderId(file.getUploaderId())
                .accessLevel(file.getAccessLevel())
                .downloadCount(file.getDownloadCount())
                .storageType(file.getStorageType())
                .createdAt(file.getCreateTime())
                .build();
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || filename.lastIndexOf('.') < 0) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String detectFileType(String extension) {
        return switch (extension) {
            case "png", "jpg", "jpeg", "gif", "bmp", "svg" -> "IMAGE";
            case "mp4", "avi", "mov", "wmv" -> "VIDEO";
            case "mp3", "wav", "flac" -> "AUDIO";
            case "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md" -> "DOCUMENT";
            default -> "OTHER";
        };
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return "%.2f KB".formatted(bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return "%.2f MB".formatted(bytes / (1024.0 * 1024));
        return "%.2f GB".formatted(bytes / (1024.0 * 1024 * 1024));
    }
}
