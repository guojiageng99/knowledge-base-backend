USE kb_document;

INSERT INTO tb_tag (id, tag_name, tag_code, category_id, tag_type, color, icon, doc_count, status) VALUES
    (1000000000000000001, '置顶', 'TAG_TOP', NULL, 0, '#ff4d4f', '📌', 0, 1),
    (1000000000000000002, '推荐', 'TAG_RECOMMEND', NULL, 0, '#ffec3d', '🔖', 0, 1),
    (1000000000000000003, '精选', 'TAG_FEATURED', NULL, 0, '#ffd666', '⭐', 0, 1),
    (1000000000000000004, '原创', 'TAG_ORIGINAL', NULL, 0, '#95de64', '📄', 5, 1),
    (1000000000000000005, '翻译', 'TAG_TRANSLATE', NULL, 0, '#b37feb', '🌐', 2, 1),
    (1000000000000000006, 'Java', 'TAG_JAVA', 1000000000000000004, 1, '#f759ab', '☕', 10, 1),
    (1000000000000000007, 'Spring Boot', 'TAG_SPRING_BOOT', 1000000000000000004, 1, '#6dd400', '🚀', 8, 1),
    (1000000000000000008, 'Vue.js', 'TAG_VUE', 1000000000000000005, 1, '#42b883', '💚', 6, 1),
    (1000000000000000009, 'React', 'TAG_REACT', 1000000000000000005, 1, '#61dafb', '⚛️', 5, 1),
    (1000000000000000010, 'Python', 'TAG_PYTHON', 1000000000000000004, 1, '#ffd43b', '🐍', 7, 1)
ON DUPLICATE KEY UPDATE
    tag_name = VALUES(tag_name), category_id = VALUES(category_id), tag_type = VALUES(tag_type),
    color = VALUES(color), icon = VALUES(icon), doc_count = VALUES(doc_count), status = VALUES(status), deleted = 0;
