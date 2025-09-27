// 移动端手势操作工具类
interface TouchEvent {
  touches: TouchList
  changedTouches: TouchList
  type: string
  preventDefault(): void
}

interface SwipeDirection {
  left: boolean
  right: boolean
  up: boolean
  down: boolean
}

export class MobileGestures {
  private element: HTMLElement
  private startX: number = 0
  private startY: number = 0
  private endX: number = 0
  private endY: number = 0
  private minSwipeDistance: number = 50
  private maxSwipeTime: number = 300
  private startTime: number = 0
  private isScrolling: boolean = false

  constructor(element: HTMLElement, options?: {
    minSwipeDistance?: number
    maxSwipeTime?: number
  }) {
    this.element = element
    this.minSwipeDistance = options?.minSwipeDistance || 50
    this.maxSwipeTime = options?.maxSwipeTime || 300
    this.init()
  }

  private init() {
    this.element.addEventListener('touchstart', this.handleTouchStart.bind(this), { passive: false })
    this.element.addEventListener('touchmove', this.handleTouchMove.bind(this), { passive: false })
    this.element.addEventListener('touchend', this.handleTouchEnd.bind(this), { passive: false })
  }

  private handleTouchStart(e: TouchEvent) {
    const touch = e.touches[0]
    this.startX = touch.clientX
    this.startY = touch.clientY
    this.startTime = Date.now()
    this.isScrolling = false
  }

  private handleTouchMove(e: TouchEvent) {
    if (!this.startX || !this.startY) return

    const touch = e.touches[0]
    const deltaX = Math.abs(touch.clientX - this.startX)
    const deltaY = Math.abs(touch.clientY - this.startY)

    // 判断是否为滚动操作
    if (deltaY > deltaX) {
      this.isScrolling = true
    }

    // 如果是水平滑动且不是滚动，阻止默认行为
    if (deltaX > deltaY && !this.isScrolling) {
      e.preventDefault()
    }
  }

  private handleTouchEnd(e: TouchEvent) {
    if (!this.startX || !this.startY || this.isScrolling) return

    const touch = e.changedTouches[0]
    this.endX = touch.clientX
    this.endY = touch.clientY

    const deltaX = this.endX - this.startX
    const deltaY = this.endY - this.startY
    const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY)
    const duration = Date.now() - this.startTime

    if (distance >= this.minSwipeDistance && duration <= this.maxSwipeTime) {
      const direction = this.getSwipeDirection(deltaX, deltaY)
      this.onSwipe(direction, { deltaX, deltaY, distance, duration })
    }

    // 重置
    this.startX = 0
    this.startY = 0
    this.endX = 0
    this.endY = 0
    this.startTime = 0
  }

  private getSwipeDirection(deltaX: number, deltaY: number): SwipeDirection {
    const absX = Math.abs(deltaX)
    const absY = Math.abs(deltaY)

    return {
      left: deltaX < 0 && absX > absY,
      right: deltaX > 0 && absX > absY,
      up: deltaY < 0 && absY > absX,
      down: deltaY > 0 && absY > absX
    }
  }

  // 子类或使用者可以重写这个方法
  protected onSwipe(direction: SwipeDirection, details: {
    deltaX: number
    deltaY: number
    distance: number
    duration: number
  }) {
    // 默认实现，可以被重写
    console.log('Swipe detected:', direction, details)
  }

  public destroy() {
    this.element.removeEventListener('touchstart', this.handleTouchStart.bind(this))
    this.element.removeEventListener('touchmove', this.handleTouchMove.bind(this))
    this.element.removeEventListener('touchend', this.handleTouchEnd.bind(this))
  }
}

// 侧边栏滑动手势
export class SidebarSwipeGesture extends MobileGestures {
  private onOpen?: () => void
  private onClose?: () => void

  constructor(element: HTMLElement, callbacks: {
    onOpen?: () => void
    onClose?: () => void
  }) {
    super(element, { minSwipeDistance: 30 })
    this.onOpen = callbacks.onOpen
    this.onClose = callbacks.onClose
  }

  protected onSwipe(direction: SwipeDirection) {
    if (direction.right) {
      this.onOpen?.()
    } else if (direction.left) {
      this.onClose?.()
    }
  }
}

// 卡片滑动手势（用于任务卡片等）
export class CardSwipeGesture extends MobileGestures {
  private onSwipeLeft?: () => void
  private onSwipeRight?: () => void

  constructor(element: HTMLElement, callbacks: {
    onSwipeLeft?: () => void
    onSwipeRight?: () => void
  }) {
    super(element, { minSwipeDistance: 60 })
    this.onSwipeLeft = callbacks.onSwipeLeft
    this.onSwipeRight = callbacks.onSwipeRight
  }

