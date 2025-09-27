package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.service.ExportService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Tag(name = "数据导出", description = "数据导出相关接口")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final UserService userService;

    @Operation(summary = "导出用户数据")
    @GetMapping("/users")
    public void exportUsers(
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        exportService.exportUsers(format, response);
    }

    @Operation(summary = "导出项目数据")
    @GetMapping("/projects")
    public void exportProjects(
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        exportService.exportProjects(format, response);
    }

    @Operation(summary = "导出任务数据")
    @GetMapping("/tasks")
    public void exportTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        exportService.exportTasks(projectId, format, response);
    }

    @Operation(summary = "导出系统日志")
    @GetMapping("/system-logs")
    public void exportSystemLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        exportService.exportSystemLogs(startDate, endDate, format, response);
    }

    @Operation(summary = "导出日历事件")
    @GetMapping("/calendar-events")
    public void exportCalendarEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        exportService.exportCalendarEvents(startDate, endDate, format, response);
    }

    @Operation(summary = "导出部门数据")
    @GetMapping("/departments")
    public void exportDepartments(
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        exportService.exportDepartments(format, response);
    }

    @Operation(summary = "导出任务模板")
    @GetMapping("/task-templates")
    public void exportTaskTemplates(
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        exportService.exportTaskTemplates(format, response);
    }

    @Operation(summary = "导出工作流数据")
    @GetMapping("/workflows")
    public void exportWorkflows(
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        exportService.exportWorkflows(format, response);
    }

    @Operation(summary = "导出项目成员")
    @GetMapping("/project-members")
    public void exportProjectMembers(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletRequest request,
            HttpServletResponse response) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // TODO: 检查用户是否有权限导出该项目的成员数据

        exportService.exportProjectMembers(projectId, format, response);
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