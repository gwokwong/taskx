import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import messageApi, { type Dialog, type Message, type CreateDialogRequest, type SendMessageRequest } from '@/services/messageApi'
import websocketService from '@/services/websocket'
import { useAuthStore } from './auth'

export const useMessageStore = defineStore('message', () => {
  const dialogs = ref<Dialog[]>([])
  const selectedDialog = ref<Dialog | null>(null)
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const isWebSocketConnected = ref(false)

  const authStore = useAuthStore()

  // Computed
  const filteredDialogs = computed(() => {
    return dialogs.value
  })

  const currentUserId = computed(() => {
    return authStore.user?.userid || null
  })

  // Actions
  async function loadDialogs() {
    try {
      loading.value = true
      dialogs.value = await messageApi.getUserDialogs()
    } catch (error) {
      console.error('Failed to load dialogs:', error)
    } finally {
      loading.value = false
    }
  }

  async function createDialog(data: CreateDialogRequest) {
    try {
      const newDialog = await messageApi.createDialog(data)
      dialogs.value.unshift(newDialog)
      return newDialog
    } catch (error) {
      console.error('Failed to create dialog:', error)
      throw error
    }
  }

  async function selectDialog(dialog: Dialog) {
    selectedDialog.value = dialog

    // Mark as read locally
    dialog.unreadCount = 0

    // Load messages
    await loadMessages(dialog.id)

    // Subscribe to WebSocket updates
    if (isWebSocketConnected.value) {
      websocketService.subscribeToDialog(dialog.id, onNewMessage)
      websocketService.markDialogAsRead(dialog.id)
    }

    // Mark as read on server
    try {
      await messageApi.markDialogAsRead(dialog.id)
    } catch (error) {
      console.error('Failed to mark dialog as read:', error)
    }
  }

  async function loadMessages(dialogId: number) {
    try {
      loading.value = true
      messages.value = await messageApi.getDialogMessages(dialogId)
    } catch (error) {
      console.error('Failed to load messages:', error)
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(content: string, messageType: string = 'text') {
    if (!selectedDialog.value || !content.trim()) return

    const dialogId = selectedDialog.value.id

    try {
      // Send via WebSocket for real-time
      if (isWebSocketConnected.value) {
        websocketService.sendMessage(dialogId, content, messageType)
      } else {
        // Fallback to HTTP API
        const message = await messageApi.sendMessage(dialogId, { content, messageType })
        addMessage(message)
        updateDialogLastMessage(dialogId, message)
      }
    } catch (error) {
      console.error('Failed to send message:', error)
      throw error
    }
  }

  function addMessage(message: Message) {
    messages.value.push(message)
  }

  function updateDialogLastMessage(dialogId: number, message: Message) {
    const dialog = dialogs.value.find(d => d.id === dialogId)
    if (dialog) {
      dialog.lastMsg = message.content
      dialog.lastAt = message.createdAt || message.sendAt

      // Move dialog to top
      const index = dialogs.value.indexOf(dialog)
      if (index > 0) {
        dialogs.value.splice(index, 1)
        dialogs.value.unshift(dialog)
      }
    }
  }

  function onNewMessage(message: Message) {
    // Add message to current dialog if it matches
    if (selectedDialog.value?.id === message.dialogId) {
      addMessage(message)
    }

    // Update dialog last message
    updateDialogLastMessage(message.dialogId, message)

    // Update unread count if not current dialog
    if (selectedDialog.value?.id !== message.dialogId) {
      const dialog = dialogs.value.find(d => d.id === message.dialogId)
      if (dialog) {
        dialog.unreadCount = (dialog.unreadCount || 0) + 1
      }
    }
  }

  async function connectWebSocket() {
    try {
      await websocketService.connect()
      isWebSocketConnected.value = true

      // Subscribe to current dialog if any
      if (selectedDialog.value) {
        websocketService.subscribeToDialog(selectedDialog.value.id, onNewMessage)
      }
    } catch (error) {
      console.error('Failed to connect WebSocket:', error)
      isWebSocketConnected.value = false
    }
  }

  function disconnectWebSocket() {
    websocketService.disconnect()
    isWebSocketConnected.value = false
  }

  function clearMessages() {
    messages.value = []
    selectedDialog.value = null
  }

  return {
    // State
    dialogs,
    selectedDialog,
    messages,
    loading,
    isWebSocketConnected,

    // Computed
    filteredDialogs,
    currentUserId,

    // Actions
    loadDialogs,
    createDialog,
    selectDialog,
    loadMessages,
    sendMessage,
    connectWebSocket,
    disconnectWebSocket,
    clearMessages,
    addMessage,
    onNewMessage
  }
})