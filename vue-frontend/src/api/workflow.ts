import { api } from '@/utils/request'

export interface Workflow {
  id?: number
  name: string
  description?: string
  category?: string
  createdBy?: number
  definition?: string
  version?: string
  status?: string
  isTemplate?: boolean
  triggerType?: string
  triggerCondition?: string
  createdAt?: string
  updatedAt?: string
}

export interface WorkflowInstance {
  id?: number
  workflowId?: number
  instanceName?: string
  initiatedBy?: number
  status?: string
  currentStep?: string
  variables?: string
  executionLog?: string
  startedAt?: string
  completedAt?: string
  createdAt?: string
  updatedAt?: string
}

export const workflowApi = {
  // 工作流管理
  createWorkflow(workflow: Workflow) {
    return api.post('/workflows', workflow)
  },

  updateWorkflow(id: number, workflow: Workflow) {
    return api.put(`/workflows/${id}`, workflow)
  },

  deleteWorkflow(id: number) {
    return api.delete(`/workflows/${id}`)
  },

  getWorkflow(id: number) {
    return api.get(`/workflows/${id}`)
  },

  getMyWorkflows() {
    return api.get('/workflows/my')
  },

  getActiveWorkflows() {
    return api.get('/workflows/active')
  },

  getWorkflowTemplates() {
    return api.get('/workflows/templates')
  },

  startWorkflow(id: number, variables?: Record<string, any>) {
    return api.post(`/workflows/${id}/start`, variables)
  },

  duplicateWorkflow(id: number) {
    return api.post(`/workflows/${id}/duplicate`)
  },

  searchWorkflows(keyword: string) {
    return api.get('/workflows/search', {
      params: { keyword }
    })
  },

  validateWorkflowDefinition(definition: string) {
    return api.post('/workflows/validate', { definition })
  },

  getWorkflowStatistics() {
    return api.get('/workflows/statistics')
  },

  // 工作流实例管理
  getMyInstances() {
    return api.get('/workflows/instances/my')
  },

  getInstance(id: number) {
    return api.get(`/workflows/instances/${id}`)
  },

  executeNextStep(id: number, variables?: Record<string, any>) {
    return api.post(`/workflows/instances/${id}/next`, variables)
  },

  completeWorkflow(id: number) {
    return api.post(`/workflows/instances/${id}/complete`)
  },

  cancelWorkflow(id: number) {
    return api.post(`/workflows/instances/${id}/cancel`)
  },

  getRunningInstances() {
    return api.get('/workflows/instances/running')
  }
}

export const exportApi = {
  exportUsers(format: 'excel' | 'csv' = 'excel') {
    return api.get('/export/users', {
      params: { format },
      responseType: 'blob'
    })
  },

  exportProjects(format: 'excel' | 'csv' = 'excel') {
    return api.get('/export/projects', {
      params: { format },
      responseType: 'blob'
    })
  },

  exportTasks(projectId?: number, format: 'excel' | 'csv' = 'excel') {
    return api.get('/export/tasks', {
      params: { projectId, format },
      responseType: 'blob'
    })
  },

  exportSystemLogs(startDate?: string, endDate?: string, format: 'excel' | 'csv' = 'excel') {
    return api.get('/export/system-logs', {
      params: { startDate, endDate, format },
      responseType: 'blob'
    })
  },

  exportCalendarEvents(startDate?: string, endDate?: string, format: 'excel' | 'csv' = 'excel') {
    return api.get('/export/calendar-events', {
      params: { startDate, endDate, format },
      responseType: 'blob'
    })
  },

  exportDepartments(format: 'excel' | 'csv' = 'excel') {
    return api.get('/export/departments', {
      params: { format },
      responseType: 'blob'
    })
  }
}

// 默认导出
const workflowModule = {
  workflow: workflowApi,
  export: exportApi
}

export default workflowModule