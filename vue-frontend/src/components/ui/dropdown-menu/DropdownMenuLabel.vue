<template>
  <DropdownMenuLabel
    v-bind="forwarded"
    :class="cn('px-2 py-1.5 text-sm font-semibold', inset && 'pl-8', props.class)"
  >
    <slot />
  </DropdownMenuLabel>
</template>

<script setup lang="ts">
import { DropdownMenuLabel, useForwardPropsEmits } from 'radix-vue'
import { cn } from '@/lib/utils'

interface Props {
  class?: string
  inset?: boolean
}

const props = defineProps<Props>()

const forwarded = useForwardPropsEmits(props, [])
</script>