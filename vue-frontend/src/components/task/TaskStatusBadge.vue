<template>
  <Badge :variant="statusVariant" :class="statusClass">
    <component :is="statusIcon" class="w-3 h-3 mr-1" />
    {{ statusText }}
  </Badge>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Clock, PlayCircle, Pause, CheckCircle, Archive, AlertTriangle
} from 'lucide-vue-next'
import { Badge } from '@/components/ui/badge'

interface TaskStatusBadgeProps {
  task: {
    complete_at?: string
    archived_at?: string
    end_at?: string
    flow_item_status?: string
  }
}

const props = defineProps<TaskStatusBadgeProps>()
const { t } = useI18n()

// 计算属性
const statusInfo = computed(() => {
  const { task } = props

  // 已归档
  if (task.archived_at) {
    return {
      key: 'archived',
      variant: 'secondary',
      icon: Archive,
      class: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400'
    }
  }

  // 已完成
  if (task.complete_at) {
    return {
      key: 'completed',
      variant: 'default',
      icon: CheckCircle,
      class: 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-400'
    }
  }

  // 检查是否逾期
  const isOverdue = task.end_at && new Date(task.end_at) < new Date()
  if (isOverdue) {
    return {
      key: 'overdue',
      variant: 'destructive',
      icon: AlertTriangle,
      class: 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-400'
    }
  }

  // 根据工作流状态判断
  switch (task.flow_item_status) {
    case 'progress':
      return {
        key: 'inProgress',
        variant: 'default',
        icon: PlayCircle,
        class: 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-400'
      }
    case 'review':
      return {
        key: 'review',
        variant: 'secondary',
        icon: Pause,
        class: 'bg-orange-100 text-orange-700 dark:bg-orange-900 dark:text-orange-400'
      }
    case 'testing':
      return {
        key: 'testing',
        variant: 'outline',
        icon: PlayCircle,
        class: 'bg-purple-100 text-purple-700 dark:bg-purple-900 dark:text-purple-400'
      }
    default:
      return {
        key: 'pending',
        variant: 'outline',
        icon: Clock,
        class: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400'
      }
  }
})

const statusText = computed(() => {
  return t(`task.status.${statusInfo.value.key}`)
})

const statusVariant = computed(() => {
  return statusInfo.value.variant
})

const statusClass = computed(() => {
  return statusInfo.value.class
})

const statusIcon = computed(() => {
  return statusInfo.value.icon
})
</script>

<style scoped>
/* 状态徽章样式已通过动态类名应用 */
</style>