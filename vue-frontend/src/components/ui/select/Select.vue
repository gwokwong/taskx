<template>
  <SelectRoot v-bind="forwarded">
    <slot />
  </SelectRoot>
</template>

<script setup lang="ts">
import { SelectRoot, useForwardPropsEmits } from 'radix-vue'

interface Props {
  defaultValue?: string
  modelValue?: string
  disabled?: boolean
  name?: string
  required?: boolean
}

const props = defineProps<Props>()

const emits = defineEmits<{
  'update:modelValue': [value: string]
}>()

const forwarded = useForwardPropsEmits(props, emits)
</script>