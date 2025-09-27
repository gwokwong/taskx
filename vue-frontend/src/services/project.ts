import { api } from './api'
import type { Project } from '@/stores/project'

export const projectService = {
  // 获取项目列表
  getProjects: (params?: any) => {
    return api.get('/projects', { params })
  },

  // 获取项目详情
  getProject: (id: number) => {
    return api.get(`/projects/${id}`)
  },

  // 创建项目
  createProject: (data: Partial<Project>) => {
    return api.post('/projects', data)
  },

  // 更新项目
  updateProject: (id: number, data: Partial<Project>) => {
    return api.put(`/projects/${id}`, data)
  },

  // 删除项目
  deleteProject: (id: number) => {
    return api.delete(`/projects/${id}`)
  },

  // 获取项目统计
  getProjectStats: (id: number) => {
    return api.get(`/projects/${id}/stats`)
  },

  // 置顶项目
  topProject: (id: number) => {
    return api.post(`/projects/${id}/top`)
  },

  // 归档项目
  archiveProject: (id: number) => {
    return api.post(`/projects/${id}/archive`)
  }
}