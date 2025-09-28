<template>
  <div class="file-upload-area">
    <!-- 拖拽上传区域 -->
    <div
      ref="dropZone"
      class="drop-zone"
      :class="{ 'drag-over': isDragOver, 'uploading': uploading }"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        :multiple="multiple"
        :accept="accept"
        class="hidden"
        @change="handleFileSelect"
      />

      <div v-if="!uploading" class="upload-content">
        <svg class="upload-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
        </svg>
        <p class="upload-text">拖拽文件到此处或点击上传</p>
        <p class="upload-hint">{{ accept ? `支持格式: ${accept}` : '支持所有文件格式' }}</p>
      </div>

      <div v-else class="upload-progress">
        <div class="progress-content">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${uploadProgress}%` }"></div>
          </div>
          <p class="progress-text">上传中... {{ uploadProgress }}%</p>
        </div>
      </div>
    </div>

    <!-- 文件列表 -->
    <div v-if="fileList.length > 0" class="file-list">
      <h4>已选择的文件</h4>
      <div class="file-items">
        <div v-for="(file, index) in fileList" :key="index" class="file-item">
          <div class="file-info">
            <svg class="file-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            <div class="file-details">
              <p class="file-name">{{ file.name }}</p>
              <p class="file-size">{{ formatFileSize(file.size) }}</p>
            </div>
          </div>
          <button
            class="remove-btn"
            @click="removeFile(index)"
            :disabled="uploading"
          >
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 上传按钮 -->
    <div v-if="fileList.length > 0 && !autoUpload" class="upload-actions">
      <Button
        @click="uploadFiles"
        :disabled="uploading"
        class="upload-btn"
      >
        {{ uploading ? '上传中...' : '开始上传' }}
      </Button>
      <Button
        variant="outline"
        @click="clearFiles"
        :disabled="uploading"
      >
        清空
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue'
import { Button } from '@/components/ui/button'
import { fileApi, type FileUploadParams } from '@/api/file'

interface Props {
  multiple?: boolean
  accept?: string
  maxSize?: number // MB
  projectId?: number
  taskId?: number
  folderId?: number
  autoUpload?: boolean
}

interface Emits {
  (e: 'upload-success', files: any[]): void
  (e: 'upload-error', error: any): void
  (e: 'file-selected', files: File[]): void
}

const props = withDefaults(defineProps<Props>(), {
  multiple: true,
  maxSize: 100,
  autoUpload: false
})

const emit = defineEmits<Emits>()

const fileInput = ref<HTMLInputElement>()
const dropZone = ref<HTMLDivElement>()
const isDragOver = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const fileList = ref<File[]>([])

const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDragEnter = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (e: DragEvent) => {
  e.preventDefault()
  // 只有在真正离开组件时才设置为false
  if (!dropZone.value?.contains(e.relatedTarget as Node)) {
    isDragOver.value = false
  }
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  isDragOver.value = false

  const files = Array.from(e.dataTransfer?.files || [])
  processFiles(files)
}

const triggerFileInput = () => {
  if (!uploading.value) {
    fileInput.value?.click()
  }
}

const handleFileSelect = (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  processFiles(files)
}

const processFiles = (files: File[]) => {
  // 验证文件
  const validFiles = files.filter(file => {
    if (props.maxSize && file.size > props.maxSize * 1024 * 1024) {
      alert(`文件 ${file.name} 超过大小限制 ${props.maxSize}MB`)
      return false
    }
    return true
  })

  if (!props.multiple) {
    fileList.value = validFiles.slice(0, 1)
  } else {
    fileList.value.push(...validFiles)
  }

  emit('file-selected', fileList.value)

  if (props.autoUpload && fileList.value.length > 0) {
    uploadFiles()
  }
}

const removeFile = (index: number) => {
  fileList.value.splice(index, 1)
}

const clearFiles = () => {
  fileList.value = []
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const uploadFiles = async () => {
  if (fileList.value.length === 0) return

  uploading.value = true
  uploadProgress.value = 0

  try {
    const uploadResults = []

    for (let i = 0; i < fileList.value.length; i++) {
      const file = fileList.value[i]

      const uploadParams: FileUploadParams = {
        file,
        projectId: props.projectId,
        taskId: props.taskId,
        folderId: props.folderId
      }

      const result = await fileApi.uploadFile(uploadParams)
      uploadResults.push(result.data)

      // 更新进度
      uploadProgress.value = Math.round(((i + 1) / fileList.value.length) * 100)
    }

    emit('upload-success', uploadResults)
    clearFiles()
  } catch (error) {
    console.error('文件上传失败:', error)
    emit('upload-error', error)
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'

  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.file-upload-area {
  width: 100%;
}

.drop-zone {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f9fafb;
}

.drop-zone:hover {
  border-color: #3b82f6;
  background-color: #eff6ff;
}

.drop-zone.drag-over {
  border-color: #3b82f6;
  background-color: #dbeafe;
}

.drop-zone.uploading {
  cursor: not-allowed;
  opacity: 0.8;
}

.hidden {
  display: none;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.upload-icon {
  width: 3rem;
  height: 3rem;
  color: #6b7280;
}

.upload-text {
  font-size: 1.125rem;
  font-weight: 500;
  color: #374151;
  margin: 0;
}

.upload-hint {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.upload-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.progress-content {
  width: 100%;
  max-width: 300px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #3b82f6;
  transition: width 0.3s ease;
}

.progress-text {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0;
}

.file-list {
  margin-top: 1.5rem;
}

.file-list h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 500;
  color: #374151;
}

.file-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background-color: white;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.file-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: #6b7280;
  flex-shrink: 0;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin: 0 0 0.25rem 0;
  word-break: break-all;
}

.file-size {
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
}

.remove-btn {
  padding: 0.25rem;
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.remove-btn:hover {
  background-color: #fef2f2;
}

.remove-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.remove-btn svg {
  width: 1rem;
  height: 1rem;
}

.upload-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.upload-btn {
  min-width: 120px;
}
</style>