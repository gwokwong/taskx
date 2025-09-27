package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_folders")
public class FileFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "owner_userid")
    private Long ownerUserid;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "is_private")
    private Boolean isPrivate = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort")
    private Integer sort = 0;

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