package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.Workflow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkflowMapper extends BaseMapper<Workflow> {

    @Select("SELECT category, COUNT(*) as count " +
            "FROM workflows " +
            "WHERE status = 'active' " +
            "GROUP BY category " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getWorkflowCategoryStats();

    @Select("SELECT w.*, u.nickname as creator_name " +
            "FROM workflows w " +
            "LEFT JOIN users u ON w.created_by = u.userid " +
            "WHERE w.status = 'active' " +
            "ORDER BY w.updated_at DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getRecentActiveWorkflows(Integer limit);
}