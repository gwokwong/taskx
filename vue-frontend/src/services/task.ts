import { api } from './api'
import type { Task } from '@/stores/task'

export const taskService = {
  // 获取任务列表
  getTasks: (projectId?: number, params?: any) => {
    const url = projectId ? `/projects/${projectId}/tasks` : '/tasks'
    return api.get(url, { params })
  },

  // 获取任务详情
  getTask: (id: number) => {
    return api.get(`/tasks/${id}`)
  },

  // 创建任务
  createTask: (data: Partial<Task>) => {
    return api.post('/tasks', data)
  },

  // 更新任务
  updateTask: (id: number, data: Partial<Task>) => {
    return api.put(`/tasks/${id}`, data)
  },

  // 删除任务
  deleteTask: (id: number) => {
    return api.delete(`/tasks/${id}`)
  },

  // 完成任务
  completeTask: (id: number) => {
    return api.post(`/tasks/${id}/complete`)
  },

  // 移动任务
  moveTask: (id: number, columnId: number, sort?: number) => {
    return api.post(`/tasks/${id}/move`, { columnId, sort })
  },

  // 分配任务
  assignTask: (id: number, userIds: number[]) => {
    return api.post(`/tasks/${id}/assign`, { userIds })
  },

  // 获取任务浏览历史
  getTaskBrowseHistory: (limit = 20) => {
    return api.get('/tasks/browse-history', { params: { limit } })
  }
}