package com.dootask.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDto {

    // 总览统计
    private Long totalProjects;
    private Long totalTasks;
    private Long totalFiles;
    private Long totalUsers;

    // 任务状态统计
    private Map<String, Long> taskStatusStats;

    // 项目状态统计
    private Map<String, Long> projectStatusStats;

    // 用户活跃度统计
    private Map<String, Long> userActivityStats;

    // 时间段统计
    private Map<LocalDate, Long> dailyTaskStats;
    private Map<LocalDate, Long> dailyProjectStats;

    // 文件类型统计
    private Map<String, Long> fileTypeStats;

    // 存储使用统计
    private Long totalStorageUsed;
    private Long storageLimit;

    // 系统性能统计
    private Map<String, Object> systemPerformance;
}