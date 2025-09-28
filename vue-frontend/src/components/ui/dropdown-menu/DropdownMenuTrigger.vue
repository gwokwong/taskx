<template>
  <DropdownMenuTrigger v-bind="forwarded">
    <slot />
  </DropdownMenuTrigger>
</template>

<script setup lang="ts">
import { DropdownMenuTrigger, useForwardPropsEmits } from 'radix-vue'

const props = defineProps<{}>()

const forwarded = useForwardPropsEmits(props, [])
</script>