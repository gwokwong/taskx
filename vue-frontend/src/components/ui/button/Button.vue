<template>
  <Primitive
    :as="as"
    :as-child="asChild"
    :class="cn(buttonVariants({ variant, size }), props.class)"
    v-bind="$attrs"
  >
    <slot />
  </Primitive>
</template>

<script setup lang="ts">
import { Primitive } from 'radix-vue'
import type { PrimitiveProps } from 'radix-vue'
import { computed } from 'vue'
import { buttonVariants, type ButtonProps } from '.'
import { cn } from '@/lib/utils'

interface Props extends PrimitiveProps, ButtonProps {}

const props = withDefaults(defineProps<Props>(), {
  as: 'button',
  variant: 'default',
  size: 'default'
})
</script>