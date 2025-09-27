<template>
  <Layout>
    <div class="calendar-container">
      <div class="calendar-header">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold">日历</h1>
          <div class="flex items-center space-x-4">
            <Button @click="showCreateDialog = true" class="bg-blue-600 hover:bg-blue-700">
              <Plus class="w-4 h-4 mr-2" />
              新建事件
            </Button>
            <div class="flex space-x-2">
              <Button variant="outline" :class="{ 'bg-blue-50': viewMode === 'month' }" @click="viewMode = 'month'">月</Button>
              <Button variant="outline" :class="{ 'bg-blue-50': viewMode === 'week' }" @click="viewMode = 'week'">周</Button>
              <Button variant="outline" :class="{ 'bg-blue-50': viewMode === 'day' }" @click="viewMode = 'day'">日</Button>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center space-x-4">
            <Button variant="outline" @click="goToPrevious">
              <ChevronLeft class="w-4 h-4" />
            </Button>
            <Button variant="outline" @click="goToNext">
              <ChevronRight class="w-4 h-4" />
            </Button>
            <Button variant="outline" @click="goToToday">今天</Button>
            <h2 class="text-lg font-semibold">{{ currentDateText }}</h2>
          </div>
          <div class="flex items-center space-x-2">
            <Input
              v-model="searchKeyword"
              placeholder="搜索事件..."
              class="w-64"
              @input="searchEvents"
            />
            <Search class="w-4 h-4 text-gray-400" />
          </div>
        </div>
      </div>

      <div class="calendar-body">
        <!-- 月视图 -->
        <div v-if="viewMode === 'month'" class="month-view">
          <div class="grid grid-cols-7 gap-1 mb-2">
            <div v-for="day in weekDays" :key="day" class="text-center text-sm font-medium text-gray-500 py-2">
              {{ day }}
            </div>
          </div>
          <div class="grid grid-cols-7 gap-1">
            <div
              v-for="date in monthDates"
              :key="date.dateStr"
              :class="[
                'min-h-[120px] p-2 border border-gray-200 bg-white hover:bg-gray-50 cursor-pointer',
                { 'bg-gray-100': !date.isCurrentMonth },
                { 'bg-blue-50 border-blue-300': date.isToday }
              ]"
              @click="selectDate(date.date)"
            >
              <div class="text-sm font-medium mb-1" :class="{ 'text-gray-400': !date.isCurrentMonth }">
                {{ date.day }}
              </div>
              <div class="space-y-1">
                <div
                  v-for="event in getEventsForDate(date.date)"
                  :key="event.id"
                  :class="[
                    'text-xs p-1 rounded truncate cursor-pointer',
                    getEventColorClass(event.color)
                  ]"
                  @click.stop="editEvent(event)"
                >
                  {{ event.title }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 周视图 -->
        <div v-if="viewMode === 'week'" class="week-view">
          <div class="grid grid-cols-8 gap-1">
            <div class="text-center text-sm font-medium text-gray-500 py-2"></div>
            <div v-for="date in weekDates" :key="date.dateStr" class="text-center text-sm font-medium text-gray-500 py-2">
              <div>{{ date.dayName }}</div>
              <div :class="{ 'text-blue-600 font-bold': date.isToday }">{{ date.day }}</div>
            </div>
          </div>
          <div class="grid grid-cols-8 gap-1" style="grid-template-rows: repeat(24, 40px);">
            <div v-for="hour in 24" :key="hour" class="text-xs text-gray-500 text-right pr-2 py-1">
              {{ String(hour - 1).padStart(2, '0') }}:00
            </div>
            <div
              v-for="(date, dateIndex) in weekDates"
              :key="`${date.dateStr}-events`"
              class="col-start-2 border-l border-gray-200 relative"
              :style="{ gridColumn: dateIndex + 2, gridRow: '1 / -1' }"
            >
              <div
                v-for="event in getEventsForDate(date.date)"
                :key="event.id"
                :class="[
                  'absolute left-1 right-1 text-xs p-1 rounded cursor-pointer z-10',
                  getEventColorClass(event.color)
                ]"
                :style="getEventPosition(event)"
                @click="editEvent(event)"
              >
                {{ event.title }}
              </div>
            </div>
          </div>
        </div>

        <!-- 日视图 -->
        <div v-if="viewMode === 'day'" class="day-view">
          <div class="grid grid-cols-2 gap-1 mb-4">
            <div class="text-center font-medium">
              {{ currentDate.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}
            </div>
          </div>
          <div class="grid grid-cols-2 gap-1" style="grid-template-rows: repeat(24, 50px);">
            <div v-for="hour in 24" :key="hour" class="text-sm text-gray-500 text-right pr-4 py-2">
              {{ String(hour - 1).padStart(2, '0') }}:00
            </div>
            <div class="border-l border-gray-200 relative">
              <div
                v-for="event in todayEvents"
                :key="event.id"
                :class="[
                  'absolute left-2 right-2 text-sm p-2 rounded cursor-pointer z-10',
                  getEventColorClass(event.color)
                ]"
                :style="getEventPosition(event)"
                @click="editEvent(event)"
              >
                <div class="font-medium">{{ event.title }}</div>
                <div class="text-xs opacity-80">{{ formatEventTime(event) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建/编辑事件对话框 -->
      <Dialog :open="showCreateDialog || showEditDialog" @update:open="closeDialog">
        <DialogContent class="max-w-md">
          <DialogHeader>
            <DialogTitle>{{ editingEvent ? '编辑事件' : '新建事件' }}</DialogTitle>
          </DialogHeader>
          <form @submit.prevent="saveEvent" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">标题</label>
              <Input v-model="eventForm.title" required />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">描述</label>
              <Textarea v-model="eventForm.description" />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium mb-1">开始时间</label>
                <Input
                  v-model="eventForm.startTime"
                  type="datetime-local"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">结束时间</label>
                <Input
                  v-model="eventForm.endTime"
                  type="datetime-local"
                  required
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">位置</label>
              <Input v-model="eventForm.location" />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium mb-1">类型</label>
                <Select v-model="eventForm.type">
                  <SelectTrigger>
                    <SelectValue placeholder="选择类型" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="task">任务</SelectItem>
                    <SelectItem value="meeting">会议</SelectItem>
                    <SelectItem value="reminder">提醒</SelectItem>
                    <SelectItem value="birthday">生日</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">颜色</label>
                <Select v-model="eventForm.color">
                  <SelectTrigger>
                    <SelectValue placeholder="选择颜色" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="#3b82f6">蓝色</SelectItem>
                    <SelectItem value="#ef4444">红色</SelectItem>
                    <SelectItem value="#10b981">绿色</SelectItem>
                    <SelectItem value="#f59e0b">橙色</SelectItem>
                    <SelectItem value="#8b5cf6">紫色</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <Checkbox v-model="eventForm.allDay" id="allDay" />
              <label for="allDay" class="text-sm font-medium">全天事件</label>
            </div>
            <div class="flex justify-end space-x-2 pt-4">
              <Button type="button" variant="outline" @click="closeDialog">取消</Button>
              <Button type="submit" class="bg-blue-600 hover:bg-blue-700">保存</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, ChevronLeft, ChevronRight, Search } from 'lucide-vue-next'
import Layout from '@/components/Layout.vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Checkbox } from '@/components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { calendarApi } from '@/api/calendar'

interface CalendarEvent {
  id?: number
  title: string
  description?: string
  startTime: string
  endTime: string
  allDay?: boolean
  color?: string
  location?: string
  type?: string
  status?: string
}

const viewMode = ref<'month' | 'week' | 'day'>('month')
const currentDate = ref(new Date())
const events = ref<CalendarEvent[]>([])
const searchKeyword = ref('')
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingEvent = ref<CalendarEvent | null>(null)

const eventForm = ref<CalendarEvent>({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  allDay: false,
  color: '#3b82f6',
  location: '',
  type: 'task'
})

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

const currentDateText = computed(() => {
  const date = currentDate.value
  if (viewMode.value === 'month') {
    return `${date.getFullYear()}年${date.getMonth() + 1}月`
  } else if (viewMode.value === 'week') {
    const startOfWeek = new Date(date)
    startOfWeek.setDate(date.getDate() - date.getDay())
    const endOfWeek = new Date(startOfWeek)
    endOfWeek.setDate(startOfWeek.getDate() + 6)
    return `${startOfWeek.getMonth() + 1}月${startOfWeek.getDate()}日 - ${endOfWeek.getMonth() + 1}月${endOfWeek.getDate()}日`
  } else {
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
  }
})

const monthDates = computed(() => {
  const dates = []
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(firstDay.getDate() - firstDay.getDay())

  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)

    dates.push({
      date,
      day: date.getDate(),
      dateStr: date.toISOString().split('T')[0],
      isCurrentMonth: date.getMonth() === month,
      isToday: isToday(date)
    })
  }

  return dates
})

