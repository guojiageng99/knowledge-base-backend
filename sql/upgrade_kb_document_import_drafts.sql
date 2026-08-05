USE kb_document;

SET @content_length_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'content_length') = 0,
    'ALTER TABLE kb_document ADD COLUMN content_length INT DEFAULT NULL COMMENT ''Document content length'' AFTER content_id',
    'SELECT 1');
PREPARE statement FROM @content_length_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @is_public_sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'kb_document' AND column_name = 'is_public') = 0,
    'ALTER TABLE kb_document ADD COLUMN is_public TINYINT NOT NULL DEFAULT 1 COMMENT ''0 private, 1 team, 2 public'' AFTER status',
    'SELECT 1');
PREPARE statement FROM @is_public_sql; EXECUTE statement; DEALLOCATE PREPARE statement;
