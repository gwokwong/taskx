package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务评论实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_comments")
public class TaskComment {

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID(用于回复)
     */
    private Long parentId;

    /**
     * 提及的用户ID数组
     */
    private String mentions;

    /**
     * 附件数组
     */
    private String attachments;

    /**
     * 是否已删除: 0-否, 1-是
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 关联用户信息（非数据库字段）
    private User user;

    // 子评论列表（非数据库字段）
    private List<TaskComment> replies;

    // 提及的用户列表（非数据库字段）
    private List<User> mentionedUsers;

    // 是否被当前用户点赞（非数据库字段）
    private Boolean isLiked;

    // 点赞数量（非数据库字段）
    private Integer likeCount;
}