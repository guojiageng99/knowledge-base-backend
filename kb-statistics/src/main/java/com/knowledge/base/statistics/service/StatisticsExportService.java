package com.knowledge.base.statistics.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

public interface StatisticsExportService {
    void exportDocumentTrend(LocalDate startDate, LocalDate endDate, String type, HttpServletResponse response) throws IOException;
    void exportHotDocuments(String type, Integer size, HttpServletResponse response) throws IOException;
    void exportActiveUsers(String type, Integer size, HttpServletResponse response) throws IOException;
}
