<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="create-task-modal max-w-2xl">
      <DialogHeader>
        <DialogTitle>{{ t('task.createNew') }}</DialogTitle>
      </DialogHeader>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- 任务名称 -->
        <div class="form-group">
          <label class="form-label required">{{ t('task.name') }}</label>
          <Input
            v-model="form.name"
            :placeholder="t('task.namePlaceholder')"
            :class="errors.name ? 'border-red-500' : ''"
            required
          />
          <div v-if="errors.name" class="error-message">{{ errors.name }}</div>
        </div>

        <!-- 项目选择 -->
        <div class="form-group">
          <label class="form-label required">{{ t('project.title') }}</label>
          <Select v-model:value="form.projectId" @change="onProjectChange">
            <SelectTrigger :class="errors.projectId ? 'border-red-500' : ''">
              <SelectValue :placeholder="t('project.selectProject')" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="project in projects"
                :key="project.id"
                :value="String(project.id)"
              >
                {{ project.name }}
              </SelectItem>
            </SelectContent>
          </Select>
          <div v-if="errors.projectId" class="error-message">{{ errors.projectId }}</div>
        </div>

        <!-- 任务列选择 -->
        <div class="form-group" v-if="availableColumns.length > 0">
          <label class="form-label">{{ t('task.column') }}</label>
          <Select v-model:value="form.columnId">
            <SelectTrigger>
              <SelectValue :placeholder="t('task.selectColumn')" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="column in availableColumns"
                :key="column.id"
                :value="String(column.id)"
              >
                {{ column.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- 任务描述 -->
        <div class="form-group">
          <label class="form-label">{{ t('task.description') }}</label>
          <Textarea
            v-model="form.desc"
            :placeholder="t('task.descriptionPlaceholder')"
            rows="4"
          />
        </div>

        <!-- 任务详情 -->
        <div class="form-group">
          <label class="form-label">{{ t('task.content') }}</label>
          <Textarea
            v-model="form.content"
            :placeholder="t('task.contentPlaceholder')"
            rows="6"
          />
        </div>

        <!-- 两列布局 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 优先级 -->
          <div class="form-group">
            <label class="form-label">{{ t('task.priority') }}</label>
            <Select v-model:value="form.priority">
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

          <!-- 截止时间 -->
          <div class="form-group">
            <label class="form-label">{{ t('task.deadline') }}</label>
            <DatePicker
              v-model="form.endAt"
              :placeholder="t('task.selectDeadline')"
              format="YYYY-MM-DD HH:mm"
              type="datetime"
              class="w-full"
            />
          </div>
        </div>

        <!-- 任务分配 -->
        <div class="form-group">
          <label class="form-label">{{ t('task.assignees') }}</label>
          <div class="assignees-section">
            <!-- 已选择的分配者 -->
            <div v-if="selectedAssignees.length > 0" class="selected-assignees">
              <div
                v-for="user in selectedAssignees"
                :key="user.id"
                class="assignee-tag"
              >
                <Avatar :user="user" size="sm" />
                <span>{{ user.nickname }}</span>
                <Button
                  variant="ghost"
                  size="sm"
                  @click="removeAssignee(user)"
                  type="button"
                >
                  <X class="w-3 h-3" />
                </Button>
              </div>
            </div>

            <!-- 分配者选择 -->
            <Select v-model:value="selectedUserId" @change="addAssignee">
              <SelectTrigger>
                <SelectValue :placeholder="t('task.selectAssignee')" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="user in availableUsers"
                  :key="user.id"
                  :value="String(user.id)"
                >
                  <div class="flex items-center space-x-2">
                    <Avatar :user="user" size="sm" />
                    <span>{{ user.nickname }}</span>
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <!-- 标签 -->
        <div class="form-group">
          <label class="form-label">{{ t('task.tags') }}</label>
          <div class="tags-section">
            <!-- 已选择的标签 -->
            <div v-if="form.tags.length > 0" class="selected-tags">
              <Badge
                v-for="tag in form.tags"
                :key="tag"
                variant="default"
                class="tag-item"
              >
                {{ tag }}
                <Button
                  variant="ghost"
                  size="sm"
                  @click="removeTag(tag)"
                  type="button"
                >
                  <X class="w-2 h-2" />
                </Button>
              </Badge>
            </div>

            <!-- 标签输入 -->
            <div class="tag-input">
              <Input
                v-model="newTag"
                :placeholder="t('task.addTag')"
                @keydown.enter.prevent="addTag"
                @keydown.comma.prevent="addTag"
              />
              <Button
                variant="outline"
                size="sm"
                @click="addTag"
                type="button"
                :disabled="!newTag.trim()"
              >
                {{ t('common.add') }}
              </Button>
            </div>
          </div>
        </div>

        <!-- 高级选项 -->
        <details class="advanced-options">
          <summary class="advanced-toggle">
            <ChevronRight class="w-4 h-4 transition-transform details-open:rotate-90" />
            {{ t('task.advancedOptions') }}
          </summary>
          <div class="advanced-content">
            <!-- 父任务选择 -->
            <div class="form-group">
              <label class="form-label">{{ t('task.parentTask') }}</label>
              <Select v-model:value="form.parentId">
                <SelectTrigger>
                  <SelectValue :placeholder="t('task.selectParentTask')" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    v-for="task in availableTasks"
                    :key="task.id"
                    :value="String(task.id)"
                  >
                    {{ task.name }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>

            <!-- 时间估算 -->
            <div class="form-group">
              <label class="form-label">{{ t('task.estimatedTime') }}</label>
              <div class="time-input">
                <Input
                  v-model="form.estimatedHoursStr"
                  type="number"
                  min="0"
                  step="0.5"
                  :placeholder="t('task.hours')"
                />
                <span class="time-unit">{{ t('task.hours') }}</span>
              </div>
            </div>

            <!-- 开始时间 -->
            <div class="form-group">
              <label class="form-label">{{ t('task.startTime') }}</label>
              <DatePicker
                v-model="form.startAt"
                :placeholder="t('task.selectStartTime')"
                format="YYYY-MM-DD HH:mm"
                type="datetime"
                class="w-full"
              />
            </div>
          </div>
        </details>

        <!-- 任务模板 -->
        <div class="form-group" v-if="taskTemplates.length > 0">
          <label class="form-label">{{ t('task.useTemplate') }}</label>
          <Select v-model:value="selectedTemplate" @change="applyTemplate">
            <SelectTrigger>
              <SelectValue :placeholder="t('task.selectTemplate')" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">{{ t('task.noTemplate') }}</SelectItem>
              <SelectItem
                v-for="template in taskTemplates"
                :key="template.id"
                :value="String(template.id)"
              >
                {{ template.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- 表单操作 -->
        <div class="form-actions">
          <Button
            type="button"
            variant="outline"
            @click="$emit('update:open', false)"
          >
            {{ t('common.cancel') }}
          </Button>
          <Button
            type="submit"
            :disabled="isSubmitting || !isFormValid"
            :loading="isSubmitting"
          >
            {{ isSubmitting ? t('common.creating') : t('common.create') }}
          </Button>
        </div>
      </form>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { ChevronRight, X } from 'lucide-vue-next'

import {
  Dialog, DialogContent, DialogHeader, DialogTitle
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import {
  Select, SelectTrigger, SelectValue, SelectContent, SelectItem
} from '@/components/ui/select'
import { DatePicker } from '@/components/ui/date-picker'

import { taskApi, projectApi } from '@/api'

interface TaskForm {
  name: string
  projectId: string | null
  columnId: string | null
  desc: string
  content: string
  priority: string
  endAt: string | null
  startAt: string | null
  parentId: string | null
  estimatedHours: number | null
  estimatedHoursStr: string
  tags: string[]
}

interface FormErrors {
  name?: string
  projectId?: string
  [key: string]: string | undefined
}

interface TaskCreateModalProps {
  open: boolean
  projects: Array<{ id: number; name: string }>
  users: Array<{ id: number; nickname: string }>
}

const props = defineProps<TaskCreateModalProps>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  created: [task: any]
}>()

const { t } = useI18n()
const { toast } = useToast()

// 表单数据
const form = reactive<TaskForm>({
  name: '',
  projectId: null,
  columnId: null,
  desc: '',
  content: '',
  priority: 'medium',
  endAt: null,
  startAt: null,
  parentId: null,
  estimatedHours: null,
  estimatedHoursStr: '',
  tags: []
})

// 状态
const isSubmitting = ref(false)
const errors = reactive<FormErrors>({})
const selectedAssignees = ref<Array<{ id: number; nickname: string }>>([])
const selectedUserId = ref<number | null>(null)
const newTag = ref('')
const selectedTemplate = ref('')

// 数据
const availableColumns = ref<Array<{ id: number; name: string }>>([])
const availableTasks = ref<Array<{ id: number; name: string }>>([])
const taskTemplates = ref<any[]>([])

// 计算属性
const availableUsers = computed(() => {
  const assignedIds = selectedAssignees.value.map(user => user.id)
  return props.users.filter(user => !assignedIds.includes(user.id))
})

const isFormValid = computed(() => {
  return form.name.trim() && form.projectId && !Object.keys(errors).length
})

// Watch for estimatedHoursStr changes and update estimatedHours
watch(() => form.estimatedHoursStr, (newValue) => {
  const num = parseFloat(newValue)
  form.estimatedHours = isNaN(num) ? null : num
}, { immediate: true })

// Watch for estimatedHours changes and update estimatedHoursStr
watch(() => form.estimatedHours, (newValue) => {
  form.estimatedHoursStr = newValue?.toString() || ''
}, { immediate: true })

// 方法
const validateForm = () => {
  const newErrors: FormErrors = {}

  if (!form.name.trim()) {
    newErrors.name = t('validation.required', { field: t('task.name') })
  }

  if (!form.projectId) {
    newErrors.projectId = t('validation.required', { field: t('project.title') })
  }

  Object.assign(errors, newErrors)
  Object.keys(errors).forEach(key => {
    if (!newErrors[key]) {
      delete errors[key]
    }
  })

  return Object.keys(newErrors).length === 0
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    projectId: null,
    columnId: null,
    desc: '',
    content: '',
    priority: 'medium',
    endAt: null,
    startAt: null,
    parentId: null,
    estimatedHours: null,
    estimatedHoursStr: '',
    tags: []
  })
  selectedAssignees.value = []
  selectedUserId.value = null
  newTag.value = ''
  selectedTemplate.value = ''
  Object.keys(errors).forEach(key => delete errors[key])
}

const onProjectChange = async (projectIdStr: string) => {
  const projectId = parseInt(projectIdStr)
  try {
    // 加载项目的任务列 - 临时解决方案，如果API不存在
    try {
      const columnsResponse = await projectApi.getProjectColumns?.(projectId)
      availableColumns.value = columnsResponse?.data || []
    } catch {
      // Fallback: 设置默认列
      availableColumns.value = [
        { id: 1, name: '待办' },
        { id: 2, name: '进行中' },
        { id: 3, name: '已完成' }
      ]
    }

    // 设置默认列
    if (availableColumns.value.length > 0) {
      form.columnId = String(availableColumns.value[0].id)
    }

    // 加载项目的任务（用于父任务选择）
    try {
      const tasksResponse = await taskApi.getTasks({ projectId })
      availableTasks.value = tasksResponse.data.records || []
    } catch {
      availableTasks.value = []
    }
  } catch (error) {
    console.error('Failed to load project data:', error)
  }
}

const addAssignee = (userIdStr: string) => {
  const userId = parseInt(userIdStr)
  const user = props.users.find(u => u.id === userId)
  if (user && !selectedAssignees.value.find(u => u.id === userId)) {
    selectedAssignees.value.push(user)
    selectedUserId.value = null
  }
}

const removeAssignee = (user: any) => {
  const index = selectedAssignees.value.findIndex(u => u.id === user.id)
  if (index > -1) {
    selectedAssignees.value.splice(index, 1)
  }
}

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !form.tags.includes(tag)) {
    form.tags.push(tag)
    newTag.value = ''
  }
}

const removeTag = (tag: string) => {
  const index = form.tags.indexOf(tag)
  if (index > -1) {
    form.tags.splice(index, 1)
  }
}

const loadTaskTemplates = async () => {
  try {
    const response = await taskApi.getTaskTemplates()
    taskTemplates.value = response.data || []
  } catch (error) {
    console.error('Failed to load task templates:', error)
  }
}

const applyTemplate = (templateId: string) => {
  if (!templateId) return

  const template = taskTemplates.value.find(t => t.id === templateId)
  if (template) {
    form.desc = template.desc || form.desc
    form.content = template.content || form.content
    form.priority = template.priority || form.priority
    form.tags = [...(template.tags || []), ...form.tags]
    form.estimatedHours = template.estimatedHours || form.estimatedHours

    toast({
      title: t('task.templateApplied'),
      description: t('task.templateAppliedDesc', { name: template.name })
    })
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return

  isSubmitting.value = true

  try {
    const taskData = {
      name: form.name,
      projectId: form.projectId ? parseInt(form.projectId) : null,
      columnId: form.columnId ? parseInt(form.columnId) : null,
      desc: form.desc,
      content: form.content,
      priority: form.priority,
      endAt: form.endAt,
      startAt: form.startAt,
      parentId: form.parentId ? parseInt(form.parentId) : null,
      estimatedHours: form.estimatedHours,
      tags: form.tags,
      owner: selectedAssignees.value.length > 0
        ? JSON.stringify(selectedAssignees.value.map(user => user.id))
        : null
    }

    // 确保projectId不为null
    if (!taskData.projectId) {
      throw new Error('Project ID is required')
    }

    const response = await taskApi.createTask({
      ...taskData,
      projectId: taskData.projectId as number // 已确保不为null
    })

    emit('created', response.data)
    resetForm()

    toast({
      title: t('success.created'),
      description: t('task.createSuccess')
    })
  } catch (error: unknown) {
    toast({
      title: t('error.createFailed'),
      description: error instanceof Error ? error.message : 'Unknown error',
      variant: 'destructive'
    })
  } finally {
    isSubmitting.value = false
  }
}

// 监听器
watch(() => props.open, (open) => {
  if (open) {
    loadTaskTemplates()
  } else {
    resetForm()
  }
})

watch(() => form.name, () => {
  if (errors.name) {
    delete errors.name
  }
})

watch(() => form.projectId, () => {
  if (errors.projectId) {
    delete errors.projectId
  }
})

// 生命周期
onMounted(() => {
  if (props.open) {
    loadTaskTemplates()
  }
})
</script>

<style scoped>
.create-task-modal {
  @apply max-h-[90vh] overflow-hidden;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.form-label.required::after {
  @apply text-red-500 ml-1;
  content: '*';
}

.error-message {
  @apply text-sm text-red-600 dark:text-red-400;
}

.assignees-section {
  @apply space-y-3;
}

.selected-assignees {
  @apply flex flex-wrap gap-2;
}

.assignee-tag {
  @apply flex items-center space-x-2 bg-blue-50 dark:bg-blue-900 text-blue-700 dark:text-blue-300 px-3 py-1 rounded-full border border-blue-200 dark:border-blue-800;
}

.tags-section {
  @apply space-y-3;
}

.selected-tags {
  @apply flex flex-wrap gap-2;
}

.tag-item {
  @apply flex items-center space-x-1;
}

.tag-input {
  @apply flex items-center space-x-2;
}

.advanced-options {
  @apply border border-gray-200 dark:border-gray-700 rounded-lg p-4;
}

.advanced-toggle {
  @apply flex items-center space-x-2 cursor-pointer font-medium text-gray-700 dark:text-gray-300 select-none;
}

.advanced-content {
  @apply mt-4 space-y-4;
}

.time-input {
  @apply flex items-center space-x-2;
}

.time-unit {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.form-actions {
  @apply flex items-center justify-end space-x-3 pt-4 border-t border-gray-200 dark:border-gray-700;
}

/* Details元素样式 */
details[open] .details-open {
  @apply rotate-90;
}

details summary {
  @apply list-none;
}

details summary::-webkit-details-marker {
  @apply hidden;
}
</style>