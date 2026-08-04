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
