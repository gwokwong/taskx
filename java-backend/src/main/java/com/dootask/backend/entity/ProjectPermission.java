package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目权限实体
 */
@Data
@Entity
@Table(name = "project_permissions")
@TableName("project_permissions")
public class ProjectPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 权限名称
     */
    @Column(name = "permission_name", length = 100, nullable = false)
    private String permissionName;

    /**
     * 权限代码
     */
    @Column(name = "permission_code", length = 100, nullable = false)
    private String permissionCode;

    /**
     * 权限描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 权限类型: project(项目级), task(任务级), member(成员级), file(文件级)
     */
    @Column(name = "permission_type", length = 20)
    private String permissionType = "project";

    /**
     * 权限级别: 1-10，数字越高权限越大
     */
    @Column(name = "permission_level")
    private Integer permissionLevel = 1;

    /**
     * 是否是系统权限
     */
    @Column(name = "is_system")
    private Boolean isSystem = false;

    /**
     * 权限分组
     */
    @Column(name = "permission_group", length = 50)
    private String permissionGroup;

    /**
     * 权限依赖(JSON格式)
     */
    @Column(name = "dependencies", columnDefinition = "JSON")
    private String dependencies;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

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