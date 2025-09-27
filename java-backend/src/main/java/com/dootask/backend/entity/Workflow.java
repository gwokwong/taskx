package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflows")
public class Workflow {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String category;

    private Long createdBy;

    private String definition; // JSON格式的工作流定义

    private String version;

    private String status; // draft, active, inactive

    private Boolean isTemplate;

    private String triggerType; // manual, auto, schedule

    private String triggerCondition; // JSON格式的触发条件

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}