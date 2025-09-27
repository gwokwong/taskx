package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "project_tasks")
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "column_id")
    private Long columnId;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String desc;

    @Column(name = "color")
    private String color;

    @Column(name = "times")
    private Integer times = 1;

    @Column(name = "level")
    private Integer level = 0;

    @Column(name = "complete_at")
    private LocalDateTime completeAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "archived_userid")
    private Long archivedUserid;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "owner")
    private String owner;

    @Column(name = "assist")
    private String assist;

    @Column(name = "subtasks")
    private Integer subtasks = 0;

    @Column(name = "subtasks_complete")
    private Integer subtasksComplete = 0;

    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "dialog_id")
    private Long dialogId;

    @Column(name = "flow_item_id")
    private Long flowItemId;

    @Column(name = "flow_item_name")
    private String flowItemName;

    @Column(name = "flow_item_status")
    private String flowItemStatus;

    @Column(name = "flow_item_color")
    private String flowItemColor;

    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;
}