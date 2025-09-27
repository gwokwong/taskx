<template>
  <Badge :variant="priorityVariant" :class="priorityClass">
    <component :is="priorityIcon" class="w-3 h-3 mr-1" />
    {{ priorityText }}
  </Badge>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  ArrowDown, Minus, ArrowUp, AlertTriangle
} from 'lucide-vue-next'
import { Badge } from '@/components/ui/badge'

interface TaskPriorityBadgeProps {
  priority?: string
}

const props = withDefaults(defineProps<TaskPriorityBadgeProps>(), {
  priority: 'medium'
})

const { t } = useI18n()

// 计算属性
const priorityInfo = computed(() => {
  switch (props.priority) {
    case 'urgent':
      return {
        variant: 'destructive',
        icon: AlertTriangle,
        class: 'bg-red-500 text-white border-red-600'
      }
    case 'high':
      return {
        variant: 'default',
        icon: ArrowUp,
        class: 'bg-orange-500 text-white border-orange-600'
      }
    case 'medium':
      return {
        variant: 'outline',
        icon: Minus,
        class: 'bg-yellow-100 text-yellow-700 border-yellow-300 dark:bg-yellow-900 dark:text-yellow-400 dark:border-yellow-700'
      }
    case 'low':
      return {
        variant: 'secondary',
        icon: ArrowDown,
        class: 'bg-green-100 text-green-700 border-green-300 dark:bg-green-900 dark:text-green-400 dark:border-green-700'
      }
    default:
      return {
        variant: 'outline',
        icon: Minus,
        class: 'bg-gray-100 text-gray-600 border-gray-300 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-600'
      }
  }
})

const priorityText = computed(() => {
  return t(`task.priority.${props.priority || 'medium'}`)
})

const priorityVariant = computed(() => {
  return priorityInfo.value.variant
})

const priorityClass = computed(() => {
  return priorityInfo.value.class
})

const priorityIcon = computed(() => {
  return priorityInfo.value.icon
})
</script>

<style scoped>
/* 优先级徽章样式已通过动态类名应用 */
</style>