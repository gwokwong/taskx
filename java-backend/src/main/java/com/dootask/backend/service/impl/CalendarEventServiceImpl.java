package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.CalendarEvent;
import com.dootask.backend.mapper.CalendarEventMapper;
import com.dootask.backend.service.CalendarEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl extends ServiceImpl<CalendarEventMapper, CalendarEvent> implements CalendarEventService {

    @Override
    public CalendarEvent create(CalendarEvent event) {
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        if (event.getStatus() == null) {
            event.setStatus("confirmed");
        }
        save(event);
        return event;
    }

    @Override
    public CalendarEvent update(CalendarEvent event) {
        event.setUpdatedAt(LocalDateTime.now());
        updateById(event);
        return event;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public CalendarEvent getById(Long id) {
        return getById(id);
    }

    @Override
    public List<CalendarEvent> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Long userId) {
        QueryWrapper<CalendarEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .and(wrapper -> wrapper
                        .between("start_time", start, end)
                        .or()
                        .between("end_time", start, end)
                        .or()
                        .and(w -> w.le("start_time", start).ge("end_time", end))
                )
                .orderByAsc("start_time");
        return list(queryWrapper);
    }

    @Override
    public List<CalendarEvent> getEventsByUser(Long userId, Integer page, Integer size) {
        QueryWrapper<CalendarEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("start_time");

        if (page != null && size != null) {
            Page<CalendarEvent> pageInfo = new Page<>(page, size);
            Page<CalendarEvent> result = page(pageInfo, queryWrapper);
            return result.getRecords();
        } else {
            return list(queryWrapper);
        }
    }

    @Override
    public List<CalendarEvent> getEventsByProject(Long projectId, Long userId) {
        QueryWrapper<CalendarEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("related_id", projectId)
                .eq("related_type", "project")
                .orderByAsc("start_time");
        return list(queryWrapper);
    }

    @Override
    public List<CalendarEvent> getUpcomingEvents(Long userId, Integer days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);

        QueryWrapper<CalendarEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .ge("start_time", now)
                .le("start_time", future)
                .orderByAsc("start_time");
        return list(queryWrapper);
    }

    @Override
    public List<CalendarEvent> getTodayEvents(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        QueryWrapper<CalendarEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .ge("start_time", startOfDay)
                .lt("start_time", endOfDay)
                .orderByAsc("start_time");
        return list(queryWrapper);
    }

    @Override
    public void createTaskReminder(Long taskId, LocalDateTime reminderTime, Long userId) {
        CalendarEvent event = new CalendarEvent();
        event.setUserId(userId);
        event.setTitle("任务提醒");
        event.setStartTime(reminderTime);
        event.setEndTime(reminderTime.plusMinutes(30));
        event.setType("reminder");
        event.setRelatedId(taskId);
        event.setRelatedType("task");
        event.setColor("#ff6b6b");
        create(event);
    }

    @Override
    public void createProjectMilestone(Long projectId, String title, LocalDateTime date, Long userId) {
        CalendarEvent event = new CalendarEvent();
        event.setUserId(userId);
        event.setTitle(title);
        event.setStartTime(date);
        event.setEndTime(date.plusHours(1));
        event.setType("milestone");
        event.setRelatedId(projectId);
        event.setRelatedType("project");
        event.setColor("#4ecdc4");
        create(event);
    }

    @Override
    public List<CalendarEvent> searchEvents(String keyword, Long userId) {
        QueryWrapper<CalendarEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .and(wrapper -> wrapper
                        .like("title", keyword)
                        .or()
                        .like("description", keyword)
                        .or()
                        .like("location", keyword)
                )
                .orderByDesc("start_time");
        return list(queryWrapper);
    }

    @Override
    public void updateRecurringEvents(CalendarEvent event) {
        if (event.getRecurrenceRule() != null && !event.getRecurrenceRule().isEmpty()) {
            // TODO: 实现重复事件逻辑
            // 这里可以根据 recurrenceRule 创建重复的事件
            // 例如：DAILY, WEEKLY, MONTHLY, YEARLY
        }
    }
}