<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
    <div class="text-center">
      <h1 class="text-6xl font-bold text-gray-900 dark:text-white">404</h1>
      <p class="text-xl text-gray-600 dark:text-gray-400 mt-4">页面未找到</p>
      <Button class="mt-6" @click="$router.push('/')">
        返回首页
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue'
</script>