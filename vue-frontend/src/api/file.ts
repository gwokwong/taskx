import { api } from '@/utils/request'

export interface FileInfo {
  id: number
  fileName: string
  originalName: string
  filePath: string
  fileSize: number
  fileType: string
  mimeType: string
  uploadUserid: number
  projectId?: number
  taskId?: number
  folderId?: number
  downloadCount: number
  isPrivate: boolean
  description?: string
  createdAt: string
  updatedAt: string
}

export interface FileUploadParams {
  file: File
  projectId?: number
  taskId?: number
  folderId?: number
}

export interface FileMoveParams {
  targetFolderId: number
}

export interface FileUpdateParams {
  name?: string
  description?: string
}

export interface FilePreview {
  id: number
  name: string
  size: number
  type: string
  mimeType: string
  url: string
  downloadUrl: string
  isImage?: boolean
  isPdf?: boolean
  isText?: boolean
  previewUrl?: string
}

export interface FileStatistics {
  totalFiles: number
  totalSize: number
  uniqueOwners: number
}

// 文件相关API
export const fileApi = {
  // 上传文件
  uploadFile: (params: FileUploadParams) => {
    const formData = new FormData()
    formData.append('file', params.file)
    if (params.projectId) formData.append('projectId', params.projectId.toString())
    if (params.taskId) formData.append('taskId', params.taskId.toString())
    if (params.folderId) formData.append('folderId', params.folderId.toString())

    return api.post<FileInfo>('/api/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 根据ID获取文件信息
  getFileById: (fileId: number) =>
    api.get<FileInfo>(`/api/files/${fileId}`),

  // 根据项目获取文件列表
  getFilesByProject: (projectId: number) =>
    api.get<FileInfo[]>(`/api/files/project/${projectId}`),

  // 根据任务获取文件列表
  getFilesByTask: (taskId: number) =>
    api.get<FileInfo[]>(`/api/files/task/${taskId}`),

  // 根据文件夹获取文件列表
  getFilesByFolder: (folderId: number) =>
    api.get<FileInfo[]>(`/api/files/folder/${folderId}`),

  // 下载文件
  downloadFile: (fileId: number) =>
    api.get(`/api/files/${fileId}/download`, { responseType: 'blob' }),

  // 获取文件预览
  getFilePreview: (fileId: number) =>
    api.get<FilePreview>(`/api/files/${fileId}/preview`),

  // 删除文件
  deleteFile: (fileId: number) =>
    api.delete(`/api/files/${fileId}`),

  // 更新文件信息
  updateFileInfo: (fileId: number, params: FileUpdateParams) =>
    api.put<FileInfo>(`/api/files/${fileId}`, params),

  // 移动文件
  moveFile: (fileId: number, params: FileMoveParams) =>
    api.post(`/api/files/${fileId}/move`, params),

  // 复制文件
  copyFile: (fileId: number, params: FileMoveParams) =>
    api.post<FileInfo>(`/api/files/${fileId}/copy`, params),

  // 批量删除文件
  batchDeleteFiles: (fileIds: number[]) =>
    api.delete('/api/files/batch', { data: { fileIds } }),

  // 搜索文件
  searchFiles: (keyword: string) =>
    api.get<FileInfo[]>('/api/files/search', { params: { keyword } }),

  // 获取文件统计信息
  getFileStatistics: () =>
    api.get<FileStatistics>('/api/files/statistics'),

  // 获取文件URL
  getFileUrl: (fileId: number) =>
    api.get<string>(`/api/files/${fileId}/url`)
}

export default fileApi