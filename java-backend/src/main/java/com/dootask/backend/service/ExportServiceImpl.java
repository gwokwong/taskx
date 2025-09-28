package com.dootask.backend.service.impl;

import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.*;
import com.dootask.backend.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectTaskService projectTaskService;
    private final SystemLogService systemLogService;
    private final CalendarEventService calendarEventService;
    private final DepartmentService departmentService;
    private final TaskTemplateService taskTemplateService;
    private final WorkflowService workflowService;
    private final ProjectMemberService projectMemberService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportUsers(String format, HttpServletResponse response) {
        try {
            List<User> users = userService.getAllUsers(null, null);

            String filename = "用户列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportUsersToExcel(users, filename, response);
            } else {
                exportUsersToCsv(users, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出用户数据失败: " + e.getMessage());
        }
    }

    @Override
    public void exportProjects(String format, HttpServletResponse response) {
        try {
            List<Project> projects = projectService.getAllProjects(null, null);

            String filename = "项目列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportProjectsToExcel(projects, filename, response);
            } else {
                exportProjectsToCsv(projects, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出项目数据失败: " + e.getMessage());
        }
    }

    @Override
    public void exportTasks(Long projectId, String format, HttpServletResponse response) {
        try {
            List<ProjectTask> tasks;
            if (projectId != null) {
                tasks = projectTaskService.getProjectTasks(projectId);
            } else {
                tasks = projectTaskService.getAllTasks(null, null);
            }

            String filename = "任务列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportTasksToExcel(tasks, filename, response);
            } else {
                exportTasksToCsv(tasks, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出任务数据失败: " + e.getMessage());
        }
    }

    @Override
    public void exportSystemLogs(LocalDateTime startDate, LocalDateTime endDate, String format, HttpServletResponse response) {
        try {
            List<SystemLog> logs = systemLogService.getLogs(null, null, startDate, endDate, null, null);

            String filename = "系统日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportSystemLogsToExcel(logs, filename, response);
            } else {
                exportSystemLogsToCsv(logs, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出系统日志失败: " + e.getMessage());
        }
    }

    @Override
    public void exportCalendarEvents(LocalDateTime startDate, LocalDateTime endDate, String format, HttpServletResponse response) {
        try {
            // 这里需要根据实际的CalendarEventService接口调整
            // List<CalendarEvent> events = calendarEventService.getEventsByDateRange(startDate, endDate, null);

            String filename = "日历事件_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 暂时返回空数据，实际使用时需要实现
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8) + "\"");

        } catch (Exception e) {
            throw new ApiException("导出日历事件失败: " + e.getMessage());
        }
    }

    @Override
    public void exportDepartments(String format, HttpServletResponse response) {
        try {
            List<Department> departments = departmentService.getAllDepartments();

            String filename = "部门列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportDepartmentsToExcel(departments, filename, response);
            } else {
                exportDepartmentsToCsv(departments, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出部门数据失败: " + e.getMessage());
        }
    }

    @Override
    public void exportTaskTemplates(String format, HttpServletResponse response) {
        try {
            List<TaskTemplate> templates = taskTemplateService.getPublicTemplates();

            String filename = "任务模板_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportTaskTemplatesToExcel(templates, filename, response);
            } else {
                exportTaskTemplatesToCsv(templates, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出任务模板失败: " + e.getMessage());
        }
    }

    @Override
    public void exportWorkflows(String format, HttpServletResponse response) {
        try {
            List<Workflow> workflows = workflowService.getActiveWorkflows();

            String filename = "工作流列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportWorkflowsToExcel(workflows, filename, response);
            } else {
                exportWorkflowsToCsv(workflows, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出工作流数据失败: " + e.getMessage());
        }
    }

    @Override
    public void exportProjectMembers(Long projectId, String format, HttpServletResponse response) {
        try {
            List<ProjectMember> members = projectMemberService.getProjectMembers(projectId);

            String filename = "项目成员_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            if ("excel".equalsIgnoreCase(format)) {
                exportProjectMembersToExcel(members, filename, response);
            } else {
                exportProjectMembersToCsv(members, filename, response);
            }
        } catch (Exception e) {
            throw new ApiException("导出项目成员失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateExcelReport(String reportType, Object data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(reportType);

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 根据报告类型生成不同的内容
            // 这里是示例实现，实际需要根据数据类型处理

            return null; // 实际实现需要返回字节数组
        } catch (Exception e) {
            throw new ApiException("生成Excel报告失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateCsvReport(String reportType, Object data) {
        try {
            StringBuilder csv = new StringBuilder();

            // 根据报告类型生成不同的CSV内容
            // 这里是示例实现

            return csv.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ApiException("生成CSV报告失败: " + e.getMessage());
        }
    }

    // 私有方法实现具体的导出逻辑

    private void exportUsersToExcel(List<User> users, String filename, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8) + "\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户列表");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"用户ID", "邮箱", "昵称", "部门", "角色", "状态", "创建时间", "最后登录"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getUserid() != null ? user.getUserid().toString() : "");
                row.createCell(1).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(2).setCellValue(user.getNickname() != null ? user.getNickname() : "");
                row.createCell(3).setCellValue(user.getDepartmentName() != null ? user.getDepartmentName() : "");
                row.createCell(4).setCellValue(user.getRole() != null ? user.getRole() : "");
                row.createCell(5).setCellValue(user.getDisableAt() == null ? "正常" : "禁用");
                row.createCell(6).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : "");
                row.createCell(7).setCellValue(user.getLastAt() != null ? user.getLastAt().format(DATE_FORMATTER) : "");
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void exportUsersToCsv(List<User> users, String filename, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename + ".csv", StandardCharsets.UTF_8) + "\"");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            // 写入BOM，确保Excel正确显示中文
            writer.write('\ufeff');

            // 写入标题行
            writer.println("用户ID,邮箱,昵称,部门,角色,状态,创建时间,最后登录");

            // 写入数据
            for (User user : users) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    user.getUserid() != null ? user.getUserid().toString() : "",
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getNickname() != null ? user.getNickname() : "",
                    user.getDepartmentName() != null ? user.getDepartmentName() : "",
                    user.getRole() != null ? user.getRole() : "",
                    user.getDisableAt() == null ? "正常" : "禁用",
                    user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : "",
                    user.getLastAt() != null ? user.getLastAt().format(DATE_FORMATTER) : "");
            }
        }
    }

    private void exportProjectsToExcel(List<Project> projects, String filename, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8) + "\"");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("项目列表");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"项目ID", "项目名称", "描述", "所有者", "任务数量", "完成任务", "进度", "创建时间", "更新时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            int rowNum = 1;
            for (Project project : projects) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(project.getId() != null ? project.getId().toString() : "");
                row.createCell(1).setCellValue(project.getName() != null ? project.getName() : "");
                row.createCell(2).setCellValue(project.getDesc() != null ? project.getDesc() : "");
                row.createCell(3).setCellValue(project.getOwnerUserid() != null ? project.getOwnerUserid().toString() : "");
                row.createCell(4).setCellValue(project.getTaskNum() != null ? project.getTaskNum() : 0);
                row.createCell(5).setCellValue(project.getTaskComplete() != null ? project.getTaskComplete() : 0);
                row.createCell(6).setCellValue(project.getTaskPercent() != null ? project.getTaskPercent() + "%" : "0%");
                row.createCell(7).setCellValue(project.getCreatedAt() != null ? project.getCreatedAt().format(DATE_FORMATTER) : "");
                row.createCell(8).setCellValue(project.getUpdatedAt() != null ? project.getUpdatedAt().format(DATE_FORMATTER) : "");
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void exportProjectsToCsv(List<Project> projects, String filename, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename + ".csv", StandardCharsets.UTF_8) + "\"");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.println("项目ID,项目名称,描述,所有者,任务数量,完成任务,进度,创建时间,更新时间");

            for (Project project : projects) {
                writer.printf("%s,%s,%s,%s,%d,%d,%s,%s,%s%n",
                    project.getId() != null ? project.getId().toString() : "",
                    project.getName() != null ? project.getName() : "",
                    project.getDesc() != null ? project.getDesc() : "",
                    project.getOwnerUserid() != null ? project.getOwnerUserid().toString() : "",
                    project.getTaskNum() != null ? project.getTaskNum() : 0,
                    project.getTaskComplete() != null ? project.getTaskComplete() : 0,
                    project.getTaskPercent() != null ? project.getTaskPercent() + "%" : "0%",
                    project.getCreatedAt() != null ? project.getCreatedAt().format(DATE_FORMATTER) : "",
                    project.getUpdatedAt() != null ? project.getUpdatedAt().format(DATE_FORMATTER) : "");
            }
        }
    }

    // 其他导出方法的实现...
    private void exportTasksToExcel(List<ProjectTask> tasks, String filename, HttpServletResponse response) throws IOException {
        // 实现任务导出到Excel的逻辑
    }

    private void exportTasksToCsv(List<ProjectTask> tasks, String filename, HttpServletResponse response) throws IOException {
        // 实现任务导出到CSV的逻辑
    }

    private void exportSystemLogsToExcel(List<SystemLog> logs, String filename, HttpServletResponse response) throws IOException {
        // 实现系统日志导出到Excel的逻辑
    }

    private void exportSystemLogsToCsv(List<SystemLog> logs, String filename, HttpServletResponse response) throws IOException {
        // 实现系统日志导出到CSV的逻辑
    }

    private void exportDepartmentsToExcel(List<Department> departments, String filename, HttpServletResponse response) throws IOException {
        // 实现部门导出到Excel的逻辑
    }

    private void exportDepartmentsToCsv(List<Department> departments, String filename, HttpServletResponse response) throws IOException {
        // 实现部门导出到CSV的逻辑
    }

    private void exportTaskTemplatesToExcel(List<TaskTemplate> templates, String filename, HttpServletResponse response) throws IOException {
        // 实现任务模板导出到Excel的逻辑
    }

    private void exportTaskTemplatesToCsv(List<TaskTemplate> templates, String filename, HttpServletResponse response) throws IOException {
        // 实现任务模板导出到CSV的逻辑
    }

    private void exportWorkflowsToExcel(List<Workflow> workflows, String filename, HttpServletResponse response) throws IOException {
        // 实现工作流导出到Excel的逻辑
    }

    private void exportWorkflowsToCsv(List<Workflow> workflows, String filename, HttpServletResponse response) throws IOException {
        // 实现工作流导出到CSV的逻辑
    }

    private void exportProjectMembersToExcel(List<ProjectMember> members, String filename, HttpServletResponse response) throws IOException {
        // 实现项目成员导出到Excel的逻辑
    }

    private void exportProjectMembersToCsv(List<ProjectMember> members, String filename, HttpServletResponse response) throws IOException {
        // 实现项目成员导出到CSV的逻辑
    }
}