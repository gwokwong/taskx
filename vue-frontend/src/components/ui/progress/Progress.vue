<template>
  <div class="w-full">
    <div class="flex items-center justify-between mb-2">
      <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
        {{ label }}
      </span>
      <span class="text-sm text-gray-500">
        {{ percent }}%
      </span>
    </div>
    <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
      <div
        class="h-2 rounded-full transition-all duration-300 ease-in-out"
        :class="getColorClass()"
        :style="{ width: `${percent}%` }"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  percent: number
  label?: string
  color?: 'blue' | 'green' | 'yellow' | 'red' | 'purple'
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  color: 'blue',
  size: 'md'
})

const getColorClass = () => {
  const colors = {
    blue: 'bg-blue-600',
    green: 'bg-green-600',
    yellow: 'bg-yellow-600',
    red: 'bg-red-600',
    purple: 'bg-purple-600'
  }
  return colors[props.color]
}
</script>