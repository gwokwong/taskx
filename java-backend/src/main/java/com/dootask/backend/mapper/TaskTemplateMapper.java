package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.TaskTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskTemplateMapper extends BaseMapper<TaskTemplate> {

    @Select("SELECT DISTINCT category FROM task_templates WHERE status = 'active' AND category IS NOT NULL ORDER BY category")
    List<String> getTemplateCategories();

    @Select("SELECT category, COUNT(*) as count FROM task_templates WHERE status = 'active' GROUP BY category ORDER BY count DESC")
    List<Map<String, Object>> getTemplateCategoryStats();

    @Select("SELECT t.*, u.nickname as creator_name " +
            "FROM task_templates t " +
            "LEFT JOIN users u ON t.created_by = u.userid " +
            "WHERE t.is_public = true AND t.status = 'active' " +
            "ORDER BY t.use_count DESC, t.updated_at DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getPopularTemplatesWithCreator(Integer limit);
}