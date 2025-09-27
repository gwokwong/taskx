package com.dootask.backend.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface ExportService {

    void exportUsers(String format, HttpServletResponse response);

    void exportProjects(String format, HttpServletResponse response);

    void exportTasks(Long projectId, String format, HttpServletResponse response);

    void exportSystemLogs(LocalDateTime startDate, LocalDateTime endDate, String format, HttpServletResponse response);

    void exportCalendarEvents(LocalDateTime startDate, LocalDateTime endDate, String format, HttpServletResponse response);

    void exportDepartments(String format, HttpServletResponse response);

    void exportTaskTemplates(String format, HttpServletResponse response);

    void exportWorkflows(String format, HttpServletResponse response);

    void exportProjectMembers(Long projectId, String format, HttpServletResponse response);

    byte[] generateExcelReport(String reportType, Object data);

    byte[] generateCsvReport(String reportType, Object data);
}