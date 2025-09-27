package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM workflow_instances " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getInstanceCreationStats(Integer days);

    @Select("SELECT status, COUNT(*) as count " +
            "FROM workflow_instances " +
            "GROUP BY status")
    List<Map<String, Object>> getInstanceStatusStats();

    @Select("SELECT wi.*, w.name as workflow_name, u.nickname as initiator_name " +
            "FROM workflow_instances wi " +
            "LEFT JOIN workflows w ON wi.workflow_id = w.id " +
            "LEFT JOIN users u ON wi.initiated_by = u.userid " +
            "WHERE wi.initiated_by = #{userId} " +
            "ORDER BY wi.created_at DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getUserInstancesWithDetails(Long userId, Integer limit);
}