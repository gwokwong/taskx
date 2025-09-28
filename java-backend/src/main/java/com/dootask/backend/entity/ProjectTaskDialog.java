package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目任务对话/评论实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "project_task_dialogs")
@TableName("project_task_dialogs")
public class ProjectTaskDialog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
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
     * 对话类型: text, file, image, system
     */
    @Column(name = "type", length = 20)
    private String type = "text";

    /**
     * 消息内容
     */
    @Column(name = "msg", columnDefinition = "TEXT")
    private String msg;

    /**
     * 是否已读
     */
    @Column(name = "read", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean read = false;

    /**
     * 文件信息(JSON格式)
     */
    @Column(name = "file", columnDefinition = "JSON")
    private String file;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    @Column(name = "deleted")
    private Integer deleted = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}