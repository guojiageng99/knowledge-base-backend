USE kb_document;

INSERT INTO kb_category (id, parent_id, category_name, category_code, description, icon, sort, status, document_count)
VALUES
    (1000000000000000001, 0, '技术文档', 'CAT_TECH_DOC', '技术类文档', '📚', 1, 1, 0),
    (1000000000000000002, 0, '产品文档', 'CAT_PROD_DOC', '产品类文档', '📄', 2, 1, 0),
    (1000000000000000003, 0, '运维文档', 'CAT_OPS_DOC', '运维类文档', '⚙️', 3, 1, 0),
    (1000000000000000004, 1000000000000000001, '后端开发', 'CAT_BACKEND', '后端开发技术', '🔧', 1, 1, 0),
    (1000000000000000005, 1000000000000000001, '前端开发', 'CAT_FRONTEND', '前端开发技术', '🎨', 2, 1, 0),
    (1000000000000000006, 1000000000000000002, 'PRD文档', 'CAT_PRD', '产品需求文档', '📋', 1, 1, 0),
    (1000000000000000007, 1000000000000000002, '用户手册', 'CAT_USER_MANUAL', '用户使用手册', '📖', 2, 1, 0),
    (1000000000000000008, 1000000000000000004, 'Java', 'CAT_JAVA', 'Java编程语言', '☕', 1, 1, 0),
    (1000000000000000009, 1000000000000000004, 'Spring Boot', 'CAT_SPRING_BOOT', 'Spring Boot框架', '🚀', 2, 1, 0),
    (1000000000000000010, 1000000000000000005, 'Vue.js', 'CAT_VUE', 'Vue.js框架', '💚', 1, 1, 0),
    (1000000000000000011, 1000000000000000005, 'React', 'CAT_REACT', 'React框架', '⚛️', 2, 1, 0)
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id), category_name = VALUES(category_name), category_code = VALUES(category_code),
    description = VALUES(description), icon = VALUES(icon), sort = VALUES(sort), status = VALUES(status),
    document_count = VALUES(document_count), deleted = 0;
