<template>
  <div class="task-management-view">
    <!-- 页面头部 -->
    <div class="task-header">
      <div class="header-left">
        <h1 class="page-title">{{ t('task.management') }}</h1>
        <div class="task-stats">
          <div class="stat-item">
            <span class="stat-label">{{ t('task.total') }}</span>
            <span class="stat-value">{{ taskStats.total }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">{{ t('task.completed') }}</span>
            <span class="stat-value">{{ taskStats.completed }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">{{ t('task.pending') }}</span>
            <span class="stat-value">{{ taskStats.pending }}</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <Button type="primary" @click="showCreateTaskModal = true">
          <Plus class="w-4 h-4 mr-2" />
          {{ t('task.create') }}
        </Button>
        <Button variant="outline" @click="showFilterPanel = !showFilterPanel">
          <Filter class="w-4 h-4 mr-2" />
          {{ t('common.filter') }}
        </Button>
        <Button variant="outline" @click="exportTasks">
          <Download class="w-4 h-4 mr-2" />
          {{ t('common.export') }}
        </Button>
      </div>
    </div>

    <!-- 筛选面板 -->
    <div v-if="showFilterPanel" class="filter-panel">
      <div class="filter-row">
        <div class="filter-item">
          <label>{{ t('project.title') }}</label>
          <Select v-model:value="filters.projectId" @change="applyFilters">
            <SelectItem value="">{{ t('common.all') }}</SelectItem>
            <SelectItem v-for="project in projects" :key="project.id" :value="project.id">
              {{ project.name }}
            </SelectItem>
          </Select>
        </div>
        <div class="filter-item">
          <label>{{ t('task.status') }}</label>
          <Select v-model:value="filters.status" @change="applyFilters">
            <SelectItem value="">{{ t('common.all') }}</SelectItem>
            <SelectItem value="pending">{{ t('task.pending') }}</SelectItem>
            <SelectItem value="completed">{{ t('task.completed') }}</SelectItem>
            <SelectItem value="archived">{{ t('task.archived') }}</SelectItem>
          </Select>
        </div>
        <div class="filter-item">
          <label>{{ t('task.assignee') }}</label>
          <Select v-model:value="filters.assigneeId" @change="applyFilters">
            <SelectItem value="">{{ t('common.all') }}</SelectItem>
            <SelectItem v-for="user in users" :key="user.id" :value="user.id">
              {{ user.nickname }}
            </SelectItem>
          </Select>
        </div>
        <div class="filter-item">
          <label>{{ t('task.priority') }}</label>
          <Select v-model:value="filters.priority" @change="applyFilters">
            <SelectItem value="">{{ t('common.all') }}</SelectItem>
            <SelectItem value="low">{{ t('task.priority.low') }}</SelectItem>
            <SelectItem value="medium">{{ t('task.priority.medium') }}</SelectItem>
            <SelectItem value="high">{{ t('task.priority.high') }}</SelectItem>
            <SelectItem value="urgent">{{ t('task.priority.urgent') }}</SelectItem>
          </Select>
        </div>
      </div>
    </div>

    <!-- 任务看板视图 -->
    <div class="task-board" v-if="viewMode === 'kanban'">
      <div class="board-columns">
        <div v-for="column in taskColumns" :key="column.id" class="board-column">
          <div class="column-header">
            <h3 class="column-title">{{ column.name }}</h3>
            <span class="task-count">{{ column.tasks?.length || 0 }}</span>
          </div>
          <div class="column-content">
            <draggable
              v-model="column.tasks"
              :group="{ name: 'tasks' }"
              item-key="id"
              class="task-list"
              @end="onTaskMove"
            >
              <template #item="{ element: task }">
                <TaskCard
                  :task="task"
                  @click="openTaskDetail(task)"
                  @update="updateTask"
                  @delete="deleteTask"
                  @complete="completeTask"
                  @archive="archiveTask"
                />
              </template>
            </draggable>
            <Button
              variant="ghost"
              class="add-task-btn w-full"
              @click="addTaskToColumn(column.id)"
            >
              <Plus class="w-4 h-4 mr-2" />
              {{ t('task.addToColumn') }}
            </Button>
          </div>
        </div>
      </div>
    </div>

    <!-- 任务列表视图 -->
    <div class="task-list-view" v-else>
      <div class="list-header">
        <div class="view-controls">
          <Button
            :variant="viewMode === 'list' ? 'default' : 'outline'"
            @click="viewMode = 'list'"
          >
            <List class="w-4 h-4 mr-2" />
            {{ t('view.list') }}
          </Button>
          <Button
            :variant="viewMode === 'kanban' ? 'default' : 'outline'"
            @click="viewMode = 'kanban'"
          >
            <LayoutGrid class="w-4 h-4 mr-2" />
            {{ t('view.kanban') }}
          </Button>
        </div>
        <div class="sort-controls">
          <Select v-model:value="sortBy" @change="applySorting">
            <SelectItem value="created_at">{{ t('task.sort.created') }}</SelectItem>
            <SelectItem value="updated_at">{{ t('task.sort.updated') }}</SelectItem>
            <SelectItem value="end_at">{{ t('task.sort.deadline') }}</SelectItem>
            <SelectItem value="priority">{{ t('task.sort.priority') }}</SelectItem>
          </Select>
          <Button variant="outline" @click="sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'">
            <ArrowUpDown class="w-4 h-4" />
          </Button>
        </div>
      </div>

      <div class="task-table">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="w-8">
                <Checkbox
                  v-model:checked="selectAll"
                  @update:checked="toggleSelectAll"
                />
              </TableHead>
              <TableHead>{{ t('task.name') }}</TableHead>
              <TableHead>{{ t('project.title') }}</TableHead>
              <TableHead>{{ t('task.assignee') }}</TableHead>
              <TableHead>{{ t('task.status') }}</TableHead>
              <TableHead>{{ t('task.priority') }}</TableHead>
              <TableHead>{{ t('task.deadline') }}</TableHead>
              <TableHead>{{ t('task.progress') }}</TableHead>
              <TableHead class="w-24">{{ t('common.actions') }}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="task in filteredTasks"
              :key="task.id"
              :class="{
                'selected': selectedTasks.includes(task.id),
                'overdue': isOverdue(task),
                'completed': task.complete_at
              }"
            >
              <TableCell>
                <Checkbox
                  :checked="selectedTasks.includes(task.id)"
                  @update:checked="toggleTaskSelection(task.id)"
                />
              </TableCell>
              <TableCell>
                <div class="task-name-cell">
                  <div
                    class="task-name"
                    :class="{ 'has-subtasks': task.subtasks > 0 }"
                    @click="openTaskDetail(task)"
                  >
                    {{ task.name }}
                  </div>
                  <div v-if="task.subtasks > 0" class="subtask-indicator">
                    <Badge variant="secondary">
                      {{ task.subtasks_complete }}/{{ task.subtasks }}
                    </Badge>
                  </div>
                </div>
              </TableCell>
              <TableCell>
                <div class="project-cell">
                  {{ getProjectName(task.project_id) }}
                </div>
              </TableCell>
              <TableCell>
                <div class="assignee-cell">
                  <AvatarGroup :users="getTaskAssignees(task)" :max="3" />
                </div>
              </TableCell>
              <TableCell>
                <TaskStatusBadge :task="task" />
              </TableCell>
              <TableCell>
                <TaskPriorityBadge :priority="task.priority" />
              </TableCell>
              <TableCell>
                <div class="deadline-cell" :class="{ 'overdue': isOverdue(task) }">
                  {{ formatDate(task.end_at) }}
                </div>
              </TableCell>
              <TableCell>
                <div class="progress-cell">
                  <Progress :value="getTaskProgress(task)" class="w-full" />
                  <span class="progress-text">{{ getTaskProgress(task) }}%</span>
                </div>
              </TableCell>
              <TableCell>
                <div class="action-buttons">
                  <Button variant="ghost" size="sm" @click="openTaskDetail(task)">
                    <Eye class="w-4 h-4" />
                  </Button>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="sm">
                        <MoreHorizontal class="w-4 h-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent>
                      <DropdownMenuItem @click="editTask(task)">
                        <Edit class="w-4 h-4 mr-2" />
                        {{ t('common.edit') }}
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="completeTask(task)" v-if="!task.complete_at">
                        <CheckCircle class="w-4 h-4 mr-2" />
                        {{ t('task.complete') }}
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="archiveTask(task)" v-if="!task.archived_at">
                        <Archive class="w-4 h-4 mr-2" />
                        {{ t('task.archive') }}
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem @click="deleteTask(task)" class="text-red-600">
                        <Trash2 class="w-4 h-4 mr-2" />
                        {{ t('common.delete') }}
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <Pagination
          :total="totalTasks"
          :page-size="pageSize"
          :current="currentPage"
          @update:current="handlePageChange"
        />
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedTasks.length > 0" class="bulk-actions">
      <div class="selected-info">
        {{ t('task.selectedCount', { count: selectedTasks.length }) }}
      </div>
      <div class="bulk-buttons">
        <Button variant="outline" @click="bulkComplete">
          <CheckCircle class="w-4 h-4 mr-2" />
          {{ t('task.bulkComplete') }}
        </Button>
        <Button variant="outline" @click="bulkArchive">
          <Archive class="w-4 h-4 mr-2" />
          {{ t('task.bulkArchive') }}
        </Button>
        <Button variant="outline" @click="bulkAssign">
          <Users class="w-4 h-4 mr-2" />
          {{ t('task.bulkAssign') }}
        </Button>
        <Button variant="destructive" @click="bulkDelete">
          <Trash2 class="w-4 h-4 mr-2" />
          {{ t('task.bulkDelete') }}
        </Button>
      </div>
    </div>

    <!-- 创建任务模态框 -->
    <TaskCreateModal
      v-model:open="showCreateTaskModal"
      :projects="projects"
      :users="users"
      @created="onTaskCreated"
    />

    <!-- 任务详情模态框 -->
    <TaskDetailModal
      v-model:open="showTaskDetailModal"
      :task="selectedTask"
      :projects="projects"
      :users="users"
      @updated="onTaskUpdated"
      @deleted="onTaskDeleted"
    />

    <!-- 批量分配模态框 -->
    <TaskBulkAssignModal
      v-model:open="showBulkAssignModal"
      :task-ids="selectedTasks"
      :users="users"
      @assigned="onBulkAssigned"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import draggable from 'vuedraggable'
import {
  Plus, Filter, Download, List, LayoutGrid, ArrowUpDown,
  Eye, MoreHorizontal, Edit, CheckCircle, Archive, Trash2, Users
} from 'lucide-vue-next'

// 组件导入
import { Button } from '@/components/ui/button'
import { Select, SelectItem } from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import {
  Table, TableHeader, TableBody, TableRow,
  TableHead, TableCell
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuTrigger, DropdownMenuContent,
  DropdownMenuItem, DropdownMenuSeparator
} from '@/components/ui/dropdown-menu'
import { Pagination } from '@/components/ui/pagination'

import TaskCard from '@/components/task/TaskCard.vue'
import TaskStatusBadge from '@/components/task/TaskStatusBadge.vue'
import TaskPriorityBadge from '@/components/task/TaskPriorityBadge.vue'
import AvatarGroup from '@/components/ui/AvatarGroup.vue'
import TaskCreateModal from '@/components/task/TaskCreateModal.vue'
import TaskDetailModal from '@/components/task/TaskDetailModal.vue'
import TaskBulkAssignModal from '@/components/task/TaskBulkAssignModal.vue'

// API导入
import { taskApi, projectApi, userApi } from '@/api'

const { t } = useI18n()
const { toast } = useToast()

// 响应式数据
const tasks = ref([])
const projects = ref([])
const users = ref([])
const taskColumns = ref([])
const selectedTasks = ref([])
const selectedTask = ref(null)

// 界面状态
const showFilterPanel = ref(false)
const showCreateTaskModal = ref(false)
const showTaskDetailModal = ref(false)
const showBulkAssignModal = ref(false)
const viewMode = ref('list') // list 或 kanban

// 筛选器
const filters = reactive({
  projectId: '',
  status: '',
  assigneeId: '',
  priority: '',
  keyword: ''
})

// 排序
const sortBy = ref('created_at')
const sortOrder = ref('desc')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = ref(0)

// 计算属性
const taskStats = computed(() => {
  const total = tasks.value.length
  const completed = tasks.value.filter(task => task.complete_at).length
  const pending = total - completed
  return { total, completed, pending }
})

const filteredTasks = computed(() => {
  let result = [...tasks.value]

  // 应用筛选器
  if (filters.projectId) {
    result = result.filter(task => task.project_id === filters.projectId)
  }
  if (filters.status) {
    if (filters.status === 'completed') {
      result = result.filter(task => task.complete_at)
    } else if (filters.status === 'pending') {
      result = result.filter(task => !task.complete_at && !task.archived_at)
    } else if (filters.status === 'archived') {
      result = result.filter(task => task.archived_at)
    }
  }
  if (filters.assigneeId) {
    result = result.filter(task => {
      const assignees = getTaskAssignees(task)
      return assignees.some(user => user.id === filters.assigneeId)
    })
  }
  if (filters.priority) {
    result = result.filter(task => task.priority === filters.priority)
  }
  if (filters.keyword) {
    const keyword = filters.keyword.toLowerCase()
    result = result.filter(task =>
      task.name.toLowerCase().includes(keyword) ||
      task.content?.toLowerCase().includes(keyword) ||
      task.desc?.toLowerCase().includes(keyword)
    )
  }

  // 应用排序
  result.sort((a, b) => {
    let valueA = a[sortBy.value]
    let valueB = b[sortBy.value]

    if (sortBy.value === 'priority') {
      const priorityOrder = { urgent: 4, high: 3, medium: 2, low: 1 }
      valueA = priorityOrder[valueA] || 0
      valueB = priorityOrder[valueB] || 0
    }

    if (valueA < valueB) return sortOrder.value === 'asc' ? -1 : 1
    if (valueA > valueB) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })

  return result
})

const selectAll = computed({
  get: () => selectedTasks.value.length === filteredTasks.value.length && filteredTasks.value.length > 0,
  set: (value) => {
    if (value) {
      selectedTasks.value = filteredTasks.value.map(task => task.id)
    } else {
      selectedTasks.value = []
    }
  }
})

// 方法
const loadTasks = async () => {
  try {
    const response = await taskApi.getTasks({
      page: currentPage.value,
      size: pageSize.value,
      projectId: filters.projectId || undefined,
      status: filters.status || undefined
    })
    tasks.value = response.data.records
    totalTasks.value = response.data.total
  } catch (error) {
    toast({
      title: t('error.loadFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const loadProjects = async () => {
  try {
    const response = await projectApi.getProjects()
    projects.value = response.data
  } catch (error) {
    console.error('Failed to load projects:', error)
  }
}

const loadUsers = async () => {
  try {
    const response = await userApi.getUsers()
    users.value = response.data
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

const loadTaskColumns = async () => {
  // 获取任务列数据
  taskColumns.value = [
    { id: 1, name: t('task.column.todo'), tasks: [] },
    { id: 2, name: t('task.column.inProgress'), tasks: [] },
    { id: 3, name: t('task.column.review'), tasks: [] },
    { id: 4, name: t('task.column.done'), tasks: [] }
  ]

  // 将任务分配到对应列
  tasks.value.forEach(task => {
    let columnId = 1 // 默认待办
    if (task.complete_at) {
      columnId = 4 // 已完成
    } else if (task.flow_item_status === 'review') {
      columnId = 3 // 评审中
    } else if (task.flow_item_status === 'progress') {
      columnId = 2 // 进行中
    }

    const column = taskColumns.value.find(col => col.id === columnId)
    if (column) {
      column.tasks.push(task)
    }
  })
}

const applyFilters = () => {
  currentPage.value = 1
  loadTasks()
}

const applySorting = () => {
  // 重新计算filteredTasks
}

const toggleSelectAll = () => {
  selectAll.value = !selectAll.value
}

const toggleTaskSelection = (taskId) => {
  const index = selectedTasks.value.indexOf(taskId)
  if (index > -1) {
    selectedTasks.value.splice(index, 1)
  } else {
    selectedTasks.value.push(taskId)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadTasks()
}

const openTaskDetail = (task) => {
  selectedTask.value = task
  showTaskDetailModal.value = true
}

const editTask = (task) => {
  openTaskDetail(task)
}

const updateTask = async (task) => {
  try {
    await taskApi.updateTask(task.id, task)
    await loadTasks()
    toast({
      title: t('success.updated'),
      description: t('task.updateSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.updateFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const deleteTask = async (task) => {
  try {
    await taskApi.deleteTask(task.id)
    await loadTasks()
    toast({
      title: t('success.deleted'),
      description: t('task.deleteSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.deleteFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const completeTask = async (task) => {
  try {
    await taskApi.completeTask(task.id)
    await loadTasks()
    toast({
      title: t('success.completed'),
      description: t('task.completeSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.completeFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const archiveTask = async (task) => {
  try {
    await taskApi.archiveTask(task.id)
    await loadTasks()
    toast({
      title: t('success.archived'),
      description: t('task.archiveSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.archiveFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const bulkComplete = async () => {
  try {
    await Promise.all(selectedTasks.value.map(id => taskApi.completeTask(id)))
    selectedTasks.value = []
    await loadTasks()
    toast({
      title: t('success.bulkCompleted'),
      description: t('task.bulkCompleteSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.bulkCompleteFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const bulkArchive = async () => {
  try {
    await Promise.all(selectedTasks.value.map(id => taskApi.archiveTask(id)))
    selectedTasks.value = []
    await loadTasks()
    toast({
      title: t('success.bulkArchived'),
      description: t('task.bulkArchiveSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.bulkArchiveFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const bulkAssign = () => {
  showBulkAssignModal.value = true
}

const bulkDelete = async () => {
  try {
    await Promise.all(selectedTasks.value.map(id => taskApi.deleteTask(id)))
    selectedTasks.value = []
    await loadTasks()
    toast({
      title: t('success.bulkDeleted'),
      description: t('task.bulkDeleteSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.bulkDeleteFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const addTaskToColumn = (columnId) => {
  // 在特定列中创建任务
  showCreateTaskModal.value = true
}

const onTaskMove = async (event) => {
  const { item, to, newIndex } = event
  const taskId = item.dataset.taskId
  const columnId = to.dataset.columnId

  try {
    await taskApi.moveTask(taskId, { columnId, sort: newIndex })
    toast({
      title: t('success.moved'),
      description: t('task.moveSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.moveFailed'),
      description: error.message,
      variant: 'destructive'
    })
    // 恢复原始位置
    await loadTasks()
  }
}

const exportTasks = async () => {
  try {
    await taskApi.exportTasks({
      format: 'excel',
      filters
    })
    toast({
      title: t('success.exported'),
      description: t('task.exportSuccess')
    })
  } catch (error) {
    toast({
      title: t('error.exportFailed'),
      description: error.message,
      variant: 'destructive'
    })
  }
}

const onTaskCreated = (task) => {
  loadTasks()
  showCreateTaskModal.value = false
}

const onTaskUpdated = (task) => {
  loadTasks()
  showTaskDetailModal.value = false
}

const onTaskDeleted = () => {
  loadTasks()
  showTaskDetailModal.value = false
}

const onBulkAssigned = () => {
  selectedTasks.value = []
  loadTasks()
  showBulkAssignModal.value = false
}

// 工具函数
const getProjectName = (projectId) => {
  const project = projects.value.find(p => p.id === projectId)
  return project?.name || ''
}

const getTaskAssignees = (task) => {
  if (!task.owner) return []

  try {
    const ownerIds = typeof task.owner === 'string'
      ? JSON.parse(task.owner)
      : [task.owner]
    return users.value.filter(user => ownerIds.includes(user.id))
  } catch {
    return []
  }
}

const getTaskProgress = (task) => {
  if (task.complete_at) return 100
  if (task.subtasks > 0) {
    return Math.round((task.subtasks_complete / task.subtasks) * 100)
  }
  return 0
}

const isOverdue = (task) => {
  if (!task.end_at || task.complete_at) return false
  return new Date(task.end_at) < new Date()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(date))
}

// 监听器
watch([currentPage, filters], () => {
  loadTasks()
}, { deep: true })

watch(viewMode, () => {
  if (viewMode.value === 'kanban') {
    loadTaskColumns()
  }
})

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadTasks(),
    loadProjects(),
    loadUsers()
  ])

  if (viewMode.value === 'kanban') {
    loadTaskColumns()
  }
})
</script>

<style scoped>
.task-management-view {
  @apply p-6 space-y-6;
}

.task-header {
  @apply flex items-center justify-between;
}

.header-left {
  @apply flex items-center space-x-6;
}

.page-title {
  @apply text-2xl font-bold text-gray-900 dark:text-gray-100;
}

.task-stats {
  @apply flex items-center space-x-4;
}

.stat-item {
  @apply flex flex-col items-center;
}

.stat-label {
  @apply text-sm text-gray-500 dark:text-gray-400;
}

.stat-value {
  @apply text-lg font-semibold text-gray-900 dark:text-gray-100;
}

.header-right {
  @apply flex items-center space-x-3;
}

.filter-panel {
  @apply bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4;
}

.filter-row {
  @apply grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4;
}

.filter-item {
  @apply space-y-2;
}

.filter-item label {
  @apply text-sm font-medium text-gray-700 dark:text-gray-300;
}

.task-board {
  @apply overflow-x-auto;
}

.board-columns {
  @apply flex space-x-4 min-w-max;
}

.board-column {
  @apply w-80 bg-gray-50 dark:bg-gray-800 rounded-lg p-4 space-y-4;
}

.column-header {
  @apply flex items-center justify-between;
}

.column-title {
  @apply font-semibold text-gray-900 dark:text-gray-100;
}

.task-count {
  @apply text-sm text-gray-500 dark:text-gray-400 bg-gray-200 dark:bg-gray-700 px-2 py-1 rounded;
}

.column-content {
  @apply space-y-3;
}

.task-list {
  @apply space-y-3 min-h-[200px];
}

.add-task-btn {
  @apply border-2 border-dashed border-gray-300 dark:border-gray-600 text-gray-600 dark:text-gray-400 hover:border-gray-400 dark:hover:border-gray-500;
}

.task-list-view {
  @apply space-y-4;
}

.list-header {
  @apply flex items-center justify-between;
}

.view-controls {
  @apply flex items-center space-x-2;
}

.sort-controls {
  @apply flex items-center space-x-2;
}

.task-table {
  @apply bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700;
}

.task-name-cell {
  @apply space-y-1;
}

.task-name {
  @apply font-medium text-gray-900 dark:text-gray-100 cursor-pointer hover:text-blue-600;
}

.task-name.has-subtasks {
  @apply text-blue-600 dark:text-blue-400;
}

.subtask-indicator {
  @apply text-xs;
}

.project-cell {
  @apply text-sm text-gray-600 dark:text-gray-400;
}

.assignee-cell {
  @apply flex items-center;
}

.deadline-cell {
  @apply text-sm text-gray-600 dark:text-gray-400;
}

.deadline-cell.overdue {
  @apply text-red-600 dark:text-red-400 font-medium;
}

.progress-cell {
  @apply flex items-center space-x-2;
}

.progress-text {
  @apply text-xs text-gray-600 dark:text-gray-400 min-w-[30px];
}

.action-buttons {
  @apply flex items-center space-x-1;
}

.pagination-wrapper {
  @apply flex justify-center;
}

.bulk-actions {
  @apply fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-4 flex items-center space-x-4;
}

.selected-info {
  @apply text-sm font-medium text-gray-900 dark:text-gray-100;
}

.bulk-buttons {
  @apply flex items-center space-x-2;
}

/* 表格行状态样式 */
.task-table tbody tr.selected {
  @apply bg-blue-50 dark:bg-blue-900/20;
}

.task-table tbody tr.overdue {
  @apply bg-red-50 dark:bg-red-900/20;
}

.task-table tbody tr.completed {
  @apply bg-green-50 dark:bg-green-900/20 opacity-75;
}

.task-table tbody tr.completed .task-name {
  @apply line-through text-gray-500 dark:text-gray-400;
}
</style>