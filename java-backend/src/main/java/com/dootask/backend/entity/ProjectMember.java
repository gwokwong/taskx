package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_members")
public class ProjectMember {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long userId;

    private String role; // owner, member, viewer

    private String status; // active, inactive

    private String permissions; // JSON格式的权限配置

    private LocalDateTime invitedAt;

    private Long invitedBy;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}