const weekDates = computed(() => {
  const dates = []
  const startOfWeek = new Date(currentDate.value)
  startOfWeek.setDate(currentDate.value.getDate() - currentDate.value.getDay())

  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)

    dates.push({
      date,
      day: date.getDate(),
      dayName: weekDays[i],
      dateStr: date.toISOString().split('T')[0],
      isToday: isToday(date)
    })
  }

  return dates
})

const todayEvents = computed(() => {
  return getEventsForDate(currentDate.value)
})

function isToday(date: Date): boolean {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

function getEventsForDate(date: Date): CalendarEvent[] {
  const dateStr = date.toISOString().split('T')[0]
  return events.value.filter(event => {
    const eventDate = new Date(event.startTime).toISOString().split('T')[0]
    return eventDate === dateStr
  })
}

function getEventColorClass(color?: string): string {
  const colorMap: Record<string, string> = {
    '#3b82f6': 'bg-blue-100 text-blue-800 border-blue-200',
    '#ef4444': 'bg-red-100 text-red-800 border-red-200',
    '#10b981': 'bg-green-100 text-green-800 border-green-200',
    '#f59e0b': 'bg-yellow-100 text-yellow-800 border-yellow-200',
    '#8b5cf6': 'bg-purple-100 text-purple-800 border-purple-200'
  }
  return colorMap[color || '#3b82f6'] || 'bg-gray-100 text-gray-800 border-gray-200'
}

function getEventPosition(event: CalendarEvent): Record<string, string> {
  const startTime = new Date(event.startTime)
  const endTime = new Date(event.endTime)

  const startHour = startTime.getHours() + startTime.getMinutes() / 60
  const endHour = endTime.getHours() + endTime.getMinutes() / 60
  const duration = endHour - startHour

  return {
    top: `${startHour * (viewMode.value === 'day' ? 50 : 40)}px`,
    height: `${duration * (viewMode.value === 'day' ? 50 : 40)}px`
  }
}

function formatEventTime(event: CalendarEvent): string {
  const start = new Date(event.startTime)
  const end = new Date(event.endTime)
  return `${start.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })} - ${end.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
}

function goToPrevious() {
  const date = new Date(currentDate.value)
  if (viewMode.value === 'month') {
    date.setMonth(date.getMonth() - 1)
  } else if (viewMode.value === 'week') {
    date.setDate(date.getDate() - 7)
  } else {
    date.setDate(date.getDate() - 1)
  }
  currentDate.value = date
}

function goToNext() {
  const date = new Date(currentDate.value)
  if (viewMode.value === 'month') {
    date.setMonth(date.getMonth() + 1)
  } else if (viewMode.value === 'week') {
    date.setDate(date.getDate() + 7)
  } else {
    date.setDate(date.getDate() + 1)
  }
  currentDate.value = date
}

function goToToday() {
  currentDate.value = new Date()
}

function selectDate(date: Date) {
  currentDate.value = date
  if (viewMode.value !== 'day') {
    viewMode.value = 'day'
  }
}

function editEvent(event: CalendarEvent) {
  editingEvent.value = event
  eventForm.value = { ...event }
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  editingEvent.value = null
  eventForm.value = {
    title: '',
    description: '',
    startTime: '',
    endTime: '',
    allDay: false,
    color: '#3b82f6',
    location: '',
    type: 'task'
  }
}

async function saveEvent() {
  try {
    if (editingEvent.value) {
      await calendarApi.updateEvent(editingEvent.value.id!, eventForm.value)
    } else {
      await calendarApi.createEvent(eventForm.value)
    }
    await loadEvents()
    closeDialog()
  } catch (error) {
    console.error('保存事件失败:', error)
  }
}

async function loadEvents() {
  try {
    const start = new Date(currentDate.value)
    start.setDate(1)
    start.setMonth(start.getMonth() - 1)

    const end = new Date(currentDate.value)
    end.setDate(0)
    end.setMonth(end.getMonth() + 2)

    const response = await calendarApi.getEventsByRange(start.toISOString(), end.toISOString())
    events.value = response.data
  } catch (error) {
    console.error('加载事件失败:', error)
  }
}

async function searchEvents() {
  if (!searchKeyword.value.trim()) {
    await loadEvents()
    return
  }

  try {
    const response = await calendarApi.searchEvents(searchKeyword.value)
    events.value = response.data
  } catch (error) {
    console.error('搜索事件失败:', error)
  }
}

watch([currentDate, viewMode], loadEvents)

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.calendar-container {
  @apply p-6 bg-white rounded-lg shadow;
}

.month-view .grid > div {
  min-height: 120px;
}

.week-view, .day-view {
  overflow-x: auto;
}
</style>