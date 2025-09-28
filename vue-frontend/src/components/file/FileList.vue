<template>
  <div class="file-list-container">
    <!-- 文件列表头部 -->
    <div class="file-list-header">
      <h3 class="file-list-title">{{ title || '文件列表' }}</h3>
      <div class="file-list-actions">
        <Button
          v-if="showUpload"
          @click="showUploadModal = true"
          size="sm"
        >
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          上传文件
        </Button>
        <Button
          v-if="selectedFiles.length > 0"
          @click="handleBatchDelete"
          variant="destructive"
          size="sm"
        >
          删除选中 ({{ selectedFiles.length }})
        </Button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div v-if="showSearch" class="search-bar">
      <Input
        v-model="searchKeyword"
        placeholder="搜索文件..."
        @input="handleSearch"
        class="search-input"
      />
    </div>

    <!-- 文件列表 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredFiles.length === 0" class="empty-state">
      <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </svg>
      <p>暂无文件</p>
    </div>

    <div v-else class="file-list">
      <div
        v-for="file in filteredFiles"
        :key="file.id"
        class="file-item"
        :class="{ 'selected': selectedFiles.includes(file.id) }"
      >
        <!-- 文件选择框 -->
        <div v-if="selectable" class="file-checkbox">
          <Checkbox
            :checked="selectedFiles.includes(file.id)"
            @update:checked="toggleFileSelection(file.id)"
          />
        </div>

        <!-- 文件图标 -->
        <div class="file-icon">
          <img
            v-if="isImage(file)"
            :src="getFileUrl(file.id)"
            :alt="file.originalName"
            class="file-thumbnail"
            @error="handleImageError"
          />
          <svg v-else class="file-type-icon" :class="getFileTypeClass(file)" fill="currentColor" viewBox="0 0 24 24">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
          </svg>
        </div>

        <!-- 文件信息 -->
        <div class="file-info" @click="handleFileClick(file)">
          <div class="file-name">{{ file.originalName }}</div>
          <div class="file-meta">
            <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
            <span class="file-date">{{ formatDate(file.createdAt) }}</span>
          </div>
        </div>

        <!-- 文件操作 -->
        <div class="file-actions">
          <DropdownMenu>
            <DropdownMenuTrigger as-child>
              <Button variant="ghost" size="sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"/>
                </svg>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuItem @click="downloadFile(file)">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                </svg>
                下载
              </DropdownMenuItem>
              <DropdownMenuItem @click="previewFile(file)">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
                预览
              </DropdownMenuItem>
              <DropdownMenuItem @click="copyFile(file)">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/>
                </svg>
                复制
              </DropdownMenuItem>
              <DropdownMenuItem @click="renameFile(file)">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
                重命名
              </DropdownMenuItem>
              <DropdownMenuItem @click="deleteFile(file)" class="text-red-600">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
                删除
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>

    <!-- 文件上传模态框 -->
    <Dialog :open="showUploadModal" @update:open="showUploadModal = $event">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>上传文件</DialogTitle>
        </DialogHeader>
        <FileUpload
          :project-id="projectId"
          :task-id="taskId"
          :folder-id="folderId"
          :auto-upload="false"
          @upload-success="handleUploadSuccess"
          @upload-error="handleUploadError"
        />
      </DialogContent>
    </Dialog>

    <!-- 文件预览模态框 -->
    <Dialog :open="!!previewingFile" @update:open="previewingFile = null">
      <DialogContent class="max-w-4xl">
        <DialogHeader>
          <DialogTitle>{{ previewingFile?.originalName }}</DialogTitle>
        </DialogHeader>
        <div v-if="previewingFile" class="file-preview">
          <FilePreview :file="previewingFile" />
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineProps, defineEmits } from 'vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Checkbox } from '@/components/ui/checkbox'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import FileUpload from './FileUpload.vue'
import FilePreview from './FilePreview.vue'
import { fileApi, type FileInfo } from '@/api/file'

interface Props {
  title?: string
  projectId?: number
  taskId?: number
  folderId?: number
  showUpload?: boolean
  showSearch?: boolean
  selectable?: boolean
  initialFiles?: FileInfo[]
}

interface Emits {
  (e: 'file-click', file: FileInfo): void
  (e: 'files-changed'): void
}

const props = withDefaults(defineProps<Props>(), {
  showUpload: true,
  showSearch: true,
  selectable: false
})

const emit = defineEmits<Emits>()

