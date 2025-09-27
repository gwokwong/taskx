import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response

    // 如果返回的状态码为200，说明接口请求成功
    if (response.status === 200) {
      return data
    }

    return Promise.reject(new Error('请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      // token过期，清除本地存储的token
      localStorage.removeItem('access_token')
      // 跳转到登录页
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

export { api }
export default api