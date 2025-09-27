import { api } from '@/utils/request'

export interface TaskTemplate {
  id?: number
  name: string
  description?: string
  category?: string
  createdBy?: number
  template?: string
  tags?: string
  isPublic?: boolean
  useCount?: number
  status?: string
  version?: string
  createdAt?: string
  updatedAt?: string
}

export const taskTemplateApi = {
  createTemplate(template: TaskTemplate) {
    return api.post('/task-templates', template)
  },

  updateTemplate(id: number, template: TaskTemplate) {
    return api.put(`/task-templates/${id}`, template)
  },

  deleteTemplate(id: number) {
    return api.delete(`/task-templates/${id}`)
  },

  getTemplate(id: number) {
    return api.get(`/task-templates/${id}`)
  },

  getAllTemplates() {
    return api.get('/task-templates')
  },

  getPublicTemplates() {
    return api.get('/task-templates/public')
  },

  getMyTemplates() {
    return api.get('/task-templates/my')
  },

  getTemplatesByCategory(category: string) {
    return api.get(`/task-templates/category/${category}`)
  },

  searchTemplates(keyword: string) {
    return api.get('/task-templates/search', {
      params: { keyword }
    })
  },

  duplicateTemplate(id: number) {
    return api.post(`/task-templates/${id}/duplicate`)
  },

  useTemplate(id: number) {
    return api.post(`/task-templates/${id}/use`)
  },

  shareTemplate(id: number, isPublic: boolean) {
    return api.post(`/task-templates/${id}/share`, null, {
      params: { isPublic }
    })
  },

  getTemplateStatistics() {
    return api.get('/task-templates/statistics')
  },

  getPopularTemplates(limit = 10) {
    return api.get('/task-templates/popular', {
      params: { limit }
    })
  },

  getTemplateCategories() {
    return api.get('/task-templates/categories')
  },

  getRecentTemplates(limit = 5) {
    return api.get('/task-templates/recent', {
      params: { limit }
    })
  },

  createFromTask(taskId: number, name: string, description?: string) {
    return api.post('/task-templates/from-task', null, {
      params: { taskId, name, description }
    })
  }
}

export default taskTemplateApi