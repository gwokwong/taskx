import { api } from '@/utils/request'

export interface Notification {
  id: number
  userId: number
  type: string
  title: string
  content: string
  isRead: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateNotificationData {
  userId?: number
  type: string
  title: string
  content: string
}

// 通知相关API
export const notificationApi = {
  // 获取通知列表
  getNotifications: (params?: { page?: number; size?: number; unreadOnly?: boolean }) =>
    api.get<{ records: Notification[]; total: number }>('/api/notifications', { params }),

  // 获取未读通知数量
  getUnreadCount: () =>
    api.get<{ count: number }>('/api/notifications/unread-count'),

  // 标记为已读
  markAsRead: (id: number) =>
    api.post(`/api/notifications/${id}/read`),

  // 批量标记为已读
  markAllAsRead: () =>
    api.post('/api/notifications/mark-all-read'),

  // 删除通知
  deleteNotification: (id: number) =>
    api.delete(`/api/notifications/${id}`),

  // 创建通知（管理员功能）
  createNotification: (data: CreateNotificationData) =>
    api.post<Notification>('/api/notifications', data)
}

export default notificationApi