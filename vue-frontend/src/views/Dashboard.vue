<template>
  <Layout>
    <div class="grid gap-6">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">总项目</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ statistics?.totalProjects || 0 }}</p>
            </div>
            <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-lg">
              <FolderIcon class="h-6 w-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </Card>

        <Card class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">总任务</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ statistics?.totalTasks || 0 }}</p>
            </div>
            <div class="p-3 bg-green-100 dark:bg-green-900 rounded-lg">
              <CheckCircleIcon class="h-6 w-6 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </Card>

        <Card class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">文件数量</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ statistics?.totalFiles || 0 }}</p>
            </div>
            <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
              <DocumentIcon class="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
            </div>
          </div>
        </Card>

        <Card class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">存储使用</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ formatStorageUsage() }}</p>
            </div>
            <div class="p-3 bg-purple-100 dark:bg-purple-900 rounded-lg">
              <CloudIcon class="h-6 w-6 text-purple-600 dark:text-purple-400" />
            </div>
          </div>
        </Card>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 任务状态分布 -->
        <Card>
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">任务状态分布</h3>
          </div>
          <div class="p-6">
            <div class="h-64">
              <PieChart
                v-if="taskStatusChartData"
                :data="taskStatusChartData"
                :options="pieChartOptions"
              />
            </div>
          </div>
        </Card>

        <!-- 文件类型分布 -->
        <Card>
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">文件类型分布</h3>
          </div>
          <div class="p-6">
            <div class="h-64">
              <PieChart
                v-if="fileTypeChartData"
                :data="fileTypeChartData"
                :options="pieChartOptions"
              />
            </div>
          </div>
        </Card>
      </div>

      <!-- 趋势图表 -->
      <div class="grid grid-cols-1 gap-6">
        <!-- 每日活动趋势 -->
        <Card>
          <div class="p-6 border-b border-gray-200 dark:border-gray-700">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-medium text-gray-900 dark:text-white">最近7天活动趋势</h3>
              <div class="flex space-x-2">
                <Badge :variant="activeTimeRange === 'tasks' ? 'info' : 'default'" size="sm" @click="activeTimeRange = 'tasks'">
                  任务
                </Badge>
                <Badge :variant="activeTimeRange === 'projects' ? 'info' : 'default'" size="sm" @click="activeTimeRange = 'projects'">
                  项目
                </Badge>
              </div>
            </div>
          </div>
          <div class="p-6">
            <div class="h-80">
              <LineChart
                v-if="trendChartData"
                :data="trendChartData"
                :options="lineChartOptions"
              />
            </div>
          </div>
        </Card>
      </div>

      <!-- 快速操作 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card class="p-6 hover:shadow-lg transition-shadow cursor-pointer" @click="$router.push('/projects/new')">
          <div class="flex items-center space-x-4">
            <div class="p-3 bg-blue-100 dark:bg-blue-900 rounded-lg">
              <PlusIcon class="h-6 w-6 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <h3 class="text-lg font-medium text-gray-900 dark:text-white">创建项目</h3>
              <p class="text-sm text-gray-500 dark:text-gray-400">开始一个新的项目</p>
            </div>
          </div>
        </Card>

        <Card class="p-6 hover:shadow-lg transition-shadow cursor-pointer" @click="$router.push('/tasks/new')">
          <div class="flex items-center space-x-4">
            <div class="p-3 bg-green-100 dark:bg-green-900 rounded-lg">
              <ClipboardDocumentListIcon class="h-6 w-6 text-green-600 dark:text-green-400" />
            </div>
            <div>
              <h3 class="text-lg font-medium text-gray-900 dark:text-white">添加任务</h3>
              <p class="text-sm text-gray-500 dark:text-gray-400">快速创建新任务</p>
            </div>
          </div>
        </Card>

        <Card class="p-6 hover:shadow-lg transition-shadow cursor-pointer" @click="$router.push('/files')">
          <div class="flex items-center space-x-4">
            <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
              <CloudArrowUpIcon class="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
            </div>
            <div>
              <h3 class="text-lg font-medium text-gray-900 dark:text-white">上传文件</h3>
              <p class="text-sm text-gray-500 dark:text-gray-400">管理文件和文档</p>
            </div>
          </div>
        </Card>
      </div>

      <!-- 最近项目 -->
      <Card>
        <div class="p-6 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">最近项目</h3>
            <Button variant="outline" size="sm" @click="$router.push('/projects')">
              查看全部
            </Button>
          </div>
        </div>
        <div class="p-6">
          <div v-if="loading" class="flex items-center justify-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          </div>
          <div v-else-if="recentProjects.length > 0" class="space-y-4">
            <div
              v-for="project in recentProjects"
              :key="project.id"
              class="flex items-center justify-between p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
              @click="() => $router.push(`/projects/${project.id}`)"
            >
              <div class="flex items-center space-x-4">
                <div class="w-10 h-10 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                  <FolderIcon class="h-5 w-5 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <h4 class="text-sm font-medium text-gray-900 dark:text-white">{{ project.name }}</h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ project.description }}</p>
                </div>
              </div>
              <div class="text-right">
                <Badge :variant="project.status === 'active' ? 'success' : 'default'" size="sm">
                  {{ project.status === 'active' ? '进行中' : '已完成' }}
                </Badge>
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  {{ formatDate(project.updatedAt) }}
                </p>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8">
            <FolderIcon class="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p class="text-gray-500 dark:text-gray-400">暂无项目</p>
          </div>
        </div>
      </Card>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import Layout from '@/components/Layout.vue'
