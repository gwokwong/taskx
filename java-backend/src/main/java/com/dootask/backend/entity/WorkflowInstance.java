package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_instances")
public class WorkflowInstance {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long workflowId;

    private String instanceName;

    private Long initiatedBy;

    private String status; // pending, running, completed, failed, cancelled

    private String currentStep;

    private String variables; // JSON格式的变量数据

    private String executionLog; // JSON格式的执行日志

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}