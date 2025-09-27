import { registerSW } from 'virtual:pwa-register'

// PWA 更新提示
const updateSW = registerSW({
  onNeedRefresh() {
    if (confirm('有新版本可用，是否立即更新？')) {
      updateSW(true)
    }
  },
  onOfflineReady() {
    console.log('应用已可离线使用')
  },
  onRegistered(r) {
    console.log(`SW 已注册: ${r}`)
  },
  onRegisterError(error) {
    console.log('SW 注册失败', error)
  },
})

// 检查是否为 PWA 环境
export const isPWA = () => {
  return window.matchMedia('(display-mode: standalone)').matches ||
         (window.navigator as any).standalone ||
         document.referrer.includes('android-app://')
}

// 获取设备信息
export const getDeviceInfo = () => {
  const ua = navigator.userAgent
  return {
    isMobile: /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua),
    isIOS: /iPad|iPhone|iPod/.test(ua),
    isAndroid: /Android/.test(ua),
    isStandalone: isPWA()
  }
}

// 安装 PWA 提示
export const installPWA = () => {
  let deferredPrompt: any = null

  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    deferredPrompt = e

    // 显示安装提示
    showInstallPrompt()
  })

  const showInstallPrompt = () => {
    const shouldShow = !localStorage.getItem('pwa-install-dismissed')

    if (shouldShow && deferredPrompt) {
      const installBanner = document.createElement('div')
      installBanner.className = 'fixed bottom-4 left-4 right-4 bg-blue-600 text-white p-4 rounded-lg shadow-lg z-50 flex items-center justify-between'
      installBanner.innerHTML = `
        <div>
          <div class="font-medium">安装 DooTask</div>
          <div class="text-sm opacity-90">安装到主屏幕，获得更好的体验</div>
        </div>
        <div class="flex space-x-2">
          <button class="install-btn bg-white text-blue-600 px-3 py-1 rounded text-sm font-medium">安装</button>
          <button class="dismiss-btn text-white opacity-75 hover:opacity-100">✕</button>
        </div>
      `

      document.body.appendChild(installBanner)

      // 安装按钮点击事件
      installBanner.querySelector('.install-btn')?.addEventListener('click', async () => {
        deferredPrompt.prompt()
        const { outcome } = await deferredPrompt.userChoice

        if (outcome === 'accepted') {
          console.log('用户接受了安装提示')
        } else {
          console.log('用户拒绝了安装提示')
        }

        deferredPrompt = null
        installBanner.remove()
      })

      // 关闭按钮点击事件
      installBanner.querySelector('.dismiss-btn')?.addEventListener('click', () => {
        localStorage.setItem('pwa-install-dismissed', 'true')
        installBanner.remove()
      })

      // 自动关闭
      setTimeout(() => {
        if (installBanner.parentNode) {
          installBanner.remove()
        }
      }, 10000)
    }
  }
}

// 初始化 PWA 功能
export const initPWA = () => {
  // 检测设备信息
  const deviceInfo = getDeviceInfo()

  // 添加 PWA 相关的 CSS 类
  if (deviceInfo.isStandalone) {
    document.body.classList.add('pwa-standalone')
  }

  if (deviceInfo.isMobile) {
    document.body.classList.add('mobile-device')
  }

  // 初始化安装提示
  installPWA()

  // 监听网络状态
  window.addEventListener('online', () => {
    console.log('网络已连接')
    // 可以在这里显示网络恢复提示
  })

  window.addEventListener('offline', () => {
    console.log('网络已断开')
    // 可以在这里显示离线提示
  })

  return deviceInfo
}