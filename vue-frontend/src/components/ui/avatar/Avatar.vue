<template>
  <div
    :class="[
      'relative inline-flex items-center justify-center overflow-hidden rounded-full',
      sizeClasses[size],
      bgColor
    ]"
  >
    <img
      v-if="src"
      :src="src"
      :alt="alt"
      class="w-full h-full object-cover"
      @error="onImageError"
    />
    <span
      v-else-if="name"
      :class="[
        'font-medium text-white',
        textSizeClasses[size]
      ]"
    >
      {{ initials }}
    </span>
    <UserIcon
      v-else
      :class="[
        'text-gray-400',
        iconSizeClasses[size]
      ]"
    />

    <!-- 在线状态指示器 -->
    <div
      v-if="showStatus"
      :class="[
        'absolute bottom-0 right-0 rounded-full border-2 border-white dark:border-gray-800',
        statusSizeClasses[size],
        statusColor
      ]"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { UserIcon } from '@heroicons/vue/24/outline'

interface Props {
  src?: string
  alt?: string
  name?: string
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  status?: 'online' | 'offline' | 'away' | 'busy'
  showStatus?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  showStatus: false
})

const imageError = ref(false)

const sizeClasses = {
  xs: 'w-6 h-6',
  sm: 'w-8 h-8',
  md: 'w-10 h-10',
  lg: 'w-12 h-12',
  xl: 'w-16 h-16'
}

const textSizeClasses = {
  xs: 'text-xs',
  sm: 'text-xs',
  md: 'text-sm',
  lg: 'text-base',
  xl: 'text-lg'
}

const iconSizeClasses = {
  xs: 'w-3 h-3',
  sm: 'w-4 h-4',
  md: 'w-5 h-5',
  lg: 'w-6 h-6',
  xl: 'w-8 h-8'
}

const statusSizeClasses = {
  xs: 'w-1.5 h-1.5',
  sm: 'w-2 h-2',
  md: 'w-2.5 h-2.5',
  lg: 'w-3 h-3',
  xl: 'w-4 h-4'
}

const initials = computed(() => {
  if (!props.name) return ''
  return props.name
    .split(' ')
    .map(word => word.charAt(0))
    .join('')
    .slice(0, 2)
    .toUpperCase()
})

const bgColor = computed(() => {
  if (props.src && !imageError.value) return 'bg-transparent'
  if (props.name) {
    // 根据名字生成背景色
    const colors = [
      'bg-red-500',
      'bg-blue-500',
      'bg-green-500',
      'bg-yellow-500',
      'bg-purple-500',
      'bg-pink-500',
      'bg-indigo-500',
      'bg-teal-500'
    ]
    const index = props.name.charCodeAt(0) % colors.length
    return colors[index]
  }
  return 'bg-gray-200 dark:bg-gray-700'
})

const statusColor = computed(() => {
  switch (props.status) {
    case 'online':
      return 'bg-green-500'
    case 'away':
      return 'bg-yellow-500'
    case 'busy':
      return 'bg-red-500'
    case 'offline':
    default:
      return 'bg-gray-400'
  }
})

const onImageError = () => {
  imageError.value = true
}
</script>