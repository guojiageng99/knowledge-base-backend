CREATE DATABASE IF NOT EXISTS `kb_ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `kb_ai`;
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL DEFAULT 'New conversation', `user_id` BIGINT NOT NULL,
  `model` VARCHAR(50) NOT NULL DEFAULT 'qwen', `system_prompt` TEXT,
  `tokens_used` INT NOT NULL DEFAULT 0, `message_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 0, `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (`id`), KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`), KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI conversation';
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT, `conversation_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL, `content` TEXT NOT NULL, `tokens` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), KEY `idx_conversation_id` (`conversation_id`), KEY `idx_role` (`role`), KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI message';
CREATE TABLE IF NOT EXISTS `ai_feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT, `conversation_id` BIGINT NOT NULL, `message_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL, `feedback_type` VARCHAR(50) NOT NULL, `feedback_content` VARCHAR(500),
  `rating` INT, `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), KEY `idx_conversation_id` (`conversation_id`), KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI feedback';
