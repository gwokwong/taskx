<template>
  <Layout>
    <div class="max-w-4xl mx-auto p-6">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-2">设置</h1>
        <p class="text-gray-600 dark:text-gray-400">管理您的账户和应用偏好设置</p>
      </div>

      <!-- 设置导航 -->
      <div class="flex space-x-1 bg-gray-100 dark:bg-gray-800 rounded-lg p-1 mb-8">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'flex-1 px-4 py-2 text-sm font-medium rounded-md transition-colors',
            activeTab === tab.key
              ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow'
              : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
          ]"
        >
          <component :is="tab.icon" class="h-4 w-4 inline mr-2" />
          {{ tab.label }}
        </button>
      </div>

      <!-- 设置内容 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <!-- 个人设置 -->
        <div v-if="activeTab === 'profile'" class="p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">个人设置</h2>

          <div class="space-y-6">
            <!-- 主题设置 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                主题
              </label>
              <select
                v-model="userSettings.theme"
                @change="updateUserSetting('theme', userSettings.theme)"
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
              >
                <option value="light">浅色</option>
                <option value="dark">深色</option>
                <option value="auto">跟随系统</option>
              </select>
            </div>

            <!-- 语言设置 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                语言
              </label>
              <select
                v-model="userSettings.language"
                @change="updateUserSetting('language', userSettings.language)"
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
              >
                <option value="zh-CN">简体中文</option>
                <option value="en-US">English</option>
                <option value="ja-JP">日本語</option>
              </select>
            </div>

            <!-- 时区设置 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                时区
              </label>
              <select
                v-model="userSettings.timezone"
                @change="updateUserSetting('timezone', userSettings.timezone)"
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:text-white"
              >
                <option value="Asia/Shanghai">Asia/Shanghai (+08:00)</option>
                <option value="America/New_York">America/New_York (-05:00)</option>
                <option value="Europe/London">Europe/London (+00:00)</option>
                <option value="Asia/Tokyo">Asia/Tokyo (+09:00)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activeTab === 'notifications'" class="p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">通知设置</h2>

          <div class="space-y-6">
            <!-- 邮件通知 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">邮件通知</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">接收重要事件的邮件通知</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  v-model="userSettings.emailNotification"
                  @change="updateUserSetting('notification.email', userSettings.emailNotification)"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              </label>
            </div>

            <!-- 桌面通知 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">桌面通知</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">在浏览器中显示通知</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  v-model="userSettings.desktopNotification"
                  @change="updateUserSetting('notification.desktop', userSettings.desktopNotification)"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              </label>
            </div>

            <!-- 声音通知 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">声音通知</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">播放通知声音</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  v-model="userSettings.soundNotification"
                  @change="updateUserSetting('notification.sound', userSettings.soundNotification)"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-if="activeTab === 'privacy'" class="p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">隐私设置</h2>

          <div class="space-y-6">
            <!-- 公开资料 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">公开资料</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">允许其他用户查看您的资料</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  v-model="userSettings.publicProfile"
                  @change="updateUserSetting('privacy.profile.public', userSettings.publicProfile)"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              </label>
            </div>

            <!-- 活动可见性 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">活动可见性</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">显示您的在线状态和活动</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  v-model="userSettings.activityVisible"
                  @change="updateUserSetting('privacy.activity.visible', userSettings.activityVisible)"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </div>
        </div>

        <!-- 系统设置 -->
        <div v-if="activeTab === 'system'" class="p-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">系统信息</h2>

          <div v-if="systemInfo" class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300">应用版本</h3>
                <p class="text-lg font-semibold text-gray-900 dark:text-white">{{ systemInfo.version }}</p>
              </div>
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300">Java版本</h3>
                <p class="text-lg font-semibold text-gray-900 dark:text-white">{{ systemInfo.javaVersion }}</p>
              </div>
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300">操作系统</h3>
                <p class="text-lg font-semibold text-gray-900 dark:text-white">{{ systemInfo.osName }}</p>
              </div>
              <div class="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300">处理器核心</h3>
                <p class="text-lg font-semibold text-gray-900 dark:text-white">{{ systemInfo.availableProcessors }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 保存按钮 -->
      <div class="flex justify-end space-x-3 mt-8">
        <Button variant="outline" @click="resetSettings">
          重置
        </Button>
        <Button @click="saveAllSettings" :disabled="saving">
          {{ saving ? '保存中...' : '保存设置' }}
        </Button>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import Button from '@/components/ui/button/Button.vue'
import {
  UserIcon,
  BellIcon,
  ShieldCheckIcon,
  CogIcon
} from '@heroicons/vue/24/outline'

const activeTab = ref('profile')
const saving = ref(false)

const tabs = [
  { key: 'profile', label: '个人设置', icon: UserIcon },
  { key: 'notifications', label: '通知', icon: BellIcon },
  { key: 'privacy', label: '隐私', icon: ShieldCheckIcon },
  { key: 'system', label: '系统', icon: CogIcon }
]

const userSettings = reactive({
  theme: 'light',
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  emailNotification: true,
  desktopNotification: true,
  soundNotification: false,
  publicProfile: false,
  activityVisible: true
})

const systemInfo = ref({
  version: '1.0.0',
  javaVersion: '17.0.0',
  osName: 'Linux',
  availableProcessors: 8
})

const updateUserSetting = async (key: string, value: any) => {
  try {
    // TODO: 调用API更新设置
    console.log('Updating setting:', key, value)
  } catch (error) {
    console.error('Failed to update setting:', error)
  }
}

const saveAllSettings = async () => {
  try {
    saving.value = true
    // TODO: 调用API保存所有设置
    console.log('Saving all settings:', userSettings)
  } catch (error) {
    console.error('Failed to save settings:', error)
  } finally {
    saving.value = false
  }
}

const resetSettings = async () => {
  try {
    // TODO: 重置到默认设置
    Object.assign(userSettings, {
      theme: 'light',
      language: 'zh-CN',
      timezone: 'Asia/Shanghai',
      emailNotification: true,
      desktopNotification: true,
      soundNotification: false,
      publicProfile: false,
      activityVisible: true
    })
  } catch (error) {
    console.error('Failed to reset settings:', error)
  }
}

onMounted(async () => {
  // TODO: 加载用户设置和系统信息
})
</script>