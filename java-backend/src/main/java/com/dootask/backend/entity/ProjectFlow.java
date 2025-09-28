package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目工作流实体
 */
@Data
@Entity
@Table(name = "project_flows")
@TableName("project_flows")
public class ProjectFlow {

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
     * 工作流名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 工作流状态: active, inactive
     */
    @Column(name = "status", length = 20)
    private String status = "active";

    /**
     * 是否为默认工作流
     */
    @Column(name = "is_default", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDefault = false;

    /**
     * 创建用户ID
     */
    @Column(name = "userid")
    private Long userid;

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