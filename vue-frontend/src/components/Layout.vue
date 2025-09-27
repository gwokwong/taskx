<template>
  <div class="min-h-screen flex mobile-layout">
    <!-- 移动端侧边栏遮罩 -->
    <div
      v-if="isMobile"
      :class="['sidebar-overlay', { 'active': isSidebarOpen }]"
      @click="closeSidebar"
    ></div>

    <!-- 侧边栏 -->
    <div :class="[
      'sidebar w-64 bg-white dark:bg-gray-800 shadow-lg',
      isMobile ? 'sidebar-mobile' : '',
      isMobile && isSidebarOpen ? 'active' : ''
    ]">
      <div class="flex items-center justify-between h-16 border-b border-gray-200 dark:border-gray-700 px-4">
        <h1 class="text-xl font-bold text-gray-900 dark:text-white">DooTask</h1>
        <button
          v-if="isMobile"
          @click="closeSidebar"
          class="p-2 rounded-md text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
        >
          <XMarkIcon class="h-5 w-5" />
        </button>
      </div>

      <nav class="mt-8 pb-20">
        <div class="px-4 space-y-2">
          <router-link
            v-for="item in navigation"
            :key="item.name"
            :to="item.href"
            :class="[
              item.current
                ? 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-200'
                : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700',
              'group flex items-center px-2 py-2 text-sm font-medium rounded-md nav-link'
            ]"
            @click="isMobile && closeSidebar()"
          >
            <component :is="item.icon" class="mr-3 h-5 w-5" />
            {{ item.name }}
          </router-link>
        </div>
      </nav>

      <!-- 用户信息 -->
      <div class="absolute bottom-0 w-64 p-4 border-t border-gray-200 dark:border-gray-700">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <div class="h-8 w-8 rounded-full bg-blue-500 flex items-center justify-center user-avatar">
              <span class="text-white text-sm font-medium">
                {{ authStore.user?.nickname?.charAt(0) }}
              </span>
            </div>
          </div>
          <div class="ml-3 flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-700 dark:text-gray-300 truncate">
              {{ authStore.user?.nickname }}
            </p>
          </div>
          <Button
            variant="ghost"
            size="sm"
            class="ml-2"
            @click="handleLogout"
          >
            <span class="hidden sm:inline">退出</span>
            <ArrowRightOnRectangleIcon class="h-4 w-4 sm:hidden" />
          </Button>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col overflow-hidden main-content">
      <!-- 顶部栏 -->
      <header class="bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <div class="px-4 sm:px-6 py-4">
          <div class="flex items-center justify-between">
            <!-- 移动端菜单按钮和标题 -->
            <div class="flex items-center">
              <button
                v-if="isMobile"
                @click="openSidebar"
                class="p-2 rounded-md text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 mr-3"
              >
                <Bars3Icon class="h-6 w-6" />
              </button>
              <h1 class="text-xl sm:text-2xl font-semibold text-gray-900 dark:text-white">
                {{ pageTitle }}
              </h1>
            </div>

            <!-- 桌面端搜索栏 -->
            <div v-if="!isMobile" class="flex-1 max-w-lg mx-8">
              <GlobalSearch />
            </div>

            <!-- 右侧功能区 -->
            <div class="flex items-center space-x-2 sm:space-x-4">
              <!-- 移动端搜索按钮 -->
              <button
                v-if="isMobile"
                @click="toggleMobileSearch"
                class="p-2 rounded-md text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
              >
                <MagnifyingGlassIcon class="h-5 w-5" />
              </button>

              <!-- 通知 -->
              <button class="relative p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white rounded-md">
                <BellIcon class="h-5 w-5 sm:h-6 sm:w-6" />
                <span class="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-400 ring-2 ring-white dark:ring-gray-800"></span>
              </button>

              <!-- 用户头像菜单 -->
              <Dropdown position="right">
                <template #trigger>
                  <div class="flex items-center space-x-2 cursor-pointer p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 clickable">
                    <div class="h-8 w-8 rounded-full bg-blue-500 flex items-center justify-center user-avatar">
                      <span class="text-white text-sm font-medium">
                        {{ authStore.user?.nickname?.charAt(0) }}
                      </span>
                    </div>
                    <span class="text-sm font-medium text-gray-700 dark:text-gray-300 hidden md:block">
                      {{ authStore.user?.nickname }}
                    </span>
                  </div>
                </template>

                <DropdownItem
                  label="个人资料"
                  description="查看和编辑个人信息"
                  :icon="UserIcon"
                  @click="router.push('/profile')"
                />
                <DropdownItem
                  label="设置"
                  description="偏好设置和系统配置"
                  :icon="CogIcon"
                  @click="router.push('/settings')"
                />
                <hr class="my-1">
                <DropdownItem
                  label="退出登录"
                  :icon="ArrowRightOnRectangleIcon"
                  danger
                  @click="handleLogout"
                />
              </Dropdown>
            </div>
          </div>

          <!-- 移动端搜索栏 -->
          <div v-if="isMobile && showMobileSearch" class="mt-4">
            <GlobalSearch @close="toggleMobileSearch" />
          </div>
        </div>
      </header>

      <!-- 主内容 -->
      <main :class="[
        'flex-1 overflow-auto p-4 sm:p-6 bg-gray-50 dark:bg-gray-900',
        isMobile ? 'pb-20' : ''
      ]">
        <RouterView />
      </main>

      <!-- 移动端底部导航 -->
      <BottomNavigation v-if="isMobile" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/button/Button.vue'
