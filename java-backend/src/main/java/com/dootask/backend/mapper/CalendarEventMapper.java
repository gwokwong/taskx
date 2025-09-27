package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.CalendarEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CalendarEventMapper extends BaseMapper<CalendarEvent> {

    @Select("SELECT DATE(start_time) as date, COUNT(*) as count " +
            "FROM calendar_events " +
            "WHERE user_id = #{userId} AND start_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(start_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getEventCountByDate(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Select("SELECT type, COUNT(*) as count " +
            "FROM calendar_events " +
            "WHERE user_id = #{userId} " +
            "GROUP BY type")
    List<Map<String, Object>> getEventCountByType(Long userId);

    @Select("SELECT * FROM calendar_events " +
            "WHERE user_id = #{userId} " +
            "AND reminder IS NOT NULL " +
            "AND start_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{minutes} MINUTE) " +
            "ORDER BY start_time")
    List<CalendarEvent> getUpcomingReminders(Long userId, Integer minutes);
}