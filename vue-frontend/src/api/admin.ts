import { api } from '@/utils/request'

export interface Department {
  id?: number
  name: string
  description?: string
  parentId?: number
  level?: string
  sort?: number
  managerId?: number
  status?: string
  code?: string
  phone?: string
  email?: string
  address?: string
  createdAt?: string
  updatedAt?: string
}

export const departmentApi = {
  createDepartment(department: Department) {
    return api.post('/departments', department)
  },

  updateDepartment(id: number, department: Department) {
    return api.put(`/departments/${id}`, department)
  },

  deleteDepartment(id: number) {
    return api.delete(`/departments/${id}`)
  },

  getDepartment(id: number) {
    return api.get(`/departments/${id}`)
  },

  getAllDepartments() {
    return api.get('/departments')
  },

  getDepartmentTree() {
    return api.get('/departments/tree')
  },

  getChildDepartments(id: number) {
    return api.get(`/departments/${id}/children`)
  },

  searchDepartments(keyword: string) {
    return api.get('/departments/search', {
      params: { keyword }
    })
  },

  moveDepartment(id: number, newParentId: number) {
    return api.post(`/departments/${id}/move`, null, {
      params: { newParentId }
    })
  },

  assignManager(departmentId: number, managerId: number) {
    return api.post(`/departments/${departmentId}/assign-manager`, null, {
      params: { managerId }
    })
  },

  getDepartmentStatistics() {
    return api.get('/departments/statistics')
  },

  getDepartmentUserCount() {
    return api.get('/departments/user-count')
  }
}

export const userManagementApi = {
  getAllUsers(page = 1, size = 20) {
    return api.get('/admin/users', {
      params: { page, size }
    })
  },

  getUser(id: number) {
    return api.get(`/admin/users/${id}`)
  },

  updateUser(id: number, user: any) {
    return api.put(`/admin/users/${id}`, user)
  },

  deleteUser(id: number) {
    return api.delete(`/admin/users/${id}`)
  },

  resetPassword(id: number, defaultPassword = '123456') {
    return api.post(`/admin/users/${id}/reset-password`, null, {
      params: { defaultPassword }
    })
  },

  changeUserStatus(id: number, status: string) {
    return api.post(`/admin/users/${id}/status`, null, {
      params: { status }
    })
  },

  assignRole(id: number, role: string) {
    return api.post(`/admin/users/${id}/assign-role`, null, {
      params: { role }
    })
  },

  removeRole(id: number, role: string) {
    return api.post(`/admin/users/${id}/remove-role`, null, {
      params: { role }
    })
  },

  assignDepartment(id: number, departmentId: number) {
    return api.post(`/admin/users/${id}/assign-department`, null, {
      params: { departmentId }
    })
  },

  getUserStatistics() {
    return api.get('/admin/users/statistics')
  },

  getUserCountByDepartment() {
    return api.get('/admin/users/department-stats')
  },

  getUserRegistrationStats(days = 30) {
    return api.get('/admin/users/registration-stats', {
      params: { days }
    })
  },

  getActiveUsers() {
    return api.get('/admin/users/active')
  },

  getUsersByRole(role: string) {
    return api.get('/admin/users/by-role', {
      params: { role }
    })
  },

  getUsersByDepartment(departmentId: number) {
    return api.get('/admin/users/by-department', {
      params: { departmentId }
    })
  }
}

// 默认导出管理API模块
const adminApi = {
  department: departmentApi,
  userManagement: userManagementApi
}

export default adminApi