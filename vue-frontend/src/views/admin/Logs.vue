<template>
  <div class="logs-management">
    <div class="page-header">
      <h1>{{ t('admin.logs.title') }}</h1>
      <p class="page-description">{{ t('admin.logs.description') }}</p>
    </div>

    <div class="logs-content">
      <div class="logs-list">
        <div class="list-header">
          <h2>{{ t('admin.logs.list') }}</h2>
          <Button @click="refreshLogs">
            <RotateCcw class="w-4 h-4 mr-2" />
            {{ t('common.refresh') }}
          </Button>
        </div>

        <div class="logs-table">
          <table>
            <thead>
              <tr>
                <th>{{ t('admin.logs.time') }}</th>
                <th>{{ t('admin.logs.level') }}</th>
                <th>{{ t('admin.logs.message') }}</th>
                <th>{{ t('admin.logs.user') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.id">
                <td>{{ formatDate(log.time) }}</td>
                <td>
                  <Badge :variant="getLogLevelVariant(log.level)">
                    {{ log.level }}
                  </Badge>
                </td>
                <td>{{ log.message }}</td>
                <td>{{ log.user }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { RotateCcw } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'

const { t } = useI18n()

interface Log {
  id: number
  time: string
  level: string
  message: string
  user: string
}

const logs = ref<Log[]>([])

const refreshLogs = () => {
  console.log('Refresh logs')
  loadLogs()
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}

const getLogLevelVariant = (level: string) => {
  switch (level.toLowerCase()) {
    case 'error':
      return 'error'
    case 'warning':
      return 'warning'
    case 'info':
      return 'info'
    default:
      return 'default'
  }
}

const loadLogs = () => {
  logs.value = [
    { id: 1, time: new Date().toISOString(), level: 'INFO', message: '用户登录', user: 'admin' },
    { id: 2, time: new Date().toISOString(), level: 'WARNING', message: '登录失败', user: 'unknown' }
  ]
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.logs-management {
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

.logs-table {
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

th {
  background: #f9fafb;
  font-weight: 600;
}

tbody tr:hover {
  background: #f9fafb;
}
</style>