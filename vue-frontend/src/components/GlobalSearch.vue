<template>
  <div class="relative">
    <!-- 搜索输入框 -->
    <div class="relative">
      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <MagnifyingGlassIcon class="h-5 w-5 text-gray-400" />
      </div>
      <input
        ref="searchInput"
        v-model="searchQuery"
        @input="handleInput"
        @focus="showDropdown = true"
        @keydown.enter="performSearch"
        @keydown.down.prevent="navigateDown"
        @keydown.up.prevent="navigateUp"
        @keydown.escape="hideDropdown"
        :placeholder="placeholder"
        class="block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-600 rounded-md leading-5 bg-white dark:bg-gray-800 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
      />
      <div class="absolute inset-y-0 right-0 pr-3 flex items-center">
        <button
          v-if="searchQuery"
          @click="clearSearch"
          class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
        >
          <XMarkIcon class="h-5 w-5" />
        </button>
        <div v-else class="text-xs text-gray-400 hidden sm:block">
          {{ shortcut }}
        </div>
      </div>
    </div>

    <!-- 搜索下拉框 -->
    <Transition
      enter-active-class="transition duration-100 ease-out"
      enter-from-class="transform scale-95 opacity-0"
      enter-to-class="transform scale-100 opacity-100"
      leave-active-class="transition duration-75 ease-in"
      leave-from-class="transform scale-100 opacity-100"
      leave-to-class="transform scale-95 opacity-0"
    >
      <div
        v-show="showDropdown && (suggestions.length > 0 || recentSearches.length > 0 || searchResults.length > 0)"
        class="absolute z-50 mt-1 w-full bg-white dark:bg-gray-800 shadow-lg max-h-96 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none"
      >
        <!-- 搜索建议 -->
        <div v-if="suggestions.length > 0 && !searchQuery">
          <div class="px-3 py-2 text-xs font-medium text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-700">
            搜索建议
          </div>
          <div
            v-for="(suggestion, index) in suggestions"
            :key="`suggestion-${index}`"
            @click="selectSuggestion(suggestion)"
            :class="[
              'px-3 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700',
              selectedIndex === index ? 'bg-gray-100 dark:bg-gray-700' : ''
            ]"
          >
            <div class="flex items-center">
              <MagnifyingGlassIcon class="h-4 w-4 text-gray-400 mr-3" />
              <span class="text-sm text-gray-900 dark:text-white">{{ suggestion }}</span>
            </div>
          </div>
        </div>

        <!-- 最近搜索 -->
        <div v-if="recentSearches.length > 0 && !searchQuery">
          <div class="px-3 py-2 text-xs font-medium text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-700">
            最近搜索
          </div>
          <div
            v-for="(search, index) in recentSearches"
            :key="`recent-${index}`"
            @click="selectSuggestion(search)"
            :class="[
              'px-3 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700',
              selectedIndex === suggestions.length + index ? 'bg-gray-100 dark:bg-gray-700' : ''
            ]"
          >
            <div class="flex items-center">
              <ClockIcon class="h-4 w-4 text-gray-400 mr-3" />
              <span class="text-sm text-gray-900 dark:text-white">{{ search }}</span>
            </div>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div v-if="searchResults.length > 0">
          <div class="px-3 py-2 text-xs font-medium text-gray-500 dark:text-gray-400 border-b border-gray-200 dark:border-gray-700">
            搜索结果 ({{ searchResults.length }})
          </div>
          <div
            v-for="(result, index) in searchResults.slice(0, 8)"
            :key="`result-${result.type}-${result.id}`"
            @click="navigateToResult(result)"
            :class="[
              'px-3 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700',
              selectedIndex === suggestions.length + recentSearches.length + index ? 'bg-gray-100 dark:bg-gray-700' : ''
            ]"
          >
            <div class="flex items-center">
              <div class="flex-shrink-0 mr-3">
                <component
                  :is="getResultIcon(result.type)"
                  class="h-4 w-4 text-gray-400"
                />
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-gray-900 dark:text-white truncate">
                  {{ result.title }}
                </div>
                <div class="text-xs text-gray-500 dark:text-gray-400 truncate">
                  {{ result.description }}
                </div>
              </div>
              <div class="flex-shrink-0 ml-2">
                <Badge :variant="getResultBadgeVariant(result.type)" size="sm">
                  {{ getResultTypeLabel(result.type) }}
                </Badge>
              </div>
            </div>
          </div>

          <div
            v-if="searchResults.length > 8"
            @click="showAllResults"
            class="px-3 py-2 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 border-t border-gray-200 dark:border-gray-700"
          >
            <div class="flex items-center justify-center text-sm text-blue-600 dark:text-blue-400">
              查看全部 {{ searchResults.length }} 个结果
              <ArrowRightIcon class="ml-2 h-4 w-4" />
            </div>
          </div>
        </div>

        <!-- 无结果 -->
        <div v-if="searchQuery && searchResults.length === 0 && !loading">
          <div class="px-3 py-8 text-center">
            <MagnifyingGlassIcon class="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p class="text-sm text-gray-500 dark:text-gray-400">没有找到相关结果</p>
            <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">尝试使用不同的关键词</p>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="px-3 py-4 text-center">
          <div class="inline-flex items-center">
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500 mr-2"></div>
            <span class="text-sm text-gray-500 dark:text-gray-400">搜索中...</span>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  MagnifyingGlassIcon,
  XMarkIcon,
  ClockIcon,
  ArrowRightIcon,
  FolderIcon,
  CheckCircleIcon,
  DocumentIcon,
  UserIcon
} from '@heroicons/vue/24/outline'
import Badge from '@/components/ui/badge/Badge.vue'
import { searchApi } from '@/services/searchApi'

