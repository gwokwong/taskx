package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.SystemLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SystemLogService extends IService<SystemLog> {

    void logAction(String action, String module, String description, Long userId, String ipAddress, String userAgent);

    void logAction(String action, String module, String description, Long userId, String ipAddress, String userAgent, String requestPath, String requestMethod, String requestParams, String responseStatus, Long executionTime);

    List<SystemLog> getLogs(String action, String module, LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer size);

    Map<String, Long> getActionStatistics();

    Map<String, Long> getModuleStatistics();

    List<Map<String, Object>> getDailyLogStatistics(LocalDateTime startDate, LocalDateTime endDate);

    void cleanOldLogs(Integer daysToKeep);
}