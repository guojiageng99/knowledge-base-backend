CREATE DATABASE IF NOT EXISTS kb_search
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kb_search.kb_search_history (
    id BIGINT NOT NULL COMMENT 'History ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    keyword VARCHAR(200) NOT NULL COMMENT 'Search keyword',
    search_count INT NOT NULL DEFAULT 1 COMMENT 'Search count',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last searched at',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_keyword (keyword),
    KEY idx_create_time (create_time),
    UNIQUE KEY uk_user_keyword (user_id, keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Search history';
