package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.Department;
import com.dootask.backend.service.DepartmentService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "部门管理", description = "部门管理相关接口")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;

    @Operation(summary = "创建部门")
    @PostMapping
    public Result<Department> createDepartment(@RequestBody Department department, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        Department created = departmentService.create(department);
        return Result.success(created);
    }

    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    public Result<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        department.setId(id);
        Department updated = departmentService.update(department);
        return Result.success(updated);
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        departmentService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取部门详情")
    @GetMapping("/{id}")
    public Result<Department> getDepartment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Department department = departmentService.getById(id);
        if (department == null) {
            return Result.error("部门不存在");
        }

        return Result.success(department);
    }

    @Operation(summary = "获取所有部门")
    @GetMapping
    public Result<List<Department>> getAllDepartments(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Department> departments = departmentService.getAllDepartments();
        return Result.success(departments);
    }

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Department> tree = departmentService.getDepartmentTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取子部门")
    @GetMapping("/{id}/children")
    public Result<List<Department>> getChildDepartments(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Department> children = departmentService.getChildDepartments(id);
        return Result.success(children);
    }

    @Operation(summary = "搜索部门")
    @GetMapping("/search")
    public Result<List<Department>> searchDepartments(@RequestParam String keyword, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Department> departments = departmentService.searchDepartments(keyword);
        return Result.success(departments);
    }

    @Operation(summary = "移动部门")
    @PostMapping("/{id}/move")
    public Result<Void> moveDepartment(
            @PathVariable Long id,
            @RequestParam Long newParentId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        departmentService.moveDepartment(id, newParentId);
        return Result.success("移动成功");
    }

    @Operation(summary = "分配部门经理")
    @PostMapping("/{id}/assign-manager")
    public Result<Void> assignManager(
            @PathVariable Long id,
            @RequestParam Long managerId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        departmentService.assignManager(id, managerId);
        return Result.success("分配成功");
    }

    @Operation(summary = "获取部门统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDepartmentStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        Map<String, Object> stats = departmentService.getDepartmentStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取部门用户数量统计")
    @GetMapping("/user-count")
    public Result<List<Map<String, Object>>> getDepartmentUserCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<Map<String, Object>> stats = departmentService.getDepartmentUserCount();
        return Result.success(stats);
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