CREATE DATABASE IF NOT EXISTS kb_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kb_user;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL PRIMARY KEY,
  username VARCHAR(64) NOT NULL COMMENT 'username',
  password VARCHAR(255) NOT NULL COMMENT 'password',
  nickname VARCHAR(64) DEFAULT NULL COMMENT 'nickname',
  email VARCHAR(128) DEFAULT NULL COMMENT 'email',
  phone VARCHAR(32) DEFAULT NULL COMMENT 'phone',
  avatar VARCHAR(255) DEFAULT NULL COMMENT 'avatar',
  status TINYINT NOT NULL DEFAULT 1 COMMENT 'status: 1 enabled, 0 disabled',
  user_type TINYINT NOT NULL DEFAULT 1 COMMENT 'user type',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 normal, 1 deleted',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user table';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL PRIMARY KEY,
  role_name VARCHAR(64) NOT NULL COMMENT 'role name',
  role_code VARCHAR(64) NOT NULL COMMENT 'role code',
  description VARCHAR(255) DEFAULT NULL COMMENT 'description',
  status TINYINT NOT NULL DEFAULT 1 COMMENT 'status: 1 enabled, 0 disabled',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 normal, 1 deleted',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role table';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL PRIMARY KEY,
  permission_name VARCHAR(64) NOT NULL COMMENT 'permission name',
  permission_code VARCHAR(128) NOT NULL COMMENT 'permission code',
  resource_type VARCHAR(32) DEFAULT NULL COMMENT 'resource type',
  resource_path VARCHAR(255) DEFAULT NULL COMMENT 'resource path',
  parent_id BIGINT DEFAULT 0 COMMENT 'parent permission id',
  sort_order INT DEFAULT 0 COMMENT 'sort order',
  status TINYINT NOT NULL DEFAULT 1 COMMENT 'status: 1 enabled, 0 disabled',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 normal, 1 deleted',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  UNIQUE KEY uk_sys_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='permission table';
