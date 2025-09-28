<template>
  <td :class="cn('px-4 py-2 text-gray-900', props.class)">
    <slot />
  </td>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

interface Props {
  class?: string
}

const props = defineProps<Props>()
</script>