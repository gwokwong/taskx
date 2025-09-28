<template>
  <div class="comment-form">
    <div class="form-content">
      <Textarea
        v-model="content"
        :placeholder="placeholder"
        :rows="editMode ? 4 : 3"
        class="comment-textarea"
        @keydown="handleKeydown"
      />

      <!-- 工具栏 -->
      <div class="form-toolbar">
        <div class="toolbar-left">
          <!-- 表情选择器 -->
          <Button
            @click="showEmojiPicker = !showEmojiPicker"
            size="sm"
            variant="ghost"
            class="toolbar-btn"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </Button>

          <!-- @提及 -->
          <Button
            @click="insertMention"
            size="sm"
            variant="ghost"
            class="toolbar-btn"
          >
            @
          </Button>

          <!-- 文件附件 -->
          <Button
            @click="triggerFileUpload"
            size="sm"
            variant="ghost"
            class="toolbar-btn"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"/>
            </svg>
          </Button>

          <input
            ref="fileInput"
            type="file"
            multiple
            class="hidden"
            @change="handleFileSelect"
          />
        </div>

        <div class="toolbar-right">
          <span class="char-count">{{ content.length }}/1000</span>
        </div>
      </div>

      <!-- 表情选择器 -->
      <div v-if="showEmojiPicker" class="emoji-picker">
        <div class="emoji-grid">
          <button
            v-for="emoji in commonEmojis"
            :key="emoji"
            @click="insertEmoji(emoji)"
            class="emoji-btn"
          >
            {{ emoji }}
          </button>
        </div>
      </div>

      <!-- 附件列表 -->
      <div v-if="attachments.length > 0" class="attachments-list">
        <h5>附件</h5>
        <div class="attachment-items">
          <div
            v-for="(file, index) in attachments"
            :key="index"
            class="attachment-item"
          >
            <svg class="attachment-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            <span class="attachment-name">{{ file.name }}</span>
            <button
              @click="removeAttachment(index)"
              class="remove-attachment"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <Button
          v-if="editMode || parentId"
          @click="handleCancel"
          variant="outline"
          size="sm"
        >
          取消
        </Button>
        <Button
          @click="handleSubmit"
          :disabled="!content.trim() || submitting"
          size="sm"
        >
          {{ submitting ? '发送中...' : (editMode ? '更新' : '发送') }}
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, onMounted } from 'vue'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { taskApi } from '@/api/task'

interface Props {
  taskId: number
  parentId?: number
  placeholder?: string
  initialContent?: string
  editMode?: boolean
}

interface Emits {
  (e: 'submit', content: string, attachments?: File[]): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '写下你的评论...',
  editMode: false
})

const emit = defineEmits<Emits>()

const content = ref('')
const submitting = ref(false)
const showEmojiPicker = ref(false)
const attachments = ref<File[]>([])
const fileInput = ref<HTMLInputElement>()

const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣',
  '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰',
  '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜',
  '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙',
  '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✍️', '💪',
  '🎉', '🎊', '💯', '✨', '🔥', '💥', '💫', '⭐'
]

onMounted(() => {
  if (props.initialContent) {
    content.value = props.initialContent
  }
})

const handleKeydown = (e: KeyboardEvent) => {
  // Ctrl+Enter 或 Cmd+Enter 快速发送
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault()
    handleSubmit()
  }
}

const handleSubmit = async () => {
  if (!content.value.trim() || submitting.value) return

  submitting.value = true

  try {
    if (props.editMode) {
      // 编辑模式直接返回内容
      emit('submit', content.value.trim())
    } else {
      // 新增评论
      const commentData = {
        content: content.value.trim(),
        parentId: props.parentId,
        mentions: extractMentions(content.value)
      }

      await taskApi.addTaskComment(props.taskId, commentData)

      // 重置表单
      content.value = ''
      attachments.value = []
      showEmojiPicker.value = false

      emit('submit', content.value.trim(), attachments.value)
    }
  } catch (error) {
    console.error('提交评论失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  content.value = props.initialContent || ''
  attachments.value = []
  showEmojiPicker.value = false
  emit('cancel')
}

const insertEmoji = (emoji: string) => {
  const textarea = document.querySelector('.comment-textarea textarea') as HTMLTextAreaElement
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    content.value = content.value.slice(0, start) + emoji + content.value.slice(end)

    // 重新设置光标位置
    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + emoji.length, start + emoji.length)
    }, 0)
  } else {
    content.value += emoji
  }
  showEmojiPicker.value = false
}

const insertMention = () => {
  const textarea = document.querySelector('.comment-textarea textarea') as HTMLTextAreaElement
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    content.value = content.value.slice(0, start) + '@' + content.value.slice(end)

    setTimeout(() => {
      textarea.focus()
      textarea.setSelectionRange(start + 1, start + 1)
    }, 0)
  } else {
    content.value += '@'
  }
}

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleFileSelect = (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  attachments.value.push(...files)
}

const removeAttachment = (index: number) => {
  attachments.value.splice(index, 1)
}

const extractMentions = (text: string): number[] => {
  const mentionRegex = /@(\w+)/g
  const mentions: number[] = []
  let match

  while ((match = mentionRegex.exec(text)) !== null) {
    // 这里应该根据用户名查找用户ID
    // 简化实现，实际应该调用API查找用户
    const username = match[1]
    // mentions.push(getUserIdByUsername(username))
  }

  return mentions
}
</script>

<style scoped>
.comment-form {
  width: 100%;
}

.form-content {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background-color: white;
  overflow: hidden;
}

.comment-textarea {
  border: none;
  border-radius: 0;
}

.comment-textarea :deep(textarea) {
  border: none;
  box-shadow: none;
  resize: none;
  min-height: 80px;
}

.comment-textarea :deep(textarea):focus {
  border: none;
  box-shadow: none;
}

.form-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background-color: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.toolbar-left {
  display: flex;
  gap: 0.25rem;
}

.toolbar-btn {
  padding: 0.25rem;
  height: auto;
  width: auto;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.char-count {
  font-size: 0.75rem;
  color: #6b7280;
}

.hidden {
  display: none;
}

.emoji-picker {
  padding: 0.75rem;
  border-top: 1px solid #e5e7eb;
  background-color: white;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 0.25rem;
}

.emoji-btn {
  padding: 0.25rem;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 4px;
  font-size: 1.25rem;
  transition: background-color 0.2s;
}

.emoji-btn:hover {
  background-color: #f3f4f6;
}

.attachments-list {
  padding: 0.75rem;
  border-top: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.attachments-list h5 {
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.attachment-items {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}

.attachment-icon {
  width: 1rem;
  height: 1rem;
  color: #6b7280;
  flex-shrink: 0;
}

.attachment-name {
  flex: 1;
  font-size: 0.875rem;
  color: #374151;
  min-width: 0;
  word-break: break-all;
}

.remove-attachment {
  padding: 0.125rem;
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  border-radius: 2px;
  flex-shrink: 0;
}

.remove-attachment:hover {
  background-color: #fef2f2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 0.75rem;
  background-color: #f9fafb;
  border-top: 1px solid #e5e7eb;
}
</style>