<template>
  <th :class="cn('px-4 py-2 text-left font-medium text-gray-900', props.class)">
    <slot />
  </th>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

interface Props {
  class?: string
}

const props = defineProps<Props>()
</script>