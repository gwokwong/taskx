<template>
  <div class="relative inline-block text-left" ref="dropdown">
    <div>
      <button
        @click="toggleDropdown"
        :class="[
          'inline-flex items-center justify-center w-full rounded-md border border-gray-300 dark:border-gray-600 shadow-sm px-4 py-2 bg-white dark:bg-gray-800 text-sm font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500',
          triggerClass
        ]"
      >
        <slot name="trigger">
          {{ label || 'Select' }}
          <ChevronDownIcon class="ml-2 -mr-1 h-5 w-5" />
        </slot>
      </button>
    </div>

    <Transition
      enter-active-class="transition duration-100 ease-out"
      enter-from-class="transform scale-95 opacity-0"
      enter-to-class="transform scale-100 opacity-100"
      leave-active-class="transition duration-75 ease-in"
      leave-from-class="transform scale-100 opacity-100"
      leave-to-class="transform scale-95 opacity-0"
    >
      <div
        v-show="isOpen"
        :class="[
          'absolute z-50 mt-2 w-56 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 focus:outline-none',
          positionClasses[position],
          dropdownClass
        ]"
        role="menu"
      >
        <div class="py-1" role="none">
          <slot :close="closeDropdown" />
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ChevronDownIcon } from '@heroicons/vue/24/outline'

interface Props {
  label?: string
  position?: 'left' | 'right'
  triggerClass?: string
  dropdownClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  position: 'left'
})

const dropdown = ref()
const isOpen = ref(false)

const positionClasses = {
  left: 'left-0 origin-top-left',
  right: 'right-0 origin-top-right'
}

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const closeDropdown = () => {
  isOpen.value = false
}

const handleClickOutside = (event: Event) => {
  if (dropdown.value && !dropdown.value.contains(event.target as Node)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>