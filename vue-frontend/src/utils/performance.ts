// 前端性能优化工具类
import { ref, onUnmounted } from 'vue'

// 防抖函数
export function useDebounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): [T, () => void] {
  let timeoutId: number | null = null

  const debouncedFn = ((...args: Parameters<T>) => {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
    }
    timeoutId = window.setTimeout(() => {
      fn(...args)
      timeoutId = null
    }, delay)
  }) as T

  const cancel = () => {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }

  onUnmounted(cancel)

  return [debouncedFn, cancel]
}

// 节流函数
export function useThrottle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): [T, () => void] {
  let timeoutId: number | null = null
  let lastExecTime = 0

  const throttledFn = ((...args: Parameters<T>) => {
    const currentTime = Date.now()

    if (currentTime - lastExecTime > delay) {
      fn(...args)
      lastExecTime = currentTime
    } else if (timeoutId === null) {
      timeoutId = window.setTimeout(() => {
        fn(...args)
        lastExecTime = Date.now()
        timeoutId = null
      }, delay - (currentTime - lastExecTime))
    }
  }) as T

  const cancel = () => {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }

  onUnmounted(cancel)

  return [throttledFn, cancel]
}

// 懒加载图片
export function useLazyImage() {
  const imageRef = ref<HTMLImageElement>()
  const isLoaded = ref(false)
  const isError = ref(false)

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && imageRef.value) {
          const img = imageRef.value
          const src = img.dataset.src

          if (src) {
            img.src = src
            img.onload = () => {
              isLoaded.value = true
              observer.unobserve(img)
            }
            img.onerror = () => {
              isError.value = true
              observer.unobserve(img)
            }
          }
        }
      })
    },
    {
      rootMargin: '50px'
    }
  )

  const observeImage = (el: HTMLImageElement) => {
    imageRef.value = el
    observer.observe(el)
  }

  onUnmounted(() => {
    observer.disconnect()
  })

  return {
    imageRef,
    isLoaded,
    isError,
    observeImage
  }
}

// 虚拟滚动
export function useVirtualScroll<T>(
  items: T[],
  itemHeight: number,
  containerHeight: number
) {
  const scrollTop = ref(0)
  const startIndex = ref(0)
  const endIndex = ref(0)
  const visibleItems = ref<T[]>([])

  const totalHeight = computed(() => items.length * itemHeight)
  const visibleCount = Math.ceil(containerHeight / itemHeight)
  const buffer = 5 // 缓冲区项目数

  const updateVisibleItems = () => {
    startIndex.value = Math.max(0, Math.floor(scrollTop.value / itemHeight) - buffer)
    endIndex.value = Math.min(
      items.length - 1,
      startIndex.value + visibleCount + buffer * 2
    )

    visibleItems.value = items.slice(startIndex.value, endIndex.value + 1)
  }

  const handleScroll = (event: Event) => {
    const target = event.target as HTMLElement
    scrollTop.value = target.scrollTop
    updateVisibleItems()
  }

  // 初始化
  updateVisibleItems()

  return {
    scrollTop,
    startIndex,
    endIndex,
    visibleItems,
    totalHeight,
    handleScroll,
    updateVisibleItems
  }
}

// 资源预加载
export function usePreloader() {
  const loadedResources = new Set<string>()
  const loadingResources = new Map<string, Promise<void>>()

  const preloadImage = (src: string): Promise<void> => {
    if (loadedResources.has(src)) {
      return Promise.resolve()
    }

    if (loadingResources.has(src)) {
      return loadingResources.get(src)!
    }

    const promise = new Promise<void>((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        loadedResources.add(src)
        loadingResources.delete(src)
        resolve()
      }
      img.onerror = () => {
        loadingResources.delete(src)
        reject(new Error(`Failed to load image: ${src}`))
      }
      img.src = src
    })

    loadingResources.set(src, promise)
    return promise
  }

  const preloadCSS = (href: string): Promise<void> => {
    if (loadedResources.has(href)) {
      return Promise.resolve()
    }

    if (loadingResources.has(href)) {
      return loadingResources.get(href)!
    }

    const promise = new Promise<void>((resolve, reject) => {
      const link = document.createElement('link')
      link.rel = 'stylesheet'
      link.href = href
      link.onload = () => {
        loadedResources.add(href)
        loadingResources.delete(href)
        resolve()
      }
      link.onerror = () => {
        loadingResources.delete(href)
        reject(new Error(`Failed to load CSS: ${href}`))
      }
      document.head.appendChild(link)
    })

    loadingResources.set(href, promise)
    return promise
  }

  const preloadScript = (src: string): Promise<void> => {
    if (loadedResources.has(src)) {
      return Promise.resolve()
    }

    if (loadingResources.has(src)) {
      return loadingResources.get(src)!
    }

    const promise = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script')
      script.src = src
      script.onload = () => {
        loadedResources.add(src)
        loadingResources.delete(src)
        resolve()
      }
      script.onerror = () => {
        loadingResources.delete(src)
        reject(new Error(`Failed to load script: ${src}`))
      }
      document.head.appendChild(script)
    })

    loadingResources.set(src, promise)
    return promise
  }

  return {
    preloadImage,
    preloadCSS,
    preloadScript,
    loadedResources: readonly(loadedResources)
  }
}

