package com.dootask.backend.service;

import com.dootask.backend.entity.CalendarEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventService {

    CalendarEvent create(CalendarEvent event);

    CalendarEvent update(CalendarEvent event);

    void delete(Long id);

    CalendarEvent getById(Long id);

    List<CalendarEvent> getEventsByDateRange(LocalDateTime start, LocalDateTime end, Long userId);

    List<CalendarEvent> getEventsByUser(Long userId, Integer page, Integer size);

    List<CalendarEvent> getEventsByProject(Long projectId, Long userId);

    List<CalendarEvent> getUpcomingEvents(Long userId, Integer days);

    List<CalendarEvent> getTodayEvents(Long userId);

    void createTaskReminder(Long taskId, LocalDateTime reminderTime, Long userId);

    void createProjectMilestone(Long projectId, String title, LocalDateTime date, Long userId);

    List<CalendarEvent> searchEvents(String keyword, Long userId);

    void updateRecurringEvents(CalendarEvent event);
}