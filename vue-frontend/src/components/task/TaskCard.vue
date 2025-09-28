<template>
  <div
    class="task-card"
    :class="{
      'overdue': isOverdue,
      'completed': task.complete_at,
      'high-priority': task.priority === 'high' || task.priority === 'urgent',
      'has-subtasks': hasSubtasks
    }"
    @click="$emit('click', task)"
  >
    <!-- 任务优先级标识 -->
    <div v-if="task.priority && task.priority !== 'medium'" class="priority-indicator" :class="task.priority">
      <div class="priority-dot"></div>
    </div>

    <!-- 工作流状态标识 -->
    <div v-if="task.flow_item_name" class="flow-status" :style="flowItemStyle">
      <span :class="task.flow_item_status">{{ task.flow_item_name }}</span>
    </div>

    <!-- 任务标题 -->
    <div class="task-title" :class="{ 'completed': task.complete_at }">
      {{ task.name }}
    </div>

    <!-- 任务描述 -->
    <div v-if="task.desc" class="task-description">
      {{ truncateText(task.desc, 80) }}
    </div>

    <!-- 子任务进度 -->
    <div v-if="hasSubtasks" class="subtask-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: subtaskProgress + '%' }"></div>
      </div>
      <span class="progress-text">
        {{ task.subtasks_complete }}/{{ task.subtasks }} {{ t('task.subtasks') }}
      </span>
    </div>

    <!-- 任务标签 -->
    <div v-if="task.tags && task.tags.length > 0" class="task-tags">
      <span v-for="tag in task.tags" :key="tag" class="tag">{{ tag }}</span>
    </div>

    <!-- 任务底部信息 -->
    <div class="task-footer">
      <!-- 截止时间 -->
      <div v-if="task.end_at" class="deadline" :class="{ 'overdue': isOverdue, 'today': isToday }">
        <Clock class="w-3 h-3" />
        <span>{{ formatDeadline(task.end_at) }}</span>
      </div>

      <!-- 任务负责人 -->
      <div v-if="assignees.length > 0" class="assignees">
        <AvatarGroup :users="assignees" :max="2" size="sm" />
      </div>

      <!-- 附件数量 -->
      <div v-if="(task.attachments_count ?? 0) > 0" class="attachments">
        <Paperclip class="w-3 h-3" />
        <span>{{ task.attachments_count }}</span>
      </div>

      <!-- 评论数量 -->
      <div v-if="(task.comments_count ?? 0) > 0" class="comments">
        <MessageCircle class="w-3 h-3" />
        <span>{{ task.comments_count }}</span>
      </div>
    </div>

    <!-- 任务操作菜单 -->
    <div class="task-actions" @click.stop>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" class="action-trigger">
            <MoreHorizontal class="w-4 h-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem @click="$emit('update', task)">
            <Edit class="w-4 h-4 mr-2" />
            {{ t('common.edit') }}
          </DropdownMenuItem>
          <DropdownMenuItem v-if="!task.complete_at" @click="$emit('complete', task)">
            <CheckCircle class="w-4 h-4 mr-2" />
            {{ t('task.complete') }}
          </DropdownMenuItem>
          <DropdownMenuItem v-if="!task.archived_at" @click="$emit('archive', task)">
            <Archive class="w-4 h-4 mr-2" />
            {{ t('task.archive') }}
          </DropdownMenuItem>
          <DropdownMenuItem @click="duplicateTask">
            <Copy class="w-4 h-4 mr-2" />
            {{ t('task.duplicate') }}
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem @click="$emit('delete', task)" class="text-red-600">
            <Trash2 class="w-4 h-4 mr-2" />
            {{ t('common.delete') }}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <!-- 拖拽提示 -->
    <div class="drag-handle" title="拖拽移动任务">
      <GripVertical class="w-4 h-4" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Clock, Paperclip, MessageCircle, MoreHorizontal,
  Edit, CheckCircle, Archive, Copy, Trash2, GripVertical
} from 'lucide-vue-next'

import { Button } from '@/components/ui/button'
import {
  DropdownMenu, DropdownMenuTrigger, DropdownMenuContent,
  DropdownMenuItem, DropdownMenuSeparator
} from '@/components/ui/dropdown-menu'
import AvatarGroup from '@/components/ui/AvatarGroup.vue'

interface TaskCardProps {
  task: {
    id: number
    name: string
    desc?: string
    priority?: string
    flow_item_name?: string
    flow_item_status?: string
    flow_item_color?: string
    complete_at?: string
    archived_at?: string
    end_at?: string
    subtasks?: number
    subtasks_complete?: number
    owner?: string
    tags?: string[]
    attachments_count?: number
    comments_count?: number
  }
  users?: any[]
}

const props = defineProps<TaskCardProps>()

const emit = defineEmits<{
  click: [task: any]
  update: [task: any]
  delete: [task: any]
  complete: [task: any]
  archive: [task: any]
}>()

const { t } = useI18n()

// 计算属性
const isOverdue = computed(() => {
  if (!props.task.end_at || props.task.complete_at) return false
  return new Date(props.task.end_at) < new Date()
})

const isToday = computed(() => {
  if (!props.task.end_at) return false
  const today = new Date()
  const deadline = new Date(props.task.end_at)
  return (
    today.getFullYear() === deadline.getFullYear() &&
    today.getMonth() === deadline.getMonth() &&
    today.getDate() === deadline.getDate()
  )
})

const hasSubtasks = computed(() => {
  return (props.task.subtasks || 0) > 0
})

