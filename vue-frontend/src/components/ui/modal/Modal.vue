<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 overflow-y-auto"
    @click="closeModal"
  >
    <div class="min-h-screen px-4 text-center">
      <div
        class="fixed inset-0 bg-black bg-opacity-25 transition-opacity"
        aria-hidden="true"
      />

      <span
        class="inline-block h-screen align-middle"
        aria-hidden="true"
      >&#8203;</span>

      <div
        class="inline-block w-full max-w-md p-6 my-8 overflow-hidden text-left align-middle transition-all transform bg-white shadow-xl rounded-2xl"
        @click.stop
      >
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-medium leading-6 text-gray-900">
            {{ title }}
          </h3>
          <button
            @click="closeModal"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <XMarkIcon class="h-5 w-5" />
          </button>
        </div>

        <div class="mb-4">
          <slot />
        </div>

        <div class="flex justify-end space-x-3" v-if="$slots.footer">
          <slot name="footer" />
        </div>
        <div class="flex justify-end space-x-3" v-else>
          <Button variant="outline" @click="closeModal">
            取消
          </Button>
          <Button @click="$emit('confirm')">
            确定
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/24/outline'
import Button from '@/components/ui/button/Button.vue'

interface Props {
  open: boolean
  title: string
}

defineProps<Props>()

const emit = defineEmits<{
  'update:open': [open: boolean]
  'close': []
  'confirm': []
}>()

const closeModal = () => {
  emit('update:open', false)
  emit('close')
}
</script>