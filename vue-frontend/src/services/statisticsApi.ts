import { api } from './api'

export interface StatisticsData {
  totalProjects: number
  totalTasks: number
  totalFiles: number
  totalUsers: number
  taskStatusStats: Record<string, number>
  projectStatusStats: Record<string, number>
  userActivityStats: Record<string, number>
  dailyTaskStats: Record<string, number>
  dailyProjectStats: Record<string, number>
  fileTypeStats: Record<string, number>
  totalStorageUsed: number
  storageLimit: number
  systemPerformance: Record<string, any>
}

export const statisticsApi = {
  async getOverviewStatistics(): Promise<StatisticsData> {
    const response = await api.get('/statistics/overview')
    return response.data.data
  },

  async getSystemStatistics(): Promise<StatisticsData> {
    const response = await api.get('/statistics/system')
    return response.data.data
  },

  async getTaskStatusStatistics(): Promise<Record<string, number>> {
    const response = await api.get('/statistics/tasks/status')
    return response.data.data
  },

  async getProjectStatusStatistics(): Promise<Record<string, number>> {
    const response = await api.get('/statistics/projects/status')
    return response.data.data
  },

  async getDailyTaskStatistics(startDate: string, endDate: string): Promise<Record<string, number>> {
    const response = await api.get('/statistics/tasks/daily', {
      params: { startDate, endDate }
    })
    return response.data.data
  },

  async getDailyProjectStatistics(startDate: string, endDate: string): Promise<Record<string, number>> {
    const response = await api.get('/statistics/projects/daily', {
      params: { startDate, endDate }
    })
    return response.data.data
  },

  async getUserActivityStatistics(): Promise<Record<string, number>> {
    const response = await api.get('/statistics/users/activity')
    return response.data.data
  },

  async getFileTypeStatistics(): Promise<Record<string, number>> {
    const response = await api.get('/statistics/files/types')
    return response.data.data
  },

  async getStorageStatistics(): Promise<Record<string, any>> {
    const response = await api.get('/statistics/storage')
    return response.data.data
  },

  async getSystemPerformanceStatistics(): Promise<Record<string, any>> {
    const response = await api.get('/statistics/performance')
    return response.data.data
  }
}

export default statisticsApi