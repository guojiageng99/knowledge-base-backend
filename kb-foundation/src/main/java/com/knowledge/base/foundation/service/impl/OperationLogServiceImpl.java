package com.knowledge.base.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.foundation.entity.OperationLog;
import com.knowledge.base.foundation.mapper.OperationLogMapper;
import com.knowledge.base.foundation.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OperationLogMapper operationLogMapper;

    @Override
    public IPage<OperationLog> pageLogs(Long current, Long size, String module, String operationType,
                                        String username, LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogMapper.selectPage(new Page<>(current, size), buildQuery(module, operationType,
                username, startTime, endTime).orderByDesc(OperationLog::getCreateTime));
    }

    @Override
    public OperationLog getLogById(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<OperationLog> logs = operationLogMapper.selectList(buildQuery(null, null, null, startTime, endTime));
        long successCount = logs.stream().filter(log -> Integer.valueOf(1).equals(log.getStatus())).count();
        long failureCount = logs.stream().filter(log -> Integer.valueOf(0).equals(log.getStatus())).count();
        double averageExecuteTime = logs.stream().filter(log -> log.getExecuteTime() != null)
                .mapToInt(OperationLog::getExecuteTime).average().orElse(0D);
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("total", logs.size());
        statistics.put("success", successCount);
        statistics.put("failure", failureCount);
        statistics.put("averageExecuteTime", averageExecuteTime);
        return statistics;
    }

    @Override
    public int deleteLogsBeforeDate(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            return 0;
        }
        return operationLogMapper.deleteBeforeDate(beforeDate.format(DATE_TIME_FORMATTER));
    }

    private LambdaQueryWrapper<OperationLog> buildQuery(String module, String operationType, String username,
                                                         LocalDateTime startTime, LocalDateTime endTime) {
        return new LambdaQueryWrapper<OperationLog>()
                .eq(StringUtils.hasText(module), OperationLog::getModule, module)
                .eq(StringUtils.hasText(operationType), OperationLog::getOperationType, operationType)
                .eq(StringUtils.hasText(username), OperationLog::getUsername, username)
                .ge(startTime != null, OperationLog::getCreateTime, startTime)
                .le(endTime != null, OperationLog::getCreateTime, endTime);
    }
}
