<template>
  <Layout>
    <div class="h-full flex">
      <!-- 对话列表 -->
      <div class="w-80 border-r border-gray-200 dark:border-gray-700 flex flex-col">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">消息</h2>
            <Button size="sm" @click="showCreateDialog = true">
              <PlusIcon class="h-4 w-4" />
            </Button>
          </div>
          <Input
            v-model="searchQuery"
            placeholder="搜索对话..."
            class="w-full"
          />
        </div>

        <div class="flex-1 overflow-y-auto">
          <div
            v-for="dialog in filteredDialogs"
            :key="dialog.id"
            @click="selectDialog(dialog)"
            :class="[
              'p-4 border-b border-gray-100 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors',
              selectedDialog?.id === dialog.id ? 'bg-blue-50 dark:bg-blue-900/20' : ''
            ]"
          >
            <div class="flex items-center space-x-3">
              <div class="relative">
                <div v-if="dialog.type === 'group'" class="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
                  <UsersIcon class="h-5 w-5 text-white" />
                </div>
                <div v-else class="w-10 h-10 bg-gray-500 rounded-full flex items-center justify-center">
                  <span class="text-white text-sm font-medium">
                    {{ dialog.name?.charAt(0) }}
                  </span>
                </div>
                <div v-if="dialog.unreadCount > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                  <span class="text-white text-xs">{{ dialog.unreadCount > 99 ? '99+' : dialog.unreadCount }}</span>
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white truncate">
                    {{ dialog.name }}
                  </h3>
                  <span class="text-xs text-gray-500 dark:text-gray-400">
                    {{ formatTime(dialog.lastAt) }}
                  </span>
                </div>
                <p class="text-sm text-gray-500 dark:text-gray-400 truncate">
                  {{ dialog.lastMsg || '暂无消息' }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="flex-1 flex flex-col">
        <div v-if="selectedDialog" class="flex-1 flex flex-col">
          <!-- 聊天头部 -->
          <div class="p-4 border-b border-gray-200 dark:border-gray-700">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div v-if="selectedDialog.type === 'group'" class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                  <UsersIcon class="h-4 w-4 text-white" />
                </div>
                <div v-else class="w-8 h-8 bg-gray-500 rounded-full flex items-center justify-center">
                  <span class="text-white text-sm font-medium">
                    {{ selectedDialog.name?.charAt(0) }}
                  </span>
                </div>
                <div>
                  <div class="flex items-center space-x-2">
                    <h3 class="font-medium text-gray-900 dark:text-white">{{ selectedDialog.name }}</h3>
                    <div class="flex items-center space-x-1">
                      <div :class="[
                        'w-2 h-2 rounded-full',
                        isWebSocketConnected ? 'bg-green-500' : 'bg-red-500'
                      ]"></div>
                      <span class="text-xs text-gray-500 dark:text-gray-400">
                        {{ isWebSocketConnected ? '在线' : '离线' }}
                      </span>
                    </div>
                  </div>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    {{ selectedDialog.type === 'group' ? '群聊' : '私聊' }}
                  </p>
                </div>
              </div>
              <div class="flex space-x-2">
                <Button variant="ghost" size="sm">
                  <PhoneIcon class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="sm">
                  <VideoCameraIcon class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="sm">
                  <EllipsisHorizontalIcon class="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4">
            <div v-if="loading" class="flex items-center justify-center py-8">
              <div class="text-gray-500 dark:text-gray-400">加载中...</div>
            </div>
            <div
              v-for="message in messages"
              :key="message.id"
              :class="[
                'flex',
                message.userId === currentUserId ? 'justify-end' : 'justify-start'
              ]"
            >
              <div :class="[
                'max-w-xs lg:max-w-md px-4 py-2 rounded-lg',
                message.userId === currentUserId
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-white'
              ]">
                <div v-if="message.userId !== currentUserId" class="text-xs text-gray-500 dark:text-gray-400 mb-1">
                  {{ message.userName }}
                </div>
                <div class="text-sm">{{ message.content }}</div>
                <div :class="[
                  'text-xs mt-1',
                  message.userId === currentUserId ? 'text-blue-100' : 'text-gray-500 dark:text-gray-400'
                ]">
                  {{ formatTime(message.sendAt || message.createdAt) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 消息输入 -->
          <div class="p-4 border-t border-gray-200 dark:border-gray-700">
            <div class="flex space-x-3">
              <Input
                v-model="newMessage"
                placeholder="输入消息..."
                class="flex-1"
                @keyup.enter="sendMessage"
              />
              <Button @click="sendMessage" :disabled="!newMessage.trim()">
                <PaperAirplaneIcon class="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="flex-1 flex items-center justify-center">
          <div class="text-center">
            <ChatBubbleLeftRightIcon class="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">选择一个对话</h3>
            <p class="text-gray-500 dark:text-gray-400">从左侧选择一个对话开始聊天</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建对话模态框 -->
    <Modal v-model:open="showCreateDialog" title="创建对话">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            对话名称
          </label>
          <Input
            v-model="newDialogName"
            placeholder="输入对话名称"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            对话类型
          </label>
          <select
            v-model="newDialogType"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
          >
            <option value="user">私聊</option>
            <option value="group">群聊</option>
          </select>
        </div>
      </div>
      <template #footer>
        <Button variant="outline" @click="showCreateDialog = false">
          取消
        </Button>
        <Button @click="createDialog" :disabled="!newDialogName">
          创建
        </Button>
      </template>
    </Modal>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useMessageStore } from '@/stores/message'
import { useAuthStore } from '@/stores/auth'
import Layout from '@/components/Layout.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Modal from '@/components/ui/modal/Modal.vue'
import {
  PlusIcon,
  UsersIcon,
  PhoneIcon,
  VideoCameraIcon,
  EllipsisHorizontalIcon,
  PaperAirplaneIcon,
  ChatBubbleLeftRightIcon,
  WifiIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

const messageStore = useMessageStore()
const authStore = useAuthStore()

const {
  dialogs,
  selectedDialog,
  messages,
  loading,
  isWebSocketConnected,
  currentUserId
} = storeToRefs(messageStore)

const searchQuery = ref('')
const newMessage = ref('')
const showCreateDialog = ref(false)
const newDialogName = ref('')
const newDialogType = ref('user')
const messagesContainer = ref()

const filteredDialogs = computed(() => {
  if (!searchQuery.value) return dialogs.value
  return dialogs.value.filter(dialog =>
    dialog.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const selectDialog = async (dialog) => {
  await messageStore.selectDialog(dialog)
  nextTick(() => {
    scrollToBottom()
  })
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedDialog.value) return

  try {
    await messageStore.sendMessage(newMessage.value.trim())
    newMessage.value = ''

    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('Failed to send message:', error)
    // Show error message to user
  }
}

const createDialog = async () => {
  if (!newDialogName.value) return

  try {
    await messageStore.createDialog({
      name: newDialogName.value,
      type: newDialogType.value as 'user' | 'group'
    })

    newDialogName.value = ''
    showCreateDialog.value = false
  } catch (error) {
    console.error('Failed to create dialog:', error)
    // Show error message to user
  }
}

const formatTime = (timeString) => {
  if (!timeString) return ''

  const date = new Date(timeString)
  const now = new Date()
  const diffInMinutes = Math.floor((now - date) / (1000 * 60))

  if (diffInMinutes < 1) return '刚刚'
  if (diffInMinutes < 60) return `${diffInMinutes}分钟前`
  if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}小时前`
  if (diffInMinutes < 10080) return `${Math.floor(diffInMinutes / 1440)}天前`

  return date.toLocaleDateString('zh-CN')
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Watch for new messages and auto scroll
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

onMounted(async () => {
  // Load dialogs
  await messageStore.loadDialogs()

  // Connect WebSocket
  await messageStore.connectWebSocket()

  // Auto select first dialog
  if (dialogs.value.length > 0) {
    await selectDialog(dialogs.value[0])
  }
})

onUnmounted(() => {
  // Disconnect WebSocket when component is destroyed
  messageStore.disconnectWebSocket()
})
</script>