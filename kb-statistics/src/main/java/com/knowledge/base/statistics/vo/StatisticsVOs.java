package com.knowledge.base.statistics.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class StatisticsVOs {
    private StatisticsVOs() { }
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Overview { private long totalDocuments, totalUsers, todayDocuments, todayUsers, totalViews, todayViews, totalLikes, totalFavorites, totalComments, pendingReviews, aiSearchCount, aiQaCount, activeUserCount; }
    @Data @NoArgsConstructor @AllArgsConstructor public static class Trend { private String date; private long count; }
    @Data @NoArgsConstructor @AllArgsConstructor public static class Activity { private long userId; private String username; private long documentCount, commentCount, viewCount, activityScore; }
    @Data @NoArgsConstructor @AllArgsConstructor public static class Category { private long categoryId; private String categoryName; private long documentCount; private double percentage; }
    @Data @NoArgsConstructor @AllArgsConstructor public static class HotDocument { private long documentId; private String title; private long authorId; private String authorName; private long categoryId; private String categoryName; private long viewCount, likeCount, favoriteCount, commentCount, statisticsValue; }
    @Data @NoArgsConstructor @AllArgsConstructor public static class ActiveUser { private long userId; private String username; private String realName; private String avatar; private long documentCount, commentCount, viewCount, statisticsValue; }
    @Data @NoArgsConstructor @AllArgsConstructor public static class Dashboard { private Overview overview; private java.util.List<Trend> documentTrend; private java.util.List<Category> categoryDistribution; private java.util.List<HotDocument> hotDocuments; private java.util.List<ActiveUser> activeUsers; }
}
