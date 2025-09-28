import { api } from '@/utils/request'

export interface Task {
  id: number
  name: string
  desc?: string
  content?: string
  projectId: number
  columnId?: number
  parentId?: number
  priority: 'low' | 'medium' | 'high' | 'urgent'
  owner?: string
  assist?: string
  startAt?: string
  endAt?: string
  completeAt?: string
  archivedAt?: string
  archivedUserid?: number
  subtasks: number
  subtasksComplete: number
  level: number
  sort: number
  tags?: string[]
  attachmentsCount?: number
  commentsCount?: number
  flowItemId?: number
  flowItemName?: string
  flowItemStatus?: string
  flowItemColor?: string
  createdAt: string
  updatedAt: string
}

export interface TaskComment {
  id: number
  taskId: number
  userId: number
  content: string
  parentId?: number
  mentions?: number[]
  attachments?: any[]
  isDeleted: number
  createdAt: string
  updatedAt: string
  user?: {
    userid: number
    nickname: string
    user_img?: string
  }
  replies?: TaskComment[]
  isLiked?: boolean
  likeCount?: number
}

export interface TaskListParams {
  page?: number
  size?: number
  projectId?: number
  columnId?: number
  status?: string
  assigneeId?: number
  priority?: string
  keyword?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export interface CreateTaskData {
  name: string
  desc?: string
  content?: string
  projectId: number
  columnId?: number
  parentId?: number
  priority?: string
  owner?: string
  assist?: string
  startAt?: string
  endAt?: string
  tags?: string[]
  estimatedHours?: number
}

export interface UpdateTaskData extends Partial<CreateTaskData> {
  id?: number
}

export interface MoveTaskData {
  columnId: number
  sort?: number
}

export interface AssignTaskData {
  userIds: number[]
}

export interface BulkOperationData {
  taskIds: number[]
  action: 'complete' | 'archive' | 'delete' | 'assign'
  data?: any
}

// 任务相关API
export const taskApi = {
  // 获取任务列表
  getTasks: (params?: TaskListParams) =>
    api.get<{ records: Task[]; total: number }>('/api/tasks', { params }),

  // 根据项目获取任务
  getTasksByProject: (projectId: number, params?: TaskListParams) =>
    api.get<Task[]>(`/api/tasks/project/${projectId}`, { params }),

  // 根据列获取任务
  getTasksByColumn: (columnId: number) =>
    api.get<Task[]>(`/api/tasks/column/${columnId}`),

  // 获取任务详情
  getTask: (id: number) =>
    api.get<Task>(`/api/tasks/${id}`),

  // 创建任务
  createTask: (data: CreateTaskData) =>
    api.post<Task>('/api/tasks', data),

  // 更新任务
  updateTask: (id: number, data: UpdateTaskData) =>
    api.put<Task>(`/api/tasks/${id}`, data),

  // 删除任务
  deleteTask: (id: number) =>
    api.delete(`/api/tasks/${id}`),

  // 完成任务
  completeTask: (id: number) =>
    api.post(`/api/tasks/${id}/complete`),

  // 重新打开任务
  reopenTask: (id: number) =>
    api.post(`/api/tasks/${id}/reopen`),

  // 归档任务
  archiveTask: (id: number) =>
    api.post(`/api/tasks/${id}/archive`),

  // 移动任务
  moveTask: (id: number, data: MoveTaskData) =>
    api.post(`/api/tasks/${id}/move`, data),

  // 分配任务
  assignTask: (id: number, userIds: number[]) =>
    api.post(`/api/tasks/${id}/assign`, { userIds }),

  // 更新任务进度
  updateTaskProgress: (id: number, progress: number) =>
    api.post(`/api/tasks/${id}/progress`, { progress }),

  // 获取子任务
  getSubtasks: (parentId: number) =>
    api.get<Task[]>(`/api/tasks/${parentId}/subtasks`),

  // 创建子任务
  createSubtask: (parentId: number, data: CreateTaskData) =>
    api.post<Task>(`/api/tasks/${parentId}/subtasks`, data),

  // 获取任务评论
  getComments: (taskId: number) =>
    api.get(`/api/tasks/${taskId}/comments`),

  // 添加评论
  addComment: (taskId: number, data: { content: string }) =>
    api.post(`/api/tasks/${taskId}/comments`, data),

  // 获取任务附件
  getAttachments: (taskId: number) =>
    api.get(`/api/tasks/${taskId}/attachments`),

  // 获取任务历史
  getTaskHistory: (taskId: number) =>
    api.get(`/api/tasks/${taskId}/history`),

  // 获取任务浏览历史
  getTaskBrowseHistory: (limit = 20) =>
    api.get<Task[]>('/api/tasks/browse-history', { params: { limit } }),

  // 批量操作
  bulkOperation: (data: BulkOperationData) =>
    api.post('/api/tasks/bulk', data),

  // 批量分配任务
  bulkAssignTasks: (taskIds: number[], userIds: number[]) =>
    api.post('/api/tasks/bulk/assign', { taskIds, userIds }),

  // 批量添加分配者
  bulkAddAssignees: (taskIds: number[], userIds: number[]) =>
    api.post('/api/tasks/bulk/add-assignees', { taskIds, userIds }),

  // 批量移除分配者
  bulkRemoveAssignees: (taskIds: number[], userIds: number[]) =>
    api.post('/api/tasks/bulk/remove-assignees', { taskIds, userIds }),

  // 导出任务
  exportTasks: (params: { format: 'excel' | 'csv'; filters?: any }) =>
    api.post('/api/tasks/export', params, { responseType: 'blob' }),

  // 获取任务统计
  getTaskStats: (projectId?: number) =>
    api.get('/api/tasks/stats', { params: { projectId } }),

  // 获取任务模板
  getTaskTemplates: () =>
    api.get('/api/task-templates'),

  // 创建任务模板
  createTaskTemplate: (data: any) =>
    api.post('/api/task-templates', data),

  // 应用任务模板
  applyTaskTemplate: (templateId: number) =>
    api.post(`/api/task-templates/${templateId}/apply`),

  // 搜索任务
  searchTasks: (query: string, params?: any) =>
    api.get<Task[]>('/api/tasks/search', { params: { q: query, ...params } }),

  // 获取我的任务
  getMyTasks: (params?: { status?: string; priority?: string }) =>
    api.get<Task[]>('/api/tasks/my', { params }),

  // 获取分配给我的任务
  getAssignedToMe: (params?: any) =>
    api.get<Task[]>('/api/tasks/assigned-to-me', { params }),

  // 获取我创建的任务
  getCreatedByMe: (params?: any) =>
    api.get<Task[]>('/api/tasks/created-by-me', { params }),

  // 获取即将到期的任务
  getUpcomingTasks: (days = 7) =>
    api.get<Task[]>('/api/tasks/upcoming', { params: { days } }),

  // 获取逾期任务
  getOverdueTasks: () =>
    api.get<Task[]>('/api/tasks/overdue'),

  // 复制任务
  duplicateTask: (id: number, data?: { name?: string; projectId?: number }) =>
    api.post<Task>(`/api/tasks/${id}/duplicate`, data),

  // 设置任务提醒
  setTaskReminder: (id: number, data: { reminderAt: string; message?: string }) =>
    api.post(`/api/tasks/${id}/reminder`, data),

  // 取消任务提醒
  cancelTaskReminder: (id: number) =>
    api.delete(`/api/tasks/${id}/reminder`),

  // 获取任务依赖
  getTaskDependencies: (id: number) =>
    api.get(`/api/tasks/${id}/dependencies`),

  // 设置任务依赖
  setTaskDependency: (id: number, dependsOnId: number) =>
    api.post(`/api/tasks/${id}/dependencies`, { dependsOnId }),

  // 移除任务依赖
  removeTaskDependency: (id: number, dependencyId: number) =>
    api.delete(`/api/tasks/${id}/dependencies/${dependencyId}`),

  // ===== 任务评论相关API =====

  // 获取任务评论列表
  getTaskComments: (taskId: number) =>
    api.get<TaskComment[]>(`/api/tasks/${taskId}/comments`),

  // 添加任务评论
  addTaskComment: (taskId: number, data: {
    content: string;
    parentId?: number;
    mentions?: number[]
  }) =>
    api.post<TaskComment>(`/api/tasks/${taskId}/comments`, data),

  // 更新任务评论
  updateTaskComment: (taskId: number, commentId: number, data: { content: string }) =>
    api.put<TaskComment>(`/api/tasks/${taskId}/comments/${commentId}`, data),

  // 删除任务评论
  deleteTaskComment: (taskId: number, commentId: number) =>
    api.delete(`/api/tasks/${taskId}/comments/${commentId}`),

  // 获取评论回复
  getCommentReplies: (taskId: number, commentId: number) =>
    api.get<TaskComment[]>(`/api/tasks/${taskId}/comments/${commentId}/replies`),

  // 获取任务评论数量
  getTaskCommentCount: (taskId: number) =>
    api.get<number>(`/api/tasks/${taskId}/comments/count`),

  // ===== 任务附件相关API =====

  // 获取任务附件列表
  getTaskAttachments: (taskId: number) =>
    api.get<any[]>(`/api/tasks/${taskId}/attachments`),

  // 上传任务附件
  uploadTaskAttachment: (taskId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/api/tasks/${taskId}/attachments`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除任务附件
  deleteTaskAttachment: (taskId: number, attachmentId: number) =>
    api.delete(`/api/tasks/${taskId}/attachments/${attachmentId}`),

  // ===== 子任务相关API =====

  // 获取子任务列表
  getSubTasks: (parentId: number) =>
    api.get<Task[]>(`/api/tasks/${parentId}/subtasks`),

  // 创建子任务
  createSubTask: (parentId: number, data: CreateTaskData) =>
    api.post<Task>(`/api/tasks/${parentId}/subtasks`, data)
}

export default taskApi