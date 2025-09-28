package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.Project;
import com.dootask.backend.service.ProjectService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import com.dootask.backend.entity.User;
import com.dootask.backend.entity.ProjectUser;
import com.dootask.backend.entity.ProjectTask;
import com.dootask.backend.entity.ProjectColumn;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "项目管理", description = "项目相关接口")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    @Operation(summary = "获取项目列表")
    @GetMapping
    public Result<List<Project>> getProjects(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Project> projects = projectService.getProjectsByUser(userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<Project> getProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Project project = projectService.getProjectById(id, userId);
        return Result.success(project);
    }

    @Operation(summary = "创建项目")
    @PostMapping
    public Result<Project> createProject(@Valid @RequestBody CreateProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        Project project = new Project();
        project.setName(request.getName());
        project.setDesc(request.getDesc());

        Project createdProject = projectService.createProject(project, userId);
        return Result.success("创建成功", createdProject);
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public Result<Project> updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        Project project = new Project();
        project.setName(request.getName());
        project.setDesc(request.getDesc());

        Project updatedProject = projectService.updateProject(id, project, userId);
        return Result.success("更新成功", updatedProject);
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<String> deleteProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.deleteProject(id, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "置顶项目")
    @PostMapping("/{id}/top")
    public Result<String> topProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.topProject(id, userId);
        return Result.success("操作成功");
    }

    @Operation(summary = "归档项目")
    @PostMapping("/{id}/archive")
    public Result<String> archiveProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.archiveProject(id, userId);
        return Result.success("归档成功");
    }

    @Operation(summary = "添加项目成员")
    @PostMapping("/{id}/members")
    public Result<String> addProjectMember(@PathVariable Long id, @RequestBody AddMemberRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.addMember(id, request.getUserIds(), request.getType(), userId);
        return Result.success("添加成员成功");
    }

    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{id}/members/{memberId}")
    public Result<String> removeProjectMember(@PathVariable Long id, @PathVariable Long memberId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.removeMember(id, memberId, userId);
        return Result.success("移除成员成功");
    }

    @Operation(summary = "获取项目成员列表")
    @GetMapping("/{id}/members")
    public Result<List<ProjectUser>> getProjectMembers(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectUser> members = projectService.getMembers(id, userId);
        return Result.success(members);
    }

    @Operation(summary = "更新成员权限")
    @PutMapping("/{id}/members/{memberId}/permission")
    public Result<String> updateMemberPermission(@PathVariable Long id, @PathVariable Long memberId, @RequestBody UpdatePermissionRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.updateMemberPermission(id, memberId, request.getType(), userId);
        return Result.success("权限更新成功");
    }

    @Operation(summary = "获取项目任务列表")
    @GetMapping("/{id}/tasks")
    public Result<Page<ProjectTask>> getProjectTasks(@PathVariable Long id, Pageable pageable, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<ProjectTask> tasks = projectService.getTasks(id, pageable, userId);
        return Result.success(tasks);
    }

    @Operation(summary = "创建项目任务")
    @PostMapping("/{id}/tasks")
    public Result<ProjectTask> createProjectTask(@PathVariable Long id, @RequestBody CreateTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ProjectTask task = projectService.createTask(id, request, userId);
        return Result.success("任务创建成功", task);
    }

    @Operation(summary = "获取项目统计信息")
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getProjectStatistics(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> statistics = projectService.getStatistics(id, userId);
        return Result.success(statistics);
    }

    @Operation(summary = "复制项目")
    @PostMapping("/{id}/copy")
    public Result<Project> copyProject(@PathVariable Long id, @RequestBody CopyProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Project newProject = projectService.copyProject(id, request.getName(), request.isCopyTasks(), userId);
        return Result.success("项目复制成功", newProject);
    }

    @Operation(summary = "项目移交")
    @PostMapping("/{id}/transfer")
    public Result<String> transferProject(@PathVariable Long id, @RequestBody TransferProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.transferProject(id, request.getNewOwnerId(), userId);
        return Result.success("项目移交成功");
    }

    @Operation(summary = "获取项目看板列")
    @GetMapping("/{id}/columns")
    public Result<List<ProjectColumn>> getProjectColumns(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectColumn> columns = projectService.getColumns(id, userId);
        return Result.success(columns);
    }

    @Operation(summary = "创建项目看板列")
    @PostMapping("/{id}/columns")
    public Result<ProjectColumn> createProjectColumn(@PathVariable Long id, @RequestBody CreateColumnRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ProjectColumn column = projectService.createColumn(id, request.getName(), request.getColor(), userId);
        return Result.success("看板列创建成功", column);
    }

    @Operation(summary = "更新项目看板列")
    @PutMapping("/{id}/columns/{columnId}")
    public Result<ProjectColumn> updateProjectColumn(@PathVariable Long id, @PathVariable Long columnId, @RequestBody UpdateColumnRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ProjectColumn column = projectService.updateColumn(id, columnId, request.getName(), request.getColor(), userId);
        return Result.success("看板列更新成功", column);
    }

    @Operation(summary = "删除项目看板列")
    @DeleteMapping("/{id}/columns/{columnId}")
    public Result<String> deleteProjectColumn(@PathVariable Long id, @PathVariable Long columnId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.deleteColumn(id, columnId, userId);
        return Result.success("看板列删除成功");
    }

    @Operation(summary = "搜索项目")
    @GetMapping("/search")
    public Result<List<Project>> searchProjects(@RequestParam String keyword, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Project> projects = projectService.searchProjects(keyword, userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取最近访问的项目")
    @GetMapping("/recent")
    public Result<List<Project>> getRecentProjects(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Project> projects = projectService.getRecentProjects(userId);
        return Result.success(projects);
    }

    @Operation(summary = "获取收藏的项目")
    @GetMapping("/favorites")
    public Result<List<Project>> getFavoriteProjects(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Project> projects = projectService.getFavoriteProjects(userId);
        return Result.success(projects);
    }

    @Operation(summary = "收藏/取消收藏项目")
    @PostMapping("/{id}/favorite")
    public Result<String> toggleFavoriteProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        boolean isFavorite = projectService.toggleFavorite(id, userId);
        return Result.success(isFavorite ? "收藏成功" : "取消收藏成功");
    }

    @Operation(summary = "获取项目模板")
    @GetMapping("/templates")
    public Result<List<Project>> getProjectTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Project> templates = projectService.getTemplates(userId);
        return Result.success(templates);
    }

    @Operation(summary = "从模板创建项目")
    @PostMapping("/from-template")
    public Result<Project> createProjectFromTemplate(@RequestBody CreateFromTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Project project = projectService.createFromTemplate(request.getTemplateId(), request.getName(), request.getDesc(), userId);
        return Result.success("项目创建成功", project);
    }

    @Operation(summary = "邀请成员加入项目")
    @PostMapping("/{id}/invite")
    public Result<String> inviteMembers(@PathVariable Long id, @RequestBody InviteMemberRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.inviteMembers(id, request.getEmails(), request.getType(), request.getMessage(), userId);
        return Result.success("邀请发送成功");
    }

    @Operation(summary = "接受项目邀请")
    @PostMapping("/invitations/{inviteCode}/accept")
    public Result<String> acceptInvitation(@PathVariable String inviteCode, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.acceptInvitation(inviteCode, userId);
        return Result.success("邀请接受成功");
    }

    @Operation(summary = "拒绝项目邀请")
    @PostMapping("/invitations/{inviteCode}/decline")
    public Result<String> declineInvitation(@PathVariable String inviteCode, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.declineInvitation(inviteCode, userId);
        return Result.success("邀请已拒绝");
    }

    @Operation(summary = "获取我的邀请列表")
    @GetMapping("/invitations")
    public Result<List<Map<String, Object>>> getMyInvitations(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Map<String, Object>> invitations = projectService.getMyInvitations(userId);
        return Result.success(invitations);
    }

    @Operation(summary = "设置项目权限配置")
    @PostMapping("/{id}/permissions")
    public Result<String> setProjectPermissions(@PathVariable Long id, @RequestBody SetPermissionsRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.setProjectPermissions(id, request.getPermissions(), userId);
        return Result.success("权限配置成功");
    }

    @Operation(summary = "获取项目权限配置")
    @GetMapping("/{id}/permissions")
    public Result<Map<String, Object>> getProjectPermissions(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> permissions = projectService.getProjectPermissions(id, userId);
        return Result.success(permissions);
    }

    @Operation(summary = "项目活动日志")
    @GetMapping("/{id}/activities")
    public Result<Page<Map<String, Object>>> getProjectActivities(@PathVariable Long id, Pageable pageable, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<Map<String, Object>> activities = projectService.getProjectActivities(id, pageable, userId);
        return Result.success(activities);
    }

    @Operation(summary = "导出项目数据")
    @GetMapping("/{id}/export")
    public Result<String> exportProject(@PathVariable Long id, @RequestParam(required = false) String format, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String downloadUrl = projectService.exportProject(id, format != null ? format : "json", userId);
        return Result.success("导出任务已创建", downloadUrl);
    }

    @Operation(summary = "批量操作项目")
    @PostMapping("/batch")
    public Result<String> batchOperateProjects(@RequestBody BatchOperateProjectRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.batchOperate(request.getProjectIds(), request.getOperation(), request.getParams(), userId);
        return Result.success("批量操作成功");
    }

    @Operation(summary = "设置项目标签")
    @PostMapping("/{id}/tags")
    public Result<String> setProjectTags(@PathVariable Long id, @RequestBody SetProjectTagsRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.setProjectTags(id, request.getTagIds(), userId);
        return Result.success("标签设置成功");
    }

    @Operation(summary = "获取项目进度报告")
    @GetMapping("/{id}/progress-report")
    public Result<Map<String, Object>> getProjectProgressReport(@PathVariable Long id, @RequestParam(required = false) String period, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> report = projectService.getProgressReport(id, period != null ? period : "week", userId);
        return Result.success(report);
    }

    @Operation(summary = "项目通知设置")
    @PostMapping("/{id}/notification-settings")
    public Result<String> setNotificationSettings(@PathVariable Long id, @RequestBody NotificationSettingsRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectService.setNotificationSettings(id, request.getSettings(), userId);
        return Result.success("通知设置成功");
    }

    @Operation(summary = "获取项目通知设置")
    @GetMapping("/{id}/notification-settings")
    public Result<Map<String, Object>> getNotificationSettings(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> settings = projectService.getNotificationSettings(id, userId);
        return Result.success(settings);
    }

    @Operation(summary = "获取项目看板视图数据")
    @GetMapping("/{id}/board")
    public Result<Map<String, Object>> getProjectBoard(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> boardData = projectService.getProjectBoard(id, userId);
        return Result.success(boardData);
    }

    @Operation(summary = "获取项目甘特图数据")
    @GetMapping("/{id}/gantt")
    public Result<Map<String, Object>> getProjectGantt(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> ganttData = projectService.getProjectGantt(id, userId);
        return Result.success(ganttData);
    }

    @Operation(summary = "项目归档恢复")
    @PostMapping("/{id}/restore")
    public Result<String> restoreProject(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectService.restoreProject(id, userId);
        return Result.success("项目恢复成功");
    }

    @Operation(summary = "获取项目时间统计")
    @GetMapping("/{id}/time-statistics")
    public Result<Map<String, Object>> getProjectTimeStatistics(@PathVariable Long id, @RequestParam(required = false) String period, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> timeStats = projectService.getTimeStatistics(id, period != null ? period : "month", userId);
        return Result.success(timeStats);
    }

    @Operation(summary = "设置项目里程碑")
    @PostMapping("/{id}/milestones")
    public Result<Map<String, Object>> createMilestone(@PathVariable Long id, @RequestBody CreateMilestoneRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Map<String, Object> milestone = projectService.createMilestone(id, request.getName(), request.getDescription(), request.getDueDate(), userId);
        return Result.success("里程碑创建成功", milestone);
    }

    @Operation(summary = "获取项目里程碑列表")
    @GetMapping("/{id}/milestones")
    public Result<List<Map<String, Object>>> getProjectMilestones(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Map<String, Object>> milestones = projectService.getMilestones(id, userId);
        return Result.success(milestones);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        // 从JWT token中获取用户ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }

    public static class CreateProjectRequest {
        @NotBlank(message = "项目名称不能为空")
        private String name;

        private String desc;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    public static class UpdateProjectRequest {
        @NotBlank(message = "项目名称不能为空")
        private String name;

        private String desc;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    public static class AddMemberRequest {
        private List<Long> userIds;
        private String type = "member"; // member, admin

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class UpdatePermissionRequest {
        private String type; // member, admin

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class CreateTaskRequest {
        @NotBlank(message = "任务名称不能为空")
        private String name;
        private String content;
        private Long columnId;
        private List<Long> assigneeIds;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private String priority = "normal";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getColumnId() { return columnId; }
        public void setColumnId(Long columnId) { this.columnId = columnId; }
        public List<Long> getAssigneeIds() { return assigneeIds; }
        public void setAssigneeIds(List<Long> assigneeIds) { this.assigneeIds = assigneeIds; }
        public LocalDateTime getStartAt() { return startAt; }
        public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
        public LocalDateTime getEndAt() { return endAt; }
        public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class CopyProjectRequest {
        @NotBlank(message = "项目名称不能为空")
        private String name;
        private boolean copyTasks = false;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isCopyTasks() { return copyTasks; }
        public void setCopyTasks(boolean copyTasks) { this.copyTasks = copyTasks; }
    }

    public static class TransferProjectRequest {
        private Long newOwnerId;

        public Long getNewOwnerId() { return newOwnerId; }
        public void setNewOwnerId(Long newOwnerId) { this.newOwnerId = newOwnerId; }
    }

    public static class CreateColumnRequest {
        @NotBlank(message = "列名称不能为空")
        private String name;
        private String color = "#1890ff";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class UpdateColumnRequest {
        @NotBlank(message = "列名称不能为空")
        private String name;
        private String color;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class CreateFromTemplateRequest {
        private Long templateId;
        @NotBlank(message = "项目名称不能为空")
        private String name;
        private String desc;

        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    public static class InviteMemberRequest {
        private List<String> emails;
        private String type = "member";
        private String message;

        public List<String> getEmails() { return emails; }
        public void setEmails(List<String> emails) { this.emails = emails; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class SetPermissionsRequest {
        private Map<String, Object> permissions;

        public Map<String, Object> getPermissions() { return permissions; }
        public void setPermissions(Map<String, Object> permissions) { this.permissions = permissions; }
    }

    public static class BatchOperateProjectRequest {
        private List<Long> projectIds;
        private String operation; // archive, delete, transfer, set_visibility
        private Map<String, Object> params;

        public List<Long> getProjectIds() { return projectIds; }
        public void setProjectIds(List<Long> projectIds) { this.projectIds = projectIds; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) { this.params = params; }
    }

    public static class SetProjectTagsRequest {
        private List<Long> tagIds;

        public List<Long> getTagIds() { return tagIds; }
        public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    }

    public static class NotificationSettingsRequest {
        private Map<String, Object> settings;

        public Map<String, Object> getSettings() { return settings; }
        public void setSettings(Map<String, Object> settings) { this.settings = settings; }
    }

    public static class CreateMilestoneRequest {
        @NotBlank(message = "里程碑名称不能为空")
        private String name;
        private String description;
        private LocalDateTime dueDate;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    }
}