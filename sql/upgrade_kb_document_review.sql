USE kb_document;

CREATE TABLE IF NOT EXISTS tb_document_review (
    id BIGINT NOT NULL COMMENT '审核记录ID（雪花ID）',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    reviewer_id BIGINT NOT NULL COMMENT '审核人ID，0表示待分配',
    reviewer_name VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    review_result TINYINT DEFAULT NULL COMMENT '审核结果：1-通过，2-驳回',
    review_comment TEXT DEFAULT NULL COMMENT '审核意见',
    before_status TINYINT DEFAULT NULL COMMENT '审核前状态',
    reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
    review_round INT NOT NULL DEFAULT 1 COMMENT '审核轮次',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_document_id (document_id),
    KEY idx_reviewer_id (reviewer_id),
    KEY idx_reviewed_at (reviewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档审核记录表';

ALTER TABLE tb_document_review MODIFY COLUMN reviewer_id BIGINT NOT NULL;
ALTER TABLE tb_document_review MODIFY COLUMN review_result TINYINT DEFAULT NULL;
ALTER TABLE tb_document_review MODIFY COLUMN reviewed_at DATETIME DEFAULT NULL;

SET @reviewer_index_exists := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'tb_document_review' AND index_name = 'idx_reviewer_id'
);
SET @reviewer_index_sql := IF(@reviewer_index_exists = 0,
    'ALTER TABLE tb_document_review ADD KEY idx_reviewer_id (reviewer_id)', 'SELECT 1');
PREPARE reviewer_index_stmt FROM @reviewer_index_sql;
EXECUTE reviewer_index_stmt;
DEALLOCATE PREPARE reviewer_index_stmt;

SET @reviewed_at_index_exists := (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'tb_document_review' AND index_name = 'idx_reviewed_at'
);
SET @reviewed_at_index_sql := IF(@reviewed_at_index_exists = 0,
    'ALTER TABLE tb_document_review ADD KEY idx_reviewed_at (reviewed_at)', 'SELECT 1');
PREPARE reviewed_at_index_stmt FROM @reviewed_at_index_sql;
EXECUTE reviewed_at_index_stmt;
DEALLOCATE PREPARE reviewed_at_index_stmt;
