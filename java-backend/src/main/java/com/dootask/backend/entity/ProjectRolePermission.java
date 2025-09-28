package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目角色权限关联实体
 */
@Data
@Entity
@Table(name = "project_role_permissions")
@TableName("project_role_permissions")
public class ProjectRolePermission {

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
     * 角色名称: owner, admin, member, viewer, custom
     */
    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    /**
     * 权限ID
     */
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    /**
     * 是否拥有该权限
     */
    @Column(name = "granted")
    private Boolean granted = true;

    /**
     * 权限范围限制(JSON格式)
     */
    @Column(name = "scope_restriction", columnDefinition = "JSON")
    private String scopeRestriction;

    /**
     * 授权人ID
     */
    @Column(name = "granted_by")
    private Long grantedBy;

    /**
     * 授权时间
     */
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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