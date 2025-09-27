<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="task-detail-modal" :class="{ 'max-w-4xl': !isMobile }">
      <DialogHeader>
        <DialogTitle class="task-title-header">
          <div class="title-content">
            <Input
              v-if="editingTitle"
              v-model="editableTask.name"
              class="title-input"
              @blur="saveTitle"
              @keydown.enter="saveTitle"
              @keydown.esc="cancelTitleEdit"
            />
            <h2
              v-else
              class="task-title"
              :class="{ 'completed': editableTask.complete_at }"
              @click="startTitleEdit"
            >
              {{ editableTask.name }}
            </h2>
            <Button v-if="!editingTitle" variant="ghost" size="sm" @click="startTitleEdit">
              <Edit class="w-4 h-4" />
            </Button>
          </div>
          <div class="title-meta">
            <TaskStatusBadge :task="editableTask" />
            <TaskPriorityBadge :priority="editableTask.priority" />
          </div>
        </DialogTitle>
      </DialogHeader>

      <div class="task-detail-content">
        <div class="main-content">
          <!-- 任务描述 -->
          <div class="description-section">
            <div class="section-header">
              <h3>{{ t('task.description') }}</h3>
              <Button variant="ghost" size="sm" @click="editingDescription = !editingDescription">
                <Edit class="w-4 h-4" />
              </Button>
            </div>
            <div v-if="editingDescription" class="description-editor">
              <Textarea
                v-model="editableTask.desc"
                :placeholder="t('task.descriptionPlaceholder')"
                rows="6"
                @blur="saveDescription"
              />
              <div class="editor-actions">
                <Button size="sm" @click="saveDescription">{{ t('common.save') }}</Button>
                <Button variant="ghost" size="sm" @click="cancelDescriptionEdit">{{ t('common.cancel') }}</Button>
              </div>
            </div>
            <div v-else class="description-display" @click="editingDescription = true">
              <div v-if="editableTask.desc" class="description-text" v-html="formatDescription(editableTask.desc)"></div>
              <div v-else class="description-placeholder">{{ t('task.addDescription') }}</div>
            </div>
          </div>

          <!-- 子任务列表 -->
          <div v-if="subtasks.length > 0 || showAddSubtask" class="subtasks-section">
            <div class="section-header">
              <h3>{{ t('task.subtasks') }} ({{ completedSubtasks }}/{{ subtasks.length }})</h3>
              <Button variant="ghost" size="sm" @click="showAddSubtask = !showAddSubtask">
                <Plus class="w-4 h-4" />
              </Button>
            </div>
            <div class="subtasks-progress">
              <Progress :value="subtaskProgress" class="w-full" />
              <span class="progress-text">{{ subtaskProgress }}%</span>
            </div>
            <div class="subtasks-list">
              <div v-for="subtask in subtasks" :key="subtask.id" class="subtask-item">
                <Checkbox
                  :checked="!!subtask.complete_at"
                  @update:checked="toggleSubtask(subtask)"
                />
                <span
                  class="subtask-name"
                  :class="{ 'completed': subtask.complete_at }"
                  @click="openSubtaskDetail(subtask)"
                >
                  {{ subtask.name }}
                </span>
                <Button variant="ghost" size="sm" @click="deleteSubtask(subtask)">
                  <Trash2 class="w-3 h-3" />
                </Button>
              </div>
            </div>
            <div v-if="showAddSubtask" class="add-subtask">
              <Input
                v-model="newSubtaskName"
                :placeholder="t('task.addSubtaskPlaceholder')"
                @keydown.enter="addSubtask"
                @keydown.esc="cancelAddSubtask"
              />
              <div class="add-subtask-actions">
                <Button size="sm" @click="addSubtask">{{ t('common.add') }}</Button>
                <Button variant="ghost" size="sm" @click="cancelAddSubtask">{{ t('common.cancel') }}</Button>
              </div>
            </div>
          </div>

          <!-- 附件列表 -->
          <div class="attachments-section">
            <div class="section-header">
              <h3>{{ t('task.attachments') }}</h3>
              <Button variant="ghost" size="sm" @click="$refs.fileInput.click()">
                <Paperclip class="w-4 h-4" />
              </Button>
            </div>
            <div v-if="attachments.length > 0" class="attachments-list">
              <div v-for="attachment in attachments" :key="attachment.id" class="attachment-item">
                <div class="attachment-info">
                  <FileText class="w-4 h-4" />
                  <span class="attachment-name">{{ attachment.name }}</span>
                  <span class="attachment-size">{{ formatFileSize(attachment.size) }}</span>
                </div>
                <div class="attachment-actions">
                  <Button variant="ghost" size="sm" @click="downloadAttachment(attachment)">
                    <Download class="w-3 h-3" />
                  </Button>
                  <Button variant="ghost" size="sm" @click="deleteAttachment(attachment)">
                    <Trash2 class="w-3 h-3" />
                  </Button>
                </div>
              </div>
            </div>
            <input
              ref="fileInput"
              type="file"
              multiple
              class="hidden"
              @change="handleFileUpload"
            />
          </div>

          <!-- 评论列表 -->
          <div class="comments-section">
            <div class="section-header">
              <h3>{{ t('task.comments') }}</h3>
            </div>
            <div class="comments-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <Avatar :user="comment.user" size="sm" />
                  <span class="comment-author">{{ comment.user.nickname }}</span>
                  <span class="comment-time">{{ formatTime(comment.created_at) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
            </div>
            <div class="add-comment">
              <Textarea
                v-model="newComment"
                :placeholder="t('task.addCommentPlaceholder')"
                rows="3"
              />
              <div class="comment-actions">
                <Button size="sm" @click="addComment" :disabled="!newComment.trim()">
                  {{ t('task.addComment') }}
                </Button>
              </div>
            </div>
          </div>
        </div>

        <!-- 侧边栏 -->
        <div class="sidebar">
          <!-- 任务状态 -->
          <div class="sidebar-section">
            <h4>{{ t('task.status') }}</h4>
            <div class="status-actions">
              <Button
                v-if="!editableTask.complete_at"
                @click="completeTask"
                class="w-full"
              >
                <CheckCircle class="w-4 h-4 mr-2" />
                {{ t('task.markComplete') }}
              </Button>
              <Button
                v-else
                variant="outline"
                @click="reopenTask"
                class="w-full"
              >
                <RotateCcw class="w-4 h-4 mr-2" />
                {{ t('task.reopen') }}
              </Button>
            </div>
          </div>

          <!-- 任务分配 -->
          <div class="sidebar-section">
            <h4>{{ t('task.assignees') }}</h4>
            <div class="assignees-list">
              <div v-for="user in assignees" :key="user.id" class="assignee-item">
                <Avatar :user="user" size="sm" />
                <span>{{ user.nickname }}</span>
                <Button variant="ghost" size="sm" @click="removeAssignee(user)">
                  <X class="w-3 h-3" />
                </Button>
              </div>
            </div>
            <Select v-model:value="selectedUserId" @change="addAssignee">
              <SelectTrigger>
                <SelectValue :placeholder="t('task.addAssignee')" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="user in availableUsers"
                  :key="user.id"
                  :value="user.id"
                >
                  {{ user.nickname }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- 截止时间 -->
          <div class="sidebar-section">
            <h4>{{ t('task.deadline') }}</h4>
            <DatePicker
              v-model="editableTask.end_at"
              :placeholder="t('task.setDeadline')"
              format="YYYY-MM-DD HH:mm"
              type="datetime"
              class="w-full"
              @change="updateDeadline"
            />
          </div>

          <!-- 优先级 -->
          <div class="sidebar-section">
            <h4>{{ t('task.priority') }}</h4>
            <Select v-model:value="editableTask.priority" @change="updatePriority">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="low">{{ t('task.priority.low') }}</SelectItem>
                <SelectItem value="medium">{{ t('task.priority.medium') }}</SelectItem>
                <SelectItem value="high">{{ t('task.priority.high') }}</SelectItem>
                <SelectItem value="urgent">{{ t('task.priority.urgent') }}</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- 标签 -->
          <div class="sidebar-section">
            <h4>{{ t('task.tags') }}</h4>
            <div class="tags-list">
              <Badge
                v-for="tag in editableTask.tags || []"
                :key="tag"
                variant="secondary"
                class="tag-item"
              >
                {{ tag }}
                <Button variant="ghost" size="sm" @click="removeTag(tag)">
                  <X class="w-2 h-2" />
                </Button>
              </Badge>
            </div>
            <Input
              v-model="newTag"
              :placeholder="t('task.addTag')"
              @keydown.enter="addTag"
            />
          </div>

          <!-- 任务操作 -->
          <div class="sidebar-section">
            <h4>{{ t('common.actions') }}</h4>
            <div class="action-buttons">
              <Button variant="outline" @click="duplicateTask" class="w-full">
                <Copy class="w-4 h-4 mr-2" />
                {{ t('task.duplicate') }}
              </Button>
              <Button variant="outline" @click="archiveTask" class="w-full">
                <Archive class="w-4 h-4 mr-2" />
                {{ t('task.archive') }}
              </Button>
              <Button variant="destructive" @click="deleteTask" class="w-full">
                <Trash2 class="w-4 h-4 mr-2" />
                {{ t('common.delete') }}
              </Button>
            </div>
          </div>

          <!-- 任务信息 -->
          <div class="sidebar-section">
            <h4>{{ t('task.info') }}</h4>
            <div class="task-info">
              <div class="info-item">
                <span class="info-label">{{ t('task.created') }}</span>
                <span class="info-value">{{ formatTime(editableTask.created_at) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ t('task.updated') }}</span>
                <span class="info-value">{{ formatTime(editableTask.updated_at) }}</span>
              </div>
              <div v-if="editableTask.complete_at" class="info-item">
                <span class="info-label">{{ t('task.completed') }}</span>
                <span class="info-value">{{ formatTime(editableTask.complete_at) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useBreakpoints } from '@/hooks/use-breakpoints'
import {
  Edit, Plus, Trash2, Paperclip, FileText, Download, CheckCircle,
  RotateCcw, X, Copy, Archive
} from 'lucide-vue-next'

import {
  Dialog, DialogContent, DialogHeader, DialogTitle
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Checkbox } from '@/components/ui/checkbox'
import { Progress } from '@/components/ui/progress'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import {
  Select, SelectTrigger, SelectValue, SelectContent, SelectItem
} from '@/components/ui/select'
import { DatePicker } from '@/components/ui/date-picker'

import TaskStatusBadge from './TaskStatusBadge.vue'
import TaskPriorityBadge from './TaskPriorityBadge.vue'

import { taskApi, uploadApi } from '@/api'

interface TaskDetailModalProps {
  open: boolean
  task: any
  projects: any[]
  users: any[]
}

const props = defineProps<TaskDetailModalProps>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  updated: [task: any]
  deleted: []
}>()

const { t } = useI18n()
const { toast } = useToast()
const { isMobile } = useBreakpoints()

// 响应式数据
const editableTask = reactive({ ...props.task })
const subtasks = ref([])
const attachments = ref([])
const comments = ref([])

// 编辑状态
const editingTitle = ref(false)
const editingDescription = ref(false)
const showAddSubtask = ref(false)

// 表单数据
const newSubtaskName = ref('')
const newComment = ref('')
const newTag = ref('')
const selectedUserId = ref(null)

// 计算属性
const assignees = computed(() => {
  if (!editableTask.owner || !props.users) return []

  try {
    const ownerIds = typeof editableTask.owner === 'string'
      ? JSON.parse(editableTask.owner)
      : [editableTask.owner]
    return props.users.filter(user => ownerIds.includes(user.id))
  } catch {
    return []
  }
})

const availableUsers = computed(() => {
  const assignedIds = assignees.value.map(user => user.id)
  return props.users.filter(user => !assignedIds.includes(user.id))
})

const completedSubtasks = computed(() => {
  return subtasks.value.filter(subtask => subtask.complete_at).length
})

const subtaskProgress = computed(() => {
  if (subtasks.value.length === 0) return 0
  return Math.round((completedSubtasks.value / subtasks.value.length) * 100)
})

// 方法
const startTitleEdit = () => {
  editingTitle.value = true
}

const saveTitle = async () => {
  try {
    await taskApi.updateTask(editableTask.id, { name: editableTask.name })
    editingTitle.value = false
    emit('updated', editableTask)
    toast({ title: t('success.updated') })
  } catch (error) {
    toast({
      title: t('error.updateFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const cancelTitleEdit = () => {
  editableTask.name = props.task.name
  editingTitle.value = false
}

const saveDescription = async () => {
  try {
    await taskApi.updateTask(editableTask.id, { desc: editableTask.desc })
    editingDescription.value = false
    emit('updated', editableTask)
    toast({ title: t('success.updated') })
  } catch (error) {
    toast({
      title: t('error.updateFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const cancelDescriptionEdit = () => {
  editableTask.desc = props.task.desc
  editingDescription.value = false
}

const formatDescription = (text: string): string => {
  return text.replace(/\n/g, '<br>')
}

const loadSubtasks = async () => {
  try {
    const response = await taskApi.getSubtasks(editableTask.id)
    subtasks.value = response.data
  } catch (error) {
    console.error('Failed to load subtasks:', error)
  }
}

const addSubtask = async () => {
  if (!newSubtaskName.value.trim()) return

  try {
    const subtask = await taskApi.createSubtask(editableTask.id, {
      name: newSubtaskName.value.trim()
    })
    subtasks.value.push(subtask.data)
    newSubtaskName.value = ''
    showAddSubtask.value = false
    toast({ title: t('task.subtaskAdded') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const cancelAddSubtask = () => {
  newSubtaskName.value = ''
  showAddSubtask.value = false
}

const toggleSubtask = async (subtask: any) => {
  try {
    if (subtask.complete_at) {
      await taskApi.reopenTask(subtask.id)
      subtask.complete_at = null
    } else {
      await taskApi.completeTask(subtask.id)
      subtask.complete_at = new Date().toISOString()
    }
    toast({ title: t('success.updated') })
  } catch (error) {
    toast({
      title: t('error.updateFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const deleteSubtask = async (subtask: any) => {
  try {
    await taskApi.deleteTask(subtask.id)
    const index = subtasks.value.findIndex(s => s.id === subtask.id)
    if (index > -1) {
      subtasks.value.splice(index, 1)
    }
    toast({ title: t('success.deleted') })
  } catch (error) {
    toast({
      title: t('error.deleteFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const handleFileUpload = async (event: Event) => {
  const files = (event.target as HTMLInputElement).files
  if (!files || files.length === 0) return

  try {
    for (const file of files) {
      const response = await uploadApi.uploadFile(file, {
        taskId: editableTask.id
      })
      attachments.value.push(response.data)
    }
    toast({ title: t('task.filesUploaded') })
  } catch (error) {
    toast({
      title: t('error.uploadFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const downloadAttachment = (attachment: any) => {
  window.open(attachment.url, '_blank')
}

const deleteAttachment = async (attachment: any) => {
  try {
    await uploadApi.deleteFile(attachment.id)
    const index = attachments.value.findIndex(a => a.id === attachment.id)
    if (index > -1) {
      attachments.value.splice(index, 1)
    }
    toast({ title: t('success.deleted') })
  } catch (error) {
    toast({
      title: t('error.deleteFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const loadComments = async () => {
  try {
    const response = await taskApi.getComments(editableTask.id)
    comments.value = response.data
  } catch (error) {
    console.error('Failed to load comments:', error)
  }
}

const addComment = async () => {
  if (!newComment.value.trim()) return

  try {
    const comment = await taskApi.addComment(editableTask.id, {
      content: newComment.value.trim()
    })
    comments.value.push(comment.data)
    newComment.value = ''
    toast({ title: t('task.commentAdded') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const completeTask = async () => {
  try {
    await taskApi.completeTask(editableTask.id)
    editableTask.complete_at = new Date().toISOString()
    emit('updated', editableTask)
    toast({ title: t('task.completed') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const reopenTask = async () => {
  try {
    await taskApi.reopenTask(editableTask.id)
    editableTask.complete_at = null
    emit('updated', editableTask)
    toast({ title: t('task.reopened') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const addAssignee = async (userId: number) => {
  try {
    const currentAssignees = assignees.value.map(user => user.id)
    currentAssignees.push(userId)

    await taskApi.assignTask(editableTask.id, currentAssignees)
    editableTask.owner = JSON.stringify(currentAssignees)
    selectedUserId.value = null
    emit('updated', editableTask)
    toast({ title: t('task.assigneeAdded') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const removeAssignee = async (user: any) => {
  try {
    const currentAssignees = assignees.value
      .filter(assignee => assignee.id !== user.id)
      .map(assignee => assignee.id)

    await taskApi.assignTask(editableTask.id, currentAssignees)
    editableTask.owner = JSON.stringify(currentAssignees)
    emit('updated', editableTask)
    toast({ title: t('task.assigneeRemoved') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const updateDeadline = async () => {
  try {
    await taskApi.updateTask(editableTask.id, { end_at: editableTask.end_at })
    emit('updated', editableTask)
    toast({ title: t('success.updated') })
  } catch (error) {
    toast({
      title: t('error.updateFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const updatePriority = async () => {
  try {
    await taskApi.updateTask(editableTask.id, { priority: editableTask.priority })
    emit('updated', editableTask)
    toast({ title: t('success.updated') })
  } catch (error) {
    toast({
      title: t('error.updateFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const addTag = async () => {
  if (!newTag.value.trim()) return

  const tags = editableTask.tags || []
  if (tags.includes(newTag.value.trim())) {
    toast({ title: t('task.tagExists'), variant: 'destructive' })
    return
  }

  try {
    tags.push(newTag.value.trim())
    await taskApi.updateTask(editableTask.id, { tags })
    editableTask.tags = tags
    newTag.value = ''
    emit('updated', editableTask)
    toast({ title: t('task.tagAdded') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const removeTag = async (tag: string) => {
  try {
    const tags = (editableTask.tags || []).filter(t => t !== tag)
    await taskApi.updateTask(editableTask.id, { tags })
    editableTask.tags = tags
    emit('updated', editableTask)
    toast({ title: t('task.tagRemoved') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const duplicateTask = () => {
  // 实现任务复制逻辑
  emit('updated', {
    ...editableTask,
    id: undefined,
    name: `${editableTask.name} (${t('task.copy')})`
  })
}

const archiveTask = async () => {
  try {
    await taskApi.archiveTask(editableTask.id)
    emit('updated', editableTask)
    emit('update:open', false)
    toast({ title: t('task.archived') })
  } catch (error) {
    toast({
      title: t('error.failed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const deleteTask = async () => {
  try {
    await taskApi.deleteTask(editableTask.id)
    emit('deleted')
    emit('update:open', false)
    toast({ title: t('task.deleted') })
  } catch (error) {
    toast({
      title: t('error.deleteFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const formatTime = (date: string): string => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(date))
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 监听器
watch(() => props.task, (newTask) => {
  if (newTask) {
    Object.assign(editableTask, newTask)
  }
}, { deep: true })

watch(() => props.open, (open) => {
  if (open && props.task) {
    loadSubtasks()
    loadComments()
  }
})

// 生命周期
onMounted(() => {
  if (props.open && props.task) {
    loadSubtasks()
    loadComments()
  }
})
</script>

<style scoped>
.task-detail-modal {
  @apply max-h-[90vh] overflow-hidden;
}

.task-title-header {
  @apply space-y-3;
}

.title-content {
  @apply flex items-center space-x-2;
}

.title-input {
  @apply text-lg font-semibold;
}

.task-title {
  @apply text-lg font-semibold cursor-pointer hover:text-blue-600;
}

.task-title.completed {
  @apply line-through text-gray-500;
}

.title-meta {
  @apply flex items-center space-x-2;
}

.task-detail-content {
  @apply flex gap-6 max-h-[70vh] overflow-hidden;
}

.main-content {
  @apply flex-1 overflow-y-auto space-y-6 pr-2;
}

.sidebar {
  @apply w-80 space-y-6 overflow-y-auto;
}

.section-header {
  @apply flex items-center justify-between mb-3;
}

.section-header h3 {
  @apply font-medium text-gray-900 dark:text-gray-100;
}

.section-header h4 {
  @apply font-medium text-gray-900 dark:text-gray-100 mb-3;
}

.description-editor {
  @apply space-y-3;
}

.editor-actions {
  @apply flex items-center space-x-2;
}

.description-display {
  @apply min-h-[80px] p-3 border border-gray-200 dark:border-gray-700 rounded-md cursor-pointer hover:border-gray-300 dark:hover:border-gray-600;
}

.description-text {
  @apply text-gray-900 dark:text-gray-100;
}

.description-placeholder {
  @apply text-gray-500 dark:text-gray-400 italic;
}

.subtasks-progress {
  @apply flex items-center space-x-3 mb-4;
}

.progress-text {
  @apply text-sm text-gray-600 dark:text-gray-400 min-w-[40px];
}

.subtasks-list {
  @apply space-y-2;
}

.subtask-item {
  @apply flex items-center space-x-3 p-2 border border-gray-100 dark:border-gray-800 rounded-md;
}

.subtask-name {
  @apply flex-1 cursor-pointer hover:text-blue-600;
}

.subtask-name.completed {
  @apply line-through text-gray-500 dark:text-gray-400;
}

.add-subtask {
  @apply space-y-3 p-3 border border-dashed border-gray-300 dark:border-gray-600 rounded-md;
}

.add-subtask-actions {
  @apply flex items-center space-x-2;
}

.attachments-list {
  @apply space-y-2;
}

.attachment-item {
  @apply flex items-center justify-between p-2 border border-gray-100 dark:border-gray-800 rounded-md;
}

.attachment-info {
  @apply flex items-center space-x-2 flex-1;
}

.attachment-name {
  @apply font-medium;
}

.attachment-size {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.attachment-actions {
  @apply flex items-center space-x-1;
}

.comments-list {
  @apply space-y-4 mb-4;
}

.comment-item {
  @apply space-y-2;
}

.comment-header {
  @apply flex items-center space-x-2;
}

.comment-author {
  @apply font-medium;
}

.comment-time {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.comment-content {
  @apply text-gray-700 dark:text-gray-300 ml-8;
}

.add-comment {
  @apply space-y-3;
}

.comment-actions {
  @apply flex justify-end;
}

.sidebar-section {
  @apply p-4 border border-gray-200 dark:border-gray-700 rounded-lg;
}

.status-actions {
  @apply space-y-2;
}

.assignees-list {
  @apply space-y-2 mb-3;
}

.assignee-item {
  @apply flex items-center space-x-2;
}

.tags-list {
  @apply flex flex-wrap gap-2 mb-3;
}

.tag-item {
  @apply flex items-center space-x-1;
}

.action-buttons {
  @apply space-y-2;
}

.task-info {
  @apply space-y-2;
}

.info-item {
  @apply flex justify-between text-sm;
}

.info-label {
  @apply text-gray-600 dark:text-gray-400;
}

.info-value {
  @apply text-gray-900 dark:text-gray-100;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-detail-content {
    @apply flex-col;
  }

  .sidebar {
    @apply w-full;
  }

  .main-content {
    @apply pr-0;
  }
}
</style>