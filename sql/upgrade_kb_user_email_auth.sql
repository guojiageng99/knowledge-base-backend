-- Chapter 38: make existing kb_user schemas compatible with email activation.
-- This script is idempotent for MySQL 8.0 installations.
USE kb_user;

DELIMITER $$

DROP PROCEDURE IF EXISTS upgrade_kb_user_email_auth$$
CREATE PROCEDURE upgrade_kb_user_email_auth()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'kb_user' AND column_name = 'email_verified'
    ) THEN
        ALTER TABLE kb_user ADD COLUMN email_verified TINYINT NOT NULL DEFAULT 0 COMMENT 'whether email has been verified' AFTER status;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'kb_user' AND column_name = 'activation_token'
    ) THEN
        ALTER TABLE kb_user ADD COLUMN activation_token VARCHAR(64) DEFAULT NULL AFTER email_verified;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'kb_user' AND column_name = 'activation_token_expiry'
    ) THEN
        ALTER TABLE kb_user ADD COLUMN activation_token_expiry DATETIME DEFAULT NULL AFTER activation_token;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'kb_user' AND index_name = 'uk_kb_user_activation_token'
    ) THEN
        ALTER TABLE kb_user ADD UNIQUE KEY uk_kb_user_activation_token (activation_token);
    END IF;
END$$

CALL upgrade_kb_user_email_auth()$$
DROP PROCEDURE IF EXISTS upgrade_kb_user_email_auth$$
DELIMITER ;