import Card from '@/components/ui/card/Card.vue'
import Button from '@/components/ui/button/Button.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import LineChart from '@/components/charts/LineChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import {
  FolderIcon,
  CheckCircleIcon,
  DocumentIcon,
  CloudIcon,
  PlusIcon,
  ClipboardDocumentListIcon,
  CloudArrowUpIcon
} from '@heroicons/vue/24/outline'
import { statisticsApi, type StatisticsData } from '@/services/statisticsApi'

const loading = ref(false)
const statistics = ref<StatisticsData | null>(null)
const activeTimeRange = ref<'tasks' | 'projects'>('tasks')

const recentProjects = ref([
  {
    id: 1,
    name: '网站重构项目',
    description: '前端技术栈升级',
    status: 'active',
    updatedAt: '2024-01-15T10:30:00'
  },
  {
    id: 2,
    name: '移动端应用',
    description: 'iOS和Android应用开发',
    status: 'active',
    updatedAt: '2024-01-14T16:45:00'
  },
  {
    id: 3,
    name: '数据分析平台',
    description: '业务数据可视化',
    status: 'completed',
    updatedAt: '2024-01-13T09:15:00'
  }
])

// 图表配置
const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
    },
  },
}

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top' as const,
    },
  },
  scales: {
    y: {
      beginAtZero: true,
    },
  },
}

// 任务状态图表数据
const taskStatusChartData = computed(() => {
  if (!statistics.value?.taskStatusStats) return null

  const data = statistics.value.taskStatusStats
  return {
    labels: Object.keys(data).map(key => {
      const labels: Record<string, string> = {
        pending: '待处理',
        in_progress: '进行中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return labels[key] || key
    }),
    datasets: [{
      data: Object.values(data),
      backgroundColor: [
        'rgba(99, 102, 241, 0.8)',
        'rgba(34, 197, 94, 0.8)',
        'rgba(168, 85, 247, 0.8)',
        'rgba(239, 68, 68, 0.8)'
      ],
      borderColor: [
        'rgb(99, 102, 241)',
        'rgb(34, 197, 94)',
        'rgb(168, 85, 247)',
        'rgb(239, 68, 68)'
      ],
      borderWidth: 2
    }]
  }
})

// 文件类型图表数据
const fileTypeChartData = computed(() => {
  if (!statistics.value?.fileTypeStats) return null

  const data = statistics.value.fileTypeStats
  return {
    labels: Object.keys(data).map(key => {
      const labels: Record<string, string> = {
        image: '图片',
        pdf: 'PDF',
        word: 'Word',
        excel: 'Excel',
        powerpoint: 'PPT',
        text: '文本',
        archive: '压缩包',
        video: '视频',
        audio: '音频',
        other: '其他'
      }
      return labels[key] || key
    }),
    datasets: [{
      data: Object.values(data),
      backgroundColor: [
        'rgba(59, 130, 246, 0.8)',
        'rgba(16, 185, 129, 0.8)',
        'rgba(245, 101, 101, 0.8)',
        'rgba(139, 92, 246, 0.8)',
        'rgba(249, 115, 22, 0.8)',
        'rgba(236, 72, 153, 0.8)',
        'rgba(6, 182, 212, 0.8)',
        'rgba(132, 204, 22, 0.8)',
        'rgba(251, 191, 36, 0.8)',
        'rgba(107, 114, 128, 0.8)'
      ]
    }]
  }
})

// 趋势图表数据
const trendChartData = computed(() => {
  const dailyData = activeTimeRange.value === 'tasks'
    ? statistics.value?.dailyTaskStats
    : statistics.value?.dailyProjectStats

  if (!dailyData) return null

  const labels = Object.keys(dailyData).sort()
  const data = labels.map(date => dailyData[date])

  return {
    labels: labels.map(date => {
      const d = new Date(date)
      return `${d.getMonth() + 1}/${d.getDate()}`
    }),
    datasets: [{
      label: activeTimeRange.value === 'tasks' ? '每日任务数' : '每日项目数',
      data,
      borderColor: 'rgb(59, 130, 246)',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      fill: true,
      tension: 0.4
    }]
  }
})

const formatStorageUsage = () => {
  if (!statistics.value?.totalStorageUsed) return '0 MB'

  const size = statistics.value.totalStorageUsed
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let unitIndex = 0
  let fileSize = size

  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024
    unitIndex++
  }

  return `${fileSize.toFixed(1)} ${units[unitIndex]}`
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffInDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))

  if (diffInDays === 0) return '今天'
  if (diffInDays === 1) return '昨天'
  if (diffInDays < 7) return `${diffInDays}天前`

  return date.toLocaleDateString('zh-CN')
}

const loadStatistics = async () => {
  try {
    loading.value = true
    statistics.value = await statisticsApi.getOverviewStatistics()
  } catch (error) {
    console.error('Failed to load statistics:', error)
  } finally {
    loading.value = false
  }
}

watch(activeTimeRange, () => {
  // 当时间范围切换时重新加载数据
})

onMounted(() => {
  loadStatistics()
})
</script>