import { api } from '@/utils/request'

export interface UploadResponse {
  url: string
  filename: string
  size: number
  mimetype: string
}

// 上传相关API
export const uploadApi = {
  // 上传文件
  uploadFile: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<UploadResponse>('/api/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传头像
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('avatar', file)
    return api.post<UploadResponse>('/api/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传图片
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('image', file)
    return api.post<UploadResponse>('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default uploadApi