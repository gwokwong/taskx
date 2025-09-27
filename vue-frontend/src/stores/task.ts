import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/services/api'

export interface Task {
  id: number
  projectId: number
  columnId: number
  parentId?: number
  name: string
  content?: string
  desc?: string
  color?: string
  times: number
  level: number
  completeAt?: string
  archivedAt?: string
  archivedUserid?: number
  startAt?: string
  endAt?: string
  owner: string[]
  assist: string[]
  subtasks: number
  subtasksComplete: number
  sort: number
  dialogId?: number
  flowItemId?: number
  flowItemName?: string
  flowItemStatus?: string
  flowItemColor?: string
  createdAt: string
  updatedAt: string
}

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([])
  const currentTask = ref<Task | null>(null)
  const loading = ref(false)

  const getTasks = async (projectId?: number) => {
    loading.value = true
    try {
      const url = projectId ? `/projects/${projectId}/tasks` : '/tasks'
      const response = await api.get(url)
      if (response.data.ret) {
        tasks.value = response.data.data
      }
    } catch (error) {
      console.error('获取任务列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const getTask = async (id: number) => {
    loading.value = true
    try {
      const response = await api.get(`/tasks/${id}`)
      if (response.data.ret) {
        currentTask.value = response.data.data
        return response.data.data
      }
    } catch (error) {
      console.error('获取任务详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  const createTask = async (taskData: Partial<Task>) => {
    try {
      const response = await api.post('/tasks', taskData)
      if (response.data.ret) {
        tasks.value.push(response.data.data)
        return response.data.data
      }
      throw new Error(response.data.msg || '创建任务失败')
    } catch (error) {
      console.error('创建任务失败:', error)
      throw error
    }
  }

  const updateTask = async (id: number, taskData: Partial<Task>) => {
    try {
      const response = await api.put(`/tasks/${id}`, taskData)
      if (response.data.ret) {
        const index = tasks.value.findIndex(t => t.id === id)
        if (index > -1) {
          tasks.value[index] = { ...tasks.value[index], ...response.data.data }
        }
        if (currentTask.value?.id === id) {
          currentTask.value = { ...currentTask.value, ...response.data.data }
        }
        return response.data.data
      }
      throw new Error(response.data.msg || '更新任务失败')
    } catch (error) {
      console.error('更新任务失败:', error)
      throw error
    }
  }

  const deleteTask = async (id: number) => {
    try {
      const response = await api.delete(`/tasks/${id}`)
      if (response.data.ret) {
        tasks.value = tasks.value.filter(t => t.id !== id)
        if (currentTask.value?.id === id) {
          currentTask.value = null
        }
      }
      throw new Error(response.data.msg || '删除任务失败')
    } catch (error) {
      console.error('删除任务失败:', error)
      throw error
    }
  }

  const completeTask = async (id: number) => {
    return updateTask(id, { completeAt: new Date().toISOString() })
  }

  const getTasksByColumn = (columnId: number) => {
    return tasks.value.filter(task => task.columnId === columnId)
  }

  return {
    tasks,
    currentTask,
    loading,
    getTasks,
    getTask,
    createTask,
    updateTask,
    deleteTask,
    completeTask,
    getTasksByColumn
  }
})