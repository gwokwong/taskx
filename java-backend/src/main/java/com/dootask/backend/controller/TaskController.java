package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.ProjectTask;
import com.dootask.backend.service.ProjectTaskService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "任务管理", description = "任务相关接口")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ProjectTaskService projectTaskService;
    private final UserService userService;

    @Operation(summary = "获取任务列表")
    @GetMapping
    public Result<List<ProjectTask>> getTasks(@RequestParam(required = false) Long projectId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTask> tasks;

        if (projectId != null) {
            tasks = projectTaskService.getTasksByProject(projectId, userId);
        } else {
            // 获取用户所有任务
            tasks = projectTaskService.list();
        }

        return Result.success(tasks);
    }

    @Operation(summary = "根据项目获取任务")
    @GetMapping("/project/{projectId}")
    public Result<List<ProjectTask>> getTasksByProject(@PathVariable Long projectId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTask> tasks = projectTaskService.getTasksByProject(projectId, userId);
        return Result.success(tasks);
    }

    @Operation(summary = "根据列表获取任务")
    @GetMapping("/column/{columnId}")
    public Result<List<ProjectTask>> getTasksByColumn(@PathVariable Long columnId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTask> tasks = projectTaskService.getTasksByColumn(columnId, userId);
        return Result.success(tasks);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<ProjectTask> getTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ProjectTask task = projectTaskService.getTaskById(id, userId);
        return Result.success(task);
    }

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<ProjectTask> createTask(@Valid @RequestBody CreateTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        ProjectTask task = new ProjectTask();
        task.setProjectId(request.getProjectId());
        task.setColumnId(request.getColumnId());
        task.setName(request.getName());
        task.setContent(request.getContent());
        task.setDesc(request.getDesc());
        task.setEndAt(request.getEndAt());

        ProjectTask createdTask = projectTaskService.createTask(task, userId);
        return Result.success("创建成功", createdTask);
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<ProjectTask> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        ProjectTask task = new ProjectTask();
        task.setName(request.getName());
        task.setContent(request.getContent());
        task.setDesc(request.getDesc());
        task.setEndAt(request.getEndAt());

        ProjectTask updatedTask = projectTaskService.updateTask(id, task, userId);
        return Result.success("更新成功", updatedTask);
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.deleteTask(id, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.completeTask(id, userId);
        return Result.success("任务已完成");
    }

    @Operation(summary = "移动任务")
    @PostMapping("/{id}/move")
    public Result<Void> moveTask(@PathVariable Long id, @Valid @RequestBody MoveTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.moveTask(id, request.getColumnId(), request.getSort(), userId);
        return Result.success("移动成功");
    }

    @Operation(summary = "分配任务")
    @PostMapping("/{id}/assign")
    public Result<Void> assignTask(@PathVariable Long id, @Valid @RequestBody AssignTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.assignTask(id, request.getUserIds(), userId);
        return Result.success("分配成功");
    }

    @Operation(summary = "获取任务浏览历史")
    @GetMapping("/browse-history")
    public Result<List<ProjectTask>> getTaskBrowseHistory(@RequestParam(defaultValue = "20") Integer limit, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTask> tasks = projectTaskService.getTaskBrowseHistory(userId, limit);
        return Result.success(tasks);
    }

    @Operation(summary = "归档任务")
    @PostMapping("/{id}/archive")
    public Result<Void> archiveTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.archiveTask(id, userId);
        return Result.success("归档成功");
    }

    @Operation(summary = "获取子任务列表")
    @GetMapping("/{id}/subtasks")
    public Result<List<ProjectTask>> getSubTasks(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        // 验证权限
        projectTaskService.getTaskById(id, userId);
        List<ProjectTask> subTasks = projectTaskService.getSubTasks(id);
        return Result.success(subTasks);
    }

    @Operation(summary = "更新任务进度")
    @PostMapping("/{id}/progress")
    public Result<Void> updateTaskProgress(@PathVariable Long id, @Valid @RequestBody UpdateProgressRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        // 验证权限
        projectTaskService.getTaskById(id, userId);
        projectTaskService.updateTaskProgress(id, request.getProgress());
        return Result.success("进度更新成功");
    }

    @Operation(summary = "创建子任务")
    @PostMapping("/{parentId}/subtasks")
    public Result<ProjectTask> createSubTask(@PathVariable Long parentId, @Valid @RequestBody CreateSubTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);

        // 验证父任务权限
        ProjectTask parentTask = projectTaskService.getTaskById(parentId, userId);

        ProjectTask subTask = new ProjectTask();
        subTask.setProjectId(parentTask.getProjectId());
        subTask.setColumnId(parentTask.getColumnId());
        subTask.setParentId(parentId);
        subTask.setName(request.getName());
        subTask.setContent(request.getContent());
        subTask.setDesc(request.getDesc());
        subTask.setEndAt(request.getEndAt());

        ProjectTask createdSubTask = projectTaskService.createTask(subTask, userId);
        return Result.success("子任务创建成功", createdSubTask);
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

    public static class CreateTaskRequest {
        @NotNull(message = "项目ID不能为空")
        private Long projectId;

        @NotNull(message = "列表ID不能为空")
        private Long columnId;

        @NotBlank(message = "任务名称不能为空")
        private String name;

        private String content;
        private String desc;
        private LocalDateTime endAt;

        // Getters and Setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getColumnId() { return columnId; }
        public void setColumnId(Long columnId) { this.columnId = columnId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public LocalDateTime getEndAt() { return endAt; }
        public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    }

    public static class UpdateTaskRequest {
        @NotBlank(message = "任务名称不能为空")
        private String name;

        private String content;
        private String desc;
        private LocalDateTime endAt;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public LocalDateTime getEndAt() { return endAt; }
        public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    }

    public static class MoveTaskRequest {
        @NotNull(message = "列表ID不能为空")
        private Long columnId;

        private Integer sort;

        // Getters and Setters
        public Long getColumnId() { return columnId; }
        public void setColumnId(Long columnId) { this.columnId = columnId; }
        public Integer getSort() { return sort; }
        public void setSort(Integer sort) { this.sort = sort; }
    }

    public static class AssignTaskRequest {
        @NotNull(message = "用户ID列表不能为空")
        private List<Long> userIds;

        // Getters and Setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }

    public static class UpdateProgressRequest {
        @NotNull(message = "进度不能为空")
        private Integer progress;

        // Getters and Setters
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
    }

    public static class CreateSubTaskRequest {
        @NotBlank(message = "子任务名称不能为空")
        private String name;

        private String content;
        private String desc;
        private LocalDateTime endAt;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public LocalDateTime getEndAt() { return endAt; }
        public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    }
}