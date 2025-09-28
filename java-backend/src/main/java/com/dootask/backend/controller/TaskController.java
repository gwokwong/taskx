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
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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
    public Result<String> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.deleteTask(id, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public Result<String> completeTask(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.completeTask(id, userId);
        return Result.success("任务已完成");
    }

    @Operation(summary = "移动任务")
    @PostMapping("/{id}/move")
    public Result<String> moveTask(@PathVariable Long id, @Valid @RequestBody MoveTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.moveTask(id, request.getColumnId(), request.getSort(), userId);
        return Result.success("移动成功");
    }

    @Operation(summary = "分配任务")
    @PostMapping("/{id}/assign")
    public Result<String> assignTask(@PathVariable Long id, @Valid @RequestBody AssignTaskRequest request, HttpServletRequest httpRequest) {
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
    public Result<String> archiveTask(@PathVariable Long id, HttpServletRequest request) {
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
    public Result<String> updateTaskProgress(@PathVariable Long id, @Valid @RequestBody UpdateProgressRequest request, HttpServletRequest httpRequest) {
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

    @Operation(summary = "设置任务优先级")
    @PostMapping("/{id}/priority")
    public Result<String> setTaskPriority(@PathVariable Long id, @RequestBody SetPriorityRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.setTaskPriority(id, request.getPriority(), userId);
        return Result.success("优先级设置成功");
    }

    @Operation(summary = "设置任务标签")
    @PostMapping("/{id}/tags")
    public Result<String> setTaskTags(@PathVariable Long id, @RequestBody SetTagsRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.setTaskTags(id, request.getTagIds(), userId);
        return Result.success("标签设置成功");
    }

    @Operation(summary = "复制任务")
    @PostMapping("/{id}/copy")
    public Result<ProjectTask> copyTask(@PathVariable Long id, @RequestBody CopyTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ProjectTask copiedTask = projectTaskService.copyTask(id, request.getNewName(), request.getTargetColumnId(), userId);
        return Result.success("任务复制成功", copiedTask);
    }

    @Operation(summary = "批量操作任务")
    @PostMapping("/batch")
    public Result<String> batchOperateTask(@RequestBody BatchOperateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.batchOperate(request.getTaskIds(), request.getOperation(), request.getParams(), userId);
        return Result.success("批量操作成功");
    }

    @Operation(summary = "获取任务统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getTaskStatistics(@RequestParam(required = false) Long projectId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> statistics = projectTaskService.getTaskStatistics(projectId, userId);
        return Result.success(statistics);
    }

    @Operation(summary = "搜索任务")
    @GetMapping("/search")
    public Result<Page<ProjectTask>> searchTasks(@RequestParam String keyword,
                                                 @RequestParam(required = false) Long projectId,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String priority,
                                                 Pageable pageable, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<ProjectTask> tasks = projectTaskService.searchTasks(keyword, projectId, status, priority, pageable, userId);
        return Result.success(tasks);
    }

    @Operation(summary = "获取任务依赖")
    @GetMapping("/{id}/dependencies")
    public Result<Map<String, Object>> getTaskDependencies(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> dependencies = projectTaskService.getTaskDependencies(id, userId);
        return Result.success(dependencies);
    }

    @Operation(summary = "设置任务依赖")
    @PostMapping("/{id}/dependencies")
    public Result<String> setTaskDependencies(@PathVariable Long id, @RequestBody SetDependenciesRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.setTaskDependencies(id, request.getDependentTaskIds(), userId);
        return Result.success("依赖设置成功");
    }

    @Operation(summary = "获取任务时间记录")
    @GetMapping("/{id}/time-logs")
    public Result<List<Map<String, Object>>> getTaskTimeLogs(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Map<String, Object>> timeLogs = projectTaskService.getTaskTimeLogs(id, userId);
        return Result.success(timeLogs);
    }

    @Operation(summary = "开始计时")
    @PostMapping("/{id}/start-timer")
    public Result<String> startTimer(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.startTimer(id, userId);
        return Result.success("计时开始");
    }

    @Operation(summary = "停止计时")
    @PostMapping("/{id}/stop-timer")
    public Result<String> stopTimer(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        projectTaskService.stopTimer(id, userId);
        return Result.success("计时停止");
    }

    @Operation(summary = "手动添加时间记录")
    @PostMapping("/{id}/add-time")
    public Result<String> addTimeLog(@PathVariable Long id, @RequestBody AddTimeLogRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.addTimeLog(id, request.getMinutes(), request.getDescription(), userId);
        return Result.success("时间记录添加成功");
    }

    @Operation(summary = "获取我的任务")
    @GetMapping("/my-tasks")
    public Result<Page<ProjectTask>> getMyTasks(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) String priority,
                                                @RequestParam(required = false) Boolean overdue,
                                                Pageable pageable, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<ProjectTask> tasks = projectTaskService.getMyTasks(userId, status, priority, overdue, pageable);
        return Result.success(tasks);
    }

    @Operation(summary = "获取超期任务")
    @GetMapping("/overdue")
    public Result<List<ProjectTask>> getOverdueTasks(@RequestParam(required = false) Long projectId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTask> tasks = projectTaskService.getOverdueTasks(projectId, userId);
        return Result.success(tasks);
    }

    @Operation(summary = "获取今日任务")
    @GetMapping("/today")
    public Result<List<ProjectTask>> getTodayTasks(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ProjectTask> tasks = projectTaskService.getTodayTasks(userId);
        return Result.success(tasks);
    }

    @Operation(summary = "获取任务活动日志")
    @GetMapping("/{id}/activities")
    public Result<Page<Map<String, Object>>> getTaskActivities(@PathVariable Long id, Pageable pageable, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<Map<String, Object>> activities = projectTaskService.getTaskActivities(id, pageable, userId);
        return Result.success(activities);
    }

    @Operation(summary = "任务模板导入")
    @PostMapping("/import-template")
    public Result<List<ProjectTask>> importTaskTemplate(@RequestBody ImportTemplateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<ProjectTask> tasks = projectTaskService.importTaskTemplate(request.getTemplateId(), request.getProjectId(), request.getColumnId(), userId);
        return Result.success("模板导入成功", tasks);
    }

    @Operation(summary = "导出任务")
    @GetMapping("/export")
    public Result<String> exportTasks(@RequestParam(required = false) Long projectId,
                                      @RequestParam(required = false) String format,
                                      HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String downloadUrl = projectTaskService.exportTasks(projectId, format, userId);
        return Result.success("导出任务已创建", downloadUrl);
    }

    @Operation(summary = "任务提醒设置")
    @PostMapping("/{id}/reminder")
    public Result<String> setTaskReminder(@PathVariable Long id, @RequestBody SetReminderRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        projectTaskService.setTaskReminder(id, request.getReminderTime(), request.getReminderType(), userId);
        return Result.success("提醒设置成功");
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

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public LocalDateTime getEndAt() { return endAt; }
        public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    }

    public static class SetPriorityRequest {
        @NotBlank(message = "优先级不能为空")
        private String priority; // low, medium, high, urgent

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class SetTagsRequest {
        private List<Long> tagIds;

        public List<Long> getTagIds() { return tagIds; }
        public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    }

    public static class CopyTaskRequest {
        private String newName;
        private Long targetColumnId;

        public String getNewName() { return newName; }
        public void setNewName(String newName) { this.newName = newName; }
        public Long getTargetColumnId() { return targetColumnId; }
        public void setTargetColumnId(Long targetColumnId) { this.targetColumnId = targetColumnId; }
    }

    public static class BatchOperateRequest {
        @NotNull(message = "任务ID列表不能为空")
        private List<Long> taskIds;
        @NotBlank(message = "操作类型不能为空")
        private String operation; // delete, archive, move, assign, set_priority
        private Map<String, Object> params;

        public List<Long> getTaskIds() { return taskIds; }
        public void setTaskIds(List<Long> taskIds) { this.taskIds = taskIds; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) { this.params = params; }
    }

    public static class SetDependenciesRequest {
        private List<Long> dependentTaskIds;

        public List<Long> getDependentTaskIds() { return dependentTaskIds; }
        public void setDependentTaskIds(List<Long> dependentTaskIds) { this.dependentTaskIds = dependentTaskIds; }
    }

    public static class AddTimeLogRequest {
        @NotNull(message = "时间不能为空")
        private Integer minutes;
        private String description;

        public Integer getMinutes() { return minutes; }
        public void setMinutes(Integer minutes) { this.minutes = minutes; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ImportTemplateRequest {
        @NotNull(message = "模板ID不能为空")
        private Long templateId;
        @NotNull(message = "项目ID不能为空")
        private Long projectId;
        @NotNull(message = "列表ID不能为空")
        private Long columnId;

        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getColumnId() { return columnId; }
        public void setColumnId(Long columnId) { this.columnId = columnId; }
    }

    public static class SetReminderRequest {
        @NotNull(message = "提醒时间不能为空")
        private LocalDateTime reminderTime;
        private String reminderType = "email"; // email, notification, both

        public LocalDateTime getReminderTime() { return reminderTime; }
        public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }
        public String getReminderType() { return reminderType; }
        public void setReminderType(String reminderType) { this.reminderType = reminderType; }
    }
}