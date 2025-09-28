package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dootask.backend.dto.StatisticsDto;
import com.dootask.backend.entity.*;
import com.dootask.backend.mapper.*;
import com.dootask.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ProjectMapper projectMapper;
    private final ProjectTaskMapper projectTaskMapper;
    private final FileMapper fileMapper;
    private final UserMapper userMapper;
    private final SearchLogMapper searchLogMapper;

    @Override
    public StatisticsDto getOverviewStatistics(Long userId) {
        StatisticsDto stats = new StatisticsDto();

        // 基本统计
        stats.setTotalProjects(projectMapper.selectCount(new QueryWrapper<Project>().eq("userid", userId)));
        stats.setTotalTasks(projectTaskMapper.selectCount(new QueryWrapper<ProjectTask>().eq("userid", userId)));
        stats.setTotalFiles(fileMapper.selectCount(new QueryWrapper<FileEntity>().eq("userid", userId)));

        // 任务状态统计
        stats.setTaskStatusStats(getTaskStatusStatistics(userId));

        // 项目状态统计
        stats.setProjectStatusStats(getProjectStatusStatistics(userId));

        // 最近7天的任务统计
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        stats.setDailyTaskStats(getDailyTaskStatistics(startDate, endDate, userId));
        stats.setDailyProjectStats(getDailyProjectStatistics(startDate, endDate, userId));

        // 文件类型统计
        stats.setFileTypeStats(getFileTypeStatistics(userId));

        return stats;
    }

    @Override
    public StatisticsDto getSystemStatistics() {
        StatisticsDto stats = new StatisticsDto();

        // 系统总览
        stats.setTotalProjects(projectMapper.selectCount(null));
        stats.setTotalTasks(projectTaskMapper.selectCount(null));
        stats.setTotalFiles(fileMapper.selectCount(null));
        stats.setTotalUsers(userMapper.selectCount(null));

        // 用户活跃度统计
        stats.setUserActivityStats(getUserActivityStatistics());

        // 存储统计
        stats.setTotalStorageUsed(getTotalStorageUsed());
        stats.setStorageLimit(10737418240L); // 10GB

        // 系统性能统计
        stats.setSystemPerformance(getSystemPerformanceStatistics());

        return stats;
    }

    @Override
    public Map<String, Long> getTaskStatusStatistics(Long userId) {
        List<ProjectTask> tasks = projectTaskMapper.selectList(new QueryWrapper<ProjectTask>().eq("userid", userId));

        return tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getState() != null ? task.getState() : "pending",
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getProjectStatusStatistics(Long userId) {
        List<Project> projects = projectMapper.selectList(new QueryWrapper<Project>().eq("userid", userId));

        return projects.stream()
                .collect(Collectors.groupingBy(
                        project -> project.getArchivedAt() != null ? "archived" : "active",
                        Collectors.counting()
                ));
    }

    @Override
    public Map<LocalDate, Long> getDailyTaskStatistics(LocalDate startDate, LocalDate endDate, Long userId) {
        Map<LocalDate, Long> dailyStats = new HashMap<>();

        // 初始化所有日期为0
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyStats.put(current, 0L);
            current = current.plusDays(1);
        }

        // 查询任务创建统计
        List<ProjectTask> tasks = projectTaskMapper.selectList(new QueryWrapper<ProjectTask>()
                .eq("userid", userId)
                .ge("created_at", startDate.atStartOfDay())
                .le("created_at", endDate.atTime(23, 59, 59)));

        Map<LocalDate, Long> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        dailyStats.putAll(tasksByDate);
        return dailyStats;
    }

    @Override
    public Map<LocalDate, Long> getDailyProjectStatistics(LocalDate startDate, LocalDate endDate, Long userId) {
        Map<LocalDate, Long> dailyStats = new HashMap<>();

        // 初始化所有日期为0
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyStats.put(current, 0L);
            current = current.plusDays(1);
        }

        // 查询项目创建统计
        List<Project> projects = projectMapper.selectList(new QueryWrapper<Project>()
                .eq("userid", userId)
                .ge("created_at", startDate.atStartOfDay())
                .le("created_at", endDate.atTime(23, 59, 59)));

        Map<LocalDate, Long> projectsByDate = projects.stream()
                .collect(Collectors.groupingBy(
                        project -> project.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        dailyStats.putAll(projectsByDate);
        return dailyStats;
    }

    @Override
    public Map<String, Long> getUserActivityStatistics() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 活跃用户统计（最近7天有搜索记录的用户）
        List<SearchLog> recentSearches = searchLogMapper.selectList(new QueryWrapper<SearchLog>()
                .ge("search_at", sevenDaysAgo));

        long activeUsers = recentSearches.stream()
                .map(SearchLog::getUserId)
                .distinct()
                .count();

        long totalUsers = userMapper.selectCount(null);

        Map<String, Long> stats = new HashMap<>();
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        stats.put("totalUsers", totalUsers);

        return stats;
    }

    @Override
    public Map<String, Long> getFileTypeStatistics(Long userId) {
        List<FileEntity> files = fileMapper.selectList(new QueryWrapper<FileEntity>().eq("userid", userId));

        return files.stream()
                .collect(Collectors.groupingBy(
                        file -> getFileExtension(file.getFileName()),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Long totalUsed = getTotalStorageUsed();
        Long limit = 10737418240L; // 10GB

        stats.put("totalUsed", totalUsed);
        stats.put("totalLimit", limit);
        stats.put("usagePercentage", totalUsed.doubleValue() / limit.doubleValue() * 100);
        stats.put("available", limit - totalUsed);

        return stats;
    }

    @Override
    public Map<String, Object> getSystemPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        stats.put("totalMemory", totalMemory);
        stats.put("usedMemory", usedMemory);
        stats.put("freeMemory", freeMemory);
        stats.put("memoryUsagePercentage", (double) usedMemory / totalMemory * 100);
        stats.put("availableProcessors", runtime.availableProcessors());

        return stats;
    }

    private Long getTotalStorageUsed() {
        List<FileEntity> files = fileMapper.selectList(null);
        return files.stream()
                .mapToLong(file -> file.getFileSize() != null ? file.getFileSize() : 0L)
                .sum();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "other";
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // 归类常见文件类型
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "svg":
                return "image";
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
                return "word";
            case "xls":
            case "xlsx":
                return "excel";
            case "ppt":
            case "pptx":
                return "powerpoint";
            case "txt":
                return "text";
            case "zip":
            case "rar":
            case "7z":
                return "archive";
            case "mp4":
            case "avi":
            case "mov":
                return "video";
            case "mp3":
            case "wav":
            case "flac":
                return "audio";
            default:
                return "other";
        }
    }
}