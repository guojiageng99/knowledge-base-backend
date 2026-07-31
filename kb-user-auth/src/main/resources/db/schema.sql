CREATE TABLE IF NOT EXISTS kb_user (
    id BIGINT NOT NULL COMMENT '用户ID（雪花算法）',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    department VARCHAR(100) DEFAULT NULL COMMENT '部门',
    position VARCHAR(100) DEFAULT NULL COMMENT '岗位',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_email (email),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

INSERT INTO kb_user (id, username, password, email, phone, avatar, real_name, department, position, status)
VALUES
    (1000000000000000001, 'admin', '$2a$10$VgufypNVWjDteO/9Cyt9yet3XRr7wg9XCx2kV/XATbvh0W0PbALKG', 'admin@example.com', '13800138000', NULL, '系统管理员', '技术部', '系统架构师', 1),
    (1000000000000000002, 'user', '$2a$10$VgufypNVWjDteO/9Cyt9yet3XRr7wg9XCx2kV/XATbvh0W0PbALKG', 'user@example.com', '13800138001', NULL, '普通用户', '产品部', '产品经理', 1),
    (1000000000000000003, 'test', '$2a$10$VgufypNVWjDteO/9Cyt9yet3XRr7wg9XCx2kV/XATbvh0W0PbALKG', 'test@example.com', '13800138002', NULL, '测试用户', '测试部', '测试工程师', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);
