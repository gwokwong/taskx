package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.Workflow;
import com.dootask.backend.entity.WorkflowInstance;
import com.dootask.backend.mapper.WorkflowMapper;
import com.dootask.backend.mapper.WorkflowInstanceMapper;
import com.dootask.backend.service.WorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl extends ServiceImpl<WorkflowMapper, Workflow> implements WorkflowService {

    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Workflow createWorkflow(Workflow workflow) {
        if (!StringUtils.hasText(workflow.getName())) {
            throw new ApiException("工作流名称不能为空");
        }

        if (workflow.getCreatedBy() == null) {
            throw new ApiException("创建者不能为空");
        }

        // 验证工作流定义
        if (StringUtils.hasText(workflow.getDefinition())) {
            validateWorkflowDefinition(workflow.getDefinition());
        }

        if (workflow.getStatus() == null) {
            workflow.setStatus("draft");
        }

        if (workflow.getIsTemplate() == null) {
            workflow.setIsTemplate(false);
        }

        if (workflow.getVersion() == null) {
            workflow.setVersion("1.0");
        }

        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());

        save(workflow);
        return workflow;
    }

    @Override
    public Workflow updateWorkflow(Workflow workflow) {
        if (workflow.getId() == null) {
            throw new ApiException("工作流ID不能为空");
        }

        Workflow existing = getById(workflow.getId());
        if (existing == null) {
            throw new ApiException("工作流不存在");
        }

        // 验证工作流定义
        if (StringUtils.hasText(workflow.getDefinition())) {
            validateWorkflowDefinition(workflow.getDefinition());
        }

        workflow.setUpdatedAt(LocalDateTime.now());
        updateById(workflow);
        return workflow;
    }

    @Override
    public void deleteWorkflow(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException("工作流ID不能为空");
        }

        Workflow workflow = getById(id);
        if (workflow == null) {
            throw new ApiException("工作流不存在");
        }

        // 检查是否有运行中的实例
        QueryWrapper<WorkflowInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("workflow_id", id)
                .in("status", "pending", "running");

        long runningInstances = workflowInstanceMapper.selectCount(queryWrapper);
        if (runningInstances > 0) {
            throw new ApiException("存在运行中的工作流实例，无法删除");
        }

        removeById(id);
    }

    @Override
    public Workflow getWorkflowById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return getById(id);
    }

    @Override
    public List<Workflow> getUserWorkflows(Long userId) {
        if (userId == null) {
            return List.of();
        }

        QueryWrapper<Workflow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("created_by", userId)
                .orderByDesc("updated_at");
        return list(queryWrapper);
    }

    @Override
    public List<Workflow> getActiveWorkflows() {
        QueryWrapper<Workflow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "active")
                .orderByDesc("updated_at");
        return list(queryWrapper);
    }

    @Override
    public List<Workflow> getWorkflowTemplates() {
        QueryWrapper<Workflow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_template", true)
                .eq("status", "active")
                .orderByDesc("updated_at");
        return list(queryWrapper);
    }

    @Override
    public WorkflowInstance startWorkflow(Long workflowId, Map<String, Object> variables, Long userId) {
        if (workflowId == null || userId == null) {
            throw new ApiException("参数不能为空");
        }

        Workflow workflow = getById(workflowId);
        if (workflow == null) {
            throw new ApiException("工作流不存在");
        }

        if (!"active".equals(workflow.getStatus())) {
            throw new ApiException("工作流未激活");
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowId(workflowId);
        instance.setInstanceName(workflow.getName() + " - " + LocalDateTime.now().toString());
        instance.setInitiatedBy(userId);
        instance.setStatus("running");
        instance.setCurrentStep("start");

        try {
            instance.setVariables(objectMapper.writeValueAsString(variables != null ? variables : new HashMap<>()));
            instance.setExecutionLog(objectMapper.writeValueAsString(List.of(
                Map.of("step", "start", "timestamp", LocalDateTime.now(), "message", "工作流已启动")
            )));
        } catch (Exception e) {
            throw new ApiException("变量序列化失败");
        }

        instance.setStartedAt(LocalDateTime.now());
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());

        workflowInstanceMapper.insert(instance);
        return instance;
    }

    @Override
    public WorkflowInstance executeNextStep(Long instanceId, Map<String, Object> variables) {
        if (instanceId == null) {
            throw new ApiException("实例ID不能为空");
        }

        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ApiException("工作流实例不存在");
        }

        if (!"running".equals(instance.getStatus())) {
            throw new ApiException("工作流实例未在运行中");
        }

        // 简化的步骤执行逻辑
        // 实际实现需要根据工作流定义解析和执行步骤
        instance.setUpdatedAt(LocalDateTime.now());
        workflowInstanceMapper.updateById(instance);

        return instance;
    }

    @Override
    public void completeWorkflow(Long instanceId) {
        if (instanceId == null) {
            throw new ApiException("实例ID不能为空");
        }

        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ApiException("工作流实例不存在");
        }

        instance.setStatus("completed");
        instance.setCompletedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        workflowInstanceMapper.updateById(instance);
    }

    @Override
    public void cancelWorkflow(Long instanceId) {
        if (instanceId == null) {
            throw new ApiException("实例ID不能为空");
        }

        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ApiException("工作流实例不存在");
        }

        instance.setStatus("cancelled");
        instance.setCompletedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        workflowInstanceMapper.updateById(instance);
    }

    @Override
    public List<WorkflowInstance> getUserInstances(Long userId) {
        if (userId == null) {
            return List.of();
        }

        QueryWrapper<WorkflowInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("initiated_by", userId)
                .orderByDesc("created_at");
        return workflowInstanceMapper.selectList(queryWrapper);
    }

    @Override
    public WorkflowInstance getInstanceById(Long instanceId) {
        if (instanceId == null || instanceId <= 0) {
            return null;
        }
        return workflowInstanceMapper.selectById(instanceId);
    }

    @Override
    public Map<String, Object> getWorkflowStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总工作流数
        long totalWorkflows = count();
        stats.put("totalWorkflows", totalWorkflows);

        // 活跃工作流数
        long activeWorkflows = count(new QueryWrapper<Workflow>().eq("status", "active"));
        stats.put("activeWorkflows", activeWorkflows);

        // 工作流模板数
        long templateWorkflows = count(new QueryWrapper<Workflow>().eq("is_template", true));
        stats.put("templateWorkflows", templateWorkflows);

        // 运行中的实例数
        QueryWrapper<WorkflowInstance> runningQuery = new QueryWrapper<>();
        runningQuery.eq("status", "running");
        long runningInstances = workflowInstanceMapper.selectCount(runningQuery);
        stats.put("runningInstances", runningInstances);

        // 已完成的实例数
        QueryWrapper<WorkflowInstance> completedQuery = new QueryWrapper<>();
        completedQuery.eq("status", "completed");
        long completedInstances = workflowInstanceMapper.selectCount(completedQuery);
        stats.put("completedInstances", completedInstances);

        return stats;
    }

    @Override
    public List<WorkflowInstance> getRunningInstances() {
        QueryWrapper<WorkflowInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "running")
                .orderByAsc("started_at");
        return workflowInstanceMapper.selectList(queryWrapper);
    }

    @Override
    public void validateWorkflowDefinition(String definition) {
        if (!StringUtils.hasText(definition)) {
            throw new ApiException("工作流定义不能为空");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(definition);

            // 基础验证
            if (!jsonNode.has("steps")) {
                throw new ApiException("工作流定义必须包含steps字段");
            }

            JsonNode steps = jsonNode.get("steps");
            if (!steps.isArray() || steps.size() == 0) {
                throw new ApiException("工作流至少需要一个步骤");
            }

            // 验证每个步骤
            for (JsonNode step : steps) {
                if (!step.has("id") || !step.has("type")) {
                    throw new ApiException("每个步骤必须包含id和type字段");
                }
            }

        } catch (Exception e) {
            throw new ApiException("工作流定义格式错误: " + e.getMessage());
        }
    }

    @Override
    public Workflow duplicateWorkflow(Long workflowId, Long userId) {
        if (workflowId == null || userId == null) {
            throw new ApiException("参数不能为空");
        }

        Workflow original = getById(workflowId);
        if (original == null) {
            throw new ApiException("工作流不存在");
        }

        Workflow duplicate = new Workflow();
        duplicate.setName(original.getName() + " (副本)");
        duplicate.setDescription(original.getDescription());
        duplicate.setCategory(original.getCategory());
        duplicate.setCreatedBy(userId);
        duplicate.setDefinition(original.getDefinition());
        duplicate.setStatus("draft");
        duplicate.setIsTemplate(false);
        duplicate.setTriggerType(original.getTriggerType());
        duplicate.setTriggerCondition(original.getTriggerCondition());
        duplicate.setVersion("1.0");

        return createWorkflow(duplicate);
    }

    @Override
    public List<Workflow> searchWorkflows(String keyword, Long userId) {
        QueryWrapper<Workflow> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("description", keyword)
                    .or()
                    .like("category", keyword));
        }

        if (userId != null) {
            queryWrapper.eq("created_by", userId);
        }

        queryWrapper.orderByDesc("updated_at");
        return list(queryWrapper);
    }
}