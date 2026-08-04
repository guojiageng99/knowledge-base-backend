package com.knowledge.base.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
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
import org.springframework.jdbc.core.JdbcTemplate;

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
    private final com.knowledge.base.file.service.MediaService mediaService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final JdbcTemplate jdbcTemplate;

    @org.springframework.beans.factory.annotation.Value("${file.transcode.rabbit.enabled:false}")
    private boolean transcodeRabbitEnabled;

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
        if (isMedia(fileInfo)) {
            try { mediaService.probeMediaInfo(fileInfo.getId()); fileInfo = fileMapper.selectById(fileInfo.getId()); }
            catch (RuntimeException exception) { log.warn("Media metadata probing failed for file {}", fileInfo.getId(), exception); }
        }
        return toVO(fileInfo);
    }

    @Override
    public FileInfoVO uploadFromUrl(String url, FileUploadDTO dto) {
        if (!StringUtils.hasText(url)) throw new BusinessException("File URL must not be empty");
        byte[] bytes;
        try { bytes = HttpUtil.downloadBytes(url.trim()); }
        catch (RuntimeException exception) { throw new BusinessException("Unable to download remote file", exception); }
        if (bytes == null || bytes.length == 0) throw new BusinessException("Remote file is empty");
        String name = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
        if (!StringUtils.hasText(name) || !name.contains(".")) name = "image.png";
        return uploadFile(new UrlMultipartFile(name, bytes), dto);
    }

    private static final class UrlMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] bytes;
        private UrlMultipartFile(String name, byte[] bytes) { this.name = name; this.bytes = bytes; }
        public String getName() { return "file"; }
        public String getOriginalFilename() { return name; }
        public String getContentType() { return "application/octet-stream"; }
        public boolean isEmpty() { return bytes.length == 0; }
        public long getSize() { return bytes.length; }
        public byte[] getBytes() { return bytes; }
        public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(bytes); }
        public void transferTo(java.io.File dest) throws java.io.IOException { java.nio.file.Files.write(dest.toPath(), bytes); }
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
    public String getPreviewUrl(Long fileId) {
        requireFile(fileId);
        return properties.getUrl().getPreviewPrefix() + "/" + fileId + "/content";
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
        long maxSize = getMaxFileSizeFromConfig();
        if (file.getSize() > maxSize) {
            throw new BusinessException("File size exceeds the limit");
        }
        String extension = extensionOf(file.getOriginalFilename());
        List<String> allowedTypes = getAllowedFileTypesFromConfig();
        if (extension.isEmpty() || (!allowedTypes.contains("*") && !allowedTypes.contains(extension))) {
            throw new BusinessException("Unsupported file type: " + extension);
        }
    }

    private long getMaxFileSizeFromConfig() {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM kb_foundation.kb_system_config WHERE config_key = 'file.upload.max.size' AND deleted = 0",
                    String.class);
            return StringUtils.hasText(value) ? Long.parseLong(value.trim()) : properties.getUpload().getMaxSize();
        } catch (Exception exception) {
            log.warn("Unable to read upload size setting: {}", exception.getMessage());
            return properties.getUpload().getMaxSize();
        }
    }

    private List<String> getAllowedFileTypesFromConfig() {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM kb_foundation.kb_system_config WHERE config_key = 'file.upload.allowed.types' AND deleted = 0",
                    String.class);
            if (StringUtils.hasText(value)) {
                return List.of(value.toLowerCase(Locale.ROOT).split(",")).stream().map(String::trim).filter(StringUtils::hasText).toList();
            }
        } catch (Exception exception) {
            log.warn("Unable to read allowed upload types setting: {}", exception.getMessage());
        }
        return properties.getUpload().getAllowedTypes();
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
                .storedName(file.getStoredName())
                .fileSize(file.getFileSize())
                .fileSizeReadable(formatSize(file.getFileSize()))
                .fileType(file.getFileType())
                .mimeType(file.getMimeType())
                .fileUrl(base + "/download/" + file.getId())
                .previewUrl(getPreviewUrl(file.getId()))
                .convertedUrl(getPreviewUrl(file.getId()))
                .newUrl(base + "/download/" + file.getId())
                .uploaderId(file.getUploaderId())
                .accessLevel(file.getAccessLevel())
                .downloadCount(file.getDownloadCount())
                .storageType(file.getStorageType())
                .createdAt(file.getCreateTime())
                .duration(file.getDuration())
                .resolution(file.getResolution())
                .bitrate(file.getBitrate())
                .transcodeStatus(file.getTranscodeStatus())
                .playUrl("DONE".equals(file.getTranscodeStatus()) && StringUtils.hasText(file.getHlsPath()) ? base + "/stream/" + file.getId() + "/master.m3u8" : null)
                .thumbnailUrl(StringUtils.hasText(file.getThumbnailPath()) ? base + "/thumbnail/" + file.getId() : null)
                .build();
    }

    @Override
    public String convertFileFormat(Long fileId, String targetFormat) {
        FileInfo file = requireFile(fileId);
        if (!isMedia(file)) throw new BusinessException("Only audio and video files can be transcoded");
        if (!"hls".equalsIgnoreCase(targetFormat)) throw new BusinessException("Only HLS transcoding is supported");
        mediaService.updateTranscodeStatus(fileId, "PENDING");
        com.knowledge.base.file.message.TranscodeMessage message = new com.knowledge.base.file.message.TranscodeMessage(fileId, "hls");
        if (transcodeRabbitEnabled) rabbitTemplate.convertAndSend(com.knowledge.base.file.config.TranscodeRabbitConfig.EXCHANGE, com.knowledge.base.file.config.TranscodeRabbitConfig.ROUTING_KEY, message);
        else java.util.concurrent.CompletableFuture.runAsync(() -> transcode(message));
        return fileId.toString();
    }

    @Override public void streamMasterPlaylist(Long fileId, HttpServletResponse response) throws IOException { streamHlsResource(fileId, "master.m3u8", response); }
    @Override public void streamHlsResource(Long fileId, String resource, HttpServletResponse response) throws IOException {
        FileInfo file = requireFile(fileId);
        String normalizedResource = normalizeHlsResource(resource);
        if (!StringUtils.hasText(file.getHlsPath()) || !safeResource(normalizedResource)
                || !storageFactory.getStorage().exists(file.getHlsPath() + "/" + normalizedResource)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(normalizedResource.endsWith(".m3u8")
                ? "application/vnd.apple.mpegurl" : "video/mp2t");
        response.setHeader("Access-Control-Allow-Origin", "*");
        try (InputStream input = storageFactory.getStorage().getInputStream(file.getHlsPath() + "/" + normalizedResource)) {
            input.transferTo(response.getOutputStream());
        }
    }
    @Override public void streamThumbnail(Long fileId, HttpServletResponse response) throws IOException { FileInfo file = requireFile(fileId); if (!StringUtils.hasText(file.getThumbnailPath())) { response.setStatus(HttpServletResponse.SC_NOT_FOUND); return; } response.setContentType("image/jpeg"); response.setHeader("Access-Control-Allow-Origin", "*"); try (InputStream input = storageFactory.getStorage().getInputStream(file.getThumbnailPath())) { input.transferTo(response.getOutputStream()); } }
    private void transcode(com.knowledge.base.file.message.TranscodeMessage message) { try { mediaService.updateTranscodeStatus(message.getFileId(), "PROCESSING"); if (mediaService.transcodeToHls(message.getFileId()) == null) throw new BusinessException("HLS transcoding failed"); mediaService.generateThumbnail(message.getFileId()); mediaService.updateTranscodeStatus(message.getFileId(), "DONE"); } catch (RuntimeException exception) { mediaService.updateTranscodeStatus(message.getFileId(), "FAILED"); log.error("Media transcoding failed for {}", message.getFileId(), exception); } }
    private boolean isMedia(FileInfo file) { return "VIDEO".equals(file.getFileType()) || "AUDIO".equals(file.getFileType()); }
    private boolean safeResource(String resource) { return StringUtils.hasText(resource) && !resource.contains("..") && (resource.endsWith(".m3u8") || resource.endsWith(".ts")); }
    private String normalizeHlsResource(String resource) {
        if (!StringUtils.hasText(resource)) {
            return "";
        }
        String normalized = resource.replace('\\', '/');
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
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
            case "mp4", "avi", "mov", "wmv", "mkv", "webm", "flv" -> "VIDEO";
            case "mp3", "wav", "flac", "aac", "ogg" -> "AUDIO";
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
