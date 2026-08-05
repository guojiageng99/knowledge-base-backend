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

SET @permission_type_sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND column_name = 'permission_type') = 0,
  'ALTER TABLE sys_permission ADD COLUMN permission_type TINYINT NOT NULL DEFAULT 1 COMMENT ''1 menu, 2 button, 3 api'' AFTER permission_code',
  'SELECT 1');
PREPARE statement FROM @permission_type_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @path_sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND column_name = 'path') = 0,
  'ALTER TABLE sys_permission ADD COLUMN path VARCHAR(200) DEFAULT NULL COMMENT ''menu URL'' AFTER parent_id',
  'SELECT 1');
PREPARE statement FROM @path_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @api_url_sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND column_name = 'api_url') = 0,
  'ALTER TABLE sys_permission ADD COLUMN api_url VARCHAR(500) DEFAULT NULL COMMENT ''API URL'' AFTER path',
  'SELECT 1');
PREPARE statement FROM @api_url_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @method_sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND column_name = 'method') = 0,
  'ALTER TABLE sys_permission ADD COLUMN method VARCHAR(10) DEFAULT NULL COMMENT ''HTTP method'' AFTER api_url',
  'SELECT 1');
PREPARE statement FROM @method_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @icon_sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND column_name = 'icon') = 0,
  'ALTER TABLE sys_permission ADD COLUMN icon VARCHAR(50) DEFAULT NULL COMMENT ''icon name'' AFTER method',
  'SELECT 1');
PREPARE statement FROM @icon_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @remark_sql = IF(
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND column_name = 'remark') = 0,
  'ALTER TABLE sys_permission ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT ''description'' AFTER sort_order',
  'SELECT 1');
PREPARE statement FROM @remark_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @parent_index_sql = IF(
  (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND index_name = 'idx_sys_permission_parent_id') = 0,
  'CREATE INDEX idx_sys_permission_parent_id ON sys_permission (parent_id)',
  'SELECT 1');
PREPARE statement FROM @parent_index_sql; EXECUTE statement; DEALLOCATE PREPARE statement;

SET @type_index_sql = IF(
  (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'sys_permission' AND index_name = 'idx_sys_permission_type') = 0,
  'CREATE INDEX idx_sys_permission_type ON sys_permission (permission_type)',
  'SELECT 1');
PREPARE statement FROM @type_index_sql; EXECUTE statement; DEALLOCATE PREPARE statement;
