package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目工作流项实体
 */
@Data
@Entity
@Table(name = "project_flow_items")
@TableName("project_flow_items")
public class ProjectFlowItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作流ID
     */
    @Column(name = "flow_id", nullable = false)
    private Long flowId;

    /**
     * 流程项名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 流程项状态
     */
    @Column(name = "status", length = 20)
    private String status = "normal";

    /**
     * 流程项颜色
     */
    @Column(name = "color", length = 7)
    private String color = "#1890ff";

    /**
     * 排序权重
     */
    @Column(name = "sort")
    private Integer sort = 0;

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