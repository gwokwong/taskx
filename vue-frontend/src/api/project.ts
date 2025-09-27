import { api } from '@/utils/request'

export interface Project {
  id: number
  name: string
  description?: string
  ownerId: number
  status: string
  createdAt: string
  updatedAt: string
  members?: ProjectMember[]
}

export interface ProjectMember {
  id: number
  projectId: number
  userId: number
  role: string
  joinedAt: string
  user?: {
    id: number
    nickname: string
    email: string
  }
}

export interface CreateProjectData {
  name: string
  description?: string
  members?: number[]
}

export interface UpdateProjectData extends Partial<CreateProjectData> {
  id: number
}

// 项目相关API
export const projectApi = {
  // 获取项目列表
  getProjects: () =>
    api.get<Project[]>('/api/projects'),

  // 获取项目详情
  getProject: (id: number) =>
    api.get<Project>(`/api/projects/${id}`),

  // 创建项目
  createProject: (data: CreateProjectData) =>
    api.post<Project>('/api/projects', data),

  // 更新项目
  updateProject: (id: number, data: UpdateProjectData) =>
    api.put<Project>(`/api/projects/${id}`, data),

  // 删除项目
  deleteProject: (id: number) =>
    api.delete(`/api/projects/${id}`),

  // 获取项目成员
  getProjectMembers: (id: number) =>
    api.get<ProjectMember[]>(`/api/projects/${id}/members`),

  // 添加项目成员
  addProjectMember: (id: number, userId: number, role: string = 'member') =>
    api.post(`/api/projects/${id}/members`, { userId, role }),

  // 移除项目成员
  removeProjectMember: (id: number, userId: number) =>
    api.delete(`/api/projects/${id}/members/${userId}`),

  // 更新成员角色
  updateMemberRole: (id: number, userId: number, role: string) =>
    api.put(`/api/projects/${id}/members/${userId}`, { role })
}

export default projectApi