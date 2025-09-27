package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_templates")
public class TaskTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String category; // 模板分类

    private Long createdBy;

    private String template; // JSON格式的模板配置

    private String tags; // 标签，逗号分隔

    private Boolean isPublic; // 是否公开

    private Integer useCount; // 使用次数

    private String status; // active, inactive

    private String version; // 版本号

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}