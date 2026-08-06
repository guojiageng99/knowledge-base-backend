USE kb_user;

INSERT INTO sys_role (id, role_name, role_code, description, status, deleted)
VALUES
    (1000000000000000101, 'Super Administrator', 'ROLE_SUPER_ADMIN', 'Full system administration access', 1, 0),
    (1000000000000000102, 'Reviewer', 'ROLE_REVIEWER', 'Can review submitted documents', 1, 0),
    (1000000000000000103, 'User', 'ROLE_USER', 'Standard knowledge-base user', 1, 0)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    description = VALUES(description),
    status = VALUES(status),
    deleted = VALUES(deleted);

INSERT IGNORE INTO sys_user_role (id, user_id, role_id)
VALUES
    (1000000000000000201, 1000000000000000001, 1000000000000000101),
    (1000000000000000202, 1000000000000000001, 1000000000000000102),
    (1000000000000000203, 1000000000000000002, 1000000000000000103),
    (1000000000000000204, 1000000000000000003, 1000000000000000103);
