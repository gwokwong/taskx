package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.dto.StatisticsDto;
import com.dootask.backend.service.StatisticsService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;

@Tag(name = "数据统计", description = "数据统计和图表相关接口")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @Operation(summary = "获取用户总览统计")
    @GetMapping("/overview")
    public Result<StatisticsDto> getOverviewStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        StatisticsDto statistics = statisticsService.getOverviewStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取系统统计(管理员)")
    @GetMapping("/system")
    public Result<StatisticsDto> getSystemStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        StatisticsDto statistics = statisticsService.getSystemStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取任务状态统计")
    @GetMapping("/tasks/status")
    public Result<Map<String, Long>> getTaskStatusStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Long> statistics = statisticsService.getTaskStatusStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取项目状态统计")
    @GetMapping("/projects/status")
    public Result<Map<String, Long>> getProjectStatusStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Long> statistics = statisticsService.getProjectStatusStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取每日任务统计")
    @GetMapping("/tasks/daily")
    public Result<Map<LocalDate, Long>> getDailyTaskStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<LocalDate, Long> statistics = statisticsService.getDailyTaskStatistics(startDate, endDate, userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取每日项目统计")
    @GetMapping("/projects/daily")
    public Result<Map<LocalDate, Long>> getDailyProjectStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<LocalDate, Long> statistics = statisticsService.getDailyProjectStatistics(startDate, endDate, userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取用户活跃度统计(管理员)")
    @GetMapping("/users/activity")
    public Result<Map<String, Long>> getUserActivityStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        Map<String, Long> statistics = statisticsService.getUserActivityStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取文件类型统计")
    @GetMapping("/files/types")
    public Result<Map<String, Long>> getFileTypeStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Long> statistics = statisticsService.getFileTypeStatistics(userId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取存储使用统计")
    @GetMapping("/storage")
    public Result<Map<String, Object>> getStorageStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> statistics = statisticsService.getStorageStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取系统性能统计(管理员)")
    @GetMapping("/performance")
    public Result<Map<String, Object>> getSystemPerformanceStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        // TODO: 验证管理员权限

        Map<String, Object> statistics = statisticsService.getSystemPerformanceStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "获取项目统计分析")
    @GetMapping("/projects/{projectId}/analysis")
    public Result<Map<String, Object>> getProjectAnalytics(
            @PathVariable Long projectId,
            @RequestParam(required = false) String period,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> analytics = statisticsService.getProjectAnalytics(projectId, period != null ? period : "month", userId);
        return Result.success(analytics);
    }

    @Operation(summary = "获取项目成员性能统计")
    @GetMapping("/projects/{projectId}/members/performance")
    public Result<Map<String, Object>> getProjectMemberPerformance(
            @PathVariable Long projectId,
            @RequestParam(required = false) String period,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> performance = statisticsService.getProjectMemberPerformance(projectId, period != null ? period : "month", userId);
        return Result.success(performance);
    }

    @Operation(summary = "获取项目趋势分析")
    @GetMapping("/projects/{projectId}/trends")
    public Result<Map<String, Object>> getProjectTrends(
            @PathVariable Long projectId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String metric,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> trends = statisticsService.getProjectTrends(projectId, period != null ? period : "month", metric != null ? metric : "tasks", userId);
        return Result.success(trends);
    }

    @Operation(summary = "获取项目健康度分析")
    @GetMapping("/projects/{projectId}/health")
    public Result<Map<String, Object>> getProjectHealth(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> health = statisticsService.getProjectHealth(projectId, userId);
        return Result.success(health);
    }

    @Operation(summary = "获取项目风险分析")
    @GetMapping("/projects/{projectId}/risks")
    public Result<Map<String, Object>> getProjectRisks(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> risks = statisticsService.getProjectRisks(projectId, userId);
        return Result.success(risks);
    }

    @Operation(summary = "获取用户效率分析")
    @GetMapping("/users/{targetUserId}/productivity")
    public Result<Map<String, Object>> getUserProductivity(
            @PathVariable Long targetUserId,
            @RequestParam(required = false) String period,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> productivity = statisticsService.getUserProductivity(targetUserId, period != null ? period : "month", userId);
        return Result.success(productivity);
    }

    @Operation(summary = "获取实时统计仪表盘")
    @GetMapping("/dashboard/realtime")
    public Result<Map<String, Object>> getRealtimeDashboard(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> dashboard = statisticsService.getRealtimeDashboard(userId);
        return Result.success(dashboard);
    }

    @Operation(summary = "获取对比分析")
    @GetMapping("/comparison")
    public Result<Map<String, Object>> getComparisonAnalysis(
            @RequestParam String type,
            @RequestParam String period1,
            @RequestParam String period2,
            @RequestParam(required = false) Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> comparison = statisticsService.getComparisonAnalysis(type, period1, period2, projectId, userId);
        return Result.success(comparison);
    }

    @Operation(summary = "获取预测分析")
    @GetMapping("/prediction")
    public Result<Map<String, Object>> getPredictionAnalysis(
            @RequestParam String type,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer days,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> prediction = statisticsService.getPredictionAnalysis(type, projectId, days != null ? days : 30, userId);
        return Result.success(prediction);
    }

    @Operation(summary = "生成统计报告")
    @PostMapping("/reports/generate")
    public Result<Map<String, Object>> generateStatisticsReport(
            @RequestBody GenerateReportRequest reportRequest,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> report = statisticsService.generateStatisticsReport(reportRequest, userId);
        return Result.success("报告生成成功", report);
    }

    @Operation(summary = "获取已生成报告列表")
    @GetMapping("/reports")
    public Result<Map<String, Object>> getGeneratedReports(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Map<String, Object> reports = statisticsService.getGeneratedReports(type, page != null ? page : 1, size != null ? size : 10, userId);
        return Result.success(reports);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }

    public static class GenerateReportRequest {
        private String reportType; // project, user, system, custom
        private String period; // day, week, month, quarter, year, custom
        private String format; // json, excel, pdf
        private Long projectId;
        private Long userId;
        private String startDate;
        private String endDate;
        private List<String> metrics;
        private Map<String, Object> filters;

        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public List<String> getMetrics() { return metrics; }
        public void setMetrics(List<String> metrics) { this.metrics = metrics; }
        public Map<String, Object> getFilters() { return filters; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }
    }
}