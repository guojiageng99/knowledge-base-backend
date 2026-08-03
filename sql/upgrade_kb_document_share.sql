SET NAMES utf8mb4;
USE `kb_document`;

CREATE TABLE IF NOT EXISTS `kb_document_share` (
  `id` BIGINT NOT NULL,
  `share_id` VARCHAR(32) NOT NULL,
  `document_id` BIGINT NOT NULL,
  `title` VARCHAR(255) DEFAULT NULL,
  `share_type` TINYINT DEFAULT 1,
  `share_code` VARCHAR(32) DEFAULT NULL,
  `expire_type` TINYINT DEFAULT 1,
  `expire_time` DATETIME DEFAULT NULL,
  `access_limit` INT DEFAULT 0,
  `access_count` INT DEFAULT 0,
  `require_password` TINYINT DEFAULT 0,
  `password` VARCHAR(64) DEFAULT NULL,
  `sharer_id` BIGINT NOT NULL,
  `sharer_name` VARCHAR(64) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `share_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_id` (`share_id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_sharer_id` (`sharer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_share_time` (`share_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='document shares';
