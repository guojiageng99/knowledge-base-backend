package com.knowledge.base.foundation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.foundation.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.Map;

public interface OperationLogService {

    IPage<OperationLog> pageLogs(Long current, Long size, String module, String operationType,
                                 String username, LocalDateTime startTime, LocalDateTime endTime);

    OperationLog getLogById(Long id);

    Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime);

    int deleteLogsBeforeDate(LocalDateTime beforeDate);
}
