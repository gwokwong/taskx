<template>
  <DialogTitle
    :class="cn('text-lg font-semibold leading-none tracking-tight', props.class)"
    v-bind="forwarded"
  >
    <slot />
  </DialogTitle>
</template>

<script setup lang="ts">
import { DialogTitle } from 'radix-vue'
import { cn } from '@/lib/utils'
import { useForwardPropsEmits } from 'radix-vue'

interface Props {
  class?: string
}

const props = withDefaults(defineProps<Props>(), {})

const forwarded = useForwardPropsEmits(props, [])
</script>