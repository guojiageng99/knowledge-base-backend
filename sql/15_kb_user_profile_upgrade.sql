-- Chapter 28: personal profile center upgrade.
USE kb_user;

SET @remark_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'kb_user' AND COLUMN_NAME = 'remark'
);
SET @remark_sql = IF(@remark_exists = 0,
    'ALTER TABLE kb_user ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT ''personal bio/remark''',
    'SELECT 1');
PREPARE remark_stmt FROM @remark_sql;
EXECUTE remark_stmt;
DEALLOCATE PREPARE remark_stmt;

CREATE OR REPLACE VIEW kb_document AS
SELECT id, title, author_id, author_name, category_id, status,
       view_count, like_count, favorite_count, comment_count,
       is_public, is_top, is_recommend, document_type, source,
       cover_image, summary, sort, allow_comment, publish_time,
       create_time, update_time, create_by, update_by, deleted
FROM kb_document.kb_document;
