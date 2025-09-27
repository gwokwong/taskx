// 前端错误处理工具
import { ElMessage, ElNotification } from 'element-plus'
import { router } from '@/router'

// 错误类型枚举
export enum ErrorType {
  NETWORK = 'network',
  VALIDATION = 'validation',
  AUTH = 'auth',
  PERMISSION = 'permission',
  BUSINESS = 'business',
  SYSTEM = 'system'
}

// 错误级别枚举
export enum ErrorLevel {
  INFO = 'info',
  WARNING = 'warning',
  ERROR = 'error',
  FATAL = 'fatal'
}

// 错误信息接口
export interface ErrorInfo {
  type: ErrorType
  level: ErrorLevel
  message: string
  code?: number
  details?: any
  timestamp?: number
  path?: string
  stack?: string
}

// 错误处理器类
export class ErrorHandler {
  private static instance: ErrorHandler
  private errorQueue: ErrorInfo[] = []
  private maxQueueSize = 100
  private reportUrl = '/api/errors/report'

  static getInstance(): ErrorHandler {
    if (!ErrorHandler.instance) {
      ErrorHandler.instance = new ErrorHandler()
    }
    return ErrorHandler.instance
  }

  /**
   * 处理错误
   */
  handleError(error: Error | ErrorInfo | any, context?: string): void {
    const errorInfo = this.normalizeError(error, context)

    // 添加到错误队列
    this.addToQueue(errorInfo)

    // 根据错误级别和类型处理
    this.processError(errorInfo)

    // 上报错误（异步）
    this.reportError(errorInfo)
  }

  /**
   * 标准化错误信息
   */
  private normalizeError(error: any, context?: string): ErrorInfo {
    let errorInfo: ErrorInfo

    if (error instanceof Error) {
      errorInfo = {
        type: this.determineErrorType(error),
        level: ErrorLevel.ERROR,
        message: error.message,
        stack: error.stack,
        timestamp: Date.now()
      }
    } else if (typeof error === 'object' && error.type) {
      errorInfo = {
        ...error,
        timestamp: error.timestamp || Date.now()
      }
    } else {
      errorInfo = {
        type: ErrorType.SYSTEM,
        level: ErrorLevel.ERROR,
        message: String(error),
        timestamp: Date.now()
      }
    }

    // 添加上下文信息
    if (context) {
      errorInfo.details = { ...errorInfo.details, context }
    }

    return errorInfo
  }

  /**
   * 确定错误类型
   */
  private determineErrorType(error: Error): ErrorType {
    const message = error.message.toLowerCase()

    if (message.includes('network') || message.includes('fetch')) {
      return ErrorType.NETWORK
    }
    if (message.includes('validation') || message.includes('invalid')) {
      return ErrorType.VALIDATION
    }
    if (message.includes('unauthorized') || message.includes('auth')) {
      return ErrorType.AUTH
    }
    if (message.includes('permission') || message.includes('forbidden')) {
      return ErrorType.PERMISSION
    }

    return ErrorType.SYSTEM
  }

  /**
   * 处理错误
   */
  private processError(errorInfo: ErrorInfo): void {
    switch (errorInfo.type) {
      case ErrorType.AUTH:
        this.handleAuthError(errorInfo)
        break
      case ErrorType.PERMISSION:
        this.handlePermissionError(errorInfo)
        break
      case ErrorType.NETWORK:
        this.handleNetworkError(errorInfo)
        break
      case ErrorType.VALIDATION:
        this.handleValidationError(errorInfo)
        break
      case ErrorType.BUSINESS:
        this.handleBusinessError(errorInfo)
        break
      default:
        this.handleSystemError(errorInfo)
    }
  }

  /**
   * 处理认证错误
   */
  private handleAuthError(errorInfo: ErrorInfo): void {
    ElMessage.error('登录已过期，请重新登录')

    // 清除本地认证信息
    localStorage.removeItem('token')
    localStorage.removeItem('user')

    // 跳转到登录页
    router.push('/login')
  }

  /**
   * 处理权限错误
   */
  private handlePermissionError(errorInfo: ErrorInfo): void {
    ElMessage.error('您没有权限执行此操作')

    // 可以跳转到403页面或返回上一页
    if (router.currentRoute.value.path !== '/403') {
      router.push('/403')
    }
  }

  /**
   * 处理网络错误
   */
  private handleNetworkError(errorInfo: ErrorInfo): void {
    if (errorInfo.level === ErrorLevel.FATAL) {
      ElNotification.error({
        title: '网络错误',
        message: '网络连接失败，请检查网络设置后重试',
        duration: 0 // 不自动关闭
      })
    } else {
      ElMessage.error('网络请求失败，请稍后重试')
    }
  }

