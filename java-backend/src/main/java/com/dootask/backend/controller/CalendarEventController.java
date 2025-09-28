package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.CalendarEvent;
import com.dootask.backend.service.CalendarEventService;
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

@Tag(name = "日历事件", description = "日历事件管理相关接口")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final UserService userService;

    @Operation(summary = "创建日历事件")
    @PostMapping
    public Result<CalendarEvent> createEvent(@RequestBody CalendarEvent event, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        event.setUserId(userId);
        CalendarEvent createdEvent = calendarEventService.create(event);
        return Result.success(createdEvent);
    }

    @Operation(summary = "更新日历事件")
    @PutMapping("/{id}")
    public Result<CalendarEvent> updateEvent(@PathVariable Long id, @RequestBody CalendarEvent event, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        CalendarEvent existingEvent = calendarEventService.getById(id);
        if (existingEvent == null || !existingEvent.getUserId().equals(userId)) {
            return Result.error("事件不存在或无权限");
        }

        event.setId(id);
        event.setUserId(userId);
        CalendarEvent updatedEvent = calendarEventService.update(event);
        return Result.success(updatedEvent);
    }

    @Operation(summary = "删除日历事件")
    @DeleteMapping("/{id}")
    public Result<String> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        CalendarEvent event = calendarEventService.getById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            return Result.error("事件不存在或无权限");
        }

        calendarEventService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取日历事件详情")
    @GetMapping("/{id}")
    public Result<CalendarEvent> getEvent(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        CalendarEvent event = calendarEventService.getById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            return Result.error("事件不存在或无权限");
        }

        return Result.success(event);
    }

    @Operation(summary = "获取指定日期范围的事件")
    @GetMapping("/range")
    public Result<List<CalendarEvent>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<CalendarEvent> events = calendarEventService.getEventsByDateRange(start, end, userId);
        return Result.success(events);
    }

    @Operation(summary = "获取用户所有事件")
    @GetMapping
    public Result<List<CalendarEvent>> getUserEvents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<CalendarEvent> events = calendarEventService.getEventsByUser(userId, page, size);
        return Result.success(events);
    }

    @Operation(summary = "获取今日事件")
    @GetMapping("/today")
    public Result<List<CalendarEvent>> getTodayEvents(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<CalendarEvent> events = calendarEventService.getTodayEvents(userId);
        return Result.success(events);
    }

    @Operation(summary = "获取即将到来的事件")
    @GetMapping("/upcoming")
    public Result<List<CalendarEvent>> getUpcomingEvents(
            @RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<CalendarEvent> events = calendarEventService.getUpcomingEvents(userId, days);
        return Result.success(events);
    }

    @Operation(summary = "获取项目相关事件")
    @GetMapping("/project/{projectId}")
    public Result<List<CalendarEvent>> getProjectEvents(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<CalendarEvent> events = calendarEventService.getEventsByProject(projectId, userId);
        return Result.success(events);
    }

    @Operation(summary = "搜索事件")
    @GetMapping("/search")
    public Result<List<CalendarEvent>> searchEvents(
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<CalendarEvent> events = calendarEventService.searchEvents(keyword, userId);
        return Result.success(events);
    }

    @Operation(summary = "创建任务提醒")
    @PostMapping("/task-reminder")
    public Result<String> createTaskReminder(
            @RequestParam Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reminderTime,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        calendarEventService.createTaskReminder(taskId, reminderTime, userId);
        return Result.success("提醒创建成功");
    }

    @Operation(summary = "创建项目里程碑")
    @PostMapping("/project-milestone")
    public Result<String> createProjectMilestone(
            @RequestParam Long projectId,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        calendarEventService.createProjectMilestone(projectId, title, date, userId);
        return Result.success("里程碑创建成功");
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