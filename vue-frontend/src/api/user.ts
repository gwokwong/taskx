import { api } from '@/utils/request'

export interface User {
  userid: number
  email: string
  nickname: string
  userImg?: string
  profession?: string
  tel?: string
  identity: string[]
  department: number[]
  az?: string
  pinyin?: string
  py?: string
  bot?: number
  changeNickname?: number
  department_owner?: number[]
  department_all?: any[]
  disableAt?: string
  emailTime?: string
  created_at?: string
  updated_at?: string
  last_ip?: string
  last_at?: string
  line_ip?: string
  line_at?: string
  departmentName?: string
}

export interface LoginData {
  email: string
  password: string
}

export interface RegisterData {
  email: string
  password: string
  nickname: string
}

export interface UpdateUserData {
  nickname?: string
  profession?: string
  tel?: string
  userImg?: string
}

// 用户相关API
export const userApi = {
  // 登录
  login: (data: LoginData) =>
    api.post<{ token: string; user: User }>('/api/auth/login', data),

  // 注册
  register: (data: RegisterData) =>
    api.post<{ token: string; user: User }>('/api/auth/register', data),

  // 获取当前用户信息
  getCurrentUser: () =>
    api.get<User>('/api/auth/user'),

  // 更新用户信息
  updateUser: (data: UpdateUserData) =>
    api.put<User>('/api/users/profile', data),

  // 获取用户列表
  getUsers: () =>
    api.get<User[]>('/api/users'),

  // 获取用户详情
  getUser: (id: number) =>
    api.get<User>(`/api/users/${id}`),

  // 退出登录
  logout: () =>
    api.post('/api/auth/logout'),

  // 刷新token
  refreshToken: () =>
    api.post<{ token: string }>('/api/auth/refresh')
}

export default userApi