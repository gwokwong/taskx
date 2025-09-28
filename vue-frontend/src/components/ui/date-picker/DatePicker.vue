<template>
  <input
    :type="type === 'datetime' ? 'datetime-local' : 'date'"
    :value="formattedValue"
    :placeholder="placeholder"
    :class="cn(
      'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
      props.class
    )"
    @input="handleInput"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/lib/utils'

interface Props {
  modelValue?: string | null
  placeholder?: string
  format?: string
  type?: 'date' | 'datetime'
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'date'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const formattedValue = computed(() => {
  if (!props.modelValue) return ''

  if (props.type === 'datetime') {
    // Convert to datetime-local format (YYYY-MM-DDTHH:mm)
    const date = new Date(props.modelValue)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day}T${hours}:${minutes}`
  } else {
    // Convert to date format (YYYY-MM-DD)
    const date = new Date(props.modelValue)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value

  if (!value) {
    emit('update:modelValue', null)
    return
  }

  // Convert back to ISO string
  const date = new Date(value)
  emit('update:modelValue', date.toISOString())
}
</script>