interface SearchResult {
  type: string
  id: number
  title: string
  description: string
  url: string
  icon: string
  updatedAt: string
  matchField: string
  highlight: string
  relevanceScore: number
}

interface Props {
  placeholder?: string
  shortcut?: string
  autoFocus?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '搜索项目、任务、文件...',
  shortcut: 'Ctrl+K',
  autoFocus: false
})

const router = useRouter()

const searchInput = ref()
const searchQuery = ref('')
const showDropdown = ref(false)
const loading = ref(false)
const selectedIndex = ref(-1)

const suggestions = ref<string[]>([])
const recentSearches = ref<string[]>([])
const searchResults = ref<SearchResult[]>([])

let searchTimeout: number | null = null

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

const getResultIcon = (type: string) => {
  return resultIcons[type as keyof typeof resultIcons] || MagnifyingGlassIcon
}

const getResultTypeLabel = (type: string) => {
  return resultTypeLabels[type as keyof typeof resultTypeLabels] || type
}

const getResultBadgeVariant = (type: string) => {
  return resultBadgeVariants[type as keyof typeof resultBadgeVariants] || 'default'
}

const handleInput = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  if (searchQuery.value.trim()) {
    searchTimeout = window.setTimeout(() => {
      performSearch()
    }, 300)
  } else {
    searchResults.value = []
    loadRecentSearches()
  }
}

const performSearch = async () => {
  if (!searchQuery.value.trim()) return

  loading.value = true
  try {
    const results = await searchApi.globalSearch(searchQuery.value.trim())
    searchResults.value = results
  } catch (error) {
    console.error('Search failed:', error)
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

const selectSuggestion = (suggestion: string) => {
  searchQuery.value = suggestion
  performSearch()
  hideDropdown()
}

const navigateToResult = (result: SearchResult) => {
  router.push(result.url)
  hideDropdown()
}

const showAllResults = () => {
  router.push({
    path: '/search',
    query: { q: searchQuery.value }
  })
  hideDropdown()
}

const clearSearch = () => {
  searchQuery.value = ''
  searchResults.value = []
  suggestions.value = []
  selectedIndex.value = -1
  hideDropdown()
}

const hideDropdown = () => {
  showDropdown.value = false
  selectedIndex.value = -1
}

const navigateDown = () => {
  const totalItems = suggestions.value.length + recentSearches.value.length + Math.min(searchResults.value.length, 8)
  if (selectedIndex.value < totalItems - 1) {
    selectedIndex.value++
  }
}

const navigateUp = () => {
  if (selectedIndex.value > -1) {
    selectedIndex.value--
  }
}

const loadRecentSearches = async () => {
  try {
    recentSearches.value = await searchApi.getRecentSearches()
  } catch (error) {
    console.error('Failed to load recent searches:', error)
  }
}

const loadSuggestions = async (keyword: string) => {
  try {
    suggestions.value = await searchApi.getSearchSuggestions(keyword)
  } catch (error) {
    console.error('Failed to load suggestions:', error)
  }
}

const handleKeyboardShortcut = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault()
    searchInput.value?.focus()
    showDropdown.value = true
  }
}

const handleClickOutside = (event: Event) => {
  if (searchInput.value && !searchInput.value.contains(event.target as Node)) {
    hideDropdown()
  }
}

watch(searchQuery, (newValue) => {
  if (newValue.trim()) {
    loadSuggestions(newValue.trim())
  }
})

onMounted(() => {
  loadRecentSearches()
  document.addEventListener('keydown', handleKeyboardShortcut)
  document.addEventListener('click', handleClickOutside)

  if (props.autoFocus) {
    searchInput.value?.focus()
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyboardShortcut)
  document.removeEventListener('click', handleClickOutside)
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
})
</script>