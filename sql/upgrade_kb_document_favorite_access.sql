SET NAMES utf8mb4;
USE `kb_document`;

-- Keep existing installations compatible with the evaluation counters introduced in chapter 26.
DELIMITER $$
DROP PROCEDURE IF EXISTS upgrade_kb_document_evaluation_counters$$
CREATE PROCEDURE upgrade_kb_document_evaluation_counters()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'like_count'
  ) THEN
    ALTER TABLE `kb_document` ADD COLUMN `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT 'like count';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'favorite_count'
  ) THEN
    ALTER TABLE `kb_document` ADD COLUMN `favorite_count` BIGINT NOT NULL DEFAULT 0 COMMENT 'favorite count';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'comment_count'
  ) THEN
    ALTER TABLE `kb_document` ADD COLUMN `comment_count` BIGINT NOT NULL DEFAULT 0 COMMENT 'comment count';
  END IF;
END$$
CALL upgrade_kb_document_evaluation_counters()$$
DROP PROCEDURE IF EXISTS upgrade_kb_document_evaluation_counters$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS `kb_user_favorite` (
  `id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `document_id` BIGINT NOT NULL,
  `document_title` VARCHAR(200) DEFAULT NULL,
  `document_category_id` BIGINT DEFAULT NULL,
  `favorite_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_document` (`user_id`, `document_id`, `deleted`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_favorite_time` (`favorite_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user favorites';

CREATE TABLE IF NOT EXISTS `kb_document_access` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `document_id` BIGINT NOT NULL,
  `document_title` VARCHAR(200) NOT NULL,
  `access_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_document` (`user_id`, `document_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_access_time` (`access_time`),
  KEY `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='document access history';
