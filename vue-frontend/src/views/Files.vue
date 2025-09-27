<template>
  <Layout>
    <div class="space-y-6">
      <!-- 文件管理头部 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">文件管理</h1>
          <p class="text-gray-600 dark:text-gray-400">管理您的项目文件和文档</p>
        </div>
        <div class="flex space-x-3">
          <Button variant="outline" @click="showUploadModal = true">
            <DocumentArrowUpIcon class="h-4 w-4 mr-2" />
            上传文件
          </Button>
          <Button @click="showCreateFolderModal = true">
            <FolderPlusIcon class="h-4 w-4 mr-2" />
            新建文件夹
          </Button>
        </div>
      </div>

      <!-- 文件导航 -->
      <div class="flex items-center space-x-2 text-sm">
        <button
          v-for="(breadcrumb, index) in breadcrumbs"
          :key="index"
          @click="navigateToFolder(breadcrumb.id)"
          :class="[
            'hover:text-blue-600 transition-colors',
            index === breadcrumbs.length - 1 ? 'text-gray-900 dark:text-white font-medium' : 'text-gray-500 dark:text-gray-400'
          ]"
        >
          {{ breadcrumb.name }}
          <ChevronRightIcon v-if="index < breadcrumbs.length - 1" class="h-4 w-4 inline ml-1" />
        </button>
      </div>

      <!-- 文件列表 -->
      <Card>
        <div class="p-6">
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            <!-- 文件夹 -->
            <div
              v-for="folder in folders"
              :key="`folder-${folder.id}`"
              @click="navigateToFolder(folder.id)"
              class="p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
            >
              <div class="flex items-center space-x-3">
                <FolderIcon class="h-8 w-8 text-blue-500" />
                <div>
                  <p class="font-medium text-gray-900 dark:text-white">{{ folder.name }}</p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">文件夹</p>
                </div>
              </div>
            </div>

            <!-- 文件 -->
            <div
              v-for="file in files"
              :key="`file-${file.id}`"
              @click="previewFile(file)"
              class="p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
            >
              <div class="flex items-center space-x-3">
                <component :is="getFileIcon(file.fileType)" class="h-8 w-8" :class="getFileIconColor(file.fileType)" />
                <div class="flex-1 min-w-0">
                  <p class="font-medium text-gray-900 dark:text-white truncate">{{ file.originalName }}</p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ formatFileSize(file.fileSize) }}</p>
                </div>
              </div>
              <div class="mt-3 flex items-center justify-between">
                <Badge :variant="getFileTypeVariant(file.fileType)">
                  {{ getFileTypeLabel(file.fileType) }}
                </Badge>
                <div class="flex space-x-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    @click.stop="downloadFile(file)"
                  >
                    <ArrowDownTrayIcon class="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    @click.stop="deleteFile(file)"
                  >
                    <TrashIcon class="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="files.length === 0 && folders.length === 0" class="text-center py-12">
            <DocumentIcon class="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p class="text-gray-500 dark:text-gray-400">此文件夹为空</p>
          </div>
        </div>
      </Card>

      <!-- 上传文件模态框 -->
      <Modal v-model:open="showUploadModal" title="上传文件">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              选择文件
            </label>
            <input
              ref="fileInput"
              type="file"
              multiple
              @change="handleFileSelect"
              class="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            />
          </div>
          <div v-if="selectedFiles.length > 0">
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">
              已选择 {{ selectedFiles.length }} 个文件
            </p>
            <div class="space-y-2">
              <div
                v-for="(file, index) in selectedFiles"
                :key="index"
                class="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 rounded"
              >
                <span class="text-sm">{{ file.name }}</span>
                <Button variant="ghost" size="sm" @click="removeSelectedFile(index)">
                  <XMarkIcon class="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <Button variant="outline" @click="showUploadModal = false">
            取消
          </Button>
          <Button @click="uploadFiles" :disabled="selectedFiles.length === 0 || uploading">
            {{ uploading ? '上传中...' : '上传' }}
          </Button>
        </template>
      </Modal>

      <!-- 创建文件夹模态框 -->
      <Modal v-model:open="showCreateFolderModal" title="创建文件夹">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            文件夹名称
          </label>
          <Input
            v-model="newFolderName"
            placeholder="输入文件夹名称"
            @keyup.enter="createFolder"
          />
        </div>
        <template #footer>
          <Button variant="outline" @click="showCreateFolderModal = false">
            取消
          </Button>
          <Button @click="createFolder" :disabled="!newFolderName">
            创建
          </Button>
        </template>
      </Modal>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import Layout from '@/components/Layout.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Modal from '@/components/ui/modal/Modal.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import {
  DocumentIcon,
  DocumentArrowUpIcon,
  FolderIcon,
  FolderPlusIcon,
  ChevronRightIcon,
  ArrowDownTrayIcon,
  TrashIcon,
  XMarkIcon,
  PhotoIcon,
  FilmIcon,
  MusicalNoteIcon,
  DocumentTextIcon,
  ArchiveBoxIcon
} from '@heroicons/vue/24/outline'

