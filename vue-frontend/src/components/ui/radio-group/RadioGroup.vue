<template>
  <RadioGroupRoot
    v-bind="forwarded"
    :class="cn('grid gap-2', props.class)"
  >
    <slot />
  </RadioGroupRoot>
</template>

<script setup lang="ts">
import { RadioGroupRoot, useForwardPropsEmits } from 'radix-vue'
import { cn } from '@/lib/utils'

interface Props {
  class?: string
  defaultValue?: string
  modelValue?: string
  name?: string
  required?: boolean
  disabled?: boolean
  orientation?: 'horizontal' | 'vertical'
}

const props = defineProps<Props>()

const emits = defineEmits<{
  'update:modelValue': [value: string]
}>()

const forwarded = useForwardPropsEmits(props, emits)
</script>