const files = ref<FileInfo[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedFiles = ref<number[]>([])
const showUploadModal = ref(false)
const previewingFile = ref<FileInfo | null>(null)

const filteredFiles = computed(() => {
  if (!searchKeyword.value) return files.value

  const keyword = searchKeyword.value.toLowerCase()
  return files.value.filter(file =>
    file.originalName.toLowerCase().includes(keyword) ||
    file.fileType.toLowerCase().includes(keyword)
  )
})

onMounted(() => {
  if (props.initialFiles) {
    files.value = props.initialFiles
  } else {
    loadFiles()
  }
})

const loadFiles = async () => {
  loading.value = true
  try {
    let response
    if (props.taskId) {
      response = await fileApi.getFilesByTask(props.taskId)
    } else if (props.projectId) {
      response = await fileApi.getFilesByProject(props.projectId)
    } else if (props.folderId) {
      response = await fileApi.getFilesByFolder(props.folderId)
    }

    if (response) {
      files.value = response.data
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (searchKeyword.value.trim()) {
    try {
      const response = await fileApi.searchFiles(searchKeyword.value.trim())
      files.value = response.data
    } catch (error) {
      console.error('搜索文件失败:', error)
    }
  } else {
    loadFiles()
  }
}

const toggleFileSelection = (fileId: number) => {
  const index = selectedFiles.value.indexOf(fileId)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(fileId)
  }
}

const handleFileClick = (file: FileInfo) => {
  emit('file-click', file)
}

const downloadFile = async (file: FileInfo) => {
  try {
    const response = await fileApi.downloadFile(file.id)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.originalName
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载文件失败:', error)
  }
}

const previewFile = (file: FileInfo) => {
  previewingFile.value = file
}

const copyFile = async (file: FileInfo) => {
  try {
    await fileApi.copyFile(file.id, { targetFolderId: props.folderId || 0 })
    loadFiles()
    emit('files-changed')
  } catch (error) {
    console.error('复制文件失败:', error)
  }
}

const renameFile = (file: FileInfo) => {
  const newName = prompt('请输入新的文件名:', file.originalName)
  if (newName && newName !== file.originalName) {
    updateFileName(file.id, newName)
  }
}

const updateFileName = async (fileId: number, name: string) => {
  try {
    await fileApi.updateFileInfo(fileId, { name })
    loadFiles()
    emit('files-changed')
  } catch (error) {
    console.error('重命名文件失败:', error)
  }
}

const deleteFile = async (file: FileInfo) => {
  if (confirm(`确定要删除文件 "${file.originalName}" 吗？`)) {
    try {
      await fileApi.deleteFile(file.id)
      loadFiles()
      emit('files-changed')
    } catch (error) {
      console.error('删除文件失败:', error)
    }
  }
}

const handleBatchDelete = async () => {
  if (confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`)) {
    try {
      await fileApi.batchDeleteFiles(selectedFiles.value)
      selectedFiles.value = []
      loadFiles()
      emit('files-changed')
    } catch (error) {
      console.error('批量删除文件失败:', error)
    }
  }
}

const handleUploadSuccess = () => {
  showUploadModal.value = false
  loadFiles()
  emit('files-changed')
}

const handleUploadError = (error: any) => {
  console.error('上传文件失败:', error)
}

const isImage = (file: FileInfo): boolean => {
  return file.mimeType?.startsWith('image/') || false
}

const getFileUrl = (fileId: number): string => {
  return `/api/files/${fileId}/download`
}

const getFileTypeClass = (file: FileInfo): string => {
  const type = file.fileType?.toLowerCase()
  switch (type) {
    case 'pdf': return 'text-red-500'
    case 'doc':
    case 'docx': return 'text-blue-500'
    case 'xls':
    case 'xlsx': return 'text-green-500'
    case 'ppt':
    case 'pptx': return 'text-orange-500'
    case 'txt': return 'text-gray-500'
    default: return 'text-gray-400'
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}
</script>

<style scoped>
.file-list-container {
  width: 100%;
}

.file-list-header {
  display: flex;
  justify-content: between;
  align-items: center;
  margin-bottom: 1rem;
}

.file-list-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.file-list-actions {
  display: flex;
  gap: 0.5rem;
}

.search-bar {
  margin-bottom: 1rem;
}

.search-input {
  max-width: 300px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #6b7280;
}

.loading-spinner {
  width: 2rem;
  height: 2rem;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  width: 3rem;
  height: 3rem;
  margin-bottom: 1rem;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background-color: white;
  transition: all 0.2s;
}

.file-item:hover {
  background-color: #f9fafb;
  border-color: #d1d5db;
}

.file-item.selected {
  background-color: #eff6ff;
  border-color: #3b82f6;
}

.file-checkbox {
  flex-shrink: 0;
}

.file-icon {
  flex-shrink: 0;
  width: 3rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.file-type-icon {
  width: 2rem;
  height: 2rem;
}

.file-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.file-name {
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.25rem;
  word-break: break-all;
}

.file-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.file-actions {
  flex-shrink: 0;
}

.file-preview {
  max-height: 70vh;
  overflow: auto;
}
</style>