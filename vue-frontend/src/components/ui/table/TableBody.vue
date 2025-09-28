<template>
  <tbody :class="cn('divide-y divide-gray-200', props.class)">
    <slot />
  </tbody>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

interface Props {
  class?: string
}

const props = defineProps<Props>()
</script>