package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.ProjectPermission;
import com.dootask.backend.entity.ProjectRolePermission;
import com.dootask.backend.service.ProjectPermissionService;
import com.dootask.backend.service.ProjectMemberService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Tag(name = "项目权限管理", description = "项目权限控制相关接口")
@RestController
@RequestMapping("/api/projects/{projectId}/permissions")
@RequiredArgsConstructor
public class ProjectPermissionController {

    private final ProjectPermissionService projectPermissionService;
    private final ProjectMemberService projectMemberService;
    private final UserService userService;

    @Operation(summary = "创建项目权限")
    @PostMapping
    public Result<ProjectPermission> createPermission(
            @PathVariable Long projectId,
            @Valid @RequestBody CreatePermissionRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserId(httpRequest);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限管理项目权限");
        }

        ProjectPermission permission = new ProjectPermission();
        permission.setProjectId(projectId);
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionCode(request.getPermissionCode());
        permission.setDescription(request.getDescription());
        permission.setPermissionType(request.getPermissionType());
        permission.setPermissionLevel(request.getPermissionLevel());
        permission.setPermissionGroup(request.getPermissionGroup());

        ProjectPermission created = projectPermissionService.createPermission(permission);
        return Result.success("权限创建成功", created);
    }

    @Operation(summary = "获取项目权限列表")
    @GetMapping
    public Result<List<ProjectPermission>> getProjectPermissions(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.isMember(projectId, userId)) {
            return Result.error("无权限查看项目权限");
        }

        List<ProjectPermission> permissions = projectPermissionService.getProjectPermissions(projectId);
        return Result.success(permissions);
    }

    @Operation(summary = "为角色授权")
    @PostMapping("/roles/{roleName}/grant")
    public Result<String> grantPermissionToRole(
            @PathVariable Long projectId,
            @PathVariable String roleName,
            @RequestBody GrantPermissionRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserId(httpRequest);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限管理角色权限");
        }

        projectPermissionService.grantPermissionToRole(projectId, roleName, request.getPermissionId(), userId);
        return Result.success("权限授权成功");
    }

    @Operation(summary = "撤销角色权限")
    @PostMapping("/roles/{roleName}/revoke")
    public Result<String> revokePermissionFromRole(
            @PathVariable Long projectId,
            @PathVariable String roleName,
            @RequestBody RevokePermissionRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserId(httpRequest);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限管理角色权限");
        }

        projectPermissionService.revokePermissionFromRole(projectId, roleName, request.getPermissionId());
        return Result.success("权限撤销成功");
    }

    @Operation(summary = "获取角色权限列表")
    @GetMapping("/roles/{roleName}")
    public Result<List<ProjectRolePermission>> getRolePermissions(
            @PathVariable Long projectId,
            @PathVariable String roleName,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.isMember(projectId, userId)) {
            return Result.error("无权限查看角色权限");
        }

        List<ProjectRolePermission> rolePermissions = projectPermissionService.getRolePermissions(projectId, roleName);
        return Result.success(rolePermissions);
    }

    @Operation(summary = "检查用户权限")
    @GetMapping("/users/{targetUserId}/check")
    public Result<Boolean> checkUserPermission(
            @PathVariable Long projectId,
            @PathVariable Long targetUserId,
            @RequestParam String permissionCode,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.isMember(projectId, userId)) {
            return Result.error("无权限检查用户权限");
        }

        boolean hasPermission = projectPermissionService.hasPermission(projectId, targetUserId, permissionCode);
        return Result.success(hasPermission);
    }

    @Operation(summary = "获取用户权限列表")
    @GetMapping("/users/{targetUserId}")
    public Result<Map<String, Boolean>> getUserPermissions(
            @PathVariable Long projectId,
            @PathVariable Long targetUserId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.isMember(projectId, userId)) {
            return Result.error("无权限查看用户权限");
        }

        Map<String, Boolean> userPermissions = projectPermissionService.getUserPermissionMap(projectId, targetUserId);
        return Result.success(userPermissions);
    }

    @Operation(summary = "批量授权")
    @PostMapping("/roles/{roleName}/batch-grant")
    public Result<String> batchGrantPermissions(
            @PathVariable Long projectId,
            @PathVariable String roleName,
            @RequestBody BatchGrantRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserId(httpRequest);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限批量授权");
        }

        projectPermissionService.batchGrantPermissions(projectId, roleName, request.getPermissionIds(), userId);
        return Result.success("批量授权成功");
    }

    @Operation(summary = "批量撤销权限")
    @PostMapping("/roles/{roleName}/batch-revoke")
    public Result<String> batchRevokePermissions(
            @PathVariable Long projectId,
            @PathVariable String roleName,
            @RequestBody BatchRevokeRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserId(httpRequest);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限批量撤销");
        }

        projectPermissionService.batchRevokePermissions(projectId, roleName, request.getPermissionIds());
        return Result.success("批量撤销成功");
    }

    @Operation(summary = "应用权限模板")
    @PostMapping("/templates/{templateName}/apply")
    public Result<String> applyPermissionTemplate(
            @PathVariable Long projectId,
            @PathVariable String templateName,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限应用权限模板");
        }

        projectPermissionService.applyPermissionTemplate(projectId, templateName);
        return Result.success("权限模板应用成功");
    }

    @Operation(summary = "获取可用权限模板")
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getAvailableTemplates(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.isMember(projectId, userId)) {
            return Result.error("无权限查看权限模板");
        }

        List<Map<String, Object>> templates = projectPermissionService.getAvailableTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "复制角色权限")
    @PostMapping("/roles/{sourceRole}/copy-to/{targetRole}")
    public Result<String> copyRolePermissions(
            @PathVariable Long projectId,
            @PathVariable String sourceRole,
            @PathVariable String targetRole,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.hasPermission(projectId, userId, "manage_permissions")) {
            return Result.error("无权限复制角色权限");
        }

        projectPermissionService.copyRolePermissions(projectId, sourceRole, targetRole, userId);
        return Result.success("角色权限复制成功");
    }

    @Operation(summary = "权限分析报告")
    @GetMapping("/analysis")
    public Result<Map<String, Object>> analyzeProjectPermissions(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.hasPermission(projectId, userId, "view_reports")) {
            return Result.error("无权限查看权限分析");
        }

        Map<String, Object> analysis = projectPermissionService.analyzeProjectPermissions(projectId);
        return Result.success(analysis);
    }

    @Operation(summary = "获取权限审计日志")
    @GetMapping("/audit-log")
    public Result<List<Map<String, Object>>> getPermissionAuditLog(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.hasPermission(projectId, userId, "view_audit")) {
            return Result.error("无权限查看审计日志");
        }

        List<Map<String, Object>> auditLog = projectPermissionService.getPermissionAuditLog(projectId);
        return Result.success(auditLog);
    }

    @Operation(summary = "查找具有特定权限的用户")
    @GetMapping("/search-users")
    public Result<List<Map<String, Object>>> findUsersWithPermission(
            @PathVariable Long projectId,
            @RequestParam String permissionCode,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (!projectMemberService.hasPermission(projectId, userId, "view_permissions")) {
            return Result.error("无权限查找用户权限");
        }

        List<Map<String, Object>> users = projectPermissionService.findUsersWithPermission(projectId, permissionCode);
        return Result.success(users);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }

    public static class CreatePermissionRequest {
        @NotBlank(message = "权限名称不能为空")
        private String permissionName;
        @NotBlank(message = "权限代码不能为空")
        private String permissionCode;
        private String description;
        private String permissionType = "project";
        private Integer permissionLevel = 1;
        private String permissionGroup;

        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
        public String getPermissionCode() { return permissionCode; }
        public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPermissionType() { return permissionType; }
        public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
        public Integer getPermissionLevel() { return permissionLevel; }
        public void setPermissionLevel(Integer permissionLevel) { this.permissionLevel = permissionLevel; }
        public String getPermissionGroup() { return permissionGroup; }
        public void setPermissionGroup(String permissionGroup) { this.permissionGroup = permissionGroup; }
    }

    public static class GrantPermissionRequest {
        @NotNull(message = "权限ID不能为空")
        private Long permissionId;

        public Long getPermissionId() { return permissionId; }
        public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    }

    public static class RevokePermissionRequest {
        @NotNull(message = "权限ID不能为空")
        private Long permissionId;

        public Long getPermissionId() { return permissionId; }
        public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }
    }

    public static class BatchGrantRequest {
        @NotNull(message = "权限ID列表不能为空")
        private List<Long> permissionIds;

        public List<Long> getPermissionIds() { return permissionIds; }
        public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
    }

    public static class BatchRevokeRequest {
        @NotNull(message = "权限ID列表不能为空")
        private List<Long> permissionIds;

        public List<Long> getPermissionIds() { return permissionIds; }
        public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
    }
}