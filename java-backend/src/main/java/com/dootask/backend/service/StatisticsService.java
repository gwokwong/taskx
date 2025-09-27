package com.dootask.backend.service;

import com.dootask.backend.dto.StatisticsDto;

import java.time.LocalDate;
import java.util.Map;

public interface StatisticsService {

    StatisticsDto getOverviewStatistics(Long userId);

    StatisticsDto getSystemStatistics();

    Map<String, Long> getTaskStatusStatistics(Long userId);

    Map<String, Long> getProjectStatusStatistics(Long userId);

    Map<LocalDate, Long> getDailyTaskStatistics(LocalDate startDate, LocalDate endDate, Long userId);

    Map<LocalDate, Long> getDailyProjectStatistics(LocalDate startDate, LocalDate endDate, Long userId);

    Map<String, Long> getUserActivityStatistics();

    Map<String, Long> getFileTypeStatistics(Long userId);

    Map<String, Object> getStorageStatistics();

    Map<String, Object> getSystemPerformanceStatistics();
}