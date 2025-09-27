<template>
  <Layout>
    <div class="max-w-4xl mx-auto p-6">
      <!-- 搜索头部 -->
      <div class="mb-8">
        <div class="flex items-center space-x-4 mb-4">
          <div class="flex-1">
            <GlobalSearch
              :placeholder="`搜索 &quot;${$route.query.q}&quot;`"
              auto-focus
            />
          </div>
        </div>

        <!-- 搜索统计 -->
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            找到 <span class="font-medium">{{ totalResults }}</span> 个结果
            <span v-if="searchTime"> ({{ searchTime }}ms)</span>
          </div>

          <!-- 筛选器 -->
          <div class="flex items-center space-x-2">
            <select
              v-model="activeFilter"
              @change="filterResults"
              class="text-sm border border-gray-300 dark:border-gray-600 rounded-md px-3 py-1 bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="all">全部</option>
              <option value="project">项目</option>
              <option value="task">任务</option>
              <option value="file">文件</option>
              <option value="user">用户</option>
            </select>

            <select
              v-model="sortBy"
              @change="sortResults"
              class="text-sm border border-gray-300 dark:border-gray-600 rounded-md px-3 py-1 bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="relevance">相关度</option>
              <option value="date">日期</option>
              <option value="name">名称</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div class="space-y-6">
        <!-- 加载状态 -->
        <div v-if="loading" class="flex items-center justify-center py-12">
          <div class="inline-flex items-center">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mr-3"></div>
            <span class="text-lg text-gray-600 dark:text-gray-400">搜索中...</span>
          </div>
        </div>

        <!-- 结果列表 -->
        <div v-else-if="filteredResults.length > 0" class="space-y-4">
          <div
            v-for="result in paginatedResults"
            :key="`${result.type}-${result.id}`"
            @click="navigateToResult(result)"
            class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-shadow cursor-pointer"
          >
            <div class="flex items-start space-x-4">
              <div class="flex-shrink-0">
                <div :class="[
                  'w-12 h-12 rounded-lg flex items-center justify-center',
                  getResultBackground(result.type)
                ]">
                  <component
                    :is="getResultIcon(result.type)"
                    class="h-6 w-6 text-white"
                  />
                </div>
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center space-x-2 mb-2">
                  <h3 class="text-lg font-medium text-gray-900 dark:text-white truncate">
                    {{ result.title }}
                  </h3>
                  <Badge :variant="getResultBadgeVariant(result.type)" size="sm">
                    {{ getResultTypeLabel(result.type) }}
                  </Badge>
                </div>

                <p class="text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2">
                  {{ result.description }}
                </p>

                <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-500">
                  <div class="flex items-center space-x-4">
                    <span>匹配字段: {{ getMatchFieldLabel(result.matchField) }}</span>
                    <span v-if="result.relevanceScore">
                      相关度: {{ Math.round(result.relevanceScore) }}%
                    </span>
                  </div>
                  <span>
                    {{ formatDate(result.updatedAt) }}
                  </span>
                </div>
              </div>

              <div class="flex-shrink-0">
                <ArrowTopRightOnSquareIcon class="h-5 w-5 text-gray-400" />
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="totalPages > 1" class="flex items-center justify-center space-x-2 mt-8">
            <Button
              variant="outline"
              size="sm"
              :disabled="currentPage === 1"
              @click="currentPage--"
            >
              上一页
            </Button>

            <span class="text-sm text-gray-600 dark:text-gray-400">
              第 {{ currentPage }} 页，共 {{ totalPages }} 页
            </span>

            <Button
              variant="outline"
              size="sm"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
            >
              下一页
            </Button>
          </div>
        </div>

        <!-- 无结果状态 -->
        <div v-else-if="!loading" class="text-center py-12">
          <MagnifyingGlassIcon class="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
            没有找到相关结果
          </h3>
          <p class="text-gray-600 dark:text-gray-400 mb-4">
            尝试使用不同的关键词或筛选条件
          </p>
          <div class="flex flex-wrap gap-2 justify-center">
            <Button
              v-for="suggestion in searchSuggestions"
              :key="suggestion"
              variant="outline"
              size="sm"
              @click="searchWithSuggestion(suggestion)"
            >
              {{ suggestion }}
            </Button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  MagnifyingGlassIcon,
  ArrowTopRightOnSquareIcon,
  FolderIcon,
  CheckCircleIcon,
  DocumentIcon,
  UserIcon
} from '@heroicons/vue/24/outline'
import Layout from '@/components/Layout.vue'
import GlobalSearch from '@/components/GlobalSearch.vue'
import Button from '@/components/ui/button/Button.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import { searchApi, type SearchResult } from '@/services/searchApi'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const searchResults = ref<SearchResult[]>([])
const activeFilter = ref('all')
const sortBy = ref('relevance')
const currentPage = ref(1)
const pageSize = 10
const searchTime = ref(0)
const searchSuggestions = ref<string[]>([])

