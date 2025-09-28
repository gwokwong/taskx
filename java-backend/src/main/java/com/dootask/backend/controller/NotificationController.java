package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.Notification;
import com.dootask.backend.service.NotificationService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "通知管理", description = "通知相关接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        List<Notification> notifications = notificationService.getNotificationsByUser(userId, page, size);
        Long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("notifications", notifications);
        data.put("unreadCount", unreadCount);
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        notificationService.markAsRead(id, userId);
        return Result.success("标记成功");
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<String> markAllAsRead(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记成功");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        notificationService.deleteNotification(id, userId);
        return Result.success("删除成功");
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