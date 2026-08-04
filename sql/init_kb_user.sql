CREATE DATABASE IF NOT EXISTS kb_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kb_user;

CREATE TABLE IF NOT EXISTS kb_user (
  id BIGINT NOT NULL PRIMARY KEY,
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
  UNIQUE KEY uk_kb_user_username (username),
  KEY idx_kb_user_email (email),
  KEY idx_kb_user_status (status),
  KEY idx_kb_user_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='authentication user table';

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

CREATE TABLE IF NOT EXISTS kb_team (
  id BIGINT NOT NULL COMMENT 'team ID',
  team_name VARCHAR(100) NOT NULL COMMENT 'team name',
  team_code VARCHAR(50) DEFAULT NULL COMMENT 'team code',
  description VARCHAR(500) DEFAULT NULL COMMENT 'team description',
  leader_id BIGINT DEFAULT NULL COMMENT 'team leader user ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT 'parent team ID; 0 is root',
  level INT NOT NULL DEFAULT 1 COMMENT 'tree level',
  path VARCHAR(1000) NOT NULL COMMENT 'materialized team path',
  member_count INT NOT NULL DEFAULT 0 COMMENT 'member count',
  doc_count INT NOT NULL DEFAULT 0 COMMENT 'document count',
  sort INT NOT NULL DEFAULT 0 COMMENT 'display order',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  update_by BIGINT DEFAULT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_team_code (team_code, deleted),
  KEY idx_leader_id (leader_id),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='team table';

CREATE TABLE IF NOT EXISTS kb_team_member (
  id BIGINT NOT NULL COMMENT 'primary key',
  team_id BIGINT NOT NULL COMMENT 'team ID',
  user_id BIGINT NOT NULL COMMENT 'user ID',
  member_role VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT 'leader or member',
  join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_team_user (team_id, user_id),
  KEY idx_team_member_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='team member table';
