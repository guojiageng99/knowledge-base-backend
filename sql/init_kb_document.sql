CREATE DATABASE IF NOT EXISTS kb_document DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kb_document;

CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT NOT NULL COMMENT 'Document ID',
    title VARCHAR(200) NOT NULL COMMENT 'Document title',
    content LONGTEXT NOT NULL COMMENT 'Document content',
    summary TEXT DEFAULT NULL COMMENT 'Document summary',
    document_type INT NOT NULL DEFAULT 1 COMMENT '1 article, 2 file',
    file_path VARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_extension VARCHAR(50) DEFAULT NULL,
    mime_type VARCHAR(100) DEFAULT NULL,
    category_id BIGINT DEFAULT NULL,
    tags VARCHAR(200) DEFAULT NULL,
    author_id BIGINT NOT NULL,
    author_name VARCHAR(50) DEFAULT NULL,
    cover_image VARCHAR(500) DEFAULT NULL,
    status INT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 published, 2 archived',
    is_public TINYINT NOT NULL DEFAULT 1,
    is_top TINYINT NOT NULL DEFAULT 0,
    is_recommend TINYINT NOT NULL DEFAULT 0,
    allow_comment TINYINT NOT NULL DEFAULT 1,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    favorite_count BIGINT NOT NULL DEFAULT 0,
    comment_count BIGINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    word_count INT DEFAULT NULL,
    publish_time DATETIME DEFAULT NULL,
    source INT NOT NULL DEFAULT 1,
    source_url VARCHAR(500) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_title (title(100)),
    KEY idx_category_id (category_id),
    KEY idx_author_id (author_id),
    KEY idx_status (status),
    KEY idx_publish_time (publish_time),
    FULLTEXT KEY ft_content (title, content, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Documents';

CREATE TABLE IF NOT EXISTS kb_category (
    id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    category_name VARCHAR(50) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT '📁',
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    document_count INT NOT NULL DEFAULT 0,
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_category_code (category_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document categories';

CREATE TABLE IF NOT EXISTS tb_tag (
    id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    tag_code VARCHAR(50) DEFAULT NULL,
    tag_type VARCHAR(20) DEFAULT 'USER',
    color VARCHAR(20) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    doc_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_code (tag_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tags';

CREATE TABLE IF NOT EXISTS tb_comment (
    id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) DEFAULT NULL,
    user_avatar VARCHAR(500) DEFAULT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    root_id BIGINT NOT NULL DEFAULT 0,
    reply_to_user_id BIGINT DEFAULT NULL,
    reply_to_user_name VARCHAR(50) DEFAULT NULL,
    like_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_document_id (document_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Comments';

CREATE TABLE IF NOT EXISTS tb_document_version (
    id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    version INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    summary TEXT DEFAULT NULL,
    change_description VARCHAR(500) DEFAULT NULL,
    change_size BIGINT DEFAULT NULL,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_doc_version (document_id, version),
    KEY idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document versions';

CREATE TABLE IF NOT EXISTS tb_document_review (
    id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewer_name VARCHAR(50) DEFAULT NULL,
    review_result INT NOT NULL,
    review_comment TEXT DEFAULT NULL,
    before_status INT DEFAULT NULL,
    reviewed_at DATETIME DEFAULT NULL,
    review_round INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document reviews';

CREATE TABLE IF NOT EXISTS kb_document_tag (
    id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_document_tag (document_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Document tags';
