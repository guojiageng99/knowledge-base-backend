USE kb_document;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'tb_document_version' AND index_name = 'idx_operator_id') = 0,
    'ALTER TABLE tb_document_version ADD KEY idx_operator_id (operator_id)',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE() AND table_name = 'tb_document_version' AND index_name = 'idx_created_at') = 0,
    'ALTER TABLE tb_document_version ADD KEY idx_created_at (created_at)',
    'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
