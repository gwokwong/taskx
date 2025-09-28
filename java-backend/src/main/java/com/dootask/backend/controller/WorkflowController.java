package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.Workflow;
import com.dootask.backend.entity.WorkflowInstance;
import com.dootask.backend.service.WorkflowService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "工作流管理", description = "工作流引擎相关接口")
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final UserService userService;

    @Operation(summary = "创建工作流")
    @PostMapping
    public Result<Workflow> createWorkflow(@RequestBody Workflow workflow, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        workflow.setCreatedBy(userId);
        Workflow created = workflowService.createWorkflow(workflow);
        return Result.success(created);
    }

    @Operation(summary = "更新工作流")
    @PutMapping("/{id}")
    public Result<Workflow> updateWorkflow(@PathVariable Long id, @RequestBody Workflow workflow, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Workflow existing = workflowService.getWorkflowById(id);
        if (existing == null) {
            return Result.error("工作流不存在");
        }

        // 检查权限：只能编辑自己创建的工作流
        if (!existing.getCreatedBy().equals(userId) && !userService.isAdmin(userId)) {
            return Result.error("无权限编辑此工作流");
        }

        workflow.setId(id);
        workflow.setCreatedBy(existing.getCreatedBy());
        Workflow updated = workflowService.updateWorkflow(workflow);
        return Result.success(updated);
    }

    @Operation(summary = "删除工作流")
    @DeleteMapping("/{id}")
    public Result<String> deleteWorkflow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Workflow workflow = workflowService.getWorkflowById(id);
        if (workflow == null) {
            return Result.error("工作流不存在");
        }

        // 检查权限：只能删除自己创建的工作流
        if (!workflow.getCreatedBy().equals(userId) && !userService.isAdmin(userId)) {
            return Result.error("无权限删除此工作流");
        }

        workflowService.deleteWorkflow(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取工作流详情")
    @GetMapping("/{id}")
    public Result<Workflow> getWorkflow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Workflow workflow = workflowService.getWorkflowById(id);
        if (workflow == null) {
            return Result.error("工作流不存在");
        }

        return Result.success(workflow);
    }

    @Operation(summary = "获取我的工作流")
    @GetMapping("/my")
    public Result<List<Workflow>> getMyWorkflows(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Workflow> workflows = workflowService.getUserWorkflows(userId);
        return Result.success(workflows);
    }

    @Operation(summary = "获取活跃工作流")
    @GetMapping("/active")
    public Result<List<Workflow>> getActiveWorkflows(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Workflow> workflows = workflowService.getActiveWorkflows();
        return Result.success(workflows);
    }

    @Operation(summary = "获取工作流模板")
    @GetMapping("/templates")
    public Result<List<Workflow>> getWorkflowTemplates(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Workflow> templates = workflowService.getWorkflowTemplates();
        return Result.success(templates);
    }

    @Operation(summary = "启动工作流")
    @PostMapping("/{id}/start")
    public Result<WorkflowInstance> startWorkflow(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> variables,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        WorkflowInstance instance = workflowService.startWorkflow(id, variables, userId);
        return Result.success(instance);
    }

    @Operation(summary = "复制工作流")
    @PostMapping("/{id}/duplicate")
    public Result<Workflow> duplicateWorkflow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        Workflow duplicated = workflowService.duplicateWorkflow(id, userId);
        return Result.success(duplicated);
    }

    @Operation(summary = "搜索工作流")
    @GetMapping("/search")
    public Result<List<Workflow>> searchWorkflows(@RequestParam String keyword, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<Workflow> workflows = workflowService.searchWorkflows(keyword, userId);
        return Result.success(workflows);
    }

    @Operation(summary = "验证工作流定义")
    @PostMapping("/validate")
    public Result<String> validateWorkflowDefinition(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        try {
            String definition = request.get("definition");
            workflowService.validateWorkflowDefinition(definition);
            return Result.success("工作流定义验证通过");
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取工作流统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getWorkflowStatistics(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        Map<String, Object> stats = workflowService.getWorkflowStatistics();
        return Result.success(stats);
    }

    // 工作流实例管理接口

    @Operation(summary = "获取我的工作流实例")
    @GetMapping("/instances/my")
    public Result<List<WorkflowInstance>> getMyInstances(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<WorkflowInstance> instances = workflowService.getUserInstances(userId);
        return Result.success(instances);
    }

    @Operation(summary = "获取工作流实例详情")
    @GetMapping("/instances/{id}")
    public Result<WorkflowInstance> getInstance(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        WorkflowInstance instance = workflowService.getInstanceById(id);
        if (instance == null) {
            return Result.error("工作流实例不存在");
        }

        // 检查权限：只能查看自己发起的实例
        if (!instance.getInitiatedBy().equals(userId) && !userService.isAdmin(userId)) {
            return Result.error("无权限查看此实例");
        }

        return Result.success(instance);
    }

    @Operation(summary = "执行下一步")
    @PostMapping("/instances/{id}/next")
    public Result<WorkflowInstance> executeNextStep(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> variables,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        WorkflowInstance instance = workflowService.getInstanceById(id);
        if (instance == null) {
            return Result.error("工作流实例不存在");
        }

        // 检查权限：只能操作自己发起的实例
        if (!instance.getInitiatedBy().equals(userId)) {
            return Result.error("无权限操作此实例");
        }

        WorkflowInstance updated = workflowService.executeNextStep(id, variables);
        return Result.success(updated);
    }

    @Operation(summary = "完成工作流")
    @PostMapping("/instances/{id}/complete")
    public Result<String> completeWorkflow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        WorkflowInstance instance = workflowService.getInstanceById(id);
        if (instance == null) {
            return Result.error("工作流实例不存在");
        }

        // 检查权限：只能操作自己发起的实例
        if (!instance.getInitiatedBy().equals(userId)) {
            return Result.error("无权限操作此实例");
        }

        workflowService.completeWorkflow(id);
        return Result.success("工作流已完成");
    }

    @Operation(summary = "取消工作流")
    @PostMapping("/instances/{id}/cancel")
    public Result<String> cancelWorkflow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        WorkflowInstance instance = workflowService.getInstanceById(id);
        if (instance == null) {
            return Result.error("工作流实例不存在");
        }

        // 检查权限：只能操作自己发起的实例
        if (!instance.getInitiatedBy().equals(userId)) {
            return Result.error("无权限操作此实例");
        }

        workflowService.cancelWorkflow(id);
        return Result.success("工作流已取消");
    }

    @Operation(summary = "获取运行中的实例(管理员)")
    @GetMapping("/instances/running")
    public Result<List<WorkflowInstance>> getRunningInstances(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null || !userService.isAdmin(userId)) {
            return Result.error("无权限访问");
        }

        List<WorkflowInstance> instances = workflowService.getRunningInstances();
        return Result.success(instances);
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
}