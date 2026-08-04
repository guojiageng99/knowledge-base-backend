package com.knowledge.base.statistics.task;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StatisticsAggregationTask {
    private final JdbcTemplate jdbcTemplate;

    public void aggregateDailyDocumentStatistics() {
        LocalDate today = LocalDate.now();
        jdbcTemplate.update("DELETE FROM kb_document_statistics WHERE stat_date = ?", today);
        jdbcTemplate.update("INSERT INTO kb_document_statistics (id, document_id, document_title, view_count, like_count, comment_count, favorite_count, stat_date) SELECT UUID_SHORT(), id, title, view_count, like_count, comment_count, favorite_count, ? FROM kb_document WHERE deleted = 0", today);
    }

    public void aggregateDailyUserStatistics() {
        LocalDate today = LocalDate.now();
        jdbcTemplate.update("DELETE FROM kb_user_statistics WHERE stat_date = ?", today);
        jdbcTemplate.update("INSERT INTO kb_user_statistics (id, user_id, username, document_count, comment_count, view_count, stat_date) SELECT UUID_SHORT(), u.id, u.username, COUNT(DISTINCT d.id), COUNT(DISTINCT c.id), COALESCE(SUM(d.view_count),0), ? FROM kb_user.kb_user u LEFT JOIN kb_document d ON d.author_id = u.id AND d.deleted = 0 LEFT JOIN tb_comment c ON c.commenter_id = u.id AND c.deleted = 0 WHERE u.deleted = 0 GROUP BY u.id, u.username", today);
    }
}
