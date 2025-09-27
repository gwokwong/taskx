import { ref } from 'vue'

export interface Toast {
  id: string
  title?: string
  description?: string
  variant?: 'default' | 'destructive' | 'success'
  duration?: number
}

const toasts = ref<Toast[]>([])

let toastIdCounter = 0

export function useToast() {
  const toast = ({
    title,
    description,
    variant = 'default',
    duration = 5000,
  }: Omit<Toast, 'id'>) => {
    const id = (++toastIdCounter).toString()

    const newToast: Toast = {
      id,
      title,
      description,
      variant,
      duration,
    }

    toasts.value.push(newToast)

    if (duration > 0) {
      setTimeout(() => {
        const index = toasts.value.findIndex(t => t.id === id)
        if (index > -1) {
          toasts.value.splice(index, 1)
        }
      }, duration)
    }

    return {
      id,
      dismiss: () => {
        const index = toasts.value.findIndex(t => t.id === id)
        if (index > -1) {
          toasts.value.splice(index, 1)
        }
      },
    }
  }

  return {
    toast,
    toasts,
    dismiss: (id: string) => {
      const index = toasts.value.findIndex(t => t.id === id)
      if (index > -1) {
        toasts.value.splice(index, 1)
      }
    },
  }
}