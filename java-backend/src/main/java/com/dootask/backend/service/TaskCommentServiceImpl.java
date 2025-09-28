package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.TaskComment;
import com.dootask.backend.entity.User;
import com.dootask.backend.mapper.TaskCommentMapper;
import com.dootask.backend.service.TaskCommentService;
import com.dootask.backend.service.UserService;
import com.dootask.backend.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl extends ServiceImpl<TaskCommentMapper, TaskComment> implements TaskCommentService {

    private final UserService userService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public List<TaskComment> getCommentsByTaskId(Long taskId) {
        List<TaskComment> comments = baseMapper.getCommentsByTaskId(taskId);

        // 构建评论树结构
        List<TaskComment> rootComments = new ArrayList<>();
        for (TaskComment comment : comments) {
            if (comment.getParentId() == null) {
                // 获取回复
                List<TaskComment> replies = getRepliesByParentId(comment.getId());
                comment.setReplies(replies);
                rootComments.add(comment);
            }
        }

        return rootComments;
    }

    @Override
    @Transactional
    public TaskComment addComment(Long taskId, String content, Long userId, Long parentId, List<Long> mentions) {
        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setIsDeleted(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // 处理提及用户
        if (mentions != null && !mentions.isEmpty()) {
            try {
                comment.setMentions(objectMapper.writeValueAsString(mentions));
            } catch (JsonProcessingException e) {
                log.error("序列化提及用户失败", e);
            }
        }

        save(comment);

        // 发送通知给提及的用户
        if (mentions != null && !mentions.isEmpty()) {
            mentionUsers(comment.getId(), mentions);
        }

        // 获取完整的评论信息返回
        comment.setUser(userService.getUserById(userId));

        log.info("用户{}在任务{}添加了评论", userId, taskId);
        return comment;
    }

    @Override
    @Transactional
    public TaskComment updateComment(Long commentId, String content, Long userId) {
        TaskComment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此评论");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        updateById(comment);

        log.info("用户{}更新了评论{}", userId, commentId);
        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        TaskComment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此评论");
        }

        // 软删除
        comment.setIsDeleted(1);
        comment.setUpdatedAt(LocalDateTime.now());
        updateById(comment);

        // 同时删除所有回复
        QueryWrapper<TaskComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", commentId);
        List<TaskComment> replies = list(queryWrapper);
        for (TaskComment reply : replies) {
            reply.setIsDeleted(1);
            reply.setUpdatedAt(LocalDateTime.now());
            updateById(reply);
        }

        log.info("用户{}删除了评论{}", userId, commentId);
    }

    @Override
    public List<TaskComment> getRepliesByParentId(Long parentId) {
        return baseMapper.getRepliesByParentId(parentId);
    }

    @Override
    public Integer getCommentCountByTaskId(Long taskId) {
        return baseMapper.getCommentCountByTaskId(taskId);
    }

    @Override
    public void mentionUsers(Long commentId, List<Long> userIds) {
        TaskComment comment = getById(commentId);
        if (comment == null) {
            return;
        }

        User commenter = userService.getUserById(comment.getUserId());
        String commenterName = commenter != null ? commenter.getNickname() : "用户";

        for (Long userId : userIds) {
            try {
                String message = String.format("%s 在评论中提及了您", commenterName);
                notificationService.sendNotification(userId, "评论提及", message);
            } catch (Exception e) {
                log.error("发送提及通知失败: userId={}", userId, e);
            }
        }
    }
}