package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目模板实体
 */
@Data
@Entity
@Table(name = "project_templates")
@TableName("project_templates")
public class ProjectTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 模板描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 模板分类
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 模板标签(JSON格式)
     */
    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    /**
     * 项目配置(JSON格式)
     */
    @Column(name = "project_config", columnDefinition = "JSON")
    private String projectConfig;

    /**
     * 看板列配置(JSON格式)
     */
    @Column(name = "columns_config", columnDefinition = "JSON")
    private String columnsConfig;

    /**
     * 任务模板(JSON格式)
     */
    @Column(name = "tasks_template", columnDefinition = "JSON")
    private String tasksTemplate;

    /**
     * 成员角色配置(JSON格式)
     */
    @Column(name = "roles_config", columnDefinition = "JSON")
    private String rolesConfig;

    /**
     * 工作流配置(JSON格式)
     */
    @Column(name = "workflow_config", columnDefinition = "JSON")
    private String workflowConfig;

    /**
     * 权限配置(JSON格式)
     */
    @Column(name = "permissions_config", columnDefinition = "JSON")
    private String permissionsConfig;

    /**
     * 是否为系统模板
     */
    @Column(name = "is_system")
    private Boolean isSystem = false;

    /**
     * 是否为公开模板
     */
    @Column(name = "is_public")
    private Boolean isPublic = false;

    /**
     * 模板创建者ID
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 使用次数
     */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    /**
     * 模板评分
     */
    @Column(name = "rating")
    private Double rating = 0.0;

    /**
     * 模板图标
     */
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 模板颜色
     */
    @Column(name = "color", length = 20)
    private String color;

    /**
     * 模板版本
     */
    @Column(name = "version", length = 20)
    private String version = "1.0.0";

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 排序权重
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 预览截图(JSON格式)
     */
    @Column(name = "screenshots", columnDefinition = "JSON")
    private String screenshots;

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