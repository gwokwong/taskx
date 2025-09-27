<template>
  <div
    ref="pullRefreshContainer"
    :class="[
      'pull-refresh-container',
      'relative overflow-hidden',
      {
        'pulling': isPulling,
        'refreshing': isRefreshing
      }
    ]"
  >
    <!-- 下拉刷新指示器 -->
    <div
      :class="[
        'pull-refresh-indicator',
        'absolute top-0 left-0 right-0 flex items-center justify-center',
        'bg-gray-50 dark:bg-gray-800 text-gray-600 dark:text-gray-400',
        'transition-transform duration-200 ease-out'
      ]"
      :style="{
        height: `${indicatorHeight}px`,
        transform: `translateY(${pullDistance - indicatorHeight}px)`
      }"
    >
      <div class="flex items-center space-x-2">
        <!-- 加载图标 -->
        <div
          v-if="isRefreshing"
          class="animate-spin rounded-full h-5 w-5 border-2 border-blue-500 border-t-transparent"
        ></div>

        <!-- 箭头图标 -->
        <ChevronDownIcon
          v-else
          :class="[
            'h-5 w-5 transition-transform duration-200',
            { 'rotate-180': shouldRefresh }
          ]"
        />

        <span class="text-sm font-medium">
          {{ refreshText }}
        </span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div
      :style="{ transform: `translateY(${pullDistance}px)` }"
      class="transition-transform duration-200 ease-out"
    >
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ChevronDownIcon } from '@heroicons/vue/24/outline'

interface Props {
  disabled?: boolean
  threshold?: number
  onRefresh?: () => Promise<void>
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  threshold: 60
})

const emit = defineEmits<{
  refresh: []
}>()

const pullRefreshContainer = ref<HTMLElement>()
const pullDistance = ref(0)
const isPulling = ref(false)
const isRefreshing = ref(false)
const indicatorHeight = 50

let startY = 0
let currentY = 0
let isAtTop = true

const shouldRefresh = computed(() => pullDistance.value >= props.threshold)

const refreshText = computed(() => {
  if (isRefreshing.value) return '正在刷新...'
  if (shouldRefresh.value) return '释放即可刷新'
  return '下拉刷新'
})

const checkIfAtTop = () => {
  if (!pullRefreshContainer.value) return false

  const scrollTop = pullRefreshContainer.value.scrollTop ||
                   document.documentElement.scrollTop ||
                   document.body.scrollTop

  return scrollTop <= 0
}

const handleTouchStart = (e: TouchEvent) => {
  if (props.disabled || isRefreshing.value) return

  isAtTop = checkIfAtTop()
  if (!isAtTop) return

  startY = e.touches[0].clientY
  isPulling.value = false
}

const handleTouchMove = (e: TouchEvent) => {
  if (props.disabled || isRefreshing.value || !isAtTop) return

  currentY = e.touches[0].clientY
  const deltaY = currentY - startY

  if (deltaY > 0) {
    // 下拉
    isPulling.value = true

    // 使用阻尼效果，拉得越远阻力越大
    const damping = Math.max(0.3, 1 - deltaY / 300)
    pullDistance.value = Math.min(deltaY * damping, props.threshold * 1.5)

    // 防止页面滚动
    e.preventDefault()
  }
}

const handleTouchEnd = async () => {
  if (props.disabled || isRefreshing.value || !isPulling.value) return

  isPulling.value = false

  if (shouldRefresh.value) {
    isRefreshing.value = true
    pullDistance.value = indicatorHeight

    try {
      if (props.onRefresh) {
        await props.onRefresh()
      } else {
        emit('refresh')
        // 默认延迟，模拟刷新
        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    } catch (error) {
      console.error('刷新失败:', error)
    } finally {
      isRefreshing.value = false
      pullDistance.value = 0
    }
  } else {
    pullDistance.value = 0
  }
}

// 监听滚动事件
const handleScroll = () => {
  isAtTop = checkIfAtTop()
}

onMounted(() => {
  if (!pullRefreshContainer.value) return

  const container = pullRefreshContainer.value

  container.addEventListener('touchstart', handleTouchStart, { passive: false })
  container.addEventListener('touchmove', handleTouchMove, { passive: false })
  container.addEventListener('touchend', handleTouchEnd, { passive: false })
  container.addEventListener('scroll', handleScroll, { passive: true })

  // 也监听 window 的滚动事件
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  if (!pullRefreshContainer.value) return

  const container = pullRefreshContainer.value

  container.removeEventListener('touchstart', handleTouchStart)
  container.removeEventListener('touchmove', handleTouchMove)
  container.removeEventListener('touchend', handleTouchEnd)
  container.removeEventListener('scroll', handleScroll)

  window.removeEventListener('scroll', handleScroll)
})

// 暴露刷新方法
defineExpose({
  refresh: async () => {
    if (isRefreshing.value) return

    isRefreshing.value = true
    pullDistance.value = indicatorHeight

    try {
      if (props.onRefresh) {
        await props.onRefresh()
      } else {
        emit('refresh')
        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    } finally {
      isRefreshing.value = false
      pullDistance.value = 0
    }
  }
})
</script>

<style scoped>
.pull-refresh-container {
  touch-action: pan-y;
}

.pulling {
  user-select: none;
}

.refreshing {
  pointer-events: none;
}

.pull-refresh-indicator {
  will-change: transform;
}
</style>