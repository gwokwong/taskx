package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.ProjectColumn;
import com.dootask.backend.mapper.ProjectColumnMapper;
import com.dootask.backend.service.ProjectColumnService;
import com.dootask.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectColumnServiceImpl extends ServiceImpl<ProjectColumnMapper, ProjectColumn> implements ProjectColumnService {

    private final ProjectService projectService;

    @Override
    public List<ProjectColumn> getColumnsByProject(Long projectId) {
        return list(new QueryWrapper<ProjectColumn>()
                .eq("project_id", projectId)
                .orderByAsc("sort"));
    }

    @Override
    public ProjectColumn createColumn(ProjectColumn column, Long userId) {
        // 验证项目权限
        projectService.getProjectById(column.getProjectId(), userId);

        column.setCreatedAt(LocalDateTime.now());
        column.setUpdatedAt(LocalDateTime.now());

        if (!save(column)) {
            throw new ApiException("创建列表失败");
        }

        return column;
    }

    @Override
    public ProjectColumn updateColumn(Long columnId, ProjectColumn column, Long userId) {
        ProjectColumn existingColumn = getById(columnId);
        if (existingColumn == null) {
            throw new ApiException("列表不存在");
        }

        // 验证项目权限
        projectService.getProjectById(existingColumn.getProjectId(), userId);

        column.setId(columnId);
        column.setUpdatedAt(LocalDateTime.now());

        if (!updateById(column)) {
            throw new ApiException("更新列表失败");
        }

        return getById(columnId);
    }

    @Override
    public void deleteColumn(Long columnId, Long userId) {
        ProjectColumn column = getById(columnId);
        if (column == null) {
            throw new ApiException("列表不存在");
        }

        // 验证项目权限
        projectService.getProjectById(column.getProjectId(), userId);

        removeById(columnId);
    }
}