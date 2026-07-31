USE kb_document;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'user_id') > 0,
    'ALTER TABLE tb_comment CHANGE COLUMN user_id commenter_id BIGINT NOT NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'user_name') > 0,
    'ALTER TABLE tb_comment CHANGE COLUMN user_name commenter_name VARCHAR(50) DEFAULT NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'user_avatar') > 0,
    'ALTER TABLE tb_comment CHANGE COLUMN user_avatar commenter_avatar VARCHAR(500) DEFAULT NULL', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'created_at') = 0,
    'ALTER TABLE tb_comment ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER status', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'updated_at') = 0,
    'ALTER TABLE tb_comment ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'create_by') = 0,
    'ALTER TABLE tb_comment ADD COLUMN create_by BIGINT DEFAULT NULL AFTER update_time', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND column_name = 'update_by') = 0,
    'ALTER TABLE tb_comment ADD COLUMN update_by BIGINT DEFAULT NULL AFTER create_by', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND index_name = 'idx_root_id') = 0,
    'ALTER TABLE tb_comment ADD KEY idx_root_id (root_id)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND index_name = 'idx_user_id') > 0,
    'ALTER TABLE tb_comment DROP INDEX idx_user_id', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_comment' AND index_name = 'idx_commenter_id') = 0,
    'ALTER TABLE tb_comment ADD KEY idx_commenter_id (commenter_id)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

CREATE TABLE IF NOT EXISTS tb_like (
    id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    target_type TINYINT NOT NULL COMMENT '1 document, 2 comment',
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_target_user_type (target_id, user_id, target_type),
    KEY idx_target_id (target_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Likes';
