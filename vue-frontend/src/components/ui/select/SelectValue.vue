<template>
  <SelectValue v-bind="forwarded">
    <slot />
  </SelectValue>
</template>

<script setup lang="ts">
import { SelectValue, useForwardPropsEmits } from 'radix-vue'

interface Props {
  placeholder?: string
}

const props = defineProps<Props>()

const forwarded = useForwardPropsEmits(props, [])
</script>