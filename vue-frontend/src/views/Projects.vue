<template>
  <Layout>
    <div class="space-y-6">
      <!-- 项目头部 -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">项目管理</h1>
          <p class="text-gray-600 dark:text-gray-400">管理您的项目和任务</p>
        </div>
        <Button @click="showCreateModal = true">
          <PlusIcon class="h-4 w-4 mr-2" />
          新建项目
        </Button>
      </div>

      <!-- 项目网格 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Card
          v-for="project in projects"
          :key="project.id"
          class="hover:shadow-lg transition-shadow cursor-pointer"
          @click="() => $router.push(`/projects/${project.id}`)"
        >
          <div class="p-6">
            <div class="flex items-start justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-10 h-10 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                  <FolderIcon class="h-5 w-5 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ project.name }}</h3>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ project.desc }}</p>
                </div>
              </div>
            </div>

            <div class="mt-4">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">进度</span>
                <span class="font-medium">{{ project.taskPercent }}%</span>
              </div>
              <div class="mt-2 w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div
                  class="bg-blue-600 h-2 rounded-full"
                  :style="{ width: `${project.taskPercent}%` }"
                ></div>
              </div>
            </div>

            <div class="mt-4 flex items-center justify-between text-sm">
              <div class="text-gray-600 dark:text-gray-400">
                {{ project.taskComplete }}/{{ project.taskNum }} 任务
              </div>
              <div class="text-gray-600 dark:text-gray-400">
                {{ formatDate(project.updatedAt) }}
              </div>
            </div>
          </div>
        </Card>
      </div>

      <!-- 创建项目模态框 -->
      <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <Card class="w-full max-w-md mx-4">
          <div class="p-6">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">创建新项目</h3>
            <form @submit.prevent="createProject" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  项目名称
                </label>
                <Input
                  v-model="newProject.name"
                  placeholder="输入项目名称"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  项目描述
                </label>
                <textarea
                  v-model="newProject.desc"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  rows="3"
                  placeholder="输入项目描述（可选）"
                ></textarea>
              </div>
              <div class="flex justify-end space-x-3">
                <Button variant="outline" type="button" @click="showCreateModal = false">
                  取消
                </Button>
                <Button type="submit" :disabled="!newProject.name">
                  创建
                </Button>
              </div>
            </form>
          </div>
        </Card>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Layout from '@/components/Layout.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import { FolderIcon, PlusIcon } from '@heroicons/vue/24/outline'

const projects = ref([
  {
    id: 1,
    name: '网站重构项目',
    desc: '前端技术栈升级到Vue3',
    taskNum: 24,
    taskComplete: 18,
    taskPercent: 75,
    updatedAt: '2024-01-15'
  },
  {
    id: 2,
    name: '移动端应用',
    desc: 'iOS和Android应用开发',
    taskNum: 16,
    taskComplete: 8,
    taskPercent: 50,
    updatedAt: '2024-01-10'
  },
  {
    id: 3,
    name: '数据分析平台',
    desc: '业务数据可视化系统',
    taskNum: 32,
    taskComplete: 20,
    taskPercent: 63,
    updatedAt: '2024-01-12'
  }
])

const showCreateModal = ref(false)
const newProject = ref({
  name: '',
  desc: ''
})

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const createProject = () => {
  // 这里应该调用API创建项目
  console.log('Creating project:', newProject.value)

  // 模拟添加到列表
  const id = projects.value.length + 1
  projects.value.unshift({
    id,
    name: newProject.value.name,
    desc: newProject.value.desc,
    taskNum: 0,
    taskComplete: 0,
    taskPercent: 0,
    updatedAt: new Date().toISOString().split('T')[0]
  })

  // 重置表单
  newProject.value = { name: '', desc: '' }
  showCreateModal.value = false
}

onMounted(() => {
  // 这里可以调用API获取项目列表
})
</script>