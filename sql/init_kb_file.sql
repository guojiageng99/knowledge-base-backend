CREATE DATABASE IF NOT EXISTS kb_file DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kb_file;

CREATE TABLE IF NOT EXISTS tb_file (
    id BIGINT NOT NULL COMMENT 'File ID',
    original_name VARCHAR(255) NOT NULL COMMENT 'Original file name',
    stored_name VARCHAR(255) DEFAULT NULL COMMENT 'Stored file name',
    file_path VARCHAR(500) NOT NULL COMMENT 'Storage-relative file path',
    file_size BIGINT NOT NULL COMMENT 'Size in bytes',
    file_type VARCHAR(50) NOT NULL COMMENT 'IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER',
    mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME type',
    file_hash VARCHAR(64) DEFAULT NULL COMMENT 'SHA-256 hash',
    storage_type VARCHAR(50) NOT NULL DEFAULT 'RUSTFS' COMMENT 'Storage implementation',
    bucket_name VARCHAR(100) DEFAULT NULL COMMENT 'Object-storage bucket',
    uploader_id BIGINT NOT NULL COMMENT 'Uploader ID',
    access_level TINYINT NOT NULL DEFAULT 0 COMMENT '0 private, 1 team, 2 public',
    download_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 deleted, 1 normal',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_file_hash (file_hash),
    KEY idx_uploader_id (uploader_id),
    KEY idx_storage_type (storage_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File metadata';
