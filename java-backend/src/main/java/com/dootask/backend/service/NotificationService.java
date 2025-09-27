package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    List<Notification> getNotificationsByUser(Long userId, Integer page, Integer size);

    Notification createNotification(Long userId, String type, String title, String content, String actionUrl, Long senderId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    Long getUnreadCount(Long userId);

    void deleteNotification(Long notificationId, Long userId);
}