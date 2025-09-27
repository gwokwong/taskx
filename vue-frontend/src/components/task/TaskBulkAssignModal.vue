<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="bulk-assign-modal max-w-md">
      <DialogHeader>
        <DialogTitle>{{ t('task.bulkAssign') }}</DialogTitle>
        <DialogDescription>
          {{ t('task.bulkAssignDescription', { count: taskIds.length }) }}
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-6">
        <!-- 当前分配情况概览 -->
        <div v-if="currentAssignments.length > 0" class="current-assignments">
          <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ t('task.currentAssignments') }}
          </h4>
          <div class="assignments-grid">
            <div
              v-for="assignment in currentAssignments"
              :key="assignment.taskId"
              class="assignment-item"
            >
              <span class="task-name">{{ assignment.taskName }}</span>
              <div class="assignees">
                <AvatarGroup :users="assignment.assignees" :max="3" size="sm" />
              </div>
            </div>
          </div>
        </div>

        <!-- 分配操作选择 -->
        <div class="assign-action">
          <label class="form-label">{{ t('task.assignAction') }}</label>
          <RadioGroup v-model:value="assignAction">
            <div class="radio-item">
              <RadioGroupItem value="replace" id="replace" />
              <label for="replace" class="radio-label">
                {{ t('task.replaceAssignees') }}
                <span class="radio-description">{{ t('task.replaceAssigneesDesc') }}</span>
              </label>
            </div>
            <div class="radio-item">
              <RadioGroupItem value="add" id="add" />
              <label for="add" class="radio-label">
                {{ t('task.addAssignees') }}
                <span class="radio-description">{{ t('task.addAssigneesDesc') }}</span>
              </label>
            </div>
            <div class="radio-item">
              <RadioGroupItem value="remove" id="remove" />
              <label for="remove" class="radio-label">
                {{ t('task.removeAssignees') }}
                <span class="radio-description">{{ t('task.removeAssigneesDesc') }}</span>
              </label>
            </div>
          </RadioGroup>
        </div>

        <!-- 用户选择 -->
        <div class="user-selection">
          <label class="form-label">
            {{
              assignAction === 'remove'
                ? t('task.selectUsersToRemove')
                : t('task.selectUsersToAssign')
            }}
          </label>

          <!-- 搜索框 -->
          <div class="search-box">
            <Search class="w-4 h-4 text-gray-400" />
            <Input
              v-model="searchQuery"
              :placeholder="t('user.searchUsers')"
              class="pl-10"
            />
          </div>

          <!-- 已选择的用户 -->
          <div v-if="selectedUsers.length > 0" class="selected-users">
            <div class="selected-header">
              <span class="selected-count">{{ t('user.selectedCount', { count: selectedUsers.length }) }}</span>
              <Button variant="ghost" size="sm" @click="clearSelection">
                {{ t('common.clearAll') }}
              </Button>
            </div>
            <div class="selected-list">
              <div
                v-for="user in selectedUsers"
                :key="user.id"
                class="selected-user"
              >
                <Avatar :user="user" size="sm" />
                <span class="user-name">{{ user.nickname }}</span>
                <span v-if="user.department" class="user-department">{{ user.department }}</span>
                <Button variant="ghost" size="sm" @click="removeUser(user)">
                  <X class="w-3 h-3" />
                </Button>
              </div>
            </div>
          </div>

          <!-- 用户列表 -->
          <div class="users-list">
            <div class="users-grid">
              <div
                v-for="user in filteredUsers"
                :key="user.id"
                class="user-item"
                :class="{ 'selected': isUserSelected(user) }"
                @click="toggleUser(user)"
              >
                <Checkbox
                  :checked="isUserSelected(user)"
                  @update:checked="toggleUser(user)"
                />
                <Avatar :user="user" size="sm" />
                <div class="user-info">
                  <span class="user-name">{{ user.nickname }}</span>
                  <span v-if="user.department" class="user-department">{{ user.department }}</span>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredUsers.length === 0" class="empty-state">
              <Users class="w-8 h-8 text-gray-400 mx-auto" />
              <p class="text-gray-500 dark:text-gray-400 text-center mt-2">
                {{ searchQuery ? t('user.noUsersFound') : t('user.noUsersAvailable') }}
              </p>
            </div>
          </div>
        </div>

        <!-- 预览变更 -->
        <div v-if="selectedUsers.length > 0" class="preview-changes">
          <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            {{ t('task.previewChanges') }}
          </h4>
          <div class="changes-summary">
            <div class="change-item">
              <span class="change-label">{{ t('task.affectedTasks') }}:</span>
              <span class="change-value">{{ taskIds.length }}</span>
            </div>
            <div class="change-item">
              <span class="change-label">
                {{
                  assignAction === 'remove'
                    ? t('task.usersToRemove')
                    : t('task.usersToAssign')
                }}:
              </span>
              <span class="change-value">{{ selectedUsers.length }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <Button
            variant="outline"
            @click="$emit('update:open', false)"
          >
            {{ t('common.cancel') }}
          </Button>
          <Button
            @click="handleAssign"
            :disabled="selectedUsers.length === 0 || isAssigning"
            :loading="isAssigning"
          >
            {{ isAssigning ? t('task.assigning') : getAssignButtonText() }}
          </Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { Search, X, Users } from 'lucide-vue-next'

import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Checkbox } from '@/components/ui/checkbox'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { Avatar } from '@/components/ui/avatar'
import AvatarGroup from '@/components/ui/AvatarGroup.vue'

