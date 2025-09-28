<template>
  <table :class="cn('w-full border-collapse border border-gray-200', props.class)">
    <slot />
  </table>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

interface Props {
  class?: string
}

const props = defineProps<Props>()
</script>