  /**
   * 处理验证错误
   */
  private handleValidationError(errorInfo: ErrorInfo): void {
    if (errorInfo.details && typeof errorInfo.details === 'object') {
      // 显示具体的验证错误
      Object.values(errorInfo.details).forEach((msg: any) => {
        ElMessage.warning(String(msg))
      })
    } else {
      ElMessage.warning(errorInfo.message || '输入数据格式不正确')
    }
  }

  /**
   * 处理业务错误
   */
  private handleBusinessError(errorInfo: ErrorInfo): void {
    ElMessage.error(errorInfo.message || '操作失败')
  }

  /**
   * 处理系统错误
   */
  private handleSystemError(errorInfo: ErrorInfo): void {
    if (errorInfo.level === ErrorLevel.FATAL) {
      ElNotification.error({
        title: '系统错误',
        message: '系统发生严重错误，请联系管理员',
        duration: 0
      })
    } else {
      ElMessage.error('系统错误，请稍后重试')
    }
  }

  /**
   * 添加到错误队列
   */
  private addToQueue(errorInfo: ErrorInfo): void {
    this.errorQueue.push(errorInfo)

    // 保持队列大小
    if (this.errorQueue.length > this.maxQueueSize) {
      this.errorQueue.shift()
    }
  }

  /**
   * 上报错误
   */
  private async reportError(errorInfo: ErrorInfo): Promise<void> {
    try {
      // 只上报重要错误
      if (errorInfo.level === ErrorLevel.ERROR || errorInfo.level === ErrorLevel.FATAL) {
        await fetch(this.reportUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            ...errorInfo,
            userAgent: navigator.userAgent,
            url: window.location.href,
            userId: this.getCurrentUserId()
          })
        })
      }
    } catch (e) {
      console.error('错误上报失败:', e)
    }
  }

  /**
   * 获取当前用户ID
   */
  private getCurrentUserId(): string | null {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.id || null
    } catch {
      return null
    }
  }

  /**
   * 获取错误统计
   */
  getErrorStats(): { total: number; byType: Record<string, number>; byLevel: Record<string, number> } {
    const byType: Record<string, number> = {}
    const byLevel: Record<string, number> = {}

    this.errorQueue.forEach(error => {
      byType[error.type] = (byType[error.type] || 0) + 1
      byLevel[error.level] = (byLevel[error.level] || 0) + 1
    })

    return {
      total: this.errorQueue.length,
      byType,
      byLevel
    }
  }

  /**
   * 清空错误队列
   */
  clearErrorQueue(): void {
    this.errorQueue = []
  }
}

// Vue 错误处理器
export function setupVueErrorHandler(app: any): void {
  app.config.errorHandler = (err: any, instance: any, info: string) => {
    const errorHandler = ErrorHandler.getInstance()

    errorHandler.handleError({
      type: ErrorType.SYSTEM,
      level: ErrorLevel.ERROR,
      message: err.message || String(err),
      stack: err.stack,
      details: {
        componentInfo: info,
        componentName: instance?.$options.name || 'Unknown'
      }
    })
  }

  // 处理 Promise 拒绝
  window.addEventListener('unhandledrejection', (event) => {
    const errorHandler = ErrorHandler.getInstance()

    errorHandler.handleError({
      type: ErrorType.SYSTEM,
      level: ErrorLevel.ERROR,
      message: event.reason?.message || String(event.reason),
      details: {
        reason: event.reason
      }
    })

    event.preventDefault()
  })

  // 处理 JavaScript 错误
  window.addEventListener('error', (event) => {
    const errorHandler = ErrorHandler.getInstance()

    errorHandler.handleError({
      type: ErrorType.SYSTEM,
      level: ErrorLevel.ERROR,
      message: event.message,
      details: {
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno
      }
    })
  })
}

// 网络请求错误处理
export function handleApiError(error: any): void {
  const errorHandler = ErrorHandler.getInstance()

  if (error.response) {
    // 服务器响应了错误状态码
    const { status, data } = error.response

    let errorType = ErrorType.BUSINESS
    if (status === 401) errorType = ErrorType.AUTH
    else if (status === 403) errorType = ErrorType.PERMISSION
    else if (status >= 500) errorType = ErrorType.SYSTEM

    errorHandler.handleError({
      type: errorType,
      level: status >= 500 ? ErrorLevel.FATAL : ErrorLevel.ERROR,
      message: data?.error || data?.message || '请求失败',
      code: status,
      details: data
    })
  } else if (error.request) {
    // 网络错误
    errorHandler.handleError({
      type: ErrorType.NETWORK,
      level: ErrorLevel.ERROR,
      message: '网络连接失败'
    })
  } else {
    // 其他错误
    errorHandler.handleError({
      type: ErrorType.SYSTEM,
      level: ErrorLevel.ERROR,
      message: error.message || '未知错误'
    })
  }
}

// 导出单例
export const errorHandler = ErrorHandler.getInstance()