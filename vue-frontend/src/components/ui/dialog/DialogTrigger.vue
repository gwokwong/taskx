<template>
  <DialogTrigger v-bind="forwarded">
    <slot />
  </DialogTrigger>
</template>

<script setup lang="ts">
import { DialogTrigger } from 'radix-vue'
import { useForwardPropsEmits } from 'radix-vue'

const props = defineProps<{}>()

const forwarded = useForwardPropsEmits(props, [])
</script>