import { taskApi } from '@/api'

interface TaskBulkAssignModalProps {
  open: boolean
  taskIds: number[]
  users: any[]
}

const props = defineProps<TaskBulkAssignModalProps>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  assigned: []
}>()

const { t } = useI18n()
const { toast } = useToast()

// 状态
const isAssigning = ref(false)
const assignAction = ref('replace') // replace, add, remove
const selectedUsers = ref([])
const searchQuery = ref('')
const currentAssignments = ref([])

// 计算属性
const filteredUsers = computed(() => {
  if (!searchQuery.value) return props.users

  const query = searchQuery.value.toLowerCase()
  return props.users.filter(user =>
    user.nickname.toLowerCase().includes(query) ||
    user.email?.toLowerCase().includes(query) ||
    user.department?.toLowerCase().includes(query)
  )
})

// 方法
const isUserSelected = (user: any): boolean => {
  return selectedUsers.value.some(u => u.id === user.id)
}

const toggleUser = (user: any) => {
  const index = selectedUsers.value.findIndex(u => u.id === user.id)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(user)
  }
}

const removeUser = (user: any) => {
  const index = selectedUsers.value.findIndex(u => u.id === user.id)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  }
}

const clearSelection = () => {
  selectedUsers.value = []
}

const getAssignButtonText = (): string => {
  switch (assignAction.value) {
    case 'replace':
      return t('task.replaceAssignees')
    case 'add':
      return t('task.addAssignees')
    case 'remove':
      return t('task.removeAssignees')
    default:
      return t('task.assign')
  }
}

const loadCurrentAssignments = async () => {
  try {
    const assignments = []

    for (const taskId of props.taskIds.slice(0, 5)) { // 只显示前5个任务的分配情况
      const task = await taskApi.getTask(taskId)
      if (task.data) {
        const assignees = getTaskAssignees(task.data)
        assignments.push({
          taskId: task.data.id,
          taskName: task.data.name,
          assignees
        })
      }
    }

    currentAssignments.value = assignments
  } catch (error) {
    console.error('Failed to load current assignments:', error)
  }
}

const getTaskAssignees = (task: any): any[] => {
  if (!task.owner || !props.users) return []

  try {
    const ownerIds = typeof task.owner === 'string'
      ? JSON.parse(task.owner)
      : [task.owner]
    return props.users.filter(user => ownerIds.includes(user.id))
  } catch {
    return []
  }
}

