USE kb_document;

SET @content_id_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'content_id') = 0,
    'ALTER TABLE kb_document ADD COLUMN content_id VARCHAR(64) DEFAULT NULL COMMENT ''MongoDB content ID'' AFTER content',
    'SELECT 1');
PREPARE statement FROM @content_id_sql; EXECUTE statement; DEALLOCATE PREPARE statement;
