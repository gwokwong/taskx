package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.TaskTemplate;
import com.dootask.backend.service.TaskTemplateService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "任务模板", description = "任务模板管理相关接口")
@RestController
@RequestMapping("/api/task-templates")
@RequiredArgsConstructor
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;
    private final UserService userService;

    @Operation(summary = "创建任务模板")
    @PostMapping
    public Result<TaskTemplate> createTemplate(@RequestBody TaskTemplate template, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        template.setCreatedBy(userId);
        TaskTemplate created = taskTemplateService.create(template);
        return Result.success(created);
    }

    @Operation(summary = "更新任务模板")
    @PutMapping("/{id}")
    public Result<TaskTemplate> updateTemplate(@PathVariable Long id, @RequestBody TaskTemplate template, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate existing = taskTemplateService.getById(id);
        if (existing == null) {
            return Result.error("模板不存在");
        }

        // 检查权限：只能编辑自己创建的模板
        if (!existing.getCreatedBy().equals(userId) && !userService.isAdmin(userId)) {
            return Result.error("无权限编辑此模板");
        }

        template.setId(id);
        template.setCreatedBy(existing.getCreatedBy()); // 保持原创建者
        TaskTemplate updated = taskTemplateService.update(template);
        return Result.success(updated);
    }

    @Operation(summary = "删除任务模板")
    @DeleteMapping("/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate template = taskTemplateService.getById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }

        // 检查权限：只能删除自己创建的模板
        if (!template.getCreatedBy().equals(userId) && !userService.isAdmin(userId)) {
            return Result.error("无权限删除此模板");
        }

        taskTemplateService.delete(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<TaskTemplate> getTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate template = taskTemplateService.getById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }

        // 检查权限：只能查看公开模板或自己的模板
        if (!template.getIsPublic() && !template.getCreatedBy().equals(userId)) {
            return Result.error("无权限查看此模板");
        }

        return Result.success(template);
    }

    @Operation(summary = "获取所有可用模板")
    @GetMapping
    public Result<List<TaskTemplate>> getAllTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.getAllTemplates(userId);
        return Result.success(templates);
    }

    @Operation(summary = "获取公开模板")
    @GetMapping("/public")
    public Result<List<TaskTemplate>> getPublicTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.getPublicTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "获取我的模板")
    @GetMapping("/my")
    public Result<List<TaskTemplate>> getMyTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.getUserTemplates(userId);
        return Result.success(templates);
    }

    @Operation(summary = "按分类获取模板")
    @GetMapping("/category/{category}")
    public Result<List<TaskTemplate>> getTemplatesByCategory(@PathVariable String category, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.getTemplatesByCategory(category);
        return Result.success(templates);
    }

    @Operation(summary = "搜索模板")
    @GetMapping("/search")
    public Result<List<TaskTemplate>> searchTemplates(@RequestParam String keyword, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.searchTemplates(keyword, userId);
        return Result.success(templates);
    }

    @Operation(summary = "复制模板")
    @PostMapping("/{id}/duplicate")
    public Result<TaskTemplate> duplicateTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate duplicated = taskTemplateService.duplicateTemplate(id, userId);
        return Result.success(duplicated);
    }

    @Operation(summary = "使用模板")
    @PostMapping("/{id}/use")
    public Result<String> useTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate template = taskTemplateService.getById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }

        // 检查权限：只能使用公开模板或自己的模板
        if (!template.getIsPublic() && !template.getCreatedBy().equals(userId)) {
            return Result.error("无权限使用此模板");
        }

        taskTemplateService.useTemplate(id);
        return Result.success("使用成功");
    }

    @Operation(summary = "分享/取消分享模板")
    @PostMapping("/{id}/share")
    public Result<String> shareTemplate(@PathVariable Long id, @RequestParam Boolean isPublic, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate template = taskTemplateService.getById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }

        // 检查权限：只能分享自己创建的模板
        if (!template.getCreatedBy().equals(userId)) {
            return Result.error("无权限分享此模板");
        }

        taskTemplateService.shareTemplate(id, isPublic);
        return Result.success(isPublic ? "分享成功" : "取消分享成功");
    }

    @Operation(summary = "获取模板统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getTemplateStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        Map<String, Object> stats = taskTemplateService.getTemplateStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取热门模板")
    @GetMapping("/popular")
    public Result<List<TaskTemplate>> getPopularTemplates(
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.getPopularTemplates(limit);
        return Result.success(templates);
    }

    @Operation(summary = "获取模板分类")
    @GetMapping("/categories")
    public Result<List<String>> getTemplateCategories(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<String> categories = taskTemplateService.getTemplateCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取最近模板")
    @GetMapping("/recent")
    public Result<List<TaskTemplate>> getRecentTemplates(
            @RequestParam(defaultValue = "5") Integer limit,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<TaskTemplate> templates = taskTemplateService.getRecentTemplates(userId, limit);
        return Result.success(templates);
    }

    @Operation(summary = "从任务创建模板")
    @PostMapping("/from-task")
    public Result<TaskTemplate> createFromTask(
            @RequestParam Long taskId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        TaskTemplate template = taskTemplateService.createFromTask(taskId, name, description, userId);
        return Result.success(template);
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