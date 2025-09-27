package com.dootask.backend.service;

import com.dootask.backend.entity.Workflow;
import com.dootask.backend.entity.WorkflowInstance;

import java.util.List;
import java.util.Map;

public interface WorkflowService {

    Workflow createWorkflow(Workflow workflow);

    Workflow updateWorkflow(Workflow workflow);

    void deleteWorkflow(Long id);

    Workflow getWorkflowById(Long id);

    List<Workflow> getUserWorkflows(Long userId);

    List<Workflow> getActiveWorkflows();

    List<Workflow> getWorkflowTemplates();

    WorkflowInstance startWorkflow(Long workflowId, Map<String, Object> variables, Long userId);

    WorkflowInstance executeNextStep(Long instanceId, Map<String, Object> variables);

    void completeWorkflow(Long instanceId);

    void cancelWorkflow(Long instanceId);

    List<WorkflowInstance> getUserInstances(Long userId);

    WorkflowInstance getInstanceById(Long instanceId);

    Map<String, Object> getWorkflowStatistics();

    List<WorkflowInstance> getRunningInstances();

    void validateWorkflowDefinition(String definition);

    Workflow duplicateWorkflow(Long workflowId, Long userId);

    List<Workflow> searchWorkflows(String keyword, Long userId);
}