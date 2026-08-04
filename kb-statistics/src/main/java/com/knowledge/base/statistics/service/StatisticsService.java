package com.knowledge.base.statistics.service;

import com.knowledge.base.statistics.vo.StatisticsVOs.*;
import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {
    Overview getOverview(LocalDate startDate, LocalDate endDate);
    Dashboard getDashboard();
    List<Trend> getDocumentTrend(LocalDate startDate, LocalDate endDate, String type);
    List<Activity> getUserActivity(LocalDate startDate, LocalDate endDate);
    List<Category> getCategoryDistribution();
    List<HotDocument> getHotDocuments(String type, Integer size);
    List<ActiveUser> getActiveUsers(String type, Integer size);
    void clearAllCache();
    void triggerDocumentAggregation();
    void triggerUserAggregation();
}
