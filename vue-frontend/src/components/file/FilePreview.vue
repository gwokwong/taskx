<template>
  <div class="file-preview-container">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载预览中...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <svg class="error-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"/>
      </svg>
      <p>{{ error }}</p>
      <Button @click="downloadFile" variant="outline">
        下载查看
      </Button>
    </div>

    <div v-else class="preview-content">
      <!-- 图片预览 -->
      <div v-if="isImage" class="image-preview">
        <img
          :src="previewUrl"
          :alt="file.originalName"
          class="preview-image"
          @load="handleImageLoad"
          @error="handleImageError"
        />
      </div>

      <!-- PDF预览 -->
      <div v-else-if="isPdf" class="pdf-preview">
        <iframe
          :src="previewUrl"
          class="pdf-iframe"
          @load="handlePdfLoad"
          @error="handlePdfError"
        />
      </div>

      <!-- 文本预览 -->
      <div v-else-if="isText" class="text-preview">
        <pre class="text-content">{{ textContent }}</pre>
      </div>

      <!-- 视频预览 -->
      <div v-else-if="isVideo" class="video-preview">
        <video
          :src="previewUrl"
          controls
          class="preview-video"
          @loadeddata="handleVideoLoad"
          @error="handleVideoError"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- 音频预览 -->
      <div v-else-if="isAudio" class="audio-preview">
        <audio
          :src="previewUrl"
          controls
          class="preview-audio"
          @loadeddata="handleAudioLoad"
          @error="handleAudioError"
        >
          您的浏览器不支持音频播放
        </audio>
        <div class="audio-info">
          <svg class="audio-icon" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12,3V13.55C11.41,13.21 10.73,13 10,13A3,3 0 0,0 7,16A3,3 0 0,0 10,19A3,3 0 0,0 13,16V7H17V3H12Z"/>
          </svg>
          <div class="audio-details">
            <p class="audio-name">{{ file.originalName }}</p>
            <p class="audio-size">{{ formatFileSize(file.fileSize) }}</p>
          </div>
        </div>
      </div>

      <!-- 代码预览 -->
      <div v-else-if="isCode" class="code-preview">
        <div class="code-header">
          <span class="code-language">{{ getCodeLanguage() }}</span>
          <Button @click="copyCode" size="sm" variant="outline">
            复制代码
          </Button>
        </div>
        <pre class="code-content"><code :class="getCodeLanguage()">{{ textContent }}</code></pre>
      </div>

      <!-- Office文件预览提示 -->
      <div v-else-if="isOffice" class="office-preview">
        <svg class="office-icon" fill="currentColor" viewBox="0 0 24 24">
          <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
        </svg>
        <p class="office-message">
          无法预览此类型文件，请下载后使用相应软件打开
        </p>
        <div class="office-actions">
          <Button @click="downloadFile">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            下载文件
          </Button>
        </div>
      </div>

      <!-- 默认预览 -->
      <div v-else class="default-preview">
        <svg class="default-icon" fill="currentColor" viewBox="0 0 24 24">
          <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
        </svg>
        <p class="default-message">此文件类型暂不支持预览</p>
        <div class="file-info">
          <p><strong>文件名:</strong> {{ file.originalName }}</p>
          <p><strong>文件大小:</strong> {{ formatFileSize(file.fileSize) }}</p>
          <p><strong>文件类型:</strong> {{ file.mimeType }}</p>
          <p><strong>上传时间:</strong> {{ formatDate(file.createdAt) }}</p>
        </div>
        <Button @click="downloadFile" class="download-btn">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          下载文件
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineProps } from 'vue'
import { Button } from '@/components/ui/button'
import { fileApi, type FileInfo } from '@/api/file'

interface Props {
  file: FileInfo
}

const props = defineProps<Props>()

const loading = ref(false)
const error = ref('')
const textContent = ref('')
const previewUrl = ref('')

const isImage = computed(() => {
  return props.file.mimeType?.startsWith('image/') || false
})

const isPdf = computed(() => {
  return props.file.mimeType === 'application/pdf'
})

const isText = computed(() => {
  const textTypes = [
    'text/',
    'application/json',
    'application/xml',
    'application/javascript'
  ]
  return textTypes.some(type => props.file.mimeType?.startsWith(type))
})

const isVideo = computed(() => {
  return props.file.mimeType?.startsWith('video/')
})

const isAudio = computed(() => {
  return props.file.mimeType?.startsWith('audio/')
})

