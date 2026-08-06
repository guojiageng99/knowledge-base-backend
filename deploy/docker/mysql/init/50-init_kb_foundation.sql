CREATE DATABASE IF NOT EXISTS kb_foundation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kb_foundation;

CREATE TABLE IF NOT EXISTS kb_dict (
  id BIGINT NOT NULL PRIMARY KEY,
  dict_code VARCHAR(100) NOT NULL,
  dict_name VARCHAR(100) NOT NULL,
  dict_type VARCHAR(50) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_dict_code (dict_code),
  KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dictionary type';

CREATE TABLE IF NOT EXISTS kb_dict_data (
  id BIGINT NOT NULL PRIMARY KEY,
  dict_id BIGINT NOT NULL,
  dict_code VARCHAR(100) NOT NULL,
  dict_label VARCHAR(100) NOT NULL,
  dict_value VARCHAR(100) NOT NULL,
  dict_sort INT NOT NULL DEFAULT 0,
  css_class VARCHAR(100) DEFAULT NULL,
  list_class VARCHAR(100) DEFAULT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_dict_id (dict_id),
  KEY idx_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dictionary data';

CREATE TABLE IF NOT EXISTS kb_system_config (
  id BIGINT NOT NULL PRIMARY KEY,
  config_key VARCHAR(100) NOT NULL,
  config_value TEXT DEFAULT NULL,
  config_type VARCHAR(20) NOT NULL,
  category VARCHAR(50) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  is_public TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_config_key (config_key),
  KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System configuration';

INSERT IGNORE INTO kb_system_config (id, config_key, config_value, config_type, category, description, is_public) VALUES
(1000000000000004001, 'system.name', '智能知识库', 'string', 'SYSTEM', '系统名称', 1),
(1000000000000004002, 'system.description', '企业级智能知识管理平台', 'string', 'SYSTEM', '系统描述', 1),
(1000000000000004003, 'system.version', 'v2.4.1', 'string', 'SYSTEM', '系统版本', 0),
(1000000000000004004, 'system.language', 'zh-CN', 'string', 'SYSTEM', '默认语言', 1),
(1000000000000004005, 'system.timezone', 'Asia/Shanghai', 'string', 'SYSTEM', '时区', 1),
(1000000000000004006, 'user.registration.enabled', 'false', 'boolean', 'SYSTEM', '允许注册', 1),
(1000000000000004007, 'system.requireApproval', 'true', 'boolean', 'SYSTEM', '文档需要审核', 1),
(1000000000000004008, 'system.enableComments', 'true', 'boolean', 'SYSTEM', '启用评论', 1),
(1000000000000004009, 'system.enableAI', 'true', 'boolean', 'SYSTEM', '启用 AI', 1),
(1000000000000004010, 'system.enableAIWriting', 'true', 'boolean', 'SYSTEM', '启用 AI 写作', 1),
(1000000000000004011, 'system.enableFullTextSearch', 'true', 'boolean', 'SYSTEM', '启用全文搜索', 1),
(1000000000000004012, 'system.passwordPolicy', 'medium', 'string', 'SECURITY', '密码策略', 1),
(1000000000000004013, 'auth.session.timeout', '3600', 'number', 'SECURITY', '会话超时', 1),
(1000000000000004014, 'system.enable2FA', 'false', 'boolean', 'SECURITY', '双因素认证', 1),
(1000000000000004015, 'system.ipRestriction', 'false', 'boolean', 'SECURITY', 'IP 限制', 1),
(1000000000000004016, 'auth.password.min.length', '8', 'number', 'SECURITY', '密码最小长度', 1),
(1000000000000004017, 'auth.password.require.special', 'true', 'boolean', 'SECURITY', '要求特殊字符', 1),
(1000000000000004018, 'auth.login.max.retry', '5', 'number', 'SECURITY', '最大登录重试次数', 1),
(1000000000000004020, 'file.upload.max.size', '104857600', 'number', 'STORAGE', '最大文件大小', 1),
(1000000000000004021, 'file.upload.allowed.types', 'pdf,doc,docx,xlsx,pptx,txt,md,jpg,png,gif', 'string', 'STORAGE', '允许文件类型', 1),
(1000000000000004022, 'rustfs.endpoints', 'http://localhost:8200', 'string', 'STORAGE', 'RustFS 地址', 0),
(1000000000000004023, 'rustfs.bucket', 'knowledge-docs', 'string', 'STORAGE', 'RustFS Bucket', 0),
(1000000000000004030, 'email.enabled', 'true', 'boolean', 'NOTIFICATION', '邮件通知开关', 1),
(1000000000000004031, 'email.host', 'smtp.example.com', 'string', 'NOTIFICATION', '邮件服务器', 0),
(1000000000000004032, 'email.port', '587', 'number', 'NOTIFICATION', '邮件端口', 0),
(1000000000000004033, 'websocket.enabled', 'true', 'boolean', 'NOTIFICATION', 'WebSocket 通知开关', 1),
(1000000000000004034, 'notification.retention.days', '90', 'number', 'NOTIFICATION', '通知保留天数', 1),
(1000000000000004040, 'qwen.model.name', 'qwen-max', 'string', 'AI', 'AI 模型名称', 1),
(1000000000000004041, 'qwen.embedding.model', 'text-embedding-v3', 'string', 'AI', 'Embedding 模型', 1),
(1000000000000004042, 'milvus.host', 'localhost', 'string', 'AI', 'Milvus 地址', 0),
(1000000000000004043, 'milvus.port', '19530', 'number', 'AI', 'Milvus 端口', 0);

  CREATE TABLE IF NOT EXISTS kb_notification (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  user_name VARCHAR(50) DEFAULT NULL,
  notification_type VARCHAR(20) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT DEFAULT NULL,
  link VARCHAR(500) DEFAULT NULL,
  related_type VARCHAR(50) DEFAULT NULL,
  related_id BIGINT DEFAULT NULL,
  is_read TINYINT NOT NULL DEFAULT 0,
    read_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_notification_user_id (user_id),
    KEY idx_notification_is_read (is_read),
    KEY idx_notification_type (notification_type),
    KEY idx_notification_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System notification';

CREATE TABLE IF NOT EXISTS kb_operation_log (
  id BIGINT NOT NULL PRIMARY KEY,
  module VARCHAR(50) DEFAULT NULL,
  operation_type VARCHAR(20) DEFAULT NULL,
  operation_desc VARCHAR(500) DEFAULT NULL,
  request_method VARCHAR(10) DEFAULT NULL,
  request_url VARCHAR(500) DEFAULT NULL,
  request_params TEXT DEFAULT NULL,
  response_result TEXT DEFAULT NULL,
  user_id BIGINT DEFAULT NULL,
  username VARCHAR(50) DEFAULT NULL,
  ip_address VARCHAR(50) DEFAULT NULL,
  location VARCHAR(200) DEFAULT NULL,
  user_agent VARCHAR(500) DEFAULT NULL,
  execute_time INT DEFAULT NULL,
  status TINYINT DEFAULT NULL,
  error_msg TEXT DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_operation_log_user_id (user_id),
  KEY idx_operation_log_create_time (create_time),
  KEY idx_operation_log_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation log';
