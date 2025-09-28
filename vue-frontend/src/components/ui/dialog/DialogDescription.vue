<template>
  <DialogDescription
    :class="cn('text-sm text-muted-foreground', props.class)"
    v-bind="forwarded"
  >
    <slot />
  </DialogDescription>
</template>

<script setup lang="ts">
import { DialogDescription } from 'radix-vue'
import { cn } from '@/lib/utils'
import { useForwardPropsEmits } from 'radix-vue'

interface Props {
  class?: string
}

const props = withDefaults(defineProps<Props>(), {})

const forwarded = useForwardPropsEmits(props, [])
</script>