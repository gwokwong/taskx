import { api } from './api'

export interface Dialog {
  id: number
  name: string
  type: 'user' | 'group'
  lastMsg?: string
  lastAt?: string
  unreadCount?: number
}

export interface Message {
  id: number
  dialogId: number
  userId: number
  userName?: string
  content: string
  messageType: string
  sendAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface CreateDialogRequest {
  name: string
  type: 'user' | 'group'
  memberIds?: number[]
}

export interface SendMessageRequest {
  content: string
  messageType?: string
}

export const messageApi = {
  // Dialog API
  async getUserDialogs(): Promise<Dialog[]> {
    const response = await api.get('/dialogs')
    return response.data.data
  },

  async createDialog(data: CreateDialogRequest): Promise<Dialog> {
    const response = await api.post('/dialogs', data)
    return response.data.data
  },

  async getDialogMessages(dialogId: number, page: number = 1, size: number = 50): Promise<Message[]> {
    const response = await api.get(`/dialogs/${dialogId}/messages`, {
      params: { page, size }
    })
    return response.data.data
  },

  async sendMessage(dialogId: number, data: SendMessageRequest): Promise<Message> {
    const response = await api.post(`/dialogs/${dialogId}/messages`, data)
    return response.data.data
  },

  async markDialogAsRead(dialogId: number): Promise<void> {
    await api.put(`/dialogs/${dialogId}/read`)
  }
}

export default messageApi