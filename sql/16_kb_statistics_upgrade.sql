-- Chapter 29: daily statistics aggregation tables.
USE kb_document;

CREATE TABLE IF NOT EXISTS kb_document_statistics (
    id BIGINT NOT NULL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    document_title VARCHAR(200) DEFAULT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    stat_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_document_stat_date (document_id, stat_date),
    KEY idx_document_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS kb_user_statistics (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) DEFAULT NULL,
    document_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    stat_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_stat_date (user_id, stat_date),
    KEY idx_user_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
