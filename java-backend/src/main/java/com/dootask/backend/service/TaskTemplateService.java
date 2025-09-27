package com.dootask.backend.service;

import com.dootask.backend.entity.TaskTemplate;

import java.util.List;
import java.util.Map;

public interface TaskTemplateService {

    TaskTemplate create(TaskTemplate template);

    TaskTemplate update(TaskTemplate template);

    void delete(Long id);

    TaskTemplate getById(Long id);

    List<TaskTemplate> getAllTemplates(Long userId);

    List<TaskTemplate> getPublicTemplates();

    List<TaskTemplate> getUserTemplates(Long userId);

    List<TaskTemplate> getTemplatesByCategory(String category);

    List<TaskTemplate> searchTemplates(String keyword, Long userId);

    TaskTemplate duplicateTemplate(Long id, Long userId);

    void useTemplate(Long id);

    Map<String, Object> getTemplateStatistics();

    List<TaskTemplate> getPopularTemplates(Integer limit);

    List<String> getTemplateCategories();

    void shareTemplate(Long id, Boolean isPublic);

    List<TaskTemplate> getRecentTemplates(Long userId, Integer limit);

    TaskTemplate createFromTask(Long taskId, String name, String description, Long userId);
}