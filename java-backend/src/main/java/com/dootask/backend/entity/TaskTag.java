package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务标签实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_tags")
public class TaskTag {

    /**
     * 标签ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 项目ID(null表示全局标签)
     */
    private Long projectId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 关联创建者信息（非数据库字段）
    private User creator;

    // 关联项目信息（非数据库字段）
    private Project project;
}