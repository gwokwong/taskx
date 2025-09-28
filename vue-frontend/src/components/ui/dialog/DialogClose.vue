<template>
  <DialogClose v-bind="forwarded">
    <slot />
  </DialogClose>
</template>

<script setup lang="ts">
import { DialogClose } from 'radix-vue'
import { useForwardPropsEmits } from 'radix-vue'

const props = defineProps<{}>()

const forwarded = useForwardPropsEmits(props, [])
</script>