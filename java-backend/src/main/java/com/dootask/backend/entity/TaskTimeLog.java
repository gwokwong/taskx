package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务时间记录实体
 */
@Data
@Entity
@Table(name = "task_time_logs")
@TableName("task_time_logs")
public class TaskTimeLog {

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
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 记录类型: manual(手动), timer(计时器)
     */
    @Column(name = "log_type", length = 20)
    private String logType = "manual";

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 持续时间(分钟)
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 是否正在进行
     */
    @Column(name = "is_running")
    private Boolean isRunning = false;

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