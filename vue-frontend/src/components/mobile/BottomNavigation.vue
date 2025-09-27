<template>
  <nav
    :class="[
      'mobile-bottom-nav',
      'fixed bottom-0 left-0 right-0 z-40',
      'bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700',
      'safe-area-bottom'
    ]"
  >
    <div class="flex items-center justify-around h-16 px-2">
      <router-link
        v-for="item in navigationItems"
        :key="item.name"
        :to="item.href"
        :class="[
          'nav-item flex flex-col items-center justify-center',
          'py-1 px-2 rounded-lg transition-all duration-200',
          'min-w-0 flex-1 relative',
          item.current
            ? 'text-blue-600 dark:text-blue-400'
            : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'
        ]"
        @click="handleNavClick(item)"
      >
        <!-- 图标 -->
        <div class="relative">
          <component
            :is="item.icon"
            class="h-5 w-5 transition-transform duration-200"
            :class="{ 'scale-110': item.current }"
          />

          <!-- 徽章指示器 -->
          <div
            v-if="item.badge && item.badge > 0"
            class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full min-w-[16px] h-4 flex items-center justify-center px-1"
          >
            {{ item.badge > 99 ? '99+' : item.badge }}
          </div>
        </div>

        <!-- 标签 -->
        <span
          :class="[
            'text-xs mt-1 transition-all duration-200 truncate max-w-full',
            item.current ? 'font-medium' : 'font-normal'
          ]"
        >
          {{ item.name }}
        </span>

        <!-- 活动指示器 -->
        <div
          v-if="item.current"
          class="absolute top-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-blue-600 dark:bg-blue-400 rounded-full"
        ></div>
      </router-link>
    </div>

    <!-- 快捷操作菜单 -->
    <div
      v-if="showQuickActions"
      class="absolute bottom-full left-0 right-0 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 p-4"
    >
      <div class="flex items-center justify-around">
        <button
          v-for="action in quickActions"
          :key="action.label"
          :class="[
            'flex flex-col items-center justify-center',
            'p-3 rounded-lg transition-all duration-200',
            'bg-gray-50 dark:bg-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600'
          ]"
          @click="action.onClick"
        >
          <component :is="action.icon" class="h-6 w-6 text-gray-600 dark:text-gray-400 mb-1" />
          <span class="text-xs text-gray-600 dark:text-gray-400">{{ action.label }}</span>
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeIcon,
  FolderIcon,
  CalendarIcon,
  ChatBubbleLeftRightIcon,
  UserIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  BellIcon,
  CogIcon
} from '@heroicons/vue/24/outline'
import {
  HomeIcon as HomeIconSolid,
  FolderIcon as FolderIconSolid,
  CalendarIcon as CalendarIconSolid,
  ChatBubbleLeftRightIcon as ChatIconSolid,
  UserIcon as UserIconSolid
} from '@heroicons/vue/24/solid'

interface NavigationItem {
  name: string
  href: string
  icon: any
  activeIcon: any
  current: boolean
  badge?: number
}

interface QuickAction {
  label: string
  icon: any
  onClick: () => void
}

const route = useRoute()
const router = useRouter()

const showQuickActions = ref(false)

// 导航项目
const navigationItems = computed<NavigationItem[]>(() => [
  {
    name: '首页',
    href: '/dashboard',
    icon: route.name === 'Dashboard' ? HomeIconSolid : HomeIcon,
    activeIcon: HomeIconSolid,
    current: route.name === 'Dashboard'
  },
  {
    name: '项目',
    href: '/projects',
    icon: route.name === 'Projects' || route.name === 'ProjectDetail' ? FolderIconSolid : FolderIcon,
    activeIcon: FolderIconSolid,
    current: route.name === 'Projects' || route.name === 'ProjectDetail'
  },
  {
    name: '日历',
    href: '/calendar',
    icon: route.name === 'Calendar' ? CalendarIconSolid : CalendarIcon,
    activeIcon: CalendarIconSolid,
    current: route.name === 'Calendar'
  },
  {
    name: '消息',
    href: '/messages',
    icon: route.name === 'Messages' ? ChatIconSolid : ChatBubbleLeftRightIcon,
    activeIcon: ChatIconSolid,
    current: route.name === 'Messages',
    badge: 3 // 示例徽章
  },
  {
    name: '我的',
    href: '/profile',
    icon: route.name === 'Profile' ? UserIconSolid : UserIcon,
    activeIcon: UserIconSolid,
    current: route.name === 'Profile'
  }
])

// 快捷操作
const quickActions = computed<QuickAction[]>(() => [
  {
    label: '新建项目',
    icon: PlusIcon,
    onClick: () => {
      router.push('/projects/new')
      showQuickActions.value = false
    }
  },
  {
    label: '搜索',
    icon: MagnifyingGlassIcon,
    onClick: () => {
      // 触发全局搜索
      showQuickActions.value = false
    }
  },
  {
    label: '通知',
    icon: BellIcon,
    onClick: () => {
      router.push('/notifications')
      showQuickActions.value = false
    }
  },
  {
    label: '设置',
    icon: CogIcon,
    onClick: () => {
      router.push('/settings')
      showQuickActions.value = false
    }
  }
])

const handleNavClick = (item: NavigationItem) => {
  // 如果点击当前页面，显示快捷操作
  if (item.current) {
    showQuickActions.value = !showQuickActions.value
  } else {
    showQuickActions.value = false
  }
}

// 监听路由变化，自动关闭快捷操作菜单
watch(() => route.path, () => {
  showQuickActions.value = false
})
</script>

<style scoped>
.mobile-bottom-nav {
  /* 支持安全区域 */
  padding-bottom: env(safe-area-inset-bottom);
}

.safe-area-bottom {
  padding-bottom: max(env(safe-area-inset-bottom), 16px);
}

.nav-item {
  touch-action: manipulation;
  user-select: none;
}

/* 点击反馈效果 */
.nav-item:active {
  transform: scale(0.95);
  background-color: rgba(0, 0, 0, 0.05);
}

.nav-item:active.dark {
  background-color: rgba(255, 255, 255, 0.05);
}

/* 徽章动画 */
.nav-item .absolute.-top-1.-right-1 {
  animation: badge-pulse 2s infinite;
}

@keyframes badge-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* 快捷操作菜单动画 */
.mobile-bottom-nav .absolute.bottom-full {
  animation: slide-up 0.3s ease-out;
}

@keyframes slide-up {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 活动指示器动画 */
.nav-item .absolute.top-0 {
  animation: indicator-appear 0.3s ease-out;
}

@keyframes indicator-appear {
  from {
    opacity: 0;
    transform: translateX(-50%) scale(0);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) scale(1);
  }
}

/* 响应式调整 */
@media (max-width: 375px) {
  .nav-item {
    padding: 0.25rem 0.125rem;
  }

  .nav-item .text-xs {
    font-size: 0.625rem;
  }
}

/* 横屏模式优化 */
@media (orientation: landscape) and (max-height: 500px) {
  .mobile-bottom-nav {
    height: 3rem;
  }

  .nav-item {
    flex-direction: row;
    padding: 0.5rem 0.25rem;
  }

  .nav-item .text-xs {
    margin-top: 0;
    margin-left: 0.25rem;
  }

  .nav-item .h-5 {
    height: 1rem;
    width: 1rem;
  }
}
</style>