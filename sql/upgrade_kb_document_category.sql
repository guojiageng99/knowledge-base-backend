USE kb_document;

-- Support older MySQL 8.0 releases that do not recognize ADD COLUMN IF NOT EXISTS.
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND column_name = 'category_code') = 0,
    'ALTER TABLE kb_category ADD COLUMN category_code VARCHAR(50) NULL AFTER category_name',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

-- Use UTF-8 bytes so Windows shell encodings cannot corrupt the emoji default.
SET @sql = CONCAT('ALTER TABLE kb_category MODIFY COLUMN icon VARCHAR(50) DEFAULT ''', _utf8mb4 0xF09F9381, '''');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND column_name = 'icon') = 0,
    'ALTER TABLE kb_category ADD COLUMN icon VARCHAR(50) DEFAULT ''📁'' AFTER description',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND column_name = 'category_icon') = 1,
    'ALTER TABLE kb_category DROP COLUMN category_icon',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND column_name = 'remark') = 0,
    'ALTER TABLE kb_category ADD COLUMN remark VARCHAR(500) DEFAULT NULL AFTER document_count',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

UPDATE kb_category
SET category_code = CONCAT('CAT_', id)
WHERE category_code IS NULL OR category_code = '';

ALTER TABLE kb_category MODIFY COLUMN category_code VARCHAR(50) NOT NULL;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND index_name = 'idx_category_code') = 0,
    'ALTER TABLE kb_category ADD KEY idx_category_code (category_code)',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'kb_category' AND index_name = 'idx_status') = 0,
    'ALTER TABLE kb_category ADD KEY idx_status (status)',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
