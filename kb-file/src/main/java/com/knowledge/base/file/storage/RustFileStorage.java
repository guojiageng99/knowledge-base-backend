package com.knowledge.base.file.storage;

import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.file.config.FileStorageProperties;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("rustFileStorage")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "rustfs")
public class RustFileStorage implements FileStorage {

    private final S3Client s3Client;
    private final MinioClient minioClient;
    private final FileStorageProperties properties;
    private final Map<String, UploadSession> uploadSessions = new ConcurrentHashMap<>();

    @Override
    public boolean upload(InputStream inputStream, String relativePath, long fileSize) {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName()).object(relativePath)
                    .stream(inputStream, fileSize, -1).build());
            return true;
        } catch (Exception exception) {
            log.error("Failed to upload object {}", relativePath, exception);
            throw new BusinessException("File upload failed", exception);
        }
    }

    @Override
    public long download(String relativePath, OutputStream outputStream) {
        try (GetObjectResponse inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName()).object(relativePath).build())) {
            return inputStream.transferTo(outputStream);
        } catch (Exception exception) {
            log.error("Failed to download object {}", relativePath, exception);
            throw new BusinessException("File download failed", exception);
        }
    }

    @Override
    public InputStream getInputStream(String relativePath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName()).object(relativePath).build());
        } catch (Exception exception) {
            log.error("Failed to open object {}", relativePath, exception);
            throw new BusinessException("Unable to open file", exception);
        }
    }

    @Override
    public boolean delete(String relativePath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName()).object(relativePath).build());
            return true;
        } catch (Exception exception) {
            log.error("Failed to delete object {}", relativePath, exception);
            throw new BusinessException("File deletion failed", exception);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName()).object(relativePath).build());
            return true;
        } catch (Exception exception) {
            return !isMissingObject(exception) && failExistenceCheck(relativePath, exception);
        }
    }

    @Override
    public long getFileSize(String relativePath) {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName()).object(relativePath).build());
            return response.size();
        } catch (Exception exception) {
            log.error("Failed to inspect object {}", relativePath, exception);
            throw new BusinessException("Unable to read file metadata", exception);
        }
    }

    private boolean failExistenceCheck(String relativePath, Exception exception) {
        log.error("Failed to check object {}", relativePath, exception);
        throw new BusinessException("File storage is unavailable", exception);
    }

    private boolean isMissingObject(Exception exception) {
        return exception instanceof io.minio.errors.ErrorResponseException error
                && ("NoSuchKey".equals(error.errorResponse().code()) || "NoSuchObject".equals(error.errorResponse().code()));
    }

    @Override
    public String getStorageType() {
        return "RUSTFS";
    }

    private String bucketName() {
        return properties.getRustfs().getBucketName();
    }

    public void initResumableUpload(String sessionId, String relativePath) {
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder().bucket(bucketName()).key(relativePath).build());
        uploadSessions.put(sessionId, new UploadSession(relativePath, response.uploadId()));
    }

    public void uploadChunk(String sessionId, int chunkIndex, InputStream inputStream, long chunkSize) {
        UploadSession session = requireSession(sessionId);
        int partNumber = chunkIndex + 1;
        UploadPartResponse response = s3Client.uploadPart(UploadPartRequest.builder()
                        .bucket(bucketName()).key(session.relativePath()).uploadId(session.uploadId()).partNumber(partNumber).build(),
                RequestBody.fromInputStream(inputStream, chunkSize));
        session.parts().put(partNumber, response.eTag());
    }

    public int[] getUploadedChunks(String sessionId) {
        return requireSession(sessionId).parts().keySet().stream()
                .mapToInt(partNumber -> partNumber - 1).sorted().toArray();
    }

    public boolean mergeChunks(String sessionId) {
        UploadSession session = requireSession(sessionId);
        if (session.parts().isEmpty()) {
            throw new BusinessException("No uploaded chunks to merge");
        }
        CompletedPart[] parts = session.parts().entrySet().stream()
                .map(entry -> CompletedPart.builder().partNumber(entry.getKey()).eTag(entry.getValue()).build())
                .sorted(Comparator.comparingInt(CompletedPart::partNumber))
                .toArray(CompletedPart[]::new);
        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(bucketName()).key(session.relativePath()).uploadId(session.uploadId())
                .multipartUpload(CompletedMultipartUpload.builder().parts(Arrays.asList(parts)).build())
                .build());
        uploadSessions.remove(sessionId);
        return true;
    }

    private UploadSession requireSession(String sessionId) {
        UploadSession session = uploadSessions.get(sessionId);
        if (session == null) {
            throw new BusinessException("Resumable upload session does not exist");
        }
        return session;
    }

    private record UploadSession(String relativePath, String uploadId, Map<Integer, String> parts) {
        private UploadSession(String relativePath, String uploadId) {
            this(relativePath, uploadId, new ConcurrentHashMap<>());
        }
    }
}