const currentFolderId = ref(0)
const files = ref([])
const folders = ref([])
const showUploadModal = ref(false)
const showCreateFolderModal = ref(false)
const selectedFiles = ref<File[]>([])
const newFolderName = ref('')
const uploading = ref(false)
const fileInput = ref<HTMLInputElement>()

const breadcrumbs = computed(() => {
  // 简化实现，实际应该根据当前文件夹路径生成
  return [{ id: 0, name: '根目录' }]
})

const navigateToFolder = (folderId: number) => {
  currentFolderId.value = folderId
  loadFiles()
}

const loadFiles = async () => {
  // 模拟数据，实际应该调用API
  files.value = [
    {
      id: 1,
      originalName: 'project-design.pdf',
      fileName: 'uuid-123.pdf',
      fileType: 'pdf',
      fileSize: 2048576,
      createdAt: '2024-01-15'
    },
    {
      id: 2,
      originalName: 'screenshot.png',
      fileName: 'uuid-456.png',
      fileType: 'image',
      fileSize: 1024000,
      createdAt: '2024-01-14'
    }
  ]

  folders.value = [
    {
      id: 1,
      name: '设计文档',
      createdAt: '2024-01-10'
    },
    {
      id: 2,
      name: '开发资料',
      createdAt: '2024-01-12'
    }
  ]
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    selectedFiles.value = Array.from(target.files)
  }
}

const removeSelectedFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) return

  uploading.value = true
  try {
    // 这里应该调用API上传文件
    console.log('Uploading files:', selectedFiles.value)

    // 模拟上传
    await new Promise(resolve => setTimeout(resolve, 2000))

    selectedFiles.value = []
    showUploadModal.value = false
    loadFiles()
  } catch (error) {
    console.error('Upload failed:', error)
  } finally {
    uploading.value = false
  }
}

const createFolder = async () => {
  if (!newFolderName.value) return

  try {
    // 这里应该调用API创建文件夹
    console.log('Creating folder:', newFolderName.value)

    newFolderName.value = ''
    showCreateFolderModal.value = false
    loadFiles()
  } catch (error) {
    console.error('Create folder failed:', error)
  }
}

const previewFile = (file: any) => {
  console.log('Preview file:', file)
  // 实现文件预览逻辑
}

const downloadFile = (file: any) => {
  console.log('Download file:', file)
  // 实现文件下载逻辑
}

const deleteFile = (file: any) => {
  if (confirm('确定要删除这个文件吗？')) {
    console.log('Delete file:', file)
    // 实现文件删除逻辑
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (fileType: string) => {
  const icons = {
    image: PhotoIcon,
    video: FilmIcon,
    audio: MusicalNoteIcon,
    pdf: DocumentTextIcon,
    word: DocumentTextIcon,
    excel: DocumentTextIcon,
    powerpoint: DocumentTextIcon,
    text: DocumentTextIcon,
    archive: ArchiveBoxIcon,
    unknown: DocumentIcon
  }
  return icons[fileType] || DocumentIcon
}

const getFileIconColor = (fileType: string) => {
  const colors = {
    image: 'text-green-500',
    video: 'text-blue-500',
    audio: 'text-purple-500',
    pdf: 'text-red-500',
    word: 'text-blue-600',
    excel: 'text-green-600',
    powerpoint: 'text-orange-500',
    text: 'text-gray-500',
    archive: 'text-yellow-500',
    unknown: 'text-gray-400'
  }
  return colors[fileType] || 'text-gray-400'
}

const getFileTypeVariant = (fileType: string) => {
  const variants = {
    image: 'success',
    video: 'info',
    audio: 'secondary',
    pdf: 'error',
    word: 'info',
    excel: 'success',
    powerpoint: 'warning',
    text: 'default',
    archive: 'warning',
    unknown: 'default'
  }
  return variants[fileType] || 'default'
}

const getFileTypeLabel = (fileType: string) => {
  const labels = {
    image: '图片',
    video: '视频',
    audio: '音频',
    pdf: 'PDF',
    word: 'Word',
    excel: 'Excel',
    powerpoint: 'PPT',
    text: '文本',
    archive: '压缩包',
    unknown: '未知'
  }
  return labels[fileType] || '未知'
}

onMounted(() => {
  loadFiles()
})
</script>