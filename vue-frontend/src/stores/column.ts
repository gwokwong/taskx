import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/services/api'

export interface Column {
  id: number
  projectId: number
  name: string
  color?: string
  sort: number
  createdAt: string
  updatedAt: string
}

export const useColumnStore = defineStore('column', () => {
  const columns = ref<Column[]>([])
  const loading = ref(false)

  const getColumns = async (projectId: number) => {
    loading.value = true
    try {
      const response = await api.get(`/projects/${projectId}/columns`)
      if (response.data.ret) {
        columns.value = response.data.data
      }
    } catch (error) {
      console.error('获取列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const createColumn = async (columnData: Partial<Column>) => {
    try {
      const response = await api.post('/columns', columnData)
      if (response.data.ret) {
        columns.value.push(response.data.data)
        return response.data.data
      }
      throw new Error(response.data.msg || '创建列表失败')
    } catch (error) {
      console.error('创建列表失败:', error)
      throw error
    }
  }

  const updateColumn = async (id: number, columnData: Partial<Column>) => {
    try {
      const response = await api.put(`/columns/${id}`, columnData)
      if (response.data.ret) {
        const index = columns.value.findIndex(c => c.id === id)
        if (index > -1) {
          columns.value[index] = { ...columns.value[index], ...response.data.data }
        }
        return response.data.data
      }
      throw new Error(response.data.msg || '更新列表失败')
    } catch (error) {
      console.error('更新列表失败:', error)
      throw error
    }
  }

  const deleteColumn = async (id: number) => {
    try {
      const response = await api.delete(`/columns/${id}`)
      if (response.data.ret) {
        columns.value = columns.value.filter(c => c.id !== id)
      }
      throw new Error(response.data.msg || '删除列表失败')
    } catch (error) {
      console.error('删除列表失败:', error)
      throw error
    }
  }

  return {
    columns,
    loading,
    getColumns,
    createColumn,
    updateColumn,
    deleteColumn
  }
})