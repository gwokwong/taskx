<template>
  <div class="w-full">
    <!-- Tab Navigation -->
    <div class="border-b border-gray-200 dark:border-gray-700">
      <nav class="-mb-px flex space-x-8">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="activeTab = tab.value"
          :class="[
            'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors',
            activeTab === tab.value
              ? 'border-blue-500 text-blue-600 dark:text-blue-400'
              : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300'
          ]"
        >
          <component
            v-if="tab.icon"
            :is="tab.icon"
            class="mr-2 h-5 w-5 inline"
          />
          {{ tab.label }}
          <span
            v-if="tab.badge"
            class="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200"
          >
            {{ tab.badge }}
          </span>
        </button>
      </nav>
    </div>

    <!-- Tab Content -->
    <div class="py-4">
      <slot :activeTab="activeTab" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

interface Tab {
  label: string
  value: string
  icon?: any
  badge?: string | number
}

interface Props {
  tabs: Tab[]
  defaultTab?: string
  modelValue?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: string]
  'tab-change': [value: string]
}>()

const activeTab = ref(props.defaultTab || props.tabs[0]?.value || '')

watch(activeTab, (newValue) => {
  emit('update:modelValue', newValue)
  emit('tab-change', newValue)
})

watch(() => props.modelValue, (newValue) => {
  if (newValue !== undefined) {
    activeTab.value = newValue
  }
})

onMounted(() => {
  if (props.modelValue) {
    activeTab.value = props.modelValue
  }
})
</script>