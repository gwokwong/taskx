package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务活动记录实体
 */
@Data
@Entity
@Table(name = "task_activities")
@TableName("task_activities")
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 操作用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 活动类型: created, updated, assigned, completed, commented, archived, deleted
     */
    @Column(name = "activity_type", length = 50)
    private String activityType;

    /**
     * 活动描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 变更前的值(JSON格式)
     */
    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    /**
     * 变更后的值(JSON格式)
     */
    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    /**
     * 关联数据ID(如评论ID、文件ID等)
     */
    @Column(name = "related_id")
    private Long relatedId;

    /**
     * 关联数据类型
     */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;
}