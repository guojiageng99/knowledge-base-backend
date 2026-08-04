USE kb_user;

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT 'kb_user ID',
  role_id BIGINT NOT NULL COMMENT 'sys_role ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user role relation';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL PRIMARY KEY,
  role_id BIGINT NOT NULL COMMENT 'sys_role ID',
  permission_id BIGINT NOT NULL COMMENT 'sys_permission ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
  KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role permission relation';

ALTER TABLE sys_permission
  ADD COLUMN permission_type TINYINT NOT NULL DEFAULT 1 COMMENT '1 menu, 2 button, 3 api' AFTER permission_code,
  ADD COLUMN path VARCHAR(200) DEFAULT NULL COMMENT 'menu URL' AFTER parent_id,
  ADD COLUMN api_url VARCHAR(500) DEFAULT NULL COMMENT 'API URL' AFTER path,
  ADD COLUMN method VARCHAR(10) DEFAULT NULL COMMENT 'HTTP method' AFTER api_url,
  ADD COLUMN icon VARCHAR(50) DEFAULT NULL COMMENT 'icon name' AFTER method,
  ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT 'description' AFTER sort_order;

CREATE INDEX idx_sys_permission_parent_id ON sys_permission (parent_id);
CREATE INDEX idx_sys_permission_type ON sys_permission (permission_type);
