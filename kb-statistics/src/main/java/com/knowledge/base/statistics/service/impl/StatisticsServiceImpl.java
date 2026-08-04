package com.knowledge.base.statistics.service.impl;

import com.knowledge.base.statistics.service.StatisticsService;
import com.knowledge.base.statistics.task.StatisticsAggregationTask;
import com.knowledge.base.statistics.vo.StatisticsVOs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;
    private final StatisticsAggregationTask aggregationTask;

    @Override
    public Overview getOverview(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate == null ? LocalDate.now().minusDays(29) : startDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        Overview value = new Overview(
                scalar("SELECT COUNT(*) FROM kb_document WHERE deleted=0"),
                scalar("SELECT COUNT(*) FROM kb_user.kb_user WHERE deleted=0"),
                scalar("SELECT COUNT(*) FROM kb_document WHERE deleted=0 AND DATE(create_time) BETWEEN ? AND ?", start, end),
                scalar("SELECT COUNT(*) FROM kb_user.kb_user WHERE deleted=0 AND DATE(create_time) BETWEEN ? AND ?", start, end),
                scalar("SELECT COALESCE(SUM(view_count),0) FROM kb_document WHERE deleted=0"),
                scalar("SELECT COALESCE(SUM(view_count),0) FROM kb_document WHERE deleted=0 AND DATE(update_time) BETWEEN ? AND ?", start, end),
                scalar("SELECT COALESCE(SUM(like_count),0) FROM kb_document WHERE deleted=0"),
                scalar("SELECT COALESCE(SUM(favorite_count),0) FROM kb_document WHERE deleted=0"),
                scalar("SELECT COALESCE(SUM(comment_count),0) FROM kb_document WHERE deleted=0"),
                scalar("SELECT COUNT(*) FROM tb_document_review WHERE review_result IS NULL"), 0, 0,
                scalar("SELECT COUNT(*) FROM kb_user.kb_user WHERE deleted=0 AND DATE(last_login_time)=CURDATE()"));
        return value;
    }

    @Override public Dashboard getDashboard() {
        return new Dashboard(getOverview(null, null), getDocumentTrend(LocalDate.now().minusDays(29), LocalDate.now(), "create"), getCategoryDistribution(), getHotDocuments("view", 10), getActiveUsers("create", 10));
    }

    @Override public List<Trend> getDocumentTrend(LocalDate start, LocalDate end, String type) {
        String field = "view".equals(type) ? "view_count" : "1";
        String sql = "view_count".equals(field)
                ? "SELECT DATE(update_time) d, COALESCE(SUM(view_count),0) c FROM kb_document WHERE deleted=0 AND DATE(update_time) BETWEEN ? AND ? GROUP BY DATE(update_time) ORDER BY d"
                : "SELECT DATE(create_time) d, COUNT(*) c FROM kb_document WHERE deleted=0 AND DATE(create_time) BETWEEN ? AND ? GROUP BY DATE(create_time) ORDER BY d";
        return jdbcTemplate.query(sql, (rs, n) -> new Trend(rs.getString("d"), rs.getLong("c")), start, end);
    }

    @Override public List<Activity> getUserActivity(LocalDate start, LocalDate end) {
        return jdbcTemplate.query("SELECT u.id user_id,u.username,COUNT(DISTINCT d.id) document_count,COUNT(DISTINCT c.id) comment_count,COALESCE(SUM(d.view_count),0) view_count FROM kb_user.kb_user u LEFT JOIN kb_document d ON d.author_id=u.id AND d.deleted=0 LEFT JOIN tb_comment c ON c.commenter_id=u.id AND c.deleted=0 WHERE u.deleted=0 GROUP BY u.id,u.username ORDER BY (COUNT(DISTINCT d.id)+COUNT(DISTINCT c.id)+COALESCE(SUM(d.view_count),0)) DESC LIMIT 20", (rs,n) -> new Activity(rs.getLong("user_id"), rs.getString("username"), rs.getLong("document_count"), rs.getLong("comment_count"), rs.getLong("view_count"), rs.getLong("document_count") + rs.getLong("comment_count") + rs.getLong("view_count")));
    }

    @Override public List<Category> getCategoryDistribution() {
        long total = scalar("SELECT COUNT(*) FROM kb_document WHERE deleted=0");
        return jdbcTemplate.query("SELECT COALESCE(category_id,0) category_id,COALESCE((SELECT category_name FROM kb_category c WHERE c.id=d.category_id),'Uncategorized') category_name,COUNT(*) document_count FROM kb_document d WHERE deleted=0 GROUP BY category_id ORDER BY document_count DESC", (rs,n) -> new Category(rs.getLong("category_id"), rs.getString("category_name"), rs.getLong("document_count"), total == 0 ? 0 : rs.getLong("document_count") * 100.0 / total));
    }

    @Override public List<HotDocument> getHotDocuments(String type, Integer size) {
        String field = "like".equals(type) ? "like_count" : "favorite".equals(type) ? "favorite_count" : "view_count";
        int limit = size == null ? 20 : Math.max(1, Math.min(size, 100));
        return jdbcTemplate.query("SELECT d.id document_id,d.title,d.author_id,d.author_name,COALESCE(d.category_id,0) category_id,COALESCE((SELECT category_name FROM kb_category c WHERE c.id=d.category_id),'Uncategorized') category_name,d.view_count,d.like_count,d.favorite_count,d.comment_count,d." + field + " statistics_value FROM kb_document d WHERE d.deleted=0 ORDER BY d." + field + " DESC LIMIT " + limit, (rs,n) -> new HotDocument(rs.getLong("document_id"), rs.getString("title"), rs.getLong("author_id"), rs.getString("author_name"), rs.getLong("category_id"), rs.getString("category_name"), rs.getLong("view_count"), rs.getLong("like_count"), rs.getLong("favorite_count"), rs.getLong("comment_count"), rs.getLong("statistics_value")));
    }

    @Override public List<ActiveUser> getActiveUsers(String type, Integer size) {
        int limit = size == null ? 20 : Math.max(1, Math.min(size, 100));
        return jdbcTemplate.query("SELECT u.id user_id,u.username,u.real_name,u.avatar,COUNT(DISTINCT d.id) document_count,COUNT(DISTINCT c.id) comment_count,COALESCE(SUM(d.view_count),0) view_count FROM kb_user.kb_user u LEFT JOIN kb_document d ON d.author_id=u.id AND d.deleted=0 LEFT JOIN tb_comment c ON c.commenter_id=u.id AND c.deleted=0 WHERE u.deleted=0 GROUP BY u.id,u.username,u.real_name,u.avatar ORDER BY document_count DESC LIMIT " + limit, (rs,n) -> new ActiveUser(rs.getLong("user_id"), rs.getString("username"), rs.getString("real_name"), rs.getString("avatar"), rs.getLong("document_count"), rs.getLong("comment_count"), rs.getLong("view_count"), rs.getLong("document_count") + rs.getLong("comment_count") + rs.getLong("view_count")));
    }

    @Override public void clearAllCache() { if (cacheManager != null) cacheManager.getCacheNames().forEach(n -> { Cache c=cacheManager.getCache(n); if(c!=null)c.clear(); }); }
    @Override public void triggerDocumentAggregation() { aggregationTask.aggregateDailyDocumentStatistics(); clearAllCache(); }
    @Override public void triggerUserAggregation() { aggregationTask.aggregateDailyUserStatistics(); clearAllCache(); }

    private long scalar(String sql, Object... args) { Long value = jdbcTemplate.queryForObject(sql, Long.class, args); return value == null ? 0 : value; }
}
