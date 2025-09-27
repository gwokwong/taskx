package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.User;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;

    @Operation(summary = "获取所有用户(管理员)")
    @GetMapping
    public Result<List<User>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<User> users = userService.getAllUsers(page, size);
        return Result.success(users);
    }

    @Operation(summary = "获取用户详情(管理员)")
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(user);
    }

    @Operation(summary = "更新用户信息(管理员)")
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        user.setUserid(id);
        User updatedUser = userService.updateUser(user);
        return Result.success(updatedUser);
    }

    @Operation(summary = "删除用户(管理员)")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "重置用户密码(管理员)")
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam(defaultValue = "123456") String defaultPassword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        userService.resetUserPassword(id, defaultPassword);
        return Result.success("密码重置成功");
    }

    @Operation(summary = "修改用户状态(管理员)")
    @PostMapping("/{id}/status")
    public Result<Void> changeUserStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        userService.changeUserStatus(id, status);
        return Result.success("状态修改成功");
    }

    @Operation(summary = "分配角色(管理员)")
    @PostMapping("/{id}/assign-role")
    public Result<Void> assignRole(
            @PathVariable Long id,
            @RequestParam String role,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        userService.assignRole(id, role);
        return Result.success("角色分配成功");
    }

    @Operation(summary = "移除角色(管理员)")
    @PostMapping("/{id}/remove-role")
    public Result<Void> removeRole(
            @PathVariable Long id,
            @RequestParam String role,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        userService.removeRole(id, role);
        return Result.success("角色移除成功");
    }

    @Operation(summary = "分配部门(管理员)")
    @PostMapping("/{id}/assign-department")
    public Result<Void> assignDepartment(
            @PathVariable Long id,
            @RequestParam Long departmentId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        userService.assignDepartment(id, departmentId);
        return Result.success("部门分配成功");
    }

    @Operation(summary = "获取用户统计数据(管理员)")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        Map<String, Object> stats = userService.getUserStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取按部门统计的用户数量(管理员)")
    @GetMapping("/department-stats")
    public Result<Map<String, Long>> getUserCountByDepartment(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        Map<String, Long> stats = userService.getUserCountByDepartment();
        return Result.success(stats);
    }

    @Operation(summary = "获取用户注册统计(管理员)")
    @GetMapping("/registration-stats")
    public Result<List<Map<String, Object>>> getUserRegistrationStats(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<Map<String, Object>> stats = userService.getUserRegistrationStats(days);
        return Result.success(stats);
    }

    @Operation(summary = "获取活跃用户列表(管理员)")
    @GetMapping("/active")
    public Result<List<User>> getActiveUsers(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<User> users = userService.getActiveUsers();
        return Result.success(users);
    }

    @Operation(summary = "按角色获取用户(管理员)")
    @GetMapping("/by-role")
    public Result<List<User>> getUsersByRole(@RequestParam String role, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<User> users = userService.getUsersByRole(role);
        return Result.success(users);
    }

    @Operation(summary = "按部门获取用户(管理员)")
    @GetMapping("/by-department")
    public Result<List<User>> getUsersByDepartment(@RequestParam Long departmentId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<User> users = userService.getUsersByDepartment(departmentId);
        return Result.success(users);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            User user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }
}