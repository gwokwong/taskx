<template>
  <div class="users-management">
    <div class="page-header">
      <h1>{{ t('admin.users.title') }}</h1>
      <p class="page-description">{{ t('admin.users.description') }}</p>
    </div>

    <div class="users-content">
      <div class="users-list">
        <div class="list-header">
          <h2>{{ t('admin.users.list') }}</h2>
          <Button @click="openCreateModal">
            <Plus class="w-4 h-4 mr-2" />
            {{ t('admin.users.createNew') }}
          </Button>
        </div>

        <div class="users-grid">
          <div
            v-for="user in users"
            :key="user.id"
            class="user-card"
          >
            <div class="user-info">
              <Avatar :user="user" size="md" />
              <div class="user-details">
                <h3>{{ user.nickname }}</h3>
                <p>{{ user.email }}</p>
              </div>
            </div>
            <div class="user-actions">
              <Button variant="outline" size="sm" @click="editUser(user)">
                {{ t('common.edit') }}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Avatar } from '@/components/ui/avatar'

const { t } = useI18n()

interface User {
  id: number
  nickname: string
  email: string
}

const users = ref<User[]>([])

const openCreateModal = () => {
  // TODO: 实现创建用户模态框
  console.log('Open create user modal')
}

const editUser = (user: User) => {
  // TODO: 实现编辑用户功能
  console.log('Edit user:', user)
}

onMounted(() => {
  // TODO: 加载用户列表
  users.value = [
    { id: 1, nickname: 'Admin', email: 'admin@example.com' },
    { id: 2, nickname: 'User', email: 'user@example.com' }
  ]
})
</script>

<style scoped>
.users-management {
  padding: 1.5rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 1.875rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.page-description {
  color: #6b7280;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.user-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-details h3 {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.user-details p {
  color: #6b7280;
  font-size: 0.875rem;
}
</style>