// 内存管理
export function useMemoryManager() {
  const observers = new Set<IntersectionObserver>()
  const timers = new Set<number>()
  const eventListeners = new Map<Element, Array<{ event: string; handler: EventListener }>>()

  const addIntersectionObserver = (observer: IntersectionObserver) => {
    observers.add(observer)
    return observer
  }

  const addTimer = (id: number) => {
    timers.add(id)
    return id
  }

  const addEventListener = (
    element: Element,
    event: string,
    handler: EventListener,
    options?: AddEventListenerOptions
  ) => {
    element.addEventListener(event, handler, options)

    if (!eventListeners.has(element)) {
      eventListeners.set(element, [])
    }
    eventListeners.get(element)!.push({ event, handler })
  }

  const cleanup = () => {
    // 清理观察器
    observers.forEach(observer => observer.disconnect())
    observers.clear()

    // 清理定时器
    timers.forEach(id => clearTimeout(id))
    timers.clear()

    // 清理事件监听器
    eventListeners.forEach((listeners, element) => {
      listeners.forEach(({ event, handler }) => {
        element.removeEventListener(event, handler)
      })
    })
    eventListeners.clear()
  }

  onUnmounted(cleanup)

  return {
    addIntersectionObserver,
    addTimer,
    addEventListener,
    cleanup
  }
}

// 性能监控
export function usePerformanceMonitor() {
  const metrics = ref({
    fps: 0,
    memoryUsage: 0,
    loadTime: 0,
    renderTime: 0
  })

  // FPS 监控
  let frameCount = 0
  let lastTime = performance.now()

  const measureFPS = () => {
    frameCount++
    const currentTime = performance.now()

    if (currentTime - lastTime >= 1000) {
      metrics.value.fps = Math.round((frameCount * 1000) / (currentTime - lastTime))
      frameCount = 0
      lastTime = currentTime
    }

    requestAnimationFrame(measureFPS)
  }

  // 内存使用监控
  const measureMemory = () => {
    if ('memory' in performance) {
      const memory = (performance as any).memory
      metrics.value.memoryUsage = Math.round(memory.usedJSHeapSize / 1024 / 1024)
    }
  }

  // 页面加载时间
  const measureLoadTime = () => {
    const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
    if (navigation) {
      metrics.value.loadTime = Math.round(navigation.loadEventEnd - navigation.fetchStart)
    }
  }

  // 渲染时间监控
  const measureRenderTime = (callback: () => void) => {
    const startTime = performance.now()

    nextTick(() => {
      const endTime = performance.now()
      metrics.value.renderTime = Math.round(endTime - startTime)
    })

    callback()
  }

  // 开始监控
  const startMonitoring = () => {
    measureFPS()
    measureLoadTime()

    const interval = setInterval(() => {
      measureMemory()
    }, 5000)

    onUnmounted(() => {
      clearInterval(interval)
    })
  }

  return {
    metrics: readonly(metrics),
    startMonitoring,
    measureRenderTime
  }
}

// 网络状态监控
export function useNetworkStatus() {
  const isOnline = ref(navigator.onLine)
  const connectionType = ref('')
  const effectiveType = ref('')

  const updateConnectionInfo = () => {
    const connection = (navigator as any).connection ||
                      (navigator as any).mozConnection ||
                      (navigator as any).webkitConnection

    if (connection) {
      connectionType.value = connection.type || ''
      effectiveType.value = connection.effectiveType || ''
    }
  }

  const handleOnline = () => {
    isOnline.value = true
    updateConnectionInfo()
  }

  const handleOffline = () => {
    isOnline.value = false
  }

  const handleConnectionChange = () => {
    updateConnectionInfo()
  }

  onMounted(() => {
    updateConnectionInfo()

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    const connection = (navigator as any).connection
    if (connection) {
      connection.addEventListener('change', handleConnectionChange)
    }
  })

  onUnmounted(() => {
    window.removeEventListener('online', handleOnline)
    window.removeEventListener('offline', handleOffline)

    const connection = (navigator as any).connection
    if (connection) {
      connection.removeEventListener('change', handleConnectionChange)
    }
  })

  return {
    isOnline: readonly(isOnline),
    connectionType: readonly(connectionType),
    effectiveType: readonly(effectiveType)
  }
}