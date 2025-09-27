import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '@/services/api'
import type { User } from '@/types/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  const login = async (email: string, password: string) => {
    try {
      const response = await api.post('/auth/login', { email, password })
      if (response.data.ret) {
        token.value = response.data.data.token
        user.value = response.data.data.userInfo
        localStorage.setItem('token', token.value)
        return response.data
      }
      throw new Error(response.data.msg || '登录失败')
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  }

  const register = async (email: string, password: string, nickname?: string) => {
    try {
      const response = await api.post('/auth/register', { email, password, nickname })
      if (response.data.ret) {
        token.value = response.data.data.token
        user.value = response.data.data.userInfo
        localStorage.setItem('token', token.value)
        return response.data
      }
      throw new Error(response.data.msg || '注册失败')
    } catch (error) {
      console.error('Register error:', error)
      throw error
    }
  }

  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  const initAuth = async () => {
    if (token.value) {
      try {
        // 这里可以验证token有效性并获取用户信息
        const response = await api.get('/user/profile')
        if (response.data.ret) {
          user.value = response.data.data
        } else {
          logout()
        }
      } catch (error) {
        logout()
      }
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    initAuth
  }
})