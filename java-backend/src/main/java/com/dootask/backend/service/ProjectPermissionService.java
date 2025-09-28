package com.dootask.backend.service;

import com.dootask.backend.entity.ProjectPermission;
import com.dootask.backend.entity.ProjectRolePermission;

import java.util.List;
import java.util.Map;

public interface ProjectPermissionService {

    // 权限管理
    ProjectPermission createPermission(ProjectPermission permission);

    void deletePermission(Long permissionId);

    ProjectPermission updatePermission(Long permissionId, ProjectPermission permission);

    List<ProjectPermission> getProjectPermissions(Long projectId);

    List<ProjectPermission> getSystemPermissions();

    // 角色权限管理
    void grantPermissionToRole(Long projectId, String roleName, Long permissionId, Long grantedBy);

    void revokePermissionFromRole(Long projectId, String roleName, Long permissionId);

    List<ProjectRolePermission> getRolePermissions(Long projectId, String roleName);

    List<String> getRolePermissionCodes(Long projectId, String roleName);

    // 用户权限检查
    boolean hasPermission(Long projectId, Long userId, String permissionCode);

    boolean hasAnyPermission(Long projectId, Long userId, String... permissionCodes);

    boolean hasAllPermissions(Long projectId, Long userId, String... permissionCodes);

    List<String> getUserPermissions(Long projectId, Long userId);

    Map<String, Boolean> getUserPermissionMap(Long projectId, Long userId);

    // 权限级别检查
    boolean hasPermissionLevel(Long projectId, Long userId, Integer requiredLevel);

    Integer getUserMaxPermissionLevel(Long projectId, Long userId);

    // 权限模板
    void applyPermissionTemplate(Long projectId, String templateName);

    Map<String, Object> getPermissionTemplate(String templateName);

    List<Map<String, Object>> getAvailableTemplates();

    // 权限审计
    List<Map<String, Object>> getPermissionAuditLog(Long projectId);

    void logPermissionChange(Long projectId, Long userId, String action, String details);

    // 批量操作
    void batchGrantPermissions(Long projectId, String roleName, List<Long> permissionIds, Long grantedBy);

    void batchRevokePermissions(Long projectId, String roleName, List<Long> permissionIds);

    void copyRolePermissions(Long projectId, String sourceRole, String targetRole, Long operatorId);

    // 权限分析
    Map<String, Object> analyzeProjectPermissions(Long projectId);

    List<Map<String, Object>> findUsersWithPermission(Long projectId, String permissionCode);

    List<Map<String, Object>> findRolesWithPermission(Long projectId, String permissionCode);

    // 权限继承
    void setPermissionInheritance(Long projectId, Long parentProjectId);

    void removePermissionInheritance(Long projectId);

    List<String> getInheritedPermissions(Long projectId, Long userId);
}