const resultIcons = {
  project: FolderIcon,
  task: CheckCircleIcon,
  file: DocumentIcon,
  user: UserIcon
}

const resultTypeLabels = {
  project: '项目',
  task: '任务',
  file: '文件',
  user: '用户'
}

const resultBadgeVariants = {
  project: 'info' as const,
  task: 'success' as const,
  file: 'warning' as const,
  user: 'default' as const
}

const resultBackgrounds = {
  project: 'bg-blue-500',
  task: 'bg-green-500',
  file: 'bg-yellow-500',
  user: 'bg-purple-500'
}

const matchFieldLabels = {
  name: '名称',
  title: '标题',
  description: '描述',
  content: '内容',
  email: '邮箱'
}

const filteredResults = computed(() => {
  let results = searchResults.value

  if (activeFilter.value !== 'all') {
    results = results.filter(result => result.type === activeFilter.value)
  }

  // 排序
  if (sortBy.value === 'relevance') {
    results.sort((a, b) => (b.relevanceScore || 0) - (a.relevanceScore || 0))
  } else if (sortBy.value === 'date') {
    results.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
  } else if (sortBy.value === 'name') {
    results.sort((a, b) => a.title.localeCompare(b.title))
  }

  return results
})

const totalResults = computed(() => filteredResults.value.length)

const totalPages = computed(() => Math.ceil(totalResults.value / pageSize))

const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredResults.value.slice(start, end)
})

const getResultIcon = (type: string) => {
  return resultIcons[type as keyof typeof resultIcons] || MagnifyingGlassIcon
}

const getResultTypeLabel = (type: string) => {
  return resultTypeLabels[type as keyof typeof resultTypeLabels] || type
}

const getResultBadgeVariant = (type: string) => {
  return resultBadgeVariants[type as keyof typeof resultBadgeVariants] || 'default'
}

const getResultBackground = (type: string) => {
  return resultBackgrounds[type as keyof typeof resultBackgrounds] || 'bg-gray-500'
}

const getMatchFieldLabel = (field: string) => {
  return matchFieldLabels[field as keyof typeof matchFieldLabels] || field
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffInDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))

  if (diffInDays === 0) return '今天'
  if (diffInDays === 1) return '昨天'
  if (diffInDays < 7) return `${diffInDays}天前`
  if (diffInDays < 30) return `${Math.floor(diffInDays / 7)}周前`
  if (diffInDays < 365) return `${Math.floor(diffInDays / 30)}个月前`

  return date.toLocaleDateString('zh-CN')
}

const performSearch = async (keyword: string) => {
  if (!keyword.trim()) return

  loading.value = true
  const startTime = Date.now()

  try {
    const results = await searchApi.globalSearch(keyword.trim(), activeFilter.value !== 'all' ? activeFilter.value : undefined)
    searchResults.value = results
    searchTime.value = Date.now() - startTime
    currentPage.value = 1
  } catch (error) {
    console.error('Search failed:', error)
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

const filterResults = () => {
  currentPage.value = 1
}

const sortResults = () => {
  currentPage.value = 1
}

const navigateToResult = (result: SearchResult) => {
  router.push(result.url)
}

const searchWithSuggestion = (suggestion: string) => {
  router.push({
    path: '/search',
    query: { q: suggestion }
  })
}

const loadSearchSuggestions = async () => {
  try {
    const suggestions = await searchApi.getRecentSearches()
    searchSuggestions.value = suggestions.slice(0, 5)
  } catch (error) {
    console.error('Failed to load search suggestions:', error)
  }
}

watch(() => route.query.q, (newQuery) => {
  if (newQuery && typeof newQuery === 'string') {
    performSearch(newQuery)
  }
})

watch(activeFilter, () => {
  filterResults()
})

watch(sortBy, () => {
  sortResults()
})

onMounted(() => {
  const query = route.query.q
  if (query && typeof query === 'string') {
    performSearch(query)
  }
  loadSearchSuggestions()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>