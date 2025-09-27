package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.TaskTemplate;
import com.dootask.backend.mapper.TaskTemplateMapper;
import com.dootask.backend.service.TaskTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskTemplateServiceImpl extends ServiceImpl<TaskTemplateMapper, TaskTemplate> implements TaskTemplateService {

    @Override
    public TaskTemplate create(TaskTemplate template) {
        if (!StringUtils.hasText(template.getName())) {
            throw new ApiException("模板名称不能为空");
        }

        if (template.getCreatedBy() == null) {
            throw new ApiException("创建者不能为空");
        }

        // 检查名称是否重复
        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", template.getName())
                .eq("created_by", template.getCreatedBy());
        if (count(queryWrapper) > 0) {
            throw new ApiException("模板名称已存在");
        }

        if (template.getIsPublic() == null) {
            template.setIsPublic(false);
        }

        if (template.getUseCount() == null) {
            template.setUseCount(0);
        }

        if (template.getStatus() == null) {
            template.setStatus("active");
        }

        if (template.getVersion() == null) {
            template.setVersion("1.0");
        }

        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        save(template);
        return template;
    }

    @Override
    public TaskTemplate update(TaskTemplate template) {
        if (template.getId() == null) {
            throw new ApiException("模板ID不能为空");
        }

        TaskTemplate existing = getById(template.getId());
        if (existing == null) {
            throw new ApiException("模板不存在");
        }

        // 检查名称是否重复
        if (StringUtils.hasText(template.getName()) && !template.getName().equals(existing.getName())) {
            QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", template.getName())
                    .eq("created_by", existing.getCreatedBy())
                    .ne("id", template.getId());
            if (count(queryWrapper) > 0) {
                throw new ApiException("模板名称已存在");
            }
        }

        template.setUpdatedAt(LocalDateTime.now());
        updateById(template);
        return template;
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException("模板ID不能为空");
        }

        TaskTemplate template = getById(id);
        if (template == null) {
            throw new ApiException("模板不存在");
        }

        removeById(id);
    }

    @Override
    public TaskTemplate getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return super.getById(id);
    }

    @Override
    public List<TaskTemplate> getAllTemplates(Long userId) {
        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq("created_by", userId)
                .or()
                .eq("is_public", true))
                .eq("status", "active")
                .orderByDesc("updated_at");
        return list(queryWrapper);
    }

    @Override
    public List<TaskTemplate> getPublicTemplates() {
        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_public", true)
                .eq("status", "active")
                .orderByDesc("use_count", "updated_at");
        return list(queryWrapper);
    }

    @Override
    public List<TaskTemplate> getUserTemplates(Long userId) {
        if (userId == null) {
            return List.of();
        }

        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("created_by", userId)
                .eq("status", "active")
                .orderByDesc("updated_at");
        return list(queryWrapper);
    }

    @Override
    public List<TaskTemplate> getTemplatesByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return List.of();
        }

        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", category)
                .eq("status", "active")
                .orderByDesc("use_count", "updated_at");
        return list(queryWrapper);
    }

    @Override
    public List<TaskTemplate> searchTemplates(String keyword, Long userId) {
        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("description", keyword)
                    .or()
                    .like("tags", keyword));
        }

        queryWrapper.and(wrapper -> wrapper
                .eq("created_by", userId)
                .or()
                .eq("is_public", true))
                .eq("status", "active")
                .orderByDesc("use_count", "updated_at");

        return list(queryWrapper);
    }

    @Override
    public TaskTemplate duplicateTemplate(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new ApiException("参数不能为空");
        }

        TaskTemplate original = getById(id);
        if (original == null) {
            throw new ApiException("模板不存在");
        }

        // 检查权限：只能复制公开模板或自己的模板
        if (!original.getIsPublic() && !original.getCreatedBy().equals(userId)) {
            throw new ApiException("无权限复制此模板");
        }

        TaskTemplate duplicate = new TaskTemplate();
        duplicate.setName(original.getName() + " (副本)");
        duplicate.setDescription(original.getDescription());
        duplicate.setCategory(original.getCategory());
        duplicate.setCreatedBy(userId);
        duplicate.setTemplate(original.getTemplate());
        duplicate.setTags(original.getTags());
        duplicate.setIsPublic(false);
        duplicate.setUseCount(0);
        duplicate.setStatus("active");
        duplicate.setVersion("1.0");

        return create(duplicate);
    }

    @Override
    public void useTemplate(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException("模板ID不能为空");
        }

        TaskTemplate template = getById(id);
        if (template == null) {
            throw new ApiException("模板不存在");
        }

        template.setUseCount(template.getUseCount() + 1);
        template.setUpdatedAt(LocalDateTime.now());
        updateById(template);
    }

    @Override
    public Map<String, Object> getTemplateStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总模板数
        long totalTemplates = count(new QueryWrapper<TaskTemplate>().eq("status", "active"));
        stats.put("totalTemplates", totalTemplates);

        // 公开模板数
        long publicTemplates = count(new QueryWrapper<TaskTemplate>()
                .eq("is_public", true)
                .eq("status", "active"));
        stats.put("publicTemplates", publicTemplates);

        // 私有模板数
        long privateTemplates = totalTemplates - publicTemplates;
        stats.put("privateTemplates", privateTemplates);

        // 总使用次数
        List<TaskTemplate> templates = list(new QueryWrapper<TaskTemplate>().eq("status", "active"));
        long totalUseCount = templates.stream().mapToLong(TaskTemplate::getUseCount).sum();
        stats.put("totalUseCount", totalUseCount);

        return stats;
    }

    @Override
    public List<TaskTemplate> getPopularTemplates(Integer limit) {
        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_public", true)
                .eq("status", "active")
                .gt("use_count", 0)
                .orderByDesc("use_count")
                .last("LIMIT " + (limit != null ? limit : 10));
        return list(queryWrapper);
    }

    @Override
    public List<String> getTemplateCategories() {
        return baseMapper.getTemplateCategories();
    }

    @Override
    public void shareTemplate(Long id, Boolean isPublic) {
        if (id == null) {
            throw new ApiException("模板ID不能为空");
        }

        TaskTemplate template = getById(id);
        if (template == null) {
            throw new ApiException("模板不存在");
        }

        template.setIsPublic(isPublic != null ? isPublic : false);
        template.setUpdatedAt(LocalDateTime.now());
        updateById(template);
    }

    @Override
    public List<TaskTemplate> getRecentTemplates(Long userId, Integer limit) {
        QueryWrapper<TaskTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq("created_by", userId)
                .or()
                .eq("is_public", true))
                .eq("status", "active")
                .orderByDesc("updated_at")
                .last("LIMIT " + (limit != null ? limit : 5));
        return list(queryWrapper);
    }

    @Override
    public TaskTemplate createFromTask(Long taskId, String name, String description, Long userId) {
        if (taskId == null || userId == null) {
            throw new ApiException("参数不能为空");
        }

        if (!StringUtils.hasText(name)) {
            throw new ApiException("模板名称不能为空");
        }

        // TODO: 从任务中获取数据并创建模板
        // 这里简化实现，实际应该从Task实体中提取配置

        TaskTemplate template = new TaskTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setCategory("custom");
        template.setCreatedBy(userId);
        template.setTemplate("{\"taskId\":" + taskId + "}"); // 简化的模板配置
        template.setIsPublic(false);

        return create(template);
    }
}