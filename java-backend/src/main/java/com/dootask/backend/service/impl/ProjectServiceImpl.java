package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.Project;
import com.dootask.backend.entity.ProjectColumn;
import com.dootask.backend.entity.ProjectTask;
import com.dootask.backend.mapper.ProjectMapper;
import com.dootask.backend.service.ProjectColumnService;
import com.dootask.backend.service.ProjectService;
import com.dootask.backend.service.ProjectTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectColumnService projectColumnService;
    private final ProjectTaskService projectTaskService;

    @Override
    public List<Project> getProjectsByUser(Long userId) {
        return list(new QueryWrapper<Project>()
                .eq("owner_userid", userId)
                .orderByDesc("sort")
                .orderByDesc("created_at"));
    }

    @Override
    @Transactional
    public Project createProject(Project project, Long userId) {
        project.setOwnerUserid(userId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        if (!save(project)) {
            throw new ApiException("创建项目失败");
        }

        // 创建默认列表
        createDefaultColumns(project.getId());

        return project;
    }

    @Override
    public Project updateProject(Long projectId, Project project, Long userId) {
        Project existingProject = getProjectById(projectId, userId);

        project.setId(projectId);
        project.setUpdatedAt(LocalDateTime.now());

        if (!updateById(project)) {
            throw new ApiException("更新项目失败");
        }

        return getById(projectId);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = getProjectById(projectId, userId);

        // 删除相关任务和列表
        projectTaskService.remove(new QueryWrapper<ProjectTask>().eq("project_id", projectId));
        projectColumnService.remove(new QueryWrapper<ProjectColumn>().eq("project_id", projectId));

        // 删除项目
        removeById(projectId);
    }

    @Override
    public Project getProjectById(Long projectId, Long userId) {
        Project project = getOne(new QueryWrapper<Project>()
                .eq("id", projectId)
                .eq("owner_userid", userId));

        if (project == null) {
            throw new ApiException("项目不存在或无权限访问");
        }

        return project;
    }

    @Override
    public void topProject(Long projectId, Long userId) {
        Project project = getProjectById(projectId, userId);

        LocalDateTime topAt = project.getTopAt() != null ? null : LocalDateTime.now();
        project.setTopAt(topAt);
        project.setUpdatedAt(LocalDateTime.now());

        updateById(project);
    }

    @Override
    public void archiveProject(Long projectId, Long userId) {
        Project project = getProjectById(projectId, userId);

        project.setArchivedAt(LocalDateTime.now());
        project.setArchivedUserid(userId);
        project.setUpdatedAt(LocalDateTime.now());

        updateById(project);
    }

    @Override
    public void updateProjectStats(Long projectId) {
        int totalTasks = projectTaskService.count(new QueryWrapper<ProjectTask>().eq("project_id", projectId));
        int completedTasks = projectTaskService.count(new QueryWrapper<ProjectTask>()
                .eq("project_id", projectId)
                .isNotNull("complete_at"));

        int percent = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

        Project project = getById(projectId);
        project.setTaskNum(totalTasks);
        project.setTaskComplete(completedTasks);
        project.setTaskPercent(percent);
        project.setUpdatedAt(LocalDateTime.now());

        updateById(project);
    }

    private void createDefaultColumns(Long projectId) {
        List<String> columnNames = Arrays.asList("待办", "进行中", "已完成");

        for (int i = 0; i < columnNames.size(); i++) {
            ProjectColumn column = new ProjectColumn();
            column.setProjectId(projectId);
            column.setName(columnNames.get(i));
            column.setSort(i);
            column.setCreatedAt(LocalDateTime.now());
            column.setUpdatedAt(LocalDateTime.now());

            projectColumnService.save(column);
        }
    }
}