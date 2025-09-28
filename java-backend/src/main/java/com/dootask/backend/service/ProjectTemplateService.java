package com.dootask.backend.service;

import com.dootask.backend.entity.ProjectTemplate;
import com.dootask.backend.entity.Project;

import java.util.List;
import java.util.Map;

public interface ProjectTemplateService {

    // 模板管理
    ProjectTemplate createTemplate(ProjectTemplate template, Long creatorId);

    ProjectTemplate updateTemplate(Long templateId, ProjectTemplate template, Long userId);

    void deleteTemplate(Long templateId, Long userId);

    ProjectTemplate getTemplateById(Long templateId);

    List<ProjectTemplate> getTemplatesByCategory(String category);

    List<ProjectTemplate> getPublicTemplates();

    List<ProjectTemplate> getMyTemplates(Long userId);

    List<ProjectTemplate> getSystemTemplates();

    // 模板搜索和过滤
    List<ProjectTemplate> searchTemplates(String keyword, String category, String tags);

    List<ProjectTemplate> getRecommendedTemplates(Long userId);

    List<ProjectTemplate> getPopularTemplates(Integer limit);

    List<ProjectTemplate> getRecentTemplates(Integer limit);

    // 模板使用
    Project createProjectFromTemplate(Long templateId, String projectName, String projectDesc, Long userId);

    void saveProjectAsTemplate(Long projectId, String templateName, String templateDesc, Long userId);

    Map<String, Object> previewTemplate(Long templateId);

    // 模板评价和统计
    void rateTemplate(Long templateId, Double rating, Long userId);

    void incrementUsageCount(Long templateId);

    Map<String, Object> getTemplateStatistics(Long templateId);

    List<Map<String, Object>> getTemplateUsageHistory(Long templateId);

    // 模板分享和协作
    String shareTemplate(Long templateId, List<String> permissions, Long userId);

    void forkTemplate(Long templateId, String newName, Long userId);

    void copyTemplate(Long templateId, String newName, Boolean isPublic, Long userId);

    // 模板导入导出
    String exportTemplate(Long templateId, String format);

    ProjectTemplate importTemplate(String templateData, String format, Long userId);

    // 模板分类管理
    List<String> getTemplateCategories();

    void createTemplateCategory(String category, String description);

    void updateTemplateCategory(String oldCategory, String newCategory);

    // 模板标签管理
    List<String> getTemplateTags();

    List<String> getPopularTags();

    void addTemplateTag(Long templateId, String tag);

    void removeTemplateTag(Long templateId, String tag);

    // 模板验证和质量检查
    Map<String, Object> validateTemplate(ProjectTemplate template);

    Map<String, Object> checkTemplateQuality(Long templateId);

    List<String> suggestImprovements(Long templateId);

    // 模板版本管理
    ProjectTemplate createTemplateVersion(Long templateId, String version, String changeLog, Long userId);

    List<ProjectTemplate> getTemplateVersions(Long templateId);

    void setActiveTemplateVersion(Long templateId, String version);

    // 模板市场功能
    List<ProjectTemplate> getFeaturedTemplates();

    List<ProjectTemplate> getTrendingTemplates();

    void submitTemplateForReview(Long templateId);

    void approveTemplate(Long templateId, Long reviewerId);

    void rejectTemplate(Long templateId, String reason, Long reviewerId);
}