USE kb_user;

CREATE TABLE IF NOT EXISTS kb_user (
    id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    real_name VARCHAR(50) DEFAULT NULL,
    department VARCHAR(100) DEFAULT NULL,
    position VARCHAR(100) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time DATETIME DEFAULT NULL,
    last_login_ip VARCHAR(50) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_email (email),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO kb_user (id, username, password, email, phone, real_name, department, position, status)
VALUES
    (1000000000000000001, 'admin', '$2a$10$VgufypNVWjDteO/9Cyt9yet3XRr7wg9XCx2kV/XATbvh0W0PbALKG', 'admin@example.com', '13800138000', '系统管理员', '技术部', '系统架构师', 1),
    (1000000000000000002, 'user', '$2a$10$VgufypNVWjDteO/9Cyt9yet3XRr7wg9XCx2kV/XATbvh0W0PbALKG', 'user@example.com', '13800138001', '普通用户', '产品部', '产品经理', 1),
    (1000000000000000003, 'test', '$2a$10$VgufypNVWjDteO/9Cyt9yet3XRr7wg9XCx2kV/XATbvh0W0PbALKG', 'test@example.com', '13800138002', '测试用户', '测试部', '测试工程师', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), status = VALUES(status);
