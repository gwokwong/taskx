import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import './styles/mobile.css'
import { initPWA } from './utils/pwa'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 初始化 PWA 功能
initPWA()

app.mount('#app')