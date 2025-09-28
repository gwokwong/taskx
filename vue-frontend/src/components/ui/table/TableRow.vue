<template>
  <tr :class="cn('hover:bg-gray-50', props.class)">
    <slot />
  </tr>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'

interface Props {
  class?: string
}

const props = defineProps<Props>()
</script>