package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {

    @Select("SELECT action, COUNT(*) as count FROM system_logs " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY action ORDER BY count DESC")
    List<Map<String, Object>> getActionStatistics();

    @Select("SELECT module, COUNT(*) as count FROM system_logs " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY module ORDER BY count DESC")
    List<Map<String, Object>> getModuleStatistics();

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM system_logs " +
            "WHERE created_at BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> getDailyLogStatistics(LocalDateTime startDate, LocalDateTime endDate);
}