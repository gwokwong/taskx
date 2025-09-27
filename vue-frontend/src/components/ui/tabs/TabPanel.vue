<template>
  <div
    v-show="value === activeTab"
    class="focus:outline-none"
    role="tabpanel"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
interface Props {
  value: string
  activeTab: string
}

defineProps<Props>()
</script>