  protected onSwipe(direction: SwipeDirection) {
    if (direction.left) {
      this.onSwipeLeft?.()
    } else if (direction.right) {
      this.onSwipeRight?.()
    }
  }
}

// 长按手势
export class LongPressGesture {
  private element: HTMLElement
  private duration: number
  private onLongPress?: () => void
  private timer?: number

  constructor(element: HTMLElement, options: {
    duration?: number
    onLongPress?: () => void
  }) {
    this.element = element
    this.duration = options.duration || 500
    this.onLongPress = options.onLongPress
    this.init()
  }

  private init() {
    this.element.addEventListener('touchstart', this.handleTouchStart.bind(this))
    this.element.addEventListener('touchend', this.handleTouchEnd.bind(this))
    this.element.addEventListener('touchmove', this.handleTouchMove.bind(this))
  }

  private handleTouchStart() {
    this.timer = window.setTimeout(() => {
      this.onLongPress?.()
    }, this.duration)
  }

  private handleTouchEnd() {
    if (this.timer) {
      clearTimeout(this.timer)
      this.timer = undefined
    }
  }

  private handleTouchMove() {
    if (this.timer) {
      clearTimeout(this.timer)
      this.timer = undefined
    }
  }

  public destroy() {
    if (this.timer) {
      clearTimeout(this.timer)
    }
    this.element.removeEventListener('touchstart', this.handleTouchStart.bind(this))
    this.element.removeEventListener('touchend', this.handleTouchEnd.bind(this))
    this.element.removeEventListener('touchmove', this.handleTouchMove.bind(this))
  }
}

// 双击手势
export class DoubleTapGesture {
  private element: HTMLElement
  private onDoubleTap?: () => void
  private lastTapTime: number = 0
  private tapDelay: number = 300

  constructor(element: HTMLElement, options: {
    onDoubleTap?: () => void
    tapDelay?: number
  }) {
    this.element = element
    this.onDoubleTap = options.onDoubleTap
    this.tapDelay = options.tapDelay || 300
    this.init()
  }

  private init() {
    this.element.addEventListener('touchend', this.handleTouchEnd.bind(this))
  }

  private handleTouchEnd() {
    const currentTime = Date.now()
    const timeDiff = currentTime - this.lastTapTime

    if (timeDiff < this.tapDelay && timeDiff > 0) {
      this.onDoubleTap?.()
      this.lastTapTime = 0
    } else {
      this.lastTapTime = currentTime
    }
  }

  public destroy() {
    this.element.removeEventListener('touchend', this.handleTouchEnd.bind(this))
  }
}

// 捏合缩放手势
export class PinchGesture {
  private element: HTMLElement
  private onPinch?: (scale: number) => void
  private initialDistance: number = 0

  constructor(element: HTMLElement, options: {
    onPinch?: (scale: number) => void
  }) {
    this.element = element
    this.onPinch = options.onPinch
    this.init()
  }

  private init() {
    this.element.addEventListener('touchstart', this.handleTouchStart.bind(this))
    this.element.addEventListener('touchmove', this.handleTouchMove.bind(this))
  }

  private getDistance(touches: TouchList): number {
    if (touches.length < 2) return 0

    const touch1 = touches[0]
    const touch2 = touches[1]

    const deltaX = touch2.clientX - touch1.clientX
    const deltaY = touch2.clientY - touch1.clientY

    return Math.sqrt(deltaX * deltaX + deltaY * deltaY)
  }

  private handleTouchStart(e: TouchEvent) {
    if (e.touches.length === 2) {
      this.initialDistance = this.getDistance(e.touches)
    }
  }

  private handleTouchMove(e: TouchEvent) {
    if (e.touches.length === 2 && this.initialDistance > 0) {
      e.preventDefault()

      const currentDistance = this.getDistance(e.touches)
      const scale = currentDistance / this.initialDistance

      this.onPinch?.(scale)
    }
  }

  public destroy() {
    this.element.removeEventListener('touchstart', this.handleTouchStart.bind(this))
    this.element.removeEventListener('touchmove', this.handleTouchMove.bind(this))
  }
}

// 组合手势管理器
export class GestureManager {
  private gestures: Array<MobileGestures | LongPressGesture | DoubleTapGesture | PinchGesture> = []

  addGesture(gesture: MobileGestures | LongPressGesture | DoubleTapGesture | PinchGesture) {
    this.gestures.push(gesture)
  }

  destroyAll() {
    this.gestures.forEach(gesture => {
      if ('destroy' in gesture) {
        gesture.destroy()
      }
    })
    this.gestures = []
  }
}