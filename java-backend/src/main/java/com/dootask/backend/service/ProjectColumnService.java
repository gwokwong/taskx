package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.ProjectColumn;

import java.util.List;

public interface ProjectColumnService extends IService<ProjectColumn> {

    List<ProjectColumn> getColumnsByProject(Long projectId);

    ProjectColumn createColumn(ProjectColumn column, Long userId);

    ProjectColumn updateColumn(Long columnId, ProjectColumn column, Long userId);

    void deleteColumn(Long columnId, Long userId);
}