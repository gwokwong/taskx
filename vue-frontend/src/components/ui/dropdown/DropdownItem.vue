<template>
  <button
    @click="$emit('click')"
    :class="[
      'block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors',
      {
        'text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20': danger
      }
    ]"
    role="menuitem"
  >
    <div class="flex items-center">
      <component
        v-if="icon"
        :is="icon"
        class="mr-3 h-5 w-5"
      />
      <div class="flex-1">
        <div class="font-medium">{{ label }}</div>
        <div v-if="description" class="text-xs text-gray-500 dark:text-gray-400">
          {{ description }}
        </div>
      </div>
      <component
        v-if="rightIcon"
        :is="rightIcon"
        class="ml-3 h-4 w-4"
      />
    </div>
  </button>
</template>

<script setup lang="ts">
interface Props {
  label: string
  description?: string
  icon?: any
  rightIcon?: any
  danger?: boolean
}

defineProps<Props>()
defineEmits<{
  click: []
}>()
</script>