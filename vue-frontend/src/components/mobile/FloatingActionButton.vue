<template>
  <div
    ref="floatingButton"
    :class="[
      'floating-action-button',
      'fixed z-50 flex items-center justify-center',
      'bg-blue-600 hover:bg-blue-700 text-white',
      'rounded-full shadow-lg transition-all duration-300',
      'border-2 border-white dark:border-gray-800',
      {
        'expanded': isExpanded,
        'dragging': isDragging
      }
    ]"
    :style="{
      bottom: `${position.y}px`,
      right: `${position.x}px`,
      width: `${size}px`,
      height: `${size}px`,
      transform: isDragging ? 'scale(1.1)' : 'scale(1)'
    }"
    @click="handleClick"
  >
    <!-- 主按钮图标 -->
    <component
      :is="mainIcon"
      :class="[
        'transition-transform duration-300',
        isExpanded ? 'rotate-45' : 'rotate-0'
      ]"
      :style="{ width: `${iconSize}px`, height: `${iconSize}px` }"
    />

    <!-- 子操作按钮 -->
    <transition-group
      name="fab-actions"
      tag="div"
      class="absolute"
    >
      <button
        v-for="(action, index) in actions"
        v-show="isExpanded"
        :key="action.label"
        :class="[
          'absolute flex items-center justify-center',
          'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300',
          'rounded-full shadow-md hover:shadow-lg',
          'transition-all duration-200',
          'border border-gray-200 dark:border-gray-600'
        ]"
        :style="{
          width: `${actionSize}px`,
          height: `${actionSize}px`,
          bottom: `${getActionPosition(index).y}px`,
          right: `${getActionPosition(index).x}px`
        }"
        @click.stop="action.onClick"
      >
        <component
          :is="action.icon"
          :style="{ width: `${actionIconSize}px`, height: `${actionIconSize}px` }"
        />

        <!-- 标签 -->
        <div
          v-if="action.label"
          class="absolute right-full mr-3 px-2 py-1 bg-gray-900 text-white text-xs rounded whitespace-nowrap opacity-90"
        >
          {{ action.label }}
        </div>
      </button>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { PlusIcon } from '@heroicons/vue/24/outline'

interface FloatingAction {
  label: string
  icon: any
  onClick: () => void
}

interface Props {
  actions?: FloatingAction[]
  mainIcon?: any
  size?: number
  actionSize?: number
  initialPosition?: { x: number; y: number }
  draggable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  actions: () => [],
  mainIcon: PlusIcon,
  size: 56,
  actionSize: 40,
  initialPosition: () => ({ x: 20, y: 80 }),
  draggable: true
})

const emit = defineEmits<{
  click: []
  expand: []
  collapse: []
}>()

const floatingButton = ref<HTMLElement>()
const isExpanded = ref(false)
const isDragging = ref(false)
const position = ref({ ...props.initialPosition })

// 计算图标大小
const iconSize = computed(() => Math.round(props.size * 0.5))
const actionIconSize = computed(() => Math.round(props.actionSize * 0.5))

// 拖拽相关
let startPos = { x: 0, y: 0 }
let startMousePos = { x: 0, y: 0 }

const getActionPosition = (index: number) => {
  const angle = (index * 45) - 90 // 从顶部开始，每个按钮间隔45度
  const radius = 70
  const x = Math.cos(angle * Math.PI / 180) * radius
  const y = Math.sin(angle * Math.PI / 180) * radius

  return {
    x: -x,
    y: -y
  }
}

const handleClick = () => {
  if (props.actions.length > 0) {
    isExpanded.value = !isExpanded.value
    emit(isExpanded.value ? 'expand' : 'collapse')
  } else {
    emit('click')
  }
}

// 点击外部区域关闭展开状态
const handleClickOutside = (e: Event) => {
  if (isExpanded.value && floatingButton.value && !floatingButton.value.contains(e.target as Node)) {
    isExpanded.value = false
    emit('collapse')
  }
}

// 拖拽功能
const handleMouseDown = (e: MouseEvent) => {
  if (!props.draggable) return

  isDragging.value = true
  startPos = { ...position.value }
  startMousePos = { x: e.clientX, y: e.clientY }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
  e.preventDefault()
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return

  const deltaX = startMousePos.x - e.clientX
  const deltaY = e.clientY - startMousePos.y

  // 限制在屏幕范围内
  const maxX = window.innerWidth - props.size - 20
  const maxY = window.innerHeight - props.size - 20

  position.value = {
    x: Math.max(20, Math.min(maxX, startPos.x + deltaX)),
    y: Math.max(20, Math.min(maxY, startPos.y + deltaY))
  }
}

const handleMouseUp = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)

  // 磁性吸附到边缘
  const centerX = position.value.x + props.size / 2
  const screenCenterX = window.innerWidth / 2

  if (centerX < screenCenterX) {
    // 吸附到左边
    position.value.x = 20
  } else {
    // 吸附到右边
    position.value.x = window.innerWidth - props.size - 20
  }
}

// 触摸事件处理
const handleTouchStart = (e: TouchEvent) => {
  if (!props.draggable) return

  isDragging.value = true
  startPos = { ...position.value }
  const touch = e.touches[0]
  startMousePos = { x: touch.clientX, y: touch.clientY }

  document.addEventListener('touchmove', handleTouchMove, { passive: false })
  document.addEventListener('touchend', handleTouchEnd)
  e.preventDefault()
}

const handleTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return

  const touch = e.touches[0]
  const deltaX = startMousePos.x - touch.clientX
  const deltaY = touch.clientY - startMousePos.y

  const maxX = window.innerWidth - props.size - 20
  const maxY = window.innerHeight - props.size - 20

  position.value = {
    x: Math.max(20, Math.min(maxX, startPos.x + deltaX)),
    y: Math.max(20, Math.min(maxY, startPos.y + deltaY))
  }

  e.preventDefault()
}

const handleTouchEnd = () => {
  isDragging.value = false
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)

  // 磁性吸附
  const centerX = position.value.x + props.size / 2
  const screenCenterX = window.innerWidth / 2

  if (centerX < screenCenterX) {
    position.value.x = 20
  } else {
    position.value.x = window.innerWidth - props.size - 20
  }
}

onMounted(() => {
  if (!floatingButton.value) return

  // 添加事件监听器
  document.addEventListener('click', handleClickOutside)

  if (props.draggable) {
    floatingButton.value.addEventListener('mousedown', handleMouseDown)
    floatingButton.value.addEventListener('touchstart', handleTouchStart, { passive: false })
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
})
</script>

<style scoped>
.floating-action-button {
  user-select: none;
  touch-action: none;
  cursor: pointer;
}

.floating-action-button.dragging {
  cursor: grabbing;
}

.floating-action-button.expanded {
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3);
}

/* 子按钮动画 */
.fab-actions-enter-active,
.fab-actions-leave-active {
  transition: all 0.3s ease;
}

.fab-actions-enter-from {
  opacity: 0;
  transform: scale(0) rotate(-180deg);
}

.fab-actions-leave-to {
  opacity: 0;
  transform: scale(0) rotate(180deg);
}

.fab-actions-enter-to,
.fab-actions-leave-from {
  opacity: 1;
  transform: scale(1) rotate(0deg);
}

/* 在移动设备上隐藏标签 */
@media (max-width: 768px) {
  .fab-actions-enter-active .absolute.right-full {
    display: none;
  }
}
</style>