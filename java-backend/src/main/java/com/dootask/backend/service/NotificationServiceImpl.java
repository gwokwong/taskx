package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.Notification;
import com.dootask.backend.mapper.NotificationMapper;
import com.dootask.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public List<Notification> getNotificationsByUser(Long userId, Integer page, Integer size) {
        Page<Notification> pageInfo = new Page<>(page != null ? page : 1, size != null ? size : 20);

        Page<Notification> result = page(pageInfo, new QueryWrapper<Notification>()
                .eq("userid", userId)
                .orderByDesc("created_at"));

        return result.getRecords();
    }

    @Override
    public Notification createNotification(Long userId, String type, String title, String content, String actionUrl, Long senderId) {
        Notification notification = new Notification();
        notification.setUserid(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setActionUrl(actionUrl);
        notification.setSenderId(senderId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());

        save(notification);
        return notification;
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = getOne(new QueryWrapper<Notification>()
                .eq("id", notificationId)
                .eq("userid", userId));

        if (notification == null) {
            throw new ApiException("通知不存在");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());
            updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = list(new QueryWrapper<Notification>()
                .eq("userid", userId)
                .eq("is_read", false));

        for (Notification notification : notifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());
        }

        updateBatchById(notifications);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return count(new QueryWrapper<Notification>()
                .eq("userid", userId)
                .eq("is_read", false));
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = getOne(new QueryWrapper<Notification>()
                .eq("id", notificationId)
                .eq("userid", userId));

        if (notification == null) {
            throw new ApiException("通知不存在");
        }

        removeById(notificationId);
    }

    @Override
    public void sendNotification(Long userId, String title, String message) {
        createNotification(userId, "system", title, message, null, null);
    }
}