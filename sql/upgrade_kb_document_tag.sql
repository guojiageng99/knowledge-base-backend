USE kb_document;

-- Upgrade the chapter-05 placeholder tables to the chapter-07 tag schema.
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND column_name = 'category_id') = 0,
    'ALTER TABLE tb_tag ADD COLUMN category_id BIGINT DEFAULT NULL AFTER tag_code', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND column_name = 'version') = 0,
    'ALTER TABLE tb_tag ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER status', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND column_name = 'create_by') = 0,
    'ALTER TABLE tb_tag ADD COLUMN create_by BIGINT DEFAULT NULL AFTER update_time', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND column_name = 'update_by') = 0,
    'ALTER TABLE tb_tag ADD COLUMN update_by BIGINT DEFAULT NULL AFTER create_by', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

UPDATE tb_tag SET tag_type = 1 WHERE tag_type IS NULL OR tag_type NOT REGEXP '^[01]$';
ALTER TABLE tb_tag MODIFY COLUMN tag_type TINYINT NOT NULL DEFAULT 1;
UPDATE tb_tag SET tag_code = CONCAT('TAG_', id) WHERE tag_code IS NULL OR tag_code = '';
ALTER TABLE tb_tag MODIFY COLUMN tag_code VARCHAR(50) NOT NULL;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND index_name = 'idx_tag_name') = 0,
    'ALTER TABLE tb_tag ADD KEY idx_tag_name (tag_name)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND index_name = 'idx_category_id') = 0,
    'ALTER TABLE tb_tag ADD KEY idx_category_id (category_id)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND index_name = 'idx_tag_type') = 0,
    'ALTER TABLE tb_tag ADD KEY idx_tag_type (tag_type)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND index_name = 'idx_status') = 0,
    'ALTER TABLE tb_tag ADD KEY idx_status (status)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'tb_tag' AND index_name = 'idx_doc_count') = 0,
    'ALTER TABLE tb_tag ADD KEY idx_doc_count (doc_count)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'kb_document_tag' AND index_name = 'idx_document_id') = 0,
    'ALTER TABLE kb_document_tag ADD KEY idx_document_id (document_id)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'kb_document_tag' AND index_name = 'idx_tag_id') = 0,
    'ALTER TABLE kb_document_tag ADD KEY idx_tag_id (tag_id)', 'SELECT 1');
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
