<template>
  <div class="avatar-group">
    <Avatar
      v-for="(user, index) in displayUsers"
      :key="user.id"
      :user="user"
      :size="size"
      :class="[
        'avatar-item',
        index > 0 && '-ml-2'
      ]"
    />
    <div
      v-if="remainingCount > 0"
      :class="[
        'remaining-count',
        sizeClasses[size],
        'bg-gray-100 border-2 border-white -ml-2'
      ]"
    >
      +{{ remainingCount }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Avatar } from '@/components/ui/avatar'

interface User {
  id: number
  nickname: string
  userImg?: string
}

interface Props {
  users: User[]
  max?: number
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
}

const props = withDefaults(defineProps<Props>(), {
  max: 3,
  size: 'md'
})

const sizeClasses = {
  xs: 'w-6 h-6 text-xs',
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-12 h-12 text-base',
  xl: 'w-16 h-16 text-lg'
}

const displayUsers = computed(() => {
  return props.users.slice(0, props.max)
})

const remainingCount = computed(() => {
  return Math.max(0, props.users.length - props.max)
})
</script>

<style scoped>
.avatar-group {
  display: flex;
  align-items: center;
}

.avatar-item {
  position: relative;
  z-index: 1;
}

.avatar-item:hover {
  z-index: 10;
}

.remaining-count {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 500;
  color: #6b7280;
  position: relative;
  z-index: 1;
}
</style>