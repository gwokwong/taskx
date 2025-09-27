<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
          登录您的账户
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
          或者
          <router-link to="/register" class="font-medium text-blue-600 hover:text-blue-500">
            注册新账户
          </router-link>
        </p>
      </div>
      <Card class="p-8">
        <form class="space-y-6" @submit.prevent="handleLogin">
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              邮箱地址
            </label>
            <div class="mt-1">
              <Input
                id="email"
                v-model="form.email"
                type="email"
                autocomplete="email"
                required
                placeholder="请输入邮箱地址"
                :disabled="loading"
              />
            </div>
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              密码
            </label>
            <div class="mt-1">
              <Input
                id="password"
                v-model="form.password"
                type="password"
                autocomplete="current-password"
                required
                placeholder="请输入密码"
                :disabled="loading"
              />
            </div>
          </div>

          <div v-if="error" class="text-red-600 text-sm">
            {{ error }}
          </div>

          <div>
            <Button
              type="submit"
              class="w-full"
              :disabled="loading"
            >
              {{ loading ? '登录中...' : '登录' }}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Card from '@/components/ui/card/Card.vue'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  email: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!form.value.email || !form.value.password) {
    error.value = '请填写邮箱和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.login(form.value.email, form.value.password)
    router.push('/dashboard')
  } catch (err: any) {
    error.value = err.response?.data?.msg || err.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>