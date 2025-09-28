package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.SystemLog;
import com.dootask.backend.service.SystemLogService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "系统日志", description = "系统日志管理相关接口")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;
    private final UserService userService;

    @Operation(summary = "获取系统日志列表(管理员)")
    @GetMapping
    public Result<List<SystemLog>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        List<SystemLog> logs = systemLogService.getLogs(action, module, startTime, endTime, page, size);
        return Result.success(logs);
    }

    @Operation(summary = "获取操作统计(管理员)")
    @GetMapping("/action-stats")
    public Result<Map<String, Long>> getActionStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        Map<String, Long> stats = systemLogService.getActionStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取模块统计(管理员)")
    @GetMapping("/module-stats")
    public Result<Map<String, Long>> getModuleStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        Map<String, Long> stats = systemLogService.getModuleStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取每日日志统计(管理员)")
    @GetMapping("/daily-stats")
    public Result<List<Map<String, Object>>> getDailyLogStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        List<Map<String, Object>> stats = systemLogService.getDailyLogStatistics(startDate, endDate);
        return Result.success(stats);
    }

    @Operation(summary = "清理旧日志(管理员)")
    @DeleteMapping("/cleanup")
    public Result<String> cleanOldLogs(
            @RequestParam(defaultValue = "90") Integer daysToKeep,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        systemLogService.cleanOldLogs(daysToKeep);
        return Result.success("日志清理完成");
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