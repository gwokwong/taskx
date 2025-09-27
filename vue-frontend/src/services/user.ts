import { api } from './api'
import type { User } from '@/types/user'

export const userService = {
  // 获取用户信息
  getProfile: () => {
    return api.get('/user/profile')
  },

  // 更新用户信息
  updateProfile: (data: Partial<User>) => {
    return api.put('/user/profile', data)
  },

  // 修改密码
  changePassword: (oldPassword: string, newPassword: string) => {
    return api.post('/user/change-password', { oldPassword, newPassword })
  },

  // 获取用户列表
  getUsers: (params?: any) => {
    return api.get('/users', { params })
  },

  // 搜索用户
  searchUsers: (keyword: string, limit = 20) => {
    return api.get('/users/search', { params: { keyword, limit } })
  },

  // 获取用户基本信息
  getUserBasic: (userId: number) => {
    return api.get(`/users/${userId}/basic`)
  }
}