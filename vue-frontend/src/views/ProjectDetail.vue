<template>
  <Layout>
    <div class="space-y-6">
      <!-- 项目详情头部 -->
      <div class="flex items-center justify-between">
        <div>
          <div class="flex items-center space-x-3">
            <Button variant="ghost" @click="$router.back()">
              <ArrowLeftIcon class="h-4 w-4" />
            </Button>
            <div>
              <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ project.name }}</h1>
              <p class="text-gray-600 dark:text-gray-400">{{ project.desc }}</p>
            </div>
          </div>
        </div>
        <Button @click="showTaskModal = true">
          <PlusIcon class="h-4 w-4 mr-2" />
          新建任务
        </Button>
      </div>

      <!-- 项目统计 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card class="p-4">
          <div class="text-center">
            <p class="text-2xl font-bold text-blue-600">{{ project.taskNum }}</p>
            <p class="text-sm text-gray-600 dark:text-gray-400">总任务</p>
          </div>
        </Card>
        <Card class="p-4">
          <div class="text-center">
            <p class="text-2xl font-bold text-green-600">{{ project.taskComplete }}</p>
            <p class="text-sm text-gray-600 dark:text-gray-400">已完成</p>
          </div>
        </Card>
        <Card class="p-4">
          <div class="text-center">
            <p class="text-2xl font-bold text-yellow-600">{{ project.taskNum - project.taskComplete }}</p>
            <p class="text-sm text-gray-600 dark:text-gray-400">进行中</p>
          </div>
        </Card>
        <Card class="p-4">
          <div class="text-center">
            <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ project.taskPercent }}%</p>
            <p class="text-sm text-gray-600 dark:text-gray-400">完成率</p>
          </div>
        </Card>
      </div>

      <!-- 任务看板 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div
          v-for="column in columns"
          :key="column.id"
          class="bg-gray-50 dark:bg-gray-800 rounded-lg p-4"
        >
          <h3 class="font-medium text-gray-900 dark:text-white mb-4">{{ column.name }}</h3>
          <div class="space-y-3">
            <Card
              v-for="task in getTasksByColumn(column.id)"
              :key="task.id"
              class="p-4 hover:shadow-md transition-shadow cursor-pointer"
            >
              <h4 class="font-medium text-gray-900 dark:text-white">{{ task.name }}</h4>
              <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ task.content }}</p>
              <div class="flex items-center justify-between mt-3">
                <div class="flex items-center space-x-2">
                  <div
                    v-for="owner in task.owner"
                    :key="owner"
                    class="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-xs text-white"
                  >
                    {{ owner.charAt(0) }}
                  </div>
                </div>
                <span v-if="task.endAt" class="text-xs text-gray-500">
                  {{ formatDate(task.endAt) }}
                </span>
              </div>
            </Card>
          </div>
        </div>
      </div>

      <!-- 新建任务模态框 -->
      <div v-if="showTaskModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <Card class="w-full max-w-md mx-4">
          <div class="p-6">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">创建新任务</h3>
            <form @submit.prevent="createTask" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  任务名称
                </label>
                <Input
                  v-model="newTask.name"
                  placeholder="输入任务名称"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  任务描述
                </label>
                <textarea
                  v-model="newTask.content"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                  rows="3"
                  placeholder="输入任务描述"
                ></textarea>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  任务列表
                </label>
                <select
                  v-model="newTask.columnId"
                  class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                >
                  <option v-for="column in columns" :key="column.id" :value="column.id">
                    {{ column.name }}
                  </option>
                </select>
              </div>
              <div class="flex justify-end space-x-3">
                <Button variant="outline" type="button" @click="showTaskModal = false">
                  取消
                </Button>
                <Button type="submit" :disabled="!newTask.name">
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
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import Layout from '@/components/Layout.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import { ArrowLeftIcon, PlusIcon } from '@heroicons/vue/24/outline'

const route = useRoute()
const projectId = computed(() => route.params.id)

const project = ref({
  id: 1,
  name: '网站重构项目',
  desc: '前端技术栈升级到Vue3',
  taskNum: 8,
  taskComplete: 3,
  taskPercent: 38
})

const columns = ref([
  { id: 1, name: '待办' },
  { id: 2, name: '进行中' },
  { id: 3, name: '已完成' }
])

const tasks = ref([
  {
    id: 1,
    name: '搭建Vue3项目',
    content: '使用Vite创建Vue3项目',
    columnId: 3,
    owner: ['张三'],
    endAt: '2024-01-15'
  },
  {
    id: 2,
    name: '配置路由',
    content: '安装并配置Vue Router',
    columnId: 3,
    owner: ['李四'],
    endAt: '2024-01-16'
  },
  {
    id: 3,
    name: '设计UI组件',
    content: '基于Shadcn UI设计组件',
    columnId: 2,
    owner: ['王五'],
    endAt: '2024-01-18'
  },
  {
    id: 4,
    name: '实现用户认证',
    content: '登录注册功能开发',
    columnId: 2,
    owner: ['赵六'],
    endAt: '2024-01-20'
  },
  {
    id: 5,
    name: 'API接口联调',
    content: '前后端接口联调测试',
    columnId: 1,
    owner: ['张三'],
    endAt: '2024-01-22'
  }
])

const showTaskModal = ref(false)
const newTask = ref({
  name: '',
  content: '',
  columnId: 1
})

const getTasksByColumn = (columnId: number) => {
  return tasks.value.filter(task => task.columnId === columnId)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const createTask = () => {
  const id = tasks.value.length + 1
  tasks.value.push({
    id,
    name: newTask.value.name,
    content: newTask.value.content,
    columnId: newTask.value.columnId,
    owner: ['当前用户'],
    endAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  })

  project.value.taskNum++
  newTask.value = { name: '', content: '', columnId: 1 }
  showTaskModal.value = false
}

onMounted(() => {
  // 根据projectId获取项目详情
  console.log('Loading project:', projectId.value)
})
</script>