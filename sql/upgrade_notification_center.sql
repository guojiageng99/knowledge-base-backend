USE kb_foundation;

SET @update_time_sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_notification' AND column_name = 'update_time') = 0,
    'ALTER TABLE kb_notification ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE statement FROM @update_time_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @create_by_sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_notification' AND column_name = 'create_by') = 0,
    'ALTER TABLE kb_notification ADD COLUMN create_by BIGINT DEFAULT NULL', 'SELECT 1');
PREPARE statement FROM @create_by_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @update_by_sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_notification' AND column_name = 'update_by') = 0,
    'ALTER TABLE kb_notification ADD COLUMN update_by BIGINT DEFAULT NULL', 'SELECT 1');
PREPARE statement FROM @update_by_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @deleted_sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_notification' AND column_name = 'deleted') = 0,
    'ALTER TABLE kb_notification ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE statement FROM @deleted_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @notification_type_index_sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'kb_notification' AND index_name = 'idx_notification_type') = 0,
    'CREATE INDEX idx_notification_type ON kb_notification (notification_type)', 'SELECT 1');
PREPARE statement FROM @notification_type_index_sql; EXECUTE statement; DEALLOCATE PREPARE statement;
