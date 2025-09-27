package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.dto.StatisticsDto;
import com.dootask.backend.service.StatisticsService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;

@Tag(name = "数据统计", description = "数据统计和图表相关接口")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @Operation(summary = "获取用户总览统计")
    @GetMapping("/overview")
    public Result<StatisticsDto> getOverviewStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        StatisticsDto statistics = statisticsService.getOverviewStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取系统统计(管理员)")
    @GetMapping("/system")
    public Result<StatisticsDto> getSystemStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        StatisticsDto statistics = statisticsService.getSystemStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取任务状态统计")
    @GetMapping("/tasks/status")
    public Result<Map<String, Long>> getTaskStatusStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Long> statistics = statisticsService.getTaskStatusStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取项目状态统计")
    @GetMapping("/projects/status")
    public Result<Map<String, Long>> getProjectStatusStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Long> statistics = statisticsService.getProjectStatusStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取每日任务统计")
    @GetMapping("/tasks/daily")
    public Result<Map<LocalDate, Long>> getDailyTaskStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<LocalDate, Long> statistics = statisticsService.getDailyTaskStatistics(startDate, endDate, userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取每日项目统计")
    @GetMapping("/projects/daily")
    public Result<Map<LocalDate, Long>> getDailyProjectStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<LocalDate, Long> statistics = statisticsService.getDailyProjectStatistics(startDate, endDate, userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取用户活跃度统计(管理员)")
    @GetMapping("/users/activity")
    public Result<Map<String, Long>> getUserActivityStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        Map<String, Long> statistics = statisticsService.getUserActivityStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取文件类型统计")
    @GetMapping("/files/types")
    public Result<Map<String, Long>> getFileTypeStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Long> statistics = statisticsService.getFileTypeStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取存储使用统计")
    @GetMapping("/storage")
    public Result<Map<String, Object>> getStorageStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> statistics = statisticsService.getStorageStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取系统性能统计(管理员)")
    @GetMapping("/performance")
    public Result<Map<String, Object>> getSystemPerformanceStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        Map<String, Object> statistics = statisticsService.getSystemPerformanceStatistics();
        return Result.success(statistics);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }
}