const handleAssign = async () => {
  if (selectedUsers.value.length === 0) return

  isAssigning.value = true

  try {
    const userIds = selectedUsers.value.map(user => user.id)

    // 根据操作类型调用不同的API
    switch (assignAction.value) {
      case 'replace':
        await taskApi.bulkAssignTasks(props.taskIds, userIds)
        break
      case 'add':
        await taskApi.bulkAddAssignees(props.taskIds, userIds)
        break
      case 'remove':
        await taskApi.bulkRemoveAssignees(props.taskIds, userIds)
        break
    }

    emit('assigned')

    toast({
      title: t('success.assigned'),
      description: t('task.bulkAssignSuccess', {
        action: getAssignButtonText(),
        tasks: props.taskIds.length,
        users: selectedUsers.value.length
      })
    })
  } catch (error) {
    toast({
      title: t('error.assignFailed'),
      description: error.message,
      variant: 'destructive'
    })
  } finally {
    isAssigning.value = false
  }
}

const resetForm = () => {
  assignAction.value = 'replace'
  selectedUsers.value = []
  searchQuery.value = ''
  currentAssignments.value = []
}

// 监听器
watch(() => props.open, (open) => {
  if (open && props.taskIds.length > 0) {
    loadCurrentAssignments()
  } else {
    resetForm()
  }
})

// 生命周期
onMounted(() => {
  if (props.open && props.taskIds.length > 0) {
    loadCurrentAssignments()
  }
})
</script>

<style scoped>
.bulk-assign-modal {
  @apply max-h-[90vh] overflow-hidden;
}

.current-assignments {
  @apply bg-gray-50 dark:bg-gray-800 rounded-lg p-4;
}

.assignments-grid {
  @apply space-y-2;
}

.assignment-item {
  @apply flex items-center justify-between p-2 bg-white dark:bg-gray-700 rounded border;
}

.task-name {
  @apply font-medium text-sm truncate flex-1 mr-2;
}

.assignees {
  @apply flex-shrink-0;
}

.assign-action {
  @apply space-y-3;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 dark:text-gray-300;
}

.radio-item {
  @apply flex items-start space-x-3;
}

.radio-label {
  @apply flex-1 cursor-pointer;
}

.radio-description {
  @apply block text-xs text-gray-500 dark:text-gray-400 mt-1;
}

.user-selection {
  @apply space-y-4;
}

.search-box {
  @apply relative;
}

.search-box .lucide-search {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2;
}

.selected-users {
  @apply space-y-3;
}

.selected-header {
  @apply flex items-center justify-between;
}

.selected-count {
  @apply text-sm font-medium text-gray-700 dark:text-gray-300;
}

.selected-list {
  @apply space-y-2;
}

.selected-user {
  @apply flex items-center space-x-3 p-2 bg-blue-50 dark:bg-blue-900 rounded border border-blue-200 dark:border-blue-800;
}

.user-name {
  @apply font-medium text-sm;
}

.user-department {
  @apply text-xs text-gray-500 dark:text-gray-400;
}

.users-list {
  @apply max-h-60 overflow-y-auto;
}

.users-grid {
  @apply space-y-1;
}

.user-item {
  @apply flex items-center space-x-3 p-2 hover:bg-gray-50 dark:hover:bg-gray-800 rounded cursor-pointer transition-colors;
}

.user-item.selected {
  @apply bg-blue-50 dark:bg-blue-900 border border-blue-200 dark:border-blue-800;
}

.user-info {
  @apply flex-1 min-w-0;
}

.user-info .user-name {
  @apply block truncate;
}

.user-info .user-department {
  @apply block truncate;
}

.empty-state {
  @apply py-8 text-center;
}

.preview-changes {
  @apply bg-gray-50 dark:bg-gray-800 rounded-lg p-4;
}

.changes-summary {
  @apply space-y-2;
}

.change-item {
  @apply flex justify-between text-sm;
}

.change-label {
  @apply text-gray-600 dark:text-gray-400;
}

.change-value {
  @apply font-medium text-gray-900 dark:text-gray-100;
}

.form-actions {
  @apply flex items-center justify-end space-x-3 pt-4 border-t border-gray-200 dark:border-gray-700;
}
</style>