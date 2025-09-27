package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String desc;

    @Column(name = "owner_userid")
    private Long ownerUserid;

    @Column(name = "dialog_id")
    private Long dialogId;

    @Column(columnDefinition = "JSON")
    private String columns;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "archived_userid")
    private Long archivedUserid;

    @Column(name = "task_num")
    private Integer taskNum = 0;

    @Column(name = "task_complete")
    private Integer taskComplete = 0;

    @Column(name = "task_percent")
    private Integer taskPercent = 0;

    private Integer sort = 0;

    @Column(name = "top_at")
    private LocalDateTime topAt;

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