const subtaskProgress = computed(() => {
  if (!hasSubtasks.value || !props.task.subtasks) return 0
  return Math.round(((props.task.subtasks_complete || 0) / props.task.subtasks) * 100)
})

const assignees = computed(() => {
  if (!props.task.owner || !props.users) return []

  try {
    const ownerIds = typeof props.task.owner === 'string'
      ? JSON.parse(props.task.owner)
      : [props.task.owner]
    return props.users.filter(user => ownerIds.includes(user.id))
  } catch {
    return []
  }
})

const flowItemStyle = computed(() => {
  if (!props.task.flow_item_color) return {}
  return {
    '--flow-item-color': props.task.flow_item_color,
    backgroundColor: `${props.task.flow_item_color}20`,
    borderColor: props.task.flow_item_color
  }
})

// 方法
const truncateText = (text: string, maxLength: number): string => {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatDeadline = (date: string): string => {
  const deadline = new Date(date)
  const now = new Date()
  const diffTime = deadline.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 0) {
    return t('task.overdue', { days: Math.abs(diffDays) })
  } else if (diffDays === 0) {
    return t('task.today')
  } else if (diffDays === 1) {
    return t('task.tomorrow')
  } else if (diffDays <= 7) {
    return t('task.daysLeft', { days: diffDays })
  } else {
    return new Intl.DateTimeFormat('zh-CN', {
      month: 'short',
      day: 'numeric'
    }).format(deadline)
  }
}

const duplicateTask = () => {
  // 复制任务逻辑
  const duplicatedTask = {
    ...props.task,
    id: undefined,
    name: `${props.task.name} (${t('task.copy')})`
  }
  emit('update', duplicatedTask)
}
</script>

<style scoped>
.task-card {
  @apply relative bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4 cursor-pointer transition-all duration-200 hover:shadow-md hover:border-gray-300 dark:hover:border-gray-600;
}

.task-card.overdue {
  @apply border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-900/10;
}

.task-card.completed {
  @apply opacity-75 bg-gray-50 dark:bg-gray-900/50;
}

.task-card.high-priority {
  @apply border-l-4 border-l-red-500;
}

.task-card.has-subtasks {
  @apply border-l-4 border-l-blue-500;
}

.priority-indicator {
  @apply absolute top-2 right-2 w-2 h-2;
}

.priority-dot {
  @apply w-full h-full rounded-full;
}

.priority-indicator.urgent .priority-dot {
  @apply bg-red-600;
}

.priority-indicator.high .priority-dot {
  @apply bg-orange-500;
}

.priority-indicator.low .priority-dot {
  @apply bg-green-500;
}

.flow-status {
  @apply inline-flex items-center px-2 py-1 rounded-full text-xs font-medium mb-2 border;
  color: var(--flow-item-color, #6b7280);
}

.task-title {
  @apply font-medium text-gray-900 dark:text-gray-100 mb-2 pr-8;
  line-height: 1.4;
}

.task-title.completed {
  @apply line-through text-gray-500 dark:text-gray-400;
}

.task-description {
  @apply text-sm text-gray-600 dark:text-gray-400 mb-3;
  line-height: 1.4;
}

.subtask-progress {
  @apply mb-3 space-y-1;
}

.progress-bar {
  @apply w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden;
}

.progress-fill {
  @apply h-full bg-blue-500 rounded-full transition-all duration-300;
}

.progress-text {
  @apply text-xs text-gray-600 dark:text-gray-400;
}

.task-tags {
  @apply flex flex-wrap gap-1 mb-3;
}

.tag {
  @apply inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300;
}

.task-footer {
  @apply flex items-center justify-between text-xs text-gray-500 dark:text-gray-400;
}

.deadline {
  @apply flex items-center space-x-1;
}

.deadline.overdue {
  @apply text-red-600 dark:text-red-400;
}

.deadline.today {
  @apply text-orange-600 dark:text-orange-400;
}

.assignees {
  @apply flex items-center;
}

.attachments,
.comments {
  @apply flex items-center space-x-1;
}

.task-actions {
  @apply absolute top-2 right-2 opacity-0 transition-opacity duration-200;
}

.task-card:hover .task-actions {
  @apply opacity-100;
}

.action-trigger {
  @apply w-6 h-6 p-0;
}

.drag-handle {
  @apply absolute top-2 left-2 opacity-0 transition-opacity duration-200 text-gray-400 dark:text-gray-500;
}

.task-card:hover .drag-handle {
  @apply opacity-50;
}

.drag-handle:hover {
  @apply opacity-100;
}

/* 拖拽状态 */
.task-card.sortable-ghost {
  @apply opacity-50;
}

.task-card.sortable-chosen {
  @apply transform rotate-2 shadow-lg;
}

.task-card.sortable-drag {
  @apply transform rotate-0 shadow-xl;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .task-card {
    @apply p-3;
  }

  .task-title {
    @apply text-sm pr-6;
  }

  .task-description {
    @apply text-xs;
  }

  .task-footer {
    @apply flex-wrap gap-2;
  }

  .task-actions {
    @apply opacity-100;
  }

  .action-trigger {
    @apply w-5 h-5;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .task-card.overdue {
    @apply border-red-600 bg-red-900/20;
  }

  .task-card.completed {
    @apply bg-gray-800/50;
  }
}

/* 打印样式 */
@media print {
  .task-card {
    @apply shadow-none border border-gray-300 break-inside-avoid;
  }

  .task-actions,
  .drag-handle {
    @apply hidden;
  }
}
</style>