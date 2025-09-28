package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.ProjectTask;

import java.util.List;

public interface ProjectTaskService extends IService<ProjectTask> {

    List<ProjectTask> getTasksByProject(Long projectId, Long userId);

    List<ProjectTask> getTasksByColumn(Long columnId, Long userId);

    ProjectTask createTask(ProjectTask task, Long userId);

    ProjectTask updateTask(Long taskId, ProjectTask task, Long userId);

    void deleteTask(Long taskId, Long userId);

    ProjectTask getTaskById(Long taskId, Long userId);

    void completeTask(Long taskId, Long userId);

    void moveTask(Long taskId, Long columnId, Integer sort, Long userId);

    void assignTask(Long taskId, List<Long> userIds, Long userId);

    List<ProjectTask> getTaskBrowseHistory(Long userId, Integer limit);

    /**
     * 归档任务
     */
    void archiveTask(Long taskId, Long userId);

    /**
     * 获取子任务列表
     */
    List<ProjectTask> getSubTasks(Long parentId);

    /**
     * 更新任务进度
     */
    void updateTaskProgress(Long taskId, Integer progress);

    /**
     * 获取项目任务
     */
    List<ProjectTask> getProjectTasks(Long projectId);

    /**
     * 获取所有任务
     */
    List<ProjectTask> getAllTasks(Integer page, Integer size);
}