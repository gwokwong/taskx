package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.TaskComment;

import java.util.List;

/**
 * 任务评论服务接口
 */
public interface TaskCommentService extends IService<TaskComment> {

    /**
     * 获取任务的评论列表
     */
    List<TaskComment> getCommentsByTaskId(Long taskId);

    /**
     * 添加评论
     */
    TaskComment addComment(Long taskId, String content, Long userId, Long parentId, List<Long> mentions);

    /**
     * 更新评论
     */
    TaskComment updateComment(Long commentId, String content, Long userId);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取评论的回复列表
     */
    List<TaskComment> getRepliesByParentId(Long parentId);

    /**
     * 获取任务的评论数量
     */
    Integer getCommentCountByTaskId(Long taskId);

    /**
     * 提及用户
     */
    void mentionUsers(Long commentId, List<Long> userIds);
}