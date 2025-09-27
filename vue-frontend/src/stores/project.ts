import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/services/api'

export interface Project {
  id: number
  name: string
  desc?: string
  ownerUserid: number
  dialogId?: number
  columns?: string
  archivedAt?: string
  archivedUserid?: number
  taskNum: number
  taskComplete: number
  taskPercent: number
  sort: number
  topAt?: string
  createdAt: string
  updatedAt: string
}

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)

  const getProjects = async () => {
    loading.value = true
    try {
      const response = await api.get('/projects')
      if (response.data.ret) {
        projects.value = response.data.data
      }
    } catch (error) {
      console.error('获取项目列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const getProject = async (id: number) => {
    loading.value = true
    try {
      const response = await api.get(`/projects/${id}`)
      if (response.data.ret) {
        currentProject.value = response.data.data
        return response.data.data
      }
    } catch (error) {
      console.error('获取项目详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  const createProject = async (projectData: Partial<Project>) => {
    try {
      const response = await api.post('/projects', projectData)
      if (response.data.ret) {
        projects.value.unshift(response.data.data)
        return response.data.data
      }
      throw new Error(response.data.msg || '创建项目失败')
    } catch (error) {
      console.error('创建项目失败:', error)
      throw error
    }
  }

  const updateProject = async (id: number, projectData: Partial<Project>) => {
    try {
      const response = await api.put(`/projects/${id}`, projectData)
      if (response.data.ret) {
        const index = projects.value.findIndex(p => p.id === id)
        if (index > -1) {
          projects.value[index] = { ...projects.value[index], ...response.data.data }
        }
        if (currentProject.value?.id === id) {
          currentProject.value = { ...currentProject.value, ...response.data.data }
        }
        return response.data.data
      }
      throw new Error(response.data.msg || '更新项目失败')
    } catch (error) {
      console.error('更新项目失败:', error)
      throw error
    }
  }

  const deleteProject = async (id: number) => {
    try {
      const response = await api.delete(`/projects/${id}`)
      if (response.data.ret) {
        projects.value = projects.value.filter(p => p.id !== id)
        if (currentProject.value?.id === id) {
          currentProject.value = null
        }
      }
      throw new Error(response.data.msg || '删除项目失败')
    } catch (error) {
      console.error('删除项目失败:', error)
      throw error
    }
  }

  return {
    projects,
    currentProject,
    loading,
    getProjects,
    getProject,
    createProject,
    updateProject,
    deleteProject
  }
})