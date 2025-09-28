<template>
  <div class="departments-management">
    <div class="page-header">
      <h1>{{ t('admin.departments.title') }}</h1>
      <p class="page-description">{{ t('admin.departments.description') }}</p>
    </div>

    <div class="departments-content">
      <div class="departments-list">
        <div class="list-header">
          <h2>{{ t('admin.departments.list') }}</h2>
          <Button @click="openCreateModal">
            <Plus class="w-4 h-4 mr-2" />
            {{ t('admin.departments.createNew') }}
          </Button>
        </div>

        <div class="departments-grid">
          <div
            v-for="dept in departments"
            :key="dept.id"
            class="department-card"
          >
            <div class="department-info">
              <h3>{{ dept.name }}</h3>
              <p>{{ dept.description }}</p>
            </div>
            <div class="department-actions">
              <Button variant="outline" size="sm" @click="editDepartment(dept)">
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

const { t } = useI18n()

interface Department {
  id: number
  name: string
  description: string
}

const departments = ref<Department[]>([])

const openCreateModal = () => {
  console.log('Open create department modal')
}

const editDepartment = (dept: Department) => {
  console.log('Edit department:', dept)
}

onMounted(() => {
  departments.value = [
    { id: 1, name: '技术部', description: '负责技术研发' },
    { id: 2, name: '产品部', description: '负责产品设计' }
  ]
})
</script>

<style scoped>
.departments-management {
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

.departments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.department-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.department-info h3 {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.department-info p {
  color: #6b7280;
  font-size: 0.875rem;
}
</style>