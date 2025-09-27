package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.ProjectMember;
import com.dootask.backend.service.ProjectMemberService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "项目成员", description = "项目成员管理相关接口")
@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final UserService userService;

    @Operation(summary = "添加项目成员")
    @PostMapping
    public Result<ProjectMember> addMember(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "member") String role,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限：只有项目所有者或管理员可以添加成员
        if (!projectMemberService.hasPermission(projectId, currentUserId, "manage_members") &&
            !userService.isAdmin(currentUserId)) {
            return Result.error("无权限添加成员");
        }

        ProjectMember member = projectMemberService.addMember(projectId, userId, role, currentUserId);
        return Result.success(member);
    }

    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{userId}")
    public Result<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限：只有项目所有者或管理员或用户自己可以移除
        if (!projectMemberService.hasPermission(projectId, currentUserId, "manage_members") &&
            !userService.isAdmin(currentUserId) &&
            !currentUserId.equals(userId)) {
            return Result.error("无权限移除成员");
        }

        projectMemberService.removeMember(projectId, userId);
        return Result.success("移除成功");
    }

    @Operation(summary = "更新成员角色")
    @PutMapping("/{userId}/role")
    public Result<Void> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestParam String role,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限：只有项目所有者或管理员可以更新角色
        if (!projectMemberService.hasPermission(projectId, currentUserId, "manage_members") &&
            !userService.isAdmin(currentUserId)) {
            return Result.error("无权限更新角色");
        }

        projectMemberService.updateMemberRole(projectId, userId, role);
        return Result.success("角色更新成功");
    }

    @Operation(summary = "更新成员权限")
    @PutMapping("/{userId}/permissions")
    public Result<Void> updateMemberPermissions(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody String permissions,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限：只有项目所有者或管理员可以更新权限
        if (!projectMemberService.hasPermission(projectId, currentUserId, "manage_members") &&
            !userService.isAdmin(currentUserId)) {
            return Result.error("无权限更新权限");
        }

        projectMemberService.updateMemberPermissions(projectId, userId, permissions);
        return Result.success("权限更新成功");
    }

    @Operation(summary = "获取项目成员列表")
    @GetMapping
    public Result<List<ProjectMember>> getProjectMembers(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查是否是项目成员
        if (!projectMemberService.isMember(projectId, currentUserId)) {
            return Result.error("无权限查看成员");
        }

        List<ProjectMember> members = projectMemberService.getProjectMembers(projectId);
        return Result.success(members);
    }

    @Operation(summary = "搜索项目成员")
    @GetMapping("/search")
    public Result<List<ProjectMember>> searchMembers(
            @PathVariable Long projectId,
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查是否是项目成员
        if (!projectMemberService.isMember(projectId, currentUserId)) {
            return Result.error("无权限搜索成员");
        }

        List<ProjectMember> members = projectMemberService.searchMembers(projectId, keyword);
        return Result.success(members);
    }

    @Operation(summary = "获取成员统计")
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getMemberStatistics(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查是否是项目成员
        if (!projectMemberService.isMember(projectId, currentUserId)) {
            return Result.error("无权限查看统计");
        }

        Map<String, Long> stats = projectMemberService.getMemberStatistics(projectId);
        return Result.success(stats);
    }

    @Operation(summary = "转移项目所有权")
    @PostMapping("/transfer-ownership")
    public Result<Void> transferOwnership(
            @PathVariable Long projectId,
            @RequestParam Long newOwnerId,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限：只有当前所有者可以转移所有权
        ProjectMember currentMember = projectMemberService.getMember(projectId, currentUserId);
        if (currentMember == null || !"owner".equals(currentMember.getRole())) {
            return Result.error("只有项目所有者可以转移所有权");
        }

        projectMemberService.transferOwnership(projectId, newOwnerId, currentUserId);
        return Result.success("所有权转移成功");
    }

    @Operation(summary = "批量添加成员")
    @PostMapping("/bulk-add")
    public Result<Void> bulkAddMembers(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限
        if (!projectMemberService.hasPermission(projectId, currentUserId, "manage_members") &&
            !userService.isAdmin(currentUserId)) {
            return Result.error("无权限添加成员");
        }

        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) request.get("userIds");
        String role = (String) request.getOrDefault("role", "member");

        projectMemberService.bulkAddMembers(projectId, userIds, role, currentUserId);
        return Result.success("批量添加成功");
    }

    @Operation(summary = "批量移除成员")
    @PostMapping("/bulk-remove")
    public Result<Void> bulkRemoveMembers(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return Result.error("未授权访问");
        }

        // 检查权限
        if (!projectMemberService.hasPermission(projectId, currentUserId, "manage_members") &&
            !userService.isAdmin(currentUserId)) {
            return Result.error("无权限移除成员");
        }

        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) request.get("userIds");

        projectMemberService.bulkRemoveMembers(projectId, userIds);
        return Result.success("批量移除成功");
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
}