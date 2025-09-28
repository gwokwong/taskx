package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务分配实体
 */
@Data
@Entity
@Table(name = "task_assignments")
@TableName("task_assignments")
public class TaskAssignment {

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
     * 被分配的用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 分配类型: owner(负责人), assignee(参与者), reviewer(审核人)
     */
    @Column(name = "assignment_type", length = 20)
    private String assignmentType = "assignee";

    /**
     * 分配人ID
     */
    @Column(name = "assigned_by")
    private Long assignedBy;

    /**
     * 分配时间
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    /**
     * 是否接受分配
     */
    @Column(name = "accepted")
    private Boolean accepted = false;

    /**
     * 接受时间
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

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