package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务评论 Mapper 接口
 */
@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {

    /**
     * 根据任务ID获取评论列表（包含用户信息）
     */
    @Select("SELECT c.*, u.nickname as user_nickname, u.user_img as user_avatar " +
            "FROM task_comments c " +
            "LEFT JOIN users u ON c.user_id = u.userid " +
            "WHERE c.task_id = #{taskId} AND c.is_deleted = 0 " +
            "ORDER BY c.created_at ASC")
    List<TaskComment> getCommentsByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据父评论ID获取回复列表
     */
    @Select("SELECT c.*, u.nickname as user_nickname, u.user_img as user_avatar " +
            "FROM task_comments c " +
            "LEFT JOIN users u ON c.user_id = u.userid " +
            "WHERE c.parent_id = #{parentId} AND c.is_deleted = 0 " +
            "ORDER BY c.created_at ASC")
    List<TaskComment> getRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 获取任务的评论数量
     */
    @Select("SELECT COUNT(*) FROM task_comments WHERE task_id = #{taskId} AND is_deleted = 0")
    Integer getCommentCountByTaskId(@Param("taskId") Long taskId);
}