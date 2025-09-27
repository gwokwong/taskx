package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.Project;

import java.util.List;

public interface ProjectService extends IService<Project> {

    List<Project> getProjectsByUser(Long userId);

    Project createProject(Project project, Long userId);

    Project updateProject(Long projectId, Project project, Long userId);

    void deleteProject(Long projectId, Long userId);

    Project getProjectById(Long projectId, Long userId);

    void topProject(Long projectId, Long userId);

    void archiveProject(Long projectId, Long userId);

    void updateProjectStats(Long projectId);
}