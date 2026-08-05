USE kb_user;

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
