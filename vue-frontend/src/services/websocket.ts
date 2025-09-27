import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '@/stores/auth'

export interface Message {
  id: number
  dialogId: number
  userId: number
  content: string
  messageType: string
  createdAt: string
  updatedAt: string
}

export interface MessageEvent {
  dialogId: number
  message: Message
}

class WebSocketService {
  private client: Client | null = null
  private isConnected = false
  private subscriptions = new Map<string, any>()
  private messageHandlers = new Map<string, ((message: Message) => void)[]>()

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      const authStore = useAuthStore()

      if (!authStore.token) {
        reject(new Error('No authentication token'))
        return
      }

      this.client = new Client({
        webSocketFactory: () => new SockJS(import.meta.env.VITE_API_BASE_URL?.replace('/api', '/ws') || 'http://localhost:8080/ws'),
        connectHeaders: {
          Authorization: `Bearer ${authStore.token}`
        },
        debug: (str) => {
          console.log('WebSocket Debug:', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket Connected')
          this.isConnected = true
          resolve()
        },
        onStompError: (frame) => {
          console.error('WebSocket Error:', frame.headers['message'])
          this.isConnected = false
          reject(new Error(frame.headers['message']))
        },
        onWebSocketClose: () => {
          console.log('WebSocket Disconnected')
          this.isConnected = false
        }
      })

      this.client.activate()
    })
  }

  disconnect(): void {
    if (this.client) {
      this.subscriptions.clear()
      this.messageHandlers.clear()
      this.client.deactivate()
      this.isConnected = false
    }
  }

  subscribeToDialog(dialogId: number, onMessage: (message: Message) => void): void {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected')
      return
    }

    const destination = `/topic/dialog/${dialogId}`

    // Add message handler
    if (!this.messageHandlers.has(destination)) {
      this.messageHandlers.set(destination, [])
    }
    this.messageHandlers.get(destination)!.push(onMessage)

    // Subscribe if not already subscribed
    if (!this.subscriptions.has(destination)) {
      const subscription = this.client.subscribe(destination, (message) => {
        const messageData = JSON.parse(message.body) as Message

        // Call all handlers for this destination
        const handlers = this.messageHandlers.get(destination) || []
        handlers.forEach(handler => handler(messageData))
      })

      this.subscriptions.set(destination, subscription)
    }
  }

  unsubscribeFromDialog(dialogId: number, onMessage?: (message: Message) => void): void {
    const destination = `/topic/dialog/${dialogId}`

    if (onMessage) {
      // Remove specific handler
      const handlers = this.messageHandlers.get(destination) || []
      const index = handlers.indexOf(onMessage)
      if (index > -1) {
        handlers.splice(index, 1)
      }

      // If no more handlers, unsubscribe
      if (handlers.length === 0) {
        this.messageHandlers.delete(destination)
        const subscription = this.subscriptions.get(destination)
        if (subscription) {
          subscription.unsubscribe()
          this.subscriptions.delete(destination)
        }
      }
    } else {
      // Remove all handlers and unsubscribe
      this.messageHandlers.delete(destination)
      const subscription = this.subscriptions.get(destination)
      if (subscription) {
        subscription.unsubscribe()
        this.subscriptions.delete(destination)
      }
    }
  }

  sendMessage(dialogId: number, content: string, messageType: string = 'text'): void {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected')
      return
    }

    this.client.publish({
      destination: `/app/dialog/${dialogId}`,
      body: JSON.stringify({
        content,
        messageType
      })
    })
  }

  markDialogAsRead(dialogId: number): void {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected')
      return
    }

    this.client.publish({
      destination: `/app/dialog/${dialogId}/read`,
      body: JSON.stringify({})
    })
  }

  isWebSocketConnected(): boolean {
    return this.isConnected
  }
}

export const websocketService = new WebSocketService()
export default websocketService