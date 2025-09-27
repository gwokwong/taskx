<template>
  <div
    ref="swipeableCard"
    :class="[
      'swipeable-card',
      'relative overflow-hidden transition-transform duration-200',
      {
        'swiping-left': isSwipingLeft,
        'swiping-right': isSwipingRight,
        'can-swipe': canSwipe
      }
    ]"
    :style="{ transform: `translateX(${swipeOffset}px)` }"
  >
    <!-- 左滑操作背景 -->
    <div v-if="leftActions.length > 0" class="absolute inset-y-0 right-0 flex items-center bg-red-500 text-white">
      <div class="flex items-center px-4 space-x-2">
        <component
          v-for="action in leftActions"
          :key="action.label"
          :is="action.icon"
          class="h-5 w-5"
        />
        <span class="text-sm font-medium">{{ leftActions[0].label }}</span>
      </div>
    </div>

    <!-- 右滑操作背景 -->
    <div v-if="rightActions.length > 0" class="absolute inset-y-0 left-0 flex items-center bg-green-500 text-white">
      <div class="flex items-center px-4 space-x-2">
        <component
          v-for="action in rightActions"
          :key="action.label"
          :is="action.icon"
          class="h-5 w-5"
        />
        <span class="text-sm font-medium">{{ rightActions[0].label }}</span>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="relative bg-white dark:bg-gray-800 shadow-sm rounded-lg">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { CardSwipeGesture } from '@/utils/gestures'

interface SwipeAction {
  label: string
  icon: any
  action: () => void
  color?: string
}

interface Props {
  leftActions?: SwipeAction[]
  rightActions?: SwipeAction[]
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  leftActions: () => [],
  rightActions: () => [],
  disabled: false
})

const swipeableCard = ref<HTMLElement>()
const swipeOffset = ref(0)
const isSwipingLeft = ref(false)
const isSwipingRight = ref(false)
let gestureHandler: CardSwipeGesture | null = null

const canSwipe = computed(() =>
  !props.disabled && (props.leftActions.length > 0 || props.rightActions.length > 0)
)

const resetSwipe = () => {
  swipeOffset.value = 0
  isSwipingLeft.value = false
  isSwipingRight.value = false
}

onMounted(() => {
  if (swipeableCard.value && canSwipe.value) {
    gestureHandler = new CardSwipeGesture(swipeableCard.value, {
      onSwipeLeft: () => {
        if (props.leftActions.length > 0) {
          isSwipingLeft.value = true
          swipeOffset.value = -100

          setTimeout(() => {
            props.leftActions[0].action()
            resetSwipe()
          }, 200)
        }
      },
      onSwipeRight: () => {
        if (props.rightActions.length > 0) {
          isSwipingRight.value = true
          swipeOffset.value = 100

          setTimeout(() => {
            props.rightActions[0].action()
            resetSwipe()
          }, 200)
        }
      }
    })
  }
})

onUnmounted(() => {
  gestureHandler?.destroy()
})

// 暴露重置方法
defineExpose({
  resetSwipe
})
</script>

<style scoped>
.swipeable-card {
  touch-action: pan-y;
}

.swipeable-card.can-swipe {
  cursor: grab;
}

.swipeable-card.can-swipe:active {
  cursor: grabbing;
}

.swiping-left {
  animation: swipe-left 0.2s ease-out;
}

.swiping-right {
  animation: swipe-right 0.2s ease-out;
}

@keyframes swipe-left {
  0% { transform: translateX(0); }
  50% { transform: translateX(-50px); }
  100% { transform: translateX(-100px); }
}

@keyframes swipe-right {
  0% { transform: translateX(0); }
  50% { transform: translateX(50px); }
  100% { transform: translateX(100px); }
}
</style>