const isCode = computed(() => {
  const codeExtensions = ['.js', '.ts', '.vue', '.html', '.css', '.scss', '.py', '.java', '.cpp', '.c', '.php', '.rb', '.go', '.rs']
  return codeExtensions.some(ext => props.file.originalName.toLowerCase().endsWith(ext))
})

const isOffice = computed(() => {
  const officeTypes = [
    'application/vnd.ms-',
    'application/vnd.openxmlformats-',
    'application/msword',
    'application/vnd.ms-excel',
    'application/vnd.ms-powerpoint'
  ]
  return officeTypes.some(type => props.file.mimeType?.startsWith(type))
})

onMounted(async () => {
  await loadPreview()
})

const loadPreview = async () => {
  loading.value = true
  error.value = ''

  try {
    if (isImage.value || isPdf.value || isVideo.value || isAudio.value) {
      previewUrl.value = `/api/files/${props.file.id}/download`
    } else if (isText.value || isCode.value) {
      await loadTextContent()
    }
  } catch (err) {
    console.error('加载预览失败:', err)
    error.value = '预览加载失败'
  } finally {
    loading.value = false
  }
}

const loadTextContent = async () => {
  try {
    const response = await fileApi.downloadFile(props.file.id)
    const blob = response.data as Blob
    textContent.value = await blob.text()
  } catch (err) {
    error.value = '无法加载文本内容'
    throw err
  }
}

const downloadFile = async () => {
  try {
    const response = await fileApi.downloadFile(props.file.id)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = props.file.originalName
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    console.error('下载文件失败:', err)
  }
}

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(textContent.value)
    // 这里可以添加一个提示
  } catch (err) {
    console.error('复制代码失败:', err)
  }
}

const getCodeLanguage = () => {
  const ext = props.file.originalName.toLowerCase().split('.').pop()
  const languageMap: Record<string, string> = {
    'js': 'javascript',
    'ts': 'typescript',
    'vue': 'vue',
    'html': 'html',
    'css': 'css',
    'scss': 'scss',
    'py': 'python',
    'java': 'java',
    'cpp': 'cpp',
    'c': 'c',
    'php': 'php',
    'rb': 'ruby',
    'go': 'go',
    'rs': 'rust'
  }
  return languageMap[ext || ''] || 'text'
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const handleImageLoad = () => {
  loading.value = false
}

const handleImageError = () => {
  error.value = '图片加载失败'
}

const handlePdfLoad = () => {
  loading.value = false
}

const handlePdfError = () => {
  error.value = 'PDF加载失败'
}

const handleVideoLoad = () => {
  loading.value = false
}

const handleVideoError = () => {
  error.value = '视频加载失败'
}

const handleAudioLoad = () => {
  loading.value = false
}

const handleAudioError = () => {
  error.value = '音频加载失败'
}
</script>

<style scoped>
.file-preview-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.loading-state,
.error-state {
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

.error-icon {
  width: 3rem;
  height: 3rem;
  color: #ef4444;
  margin-bottom: 1rem;
}

.preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.image-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem;
  flex: 1;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
}

.pdf-preview {
  flex: 1;
  min-height: 0;
}

.pdf-iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 4px;
}

.text-preview,
.code-preview {
  flex: 1;
  min-height: 0;
}

.text-content,
.code-content {
  width: 100%;
  height: 100%;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background-color: #f9fafb;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  overflow: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.code-language {
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
}

.code-content {
  border-radius: 0 0 4px 4px;
  margin-top: 0;
}

.video-preview,
.audio-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  gap: 1rem;
}

.preview-video {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 4px;
}

.preview-audio {
  width: 100%;
  max-width: 400px;
}

.audio-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 4px;
  width: 100%;
  max-width: 400px;
}

.audio-icon {
  width: 2rem;
  height: 2rem;
  color: #6b7280;
  flex-shrink: 0;
}

.audio-details {
  flex: 1;
  min-width: 0;
}

.audio-name {
  font-weight: 500;
  color: #374151;
  margin: 0 0 0.25rem 0;
  word-break: break-all;
}

.audio-size {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.office-preview,
.default-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.office-icon,
.default-icon {
  width: 4rem;
  height: 4rem;
  color: #6b7280;
  margin-bottom: 1rem;
}

.office-message,
.default-message {
  font-size: 1.125rem;
  color: #6b7280;
  margin-bottom: 2rem;
}

.file-info {
  background-color: #f9fafb;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 2rem;
  text-align: left;
}

.file-info p {
  margin: 0.5rem 0;
  font-size: 0.875rem;
  color: #374151;
}

.office-actions,
.download-btn {
  margin-top: 1rem;
}
</style>