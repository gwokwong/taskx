package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.ProjectTemplate;
import com.dootask.backend.entity.Project;
import com.dootask.backend.service.ProjectTemplateService;
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

@Tag(name = "项目模板管理", description = "项目模板相关接口")
@RestController
@RequestMapping("/api/project-templates")
@RequiredArgsConstructor
public class ProjectTemplateController {

    private final ProjectTemplateService projectTemplateService;
    private final UserService userService;

    @Operation(summary = "获取公开模板列表")
    @GetMapping("/public")
    public Result<List<ProjectTemplate>> getPublicTemplates() {
        List<ProjectTemplate> templates = projectTemplateService.getPublicTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "获取我的模板")
    @GetMapping("/my")
    public Result<List<ProjectTemplate>> getMyTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<ProjectTemplate> templates = projectTemplateService.getMyTemplates(userId);
        return Result.success(templates);
    }

    @Operation(summary = "获取系统模板")
    @GetMapping("/system")
    public Result<List<ProjectTemplate>> getSystemTemplates() {
        List<ProjectTemplate> templates = projectTemplateService.getSystemTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "根据分类获取模板")
    @GetMapping("/category/{category}")
    public Result<List<ProjectTemplate>> getTemplatesByCategory(@PathVariable String category) {
        List<ProjectTemplate> templates = projectTemplateService.getTemplatesByCategory(category);
        return Result.success(templates);
    }

    @Operation(summary = "搜索模板")
    @GetMapping("/search")
    public Result<List<ProjectTemplate>> searchTemplates(
            @RequestParam String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags) {

        List<ProjectTemplate> templates = projectTemplateService.searchTemplates(keyword, category, tags);
        return Result.success(templates);
    }

    @Operation(summary = "获取推荐模板")
    @GetMapping("/recommended")
    public Result<List<ProjectTemplate>> getRecommendedTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTemplate> templates = projectTemplateService.getRecommendedTemplates(userId);
        return Result.success(templates);
    }

    @Operation(summary = "获取热门模板")
    @GetMapping("/popular")
    public Result<List<ProjectTemplate>> getPopularTemplates(@RequestParam(required = false) Integer limit) {
        List<ProjectTemplate> templates = projectTemplateService.getPopularTemplates(limit != null ? limit : 10);
        return Result.success(templates);
    }

    @Operation(summary = "获取精选模板")
    @GetMapping("/featured")
    public Result<List<ProjectTemplate>> getFeaturedTemplates() {
        List<ProjectTemplate> templates = projectTemplateService.getFeaturedTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<ProjectTemplate> getTemplate(@PathVariable Long id) {
        ProjectTemplate template = projectTemplateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(template);
    }

    @Operation(summary = "预览模板")
    @GetMapping("/{id}/preview")
    public Result<Map<String, Object>> previewTemplate(@PathVariable Long id) {
        Map<String, Object> preview = projectTemplateService.previewTemplate(id);
        return Result.success(preview);
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public Result<ProjectTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        ProjectTemplate template = new ProjectTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setTags(request.getTags());
        template.setProjectConfig(request.getProjectConfig());
        template.setColumnsConfig(request.getColumnsConfig());
        template.setTasksTemplate(request.getTasksTemplate());
        template.setRolesConfig(request.getRolesConfig());
        template.setWorkflowConfig(request.getWorkflowConfig());
        template.setPermissionsConfig(request.getPermissionsConfig());
        template.setIsPublic(request.getIsPublic());
        template.setIcon(request.getIcon());
        template.setColor(request.getColor());

        ProjectTemplate createdTemplate = projectTemplateService.createTemplate(template, userId);
        return Result.success("模板创建成功", createdTemplate);
    }

    @Operation(summary = "从项目保存为模板")
    @PostMapping("/save-from-project")
    public Result<String> saveProjectAsTemplate(@RequestBody SaveAsTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        projectTemplateService.saveProjectAsTemplate(request.getProjectId(), request.getTemplateName(), request.getTemplateDesc(), userId);
        return Result.success("模板保存成功");
    }

    @Operation(summary = "从模板创建项目")
    @PostMapping("/{id}/create-project")
    public Result<Project> createProjectFromTemplate(@PathVariable Long id, @RequestBody CreateProjectFromTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Project project = projectTemplateService.createProjectFromTemplate(id, request.getProjectName(), request.getProjectDesc(), userId);
        return Result.success("项目创建成功", project);
    }

    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    public Result<ProjectTemplate> updateTemplate(@PathVariable Long id, @Valid @RequestBody UpdateTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        ProjectTemplate template = new ProjectTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setTags(request.getTags());
        template.setProjectConfig(request.getProjectConfig());
        template.setColumnsConfig(request.getColumnsConfig());
        template.setTasksTemplate(request.getTasksTemplate());
        template.setRolesConfig(request.getRolesConfig());
        template.setWorkflowConfig(request.getWorkflowConfig());
        template.setPermissionsConfig(request.getPermissionsConfig());
        template.setIsPublic(request.getIsPublic());
        template.setIcon(request.getIcon());
        template.setColor(request.getColor());

        ProjectTemplate updatedTemplate = projectTemplateService.updateTemplate(id, template, userId);
        return Result.success("模板更新成功", updatedTemplate);
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        projectTemplateService.deleteTemplate(id, userId);
        return Result.success("模板删除成功");
    }

    @Operation(summary = "复制模板")
    @PostMapping("/{id}/copy")
    public Result<String> copyTemplate(@PathVariable Long id, @RequestBody CopyTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        projectTemplateService.copyTemplate(id, request.getNewName(), request.getIsPublic(), userId);
        return Result.success("模板复制成功");
    }

    @Operation(summary = "分叉模板")
    @PostMapping("/{id}/fork")
    public Result<String> forkTemplate(@PathVariable Long id, @RequestBody ForkTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        projectTemplateService.forkTemplate(id, request.getNewName(), userId);
        return Result.success("模板分叉成功");
    }

    @Operation(summary = "评价模板")
    @PostMapping("/{id}/rate")
    public Result<String> rateTemplate(@PathVariable Long id, @RequestBody RateTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        projectTemplateService.rateTemplate(id, request.getRating(), userId);
        return Result.success("评价成功");
    }

    @Operation(summary = "分享模板")
    @PostMapping("/{id}/share")
    public Result<String> shareTemplate(@PathVariable Long id, @RequestBody ShareTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        String shareUrl = projectTemplateService.shareTemplate(id, request.getPermissions(), userId);
        return Result.success("分享成功", shareUrl);
    }

    @Operation(summary = "导出模板")
    @GetMapping("/{id}/export")
    public Result<String> exportTemplate(@PathVariable Long id, @RequestParam(required = false) String format) {
        String exportData = projectTemplateService.exportTemplate(id, format != null ? format : "json");
        return Result.success("导出成功", exportData);
    }

    @Operation(summary = "导入模板")
    @PostMapping("/import")
    public Result<ProjectTemplate> importTemplate(@RequestBody ImportTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        ProjectTemplate template = projectTemplateService.importTemplate(request.getTemplateData(), request.getFormat(), userId);
        return Result.success("导入成功", template);
    }

    @Operation(summary = "获取模板统计")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getTemplateStatistics(@PathVariable Long id) {
        Map<String, Object> statistics = projectTemplateService.getTemplateStatistics(id);
        return Result.success(statistics);
    }

    @Operation(summary = "获取模板分类列表")
    @GetMapping("/categories")
    public Result<List<String>> getTemplateCategories() {
        List<String> categories = projectTemplateService.getTemplateCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取模板标签列表")
    @GetMapping("/tags")
    public Result<List<String>> getTemplateTags() {
        List<String> tags = projectTemplateService.getTemplateTags();
        return Result.success(tags);
    }

    @Operation(summary = "获取热门标签")
    @GetMapping("/tags/popular")
    public Result<List<String>> getPopularTags() {
        List<String> tags = projectTemplateService.getPopularTags();
        return Result.success(tags);
    }

    @Operation(summary = "验证模板")
    @PostMapping("/{id}/validate")
    public Result<Map<String, Object>> validateTemplate(@PathVariable Long id) {
        ProjectTemplate template = projectTemplateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }

        Map<String, Object> validation = projectTemplateService.validateTemplate(template);
        return Result.success(validation);
    }

    @Operation(summary = "提交模板审核")
    @PostMapping("/{id}/submit-review")
    public Result<String> submitTemplateForReview(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        projectTemplateService.submitTemplateForReview(id);
        return Result.success("提交审核成功");
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

    public static class CreateTemplateRequest {
        @NotBlank(message = "模板名称不能为空")
        private String name;
        private String description;
        private String category;
        private String tags;
        private String projectConfig;
        private String columnsConfig;
        private String tasksTemplate;
        private String rolesConfig;
        private String workflowConfig;
        private String permissionsConfig;
        private Boolean isPublic = false;
        private String icon;
        private String color;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        public String getProjectConfig() { return projectConfig; }
        public void setProjectConfig(String projectConfig) { this.projectConfig = projectConfig; }
        public String getColumnsConfig() { return columnsConfig; }
        public void setColumnsConfig(String columnsConfig) { this.columnsConfig = columnsConfig; }
        public String getTasksTemplate() { return tasksTemplate; }
        public void setTasksTemplate(String tasksTemplate) { this.tasksTemplate = tasksTemplate; }
        public String getRolesConfig() { return rolesConfig; }
        public void setRolesConfig(String rolesConfig) { this.rolesConfig = rolesConfig; }
        public String getWorkflowConfig() { return workflowConfig; }
        public void setWorkflowConfig(String workflowConfig) { this.workflowConfig = workflowConfig; }
        public String getPermissionsConfig() { return permissionsConfig; }
        public void setPermissionsConfig(String permissionsConfig) { this.permissionsConfig = permissionsConfig; }
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class UpdateTemplateRequest extends CreateTemplateRequest {
    }

    public static class SaveAsTemplateRequest {
        @NotNull(message = "项目ID不能为空")
        private Long projectId;
        @NotBlank(message = "模板名称不能为空")
        private String templateName;
        private String templateDesc;

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getTemplateDesc() { return templateDesc; }
        public void setTemplateDesc(String templateDesc) { this.templateDesc = templateDesc; }
    }

    public static class CreateProjectFromTemplateRequest {
        @NotBlank(message = "项目名称不能为空")
        private String projectName;
        private String projectDesc;

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getProjectDesc() { return projectDesc; }
        public void setProjectDesc(String projectDesc) { this.projectDesc = projectDesc; }
    }

    public static class CopyTemplateRequest {
        @NotBlank(message = "新模板名称不能为空")
        private String newName;
        private Boolean isPublic = false;

        public String getNewName() { return newName; }
        public void setNewName(String newName) { this.newName = newName; }
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    }

    public static class ForkTemplateRequest {
        @NotBlank(message = "新模板名称不能为空")
        private String newName;

        public String getNewName() { return newName; }
        public void setNewName(String newName) { this.newName = newName; }
    }

    public static class RateTemplateRequest {
        @NotNull(message = "评分不能为空")
        private Double rating;

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
    }

    public static class ShareTemplateRequest {
        private List<String> permissions;

        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    }

    public static class ImportTemplateRequest {
        @NotBlank(message = "模板数据不能为空")
        private String templateData;
        private String format = "json";

        public String getTemplateData() { return templateData; }
        public void setTemplateData(String templateData) { this.templateData = templateData; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
    }
}