import GlobalSearch from '@/components/GlobalSearch.vue'
import Dropdown from '@/components/ui/dropdown/Dropdown.vue'
import DropdownItem from '@/components/ui/dropdown/DropdownItem.vue'
import BottomNavigation from '@/components/mobile/BottomNavigation.vue'
import {
  HomeIcon,
  FolderIcon,
  CalendarIcon,
  ChatBubbleLeftRightIcon,
  DocumentIcon,
  CogIcon,
  BellIcon,
  UserIcon,
  ArrowRightOnRectangleIcon,
  Bars3Icon,
  XMarkIcon,
  MagnifyingGlassIcon,
  ClipboardDocumentListIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 移动端状态管理
const isMobile = ref(false)
const isSidebarOpen = ref(false)
const showMobileSearch = ref(false)

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    isSidebarOpen.value = false
    showMobileSearch.value = false
  }
}

// 侧边栏控制
const openSidebar = () => {
  isSidebarOpen.value = true
  document.body.style.overflow = 'hidden'
}

const closeSidebar = () => {
  isSidebarOpen.value = false
  document.body.style.overflow = ''
}

// 移动端搜索控制
const toggleMobileSearch = () => {
  showMobileSearch.value = !showMobileSearch.value
}

// 生命周期
onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
  document.body.style.overflow = ''
})

const navigation = computed(() => [
  { name: '仪表盘', href: '/dashboard', icon: HomeIcon, current: route.name === 'Dashboard' },
  { name: '项目', href: '/projects', icon: FolderIcon, current: route.name === 'Projects' || route.name === 'ProjectDetail' },
  { name: '任务', href: '/tasks', icon: ClipboardDocumentListIcon, current: route.name === 'Tasks' },
  { name: '日历', href: '/calendar', icon: CalendarIcon, current: route.name === 'Calendar' },
  { name: '消息', href: '/messages', icon: ChatBubbleLeftRightIcon, current: route.name === 'Messages' },
  { name: '文件', href: '/files', icon: DocumentIcon, current: route.name === 'Files' },
  { name: '设置', href: '/settings', icon: CogIcon, current: route.name === 'Settings' },
])

const pageTitle = computed(() => {
  const currentNav = navigation.value.find(item => item.current)
  return currentNav?.name || '仪表盘'
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 导入移动端样式 */
@import '@/styles/mobile.css';

/* 组件特定样式 */
.mobile-layout {
  position: relative;
}

.sidebar-mobile {
  position: fixed !important;
  left: -100%;
  top: 0;
  height: 100vh;
  z-index: 1000;
  transition: left 0.3s ease;
}

.sidebar-mobile.active {
  left: 0;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.sidebar-overlay.active {
  opacity: 1;
  visibility: visible;
}

.main-content {
  width: 100%;
}

.nav-link {
  transition: all 0.2s ease;
}

.clickable {
  transition: all 0.2s ease;
}

.user-avatar {
  transition: transform 0.2s ease;
}

/* 移动端特定样式 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0 !important;
  }

  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.active {
    transform: translateX(0);
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .nav-link:active,
  .clickable:active {
    background-color: rgba(59, 130, 246, 0.1);
    transform: scale(0.98);
  }

  .user-avatar:active {
    transform: scale(0.95);
  }
}
</style>