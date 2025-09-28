package com.dootask.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.ProjectTask;
import com.dootask.backend.entity.User;
import com.dootask.backend.mapper.ProjectTaskMapper;
import com.dootask.backend.service.ProjectService;
import com.dootask.backend.service.ProjectTaskService;
import com.dootask.backend.service.UserService;
import com.dootask.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectTaskServiceImpl extends ServiceImpl<ProjectTaskMapper, ProjectTask> implements ProjectTaskService {

    private final ProjectService projectService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProjectTask> getTasksByProject(Long projectId, Long userId) {
        // 验证项目权限
        projectService.getProjectById(projectId, userId);

        return list(new QueryWrapper<ProjectTask>()
                .eq("project_id", projectId)
                .orderByAsc("sort")
                .orderByDesc("created_at"));
    }

    @Override
    public List<ProjectTask> getTasksByColumn(Long columnId, Long userId) {
        return list(new QueryWrapper<ProjectTask>()
                .eq("column_id", columnId)
                .orderByAsc("sort"));
    }

    @Override
    @Transactional
    public ProjectTask createTask(ProjectTask task, Long userId) {
        // 验证项目权限
        if (task.getProjectId() != null) {
            projectService.getProjectById(task.getProjectId(), userId);
        }

        // 设置创建者
        if (task.getOwner() == null) {
            task.setOwner(String.valueOf(userId));
        }

        // 设置时间戳
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // 处理子任务层级
        if (task.getParentId() != null && task.getParentId() > 0) {
            ProjectTask parentTask = getById(task.getParentId());
            if (parentTask != null) {
                task.setLevel(parentTask.getLevel() + 1);
                // 更新父任务的子任务数量
                parentTask.setSubtasks(parentTask.getSubtasks() + 1);
                updateById(parentTask);
            }
        }

        if (!save(task)) {
            throw new ApiException("创建任务失败");
        }

        // 发送通知给相关用户
        sendTaskNotification(task, "create", userId);

        // 更新项目统计
        if (task.getProjectId() != null) {
            projectService.updateProjectStats(task.getProjectId());
        }

        log.info("用户{}创建了任务: {}", userId, task.getName());
        return task;
    }

    @Override
    @Transactional
    public ProjectTask updateTask(Long taskId, ProjectTask task, Long userId) {
        ProjectTask existingTask = getTaskById(taskId, userId);

        task.setId(taskId);
        task.setUpdatedAt(LocalDateTime.now());

        if (!updateById(task)) {
            throw new ApiException("更新任务失败");
        }

        // 更新项目统计
        if (existingTask.getProjectId() != null) {
            projectService.updateProjectStats(existingTask.getProjectId());
        }

        return getById(taskId);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        ProjectTask task = getTaskById(taskId, userId);

        removeById(taskId);

        // 更新项目统计
        if (task.getProjectId() != null) {
            projectService.updateProjectStats(task.getProjectId());
        }
    }

    @Override
    public ProjectTask getTaskById(Long taskId, Long userId) {
        ProjectTask task = getById(taskId);
        if (task == null) {
            throw new ApiException("任务不存在");
        }

        // 验证项目权限
        if (task.getProjectId() != null) {
            projectService.getProjectById(task.getProjectId(), userId);
        }

        return task;
    }

    @Override
    @Transactional
    public void completeTask(Long taskId, Long userId) {
        ProjectTask task = getTaskById(taskId, userId);

        // 检查是否已完成
        if (task.getCompleteAt() != null) {
            throw new ApiException("任务已经完成");
        }

        task.setCompleteAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        updateById(task);

        // 如果是子任务，更新父任务的完成数量
        if (task.getParentId() != null && task.getParentId() > 0) {
            updateParentTaskProgress(task.getParentId());
        }

        // 自动完成子任务的处理逻辑
        List<ProjectTask> subTasks = getSubTasks(taskId);
        for (ProjectTask subTask : subTasks) {
            if (subTask.getCompleteAt() == null) {
                subTask.setCompleteAt(LocalDateTime.now());
                subTask.setUpdatedAt(LocalDateTime.now());
                updateById(subTask);
            }
        }

        // 发送完成通知
        sendTaskNotification(task, "complete", userId);

        // 更新项目统计
        if (task.getProjectId() != null) {
            projectService.updateProjectStats(task.getProjectId());
        }

        log.info("用户{}完成了任务: {}", userId, task.getName());
    }

    @Override
    public void moveTask(Long taskId, Long columnId, Integer sort, Long userId) {
        ProjectTask task = getTaskById(taskId, userId);

        task.setColumnId(columnId);
        if (sort != null) {
            task.setSort(sort);
        }
        task.setUpdatedAt(LocalDateTime.now());

        updateById(task);
    }

    @Override
    public void assignTask(Long taskId, List<Long> userIds, Long userId) {
        ProjectTask task = getTaskById(taskId, userId);

        // 将用户ID列表转换为JSON字符串存储
        try {
            task.setOwner(objectMapper.writeValueAsString(userIds));
        } catch (JsonProcessingException e) {
            log.error("转换用户ID列表为JSON失败", e);
            task.setOwner(userIds.toString());
        }
        task.setUpdatedAt(LocalDateTime.now());

        updateById(task);
    }

    @Override
    public List<ProjectTask> getTaskBrowseHistory(Long userId, Integer limit) {
        // 这里简化实现，实际应该根据用户的浏览历史记录
        return list(new QueryWrapper<ProjectTask>()
                .like("owner", userId.toString())
                .orderByDesc("updated_at")
                .last("LIMIT " + (limit != null ? limit : 20)));
    }

    @Override
    @Transactional
    public void archiveTask(Long taskId, Long userId) {
        ProjectTask task = getTaskById(taskId, userId);

        task.setArchivedAt(LocalDateTime.now());
        task.setArchivedUserid(userId);
        task.setUpdatedAt(LocalDateTime.now());

        updateById(task);

        // 归档子任务
        List<ProjectTask> subTasks = getSubTasks(taskId);
        for (ProjectTask subTask : subTasks) {
            subTask.setArchivedAt(LocalDateTime.now());
            subTask.setArchivedUserid(userId);
            subTask.setUpdatedAt(LocalDateTime.now());
            updateById(subTask);
        }

        log.info("用户{}归档了任务: {}", userId, task.getName());
    }

    @Override
    public List<ProjectTask> getSubTasks(Long parentId) {
        return list(new QueryWrapper<ProjectTask>()
                .eq("parent_id", parentId)
                .orderByAsc("sort")
                .orderByDesc("created_at"));
    }

    @Override
    @Transactional
    public void updateTaskProgress(Long taskId, Integer progress) {
        ProjectTask task = getById(taskId);
        if (task == null) {
            throw new ApiException("任务不存在");
        }

        // 更新进度相关逻辑
        if (progress >= 100 && task.getCompleteAt() == null) {
            task.setCompleteAt(LocalDateTime.now());
        } else if (progress < 100 && task.getCompleteAt() != null) {
            task.setCompleteAt(null);
        }

        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);

        // 更新父任务进度
        if (task.getParentId() != null && task.getParentId() > 0) {
            updateParentTaskProgress(task.getParentId());
        }
    }

    private void updateParentTaskProgress(Long parentId) {
        ProjectTask parentTask = getById(parentId);
        if (parentTask == null) return;

        List<ProjectTask> subTasks = getSubTasks(parentId);
        if (subTasks.isEmpty()) return;

        int completedCount = (int) subTasks.stream()
                .filter(task -> task.getCompleteAt() != null)
                .count();

        parentTask.setSubtasks(subTasks.size());
        parentTask.setSubtasksComplete(completedCount);
        parentTask.setUpdatedAt(LocalDateTime.now());

        // 如果所有子任务都完成，自动完成父任务
        if (completedCount == subTasks.size() && parentTask.getCompleteAt() == null) {
            parentTask.setCompleteAt(LocalDateTime.now());
        }

        updateById(parentTask);
    }

    private void sendTaskNotification(ProjectTask task, String action, Long operatorId) {
        try {
            // 获取任务相关用户
            Set<Long> userIds = new HashSet<>();

            // 添加任务负责人
            if (StringUtils.hasText(task.getOwner())) {
                try {
                    List<Long> ownerIds = objectMapper.readValue(task.getOwner(), new TypeReference<List<Long>>() {});
                    userIds.addAll(ownerIds);
                } catch (Exception e) {
                    // 如果不是JSON格式，尝试作为单个ID处理
                    try {
                        userIds.add(Long.valueOf(task.getOwner()));
                    } catch (NumberFormatException ex) {
                        log.warn("无法解析任务负责人ID: {}", task.getOwner());
                    }
                }
            }

            // 添加协助人
            if (StringUtils.hasText(task.getAssist())) {
                try {
                    List<Long> assistIds = objectMapper.readValue(task.getAssist(), new TypeReference<List<Long>>() {});
                    userIds.addAll(assistIds);
                } catch (Exception e) {
                    log.warn("无法解析任务协助人ID: {}", task.getAssist());
                }
            }

            // 移除操作者自己
            userIds.remove(operatorId);

            // 发送通知
            for (Long userId : userIds) {
                String message = generateNotificationMessage(task, action, operatorId);
                notificationService.sendNotification(userId, "任务通知", message);
            }
        } catch (Exception e) {
            log.error("发送任务通知失败", e);
        }
    }

    private String generateNotificationMessage(ProjectTask task, String action, Long operatorId) {
        User operator = userService.getUserById(operatorId);
        String operatorName = operator != null ? operator.getNickname() : "系统";

        switch (action) {
            case "create":
                return String.format("%s 创建了任务: %s", operatorName, task.getName());
            case "update":
                return String.format("%s 更新了任务: %s", operatorName, task.getName());
            case "complete":
                return String.format("%s 完成了任务: %s", operatorName, task.getName());
            case "assign":
                return String.format("%s 将任务分配给了您: %s", operatorName, task.getName());
            default:
                return String.format("%s 操作了任务: %s", operatorName, task.getName());
        }
    }

    @Override
    public List<ProjectTask> getProjectTasks(Long projectId) {
        return list(new QueryWrapper<ProjectTask>()
                .eq("project_id", projectId)
                .orderByAsc("sort")
                .orderByDesc("created_at"));
    }

    @Override
    public List<ProjectTask> getAllTasks(Integer page, Integer size) {
        QueryWrapper<ProjectTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("updated_at");

        if (page != null && size != null) {
            // 分页查询
            return page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), queryWrapper).getRecords();
        } else {
            return list(queryWrapper);
        }
    }
}