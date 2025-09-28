<template>
  <Layout>
    <div class="task-templates-container">
      <div class="templates-header">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold">任务模板</h1>
          <div class="flex items-center space-x-4">
            <Button @click="showCreateDialog = true" class="bg-blue-600 hover:bg-blue-700">
              <Plus class="w-4 h-4 mr-2" />
              新建模板
            </Button>
            <div class="flex space-x-2">
              <Button variant="outline" :class="{ 'bg-blue-50': activeTab === 'all' }" @click="activeTab = 'all'">全部</Button>
              <Button variant="outline" :class="{ 'bg-blue-50': activeTab === 'my' }" @click="activeTab = 'my'">我的</Button>
              <Button variant="outline" :class="{ 'bg-blue-50': activeTab === 'public' }" @click="activeTab = 'public'">公开</Button>
              <Button variant="outline" :class="{ 'bg-blue-50': activeTab === 'popular' }" @click="activeTab = 'popular'">热门</Button>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center space-x-4">
            <Input
              v-model="searchKeyword"
              placeholder="搜索模板..."
              class="w-64"
              @input="searchTemplates"
            />
            <Search class="w-4 h-4 text-gray-400" />
            <Select v-model="selectedCategory" @update:modelValue="filterByCategory">
              <SelectTrigger class="w-40">
                <SelectValue placeholder="选择分类" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">全部分类</SelectItem>
                <SelectItem v-for="category in categories" :key="category" :value="category">
                  {{ category }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      <div class="templates-grid">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div
            v-for="template in filteredTemplates"
            :key="template.id"
            class="template-card bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow cursor-pointer"
            @click="viewTemplate(template)"
          >
            <div class="p-6">
              <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                  <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ template.name }}</h3>
                  <p class="text-sm text-gray-600 line-clamp-2">{{ template.description || '暂无描述' }}</p>
                </div>
                <div class="flex items-center space-x-2">
                  <Badge v-if="template.isPublic" variant="secondary">公开</Badge>
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button variant="ghost" size="sm" @click.stop>
                        <MoreVertical class="w-4 h-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent>
                      <DropdownMenuItem @click="useTemplate(template)">
                        <Play class="w-4 h-4 mr-2" />
                        使用模板
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="duplicateTemplate(template)">
                        <Copy class="w-4 h-4 mr-2" />
                        复制模板
                      </DropdownMenuItem>
                      <DropdownMenuItem v-if="canEdit(template)" @click="editTemplate(template)">
                        <Edit class="w-4 h-4 mr-2" />
                        编辑模板
                      </DropdownMenuItem>
                      <DropdownMenuItem v-if="canEdit(template)" @click="shareTemplate(template)">
                        <Share class="w-4 h-4 mr-2" />
                        {{ template.isPublic ? '取消分享' : '分享模板' }}
                      </DropdownMenuItem>
                      <DropdownSeparator v-if="canEdit(template)" />
                      <DropdownMenuItem v-if="canEdit(template)" class="text-red-600" @click="deleteTemplate(template)">
                        <Trash class="w-4 h-4 mr-2" />
                        删除模板
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </div>

              <div class="flex items-center justify-between text-sm text-gray-500">
                <div class="flex items-center space-x-4">
                  <span v-if="template.category">{{ template.category }}</span>
                  <span class="flex items-center">
                    <Users class="w-4 h-4 mr-1" />
                    {{ template.useCount || 0 }}
                  </span>
                </div>
                <span>{{ formatDate(template.updatedAt) }}</span>
              </div>

              <div v-if="template.tags" class="flex flex-wrap gap-2 mt-3">
                <Badge v-for="tag in getTemplateTags(template.tags)" :key="tag" variant="outline" class="text-xs">
                  {{ tag }}
                </Badge>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredTemplates.length === 0" class="text-center py-12">
          <FileText class="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 mb-2">暂无模板</h3>
          <p class="text-gray-600 mb-4">创建您的第一个任务模板</p>
          <Button @click="showCreateDialog = true">创建模板</Button>
        </div>
      </div>

      <!-- 创建/编辑模板对话框 -->
      <Dialog :open="showCreateDialog || showEditDialog" @update:open="closeDialog">
        <DialogContent class="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{{ editingTemplate ? '编辑模板' : '新建模板' }}</DialogTitle>
          </DialogHeader>
          <form @submit.prevent="saveTemplate" class="space-y-6">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium mb-2">模板名称</label>
                <Input v-model="templateForm.name" required />
              </div>
              <div>
                <label class="block text-sm font-medium mb-2">分类</label>
                <Select v-model="templateForm.category">
                  <SelectTrigger>
                    <SelectValue placeholder="选择分类" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="development">开发</SelectItem>
                    <SelectItem value="design">设计</SelectItem>
                    <SelectItem value="testing">测试</SelectItem>
                    <SelectItem value="marketing">营销</SelectItem>
                    <SelectItem value="management">管理</SelectItem>
                    <SelectItem value="custom">自定义</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium mb-2">描述</label>
              <Textarea v-model="templateForm.description" rows="3" />
            </div>

            <div>
              <label class="block text-sm font-medium mb-2">标签 (用逗号分隔)</label>
              <Input v-model="templateForm.tags" placeholder="例如：前端,Vue,项目管理" />
            </div>

            <div>
              <label class="block text-sm font-medium mb-2">模板配置</label>
              <Textarea v-model="templateForm.template" rows="8" placeholder="请输入JSON格式的模板配置" />
            </div>

            <div class="flex items-center space-x-2">
              <Checkbox v-model="templateForm.isPublic" id="isPublic" />
              <label for="isPublic" class="text-sm font-medium">公开模板</label>
            </div>

            <div class="flex justify-end space-x-2 pt-4">
              <Button type="button" variant="outline" @click="closeDialog">取消</Button>
              <Button type="submit" class="bg-blue-600 hover:bg-blue-700">保存</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <!-- 模板详情对话框 -->
      <Dialog :open="showViewDialog" @update:open="showViewDialog = false">
        <DialogContent class="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{{ viewingTemplate?.name }}</DialogTitle>
          </DialogHeader>
          <div v-if="viewingTemplate" class="space-y-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <Badge v-if="viewingTemplate.isPublic" variant="secondary">公开</Badge>
                <Badge variant="outline">{{ viewingTemplate.category }}</Badge>
                <span class="text-sm text-gray-500">使用次数: {{ viewingTemplate.useCount }}</span>
              </div>
              <div class="flex space-x-2">
                <Button @click="useTemplate(viewingTemplate)" class="bg-blue-600 hover:bg-blue-700">
                  <Play class="w-4 h-4 mr-2" />
                  使用模板
                </Button>
                <Button variant="outline" @click="duplicateTemplate(viewingTemplate)">
                  <Copy class="w-4 h-4 mr-2" />
                  复制
                </Button>
              </div>
            </div>

            <div>
              <h4 class="font-medium mb-2">描述</h4>
              <p class="text-gray-600">{{ viewingTemplate.description || '暂无描述' }}</p>
            </div>

            <div v-if="viewingTemplate.tags">
              <h4 class="font-medium mb-2">标签</h4>
              <div class="flex flex-wrap gap-2">
                <Badge v-for="tag in getTemplateTags(viewingTemplate.tags)" :key="tag" variant="outline">
                  {{ tag }}
                </Badge>
              </div>
            </div>

            <div>
              <h4 class="font-medium mb-2">模板配置</h4>
              <pre class="bg-gray-50 p-4 rounded-lg text-sm overflow-auto max-h-60">{{ formatTemplateConfig(viewingTemplate.template) }}</pre>
            </div>

            <div class="flex items-center justify-between text-sm text-gray-500 pt-4 border-t">
              <span>创建时间: {{ formatDate(viewingTemplate.createdAt) }}</span>
              <span>更新时间: {{ formatDate(viewingTemplate.updatedAt) }}</span>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  </Layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, Search, MoreVertical, Play, Copy, Edit, Share, Trash, Users, FileText } from 'lucide-vue-next'
import Layout from '@/components/Layout.vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Checkbox } from '@/components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { taskTemplateApi, type TaskTemplate } from '@/api/taskTemplate'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const activeTab = ref('all')
const searchKeyword = ref('')
const selectedCategory = ref('')
const templates = ref<TaskTemplate[]>([])
const categories = ref<string[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showViewDialog = ref(false)
const editingTemplate = ref<TaskTemplate | null>(null)
const viewingTemplate = ref<TaskTemplate | null>(null)

const templateForm = ref<TaskTemplate>({
  name: '',
  description: '',
  category: '',
  template: '',
  tags: '',
  isPublic: false
})

const filteredTemplates = computed(() => {
  let result = templates.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(template =>
      template.name.toLowerCase().includes(keyword) ||
      template.description?.toLowerCase().includes(keyword) ||
      template.tags?.toLowerCase().includes(keyword)
    )
  }

  if (selectedCategory.value) {
    result = result.filter(template => template.category === selectedCategory.value)
  }

  return result
})

function canEdit(template: TaskTemplate): boolean {
  return template.createdBy === authStore.user?.userid || authStore.user?.role === 'admin'
}

function getTemplateTags(tags?: string): string[] {
  return tags ? tags.split(',').map(tag => tag.trim()).filter(tag => tag) : []
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function formatTemplateConfig(template?: string): string {
  if (!template) return ''
  try {
    return JSON.stringify(JSON.parse(template), null, 2)
  } catch {
    return template
  }
}

function viewTemplate(template: TaskTemplate) {
  viewingTemplate.value = template
  showViewDialog.value = true
}

function editTemplate(template: TaskTemplate) {
  editingTemplate.value = template
  templateForm.value = { ...template }
  showEditDialog.value = true
}

function closeDialog() {
  showCreateDialog.value = false
  showEditDialog.value = false
  editingTemplate.value = null
  templateForm.value = {
    name: '',
    description: '',
    category: '',
    template: '',
    tags: '',
    isPublic: false
  }
}

async function saveTemplate() {
  try {
    if (editingTemplate.value) {
      await taskTemplateApi.updateTemplate(editingTemplate.value.id!, templateForm.value)
    } else {
      await taskTemplateApi.createTemplate(templateForm.value)
    }
    await loadTemplates()
    closeDialog()
  } catch (error) {
    console.error('保存模板失败:', error)
  }
}

async function useTemplate(template: TaskTemplate) {
  try {
    await taskTemplateApi.useTemplate(template.id!)
    template.useCount = (template.useCount || 0) + 1
    // TODO: 实际使用模板创建任务
    console.log('使用模板:', template)
  } catch (error) {
    console.error('使用模板失败:', error)
  }
}

async function duplicateTemplate(template: TaskTemplate) {
  try {
    await taskTemplateApi.duplicateTemplate(template.id!)
    await loadTemplates()
  } catch (error) {
    console.error('复制模板失败:', error)
  }
}

async function shareTemplate(template: TaskTemplate) {
  try {
    await taskTemplateApi.shareTemplate(template.id!, !template.isPublic)
    template.isPublic = !template.isPublic
  } catch (error) {
    console.error('分享模板失败:', error)
  }
}

async function deleteTemplate(template: TaskTemplate) {
  if (!confirm('确定要删除这个模板吗？')) return

  try {
    await taskTemplateApi.deleteTemplate(template.id!)
    await loadTemplates()
  } catch (error) {
    console.error('删除模板失败:', error)
  }
}

async function loadTemplates() {
  try {
    let response
    switch (activeTab.value) {
      case 'my':
        response = await taskTemplateApi.getMyTemplates()
        break
      case 'public':
        response = await taskTemplateApi.getPublicTemplates()
        break
      case 'popular':
        response = await taskTemplateApi.getPopularTemplates()
        break
      default:
        response = await taskTemplateApi.getAllTemplates()
    }
    templates.value = response.data
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

async function loadCategories() {
  try {
    const response = await taskTemplateApi.getTemplateCategories()
    categories.value = response.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

async function searchTemplates() {
  if (!searchKeyword.value.trim()) {
    await loadTemplates()
    return
  }

  try {
    const response = await taskTemplateApi.searchTemplates(searchKeyword.value)
    templates.value = response.data
  } catch (error) {
    console.error('搜索模板失败:', error)
  }
}

async function filterByCategory() {
  if (!selectedCategory.value) {
    await loadTemplates()
    return
  }

  try {
    const response = await taskTemplateApi.getTemplatesByCategory(selectedCategory.value)
    templates.value = response.data
  } catch (error) {
    console.error('按分类筛选失败:', error)
  }
}

watch(activeTab, loadTemplates)

onMounted(() => {
  loadTemplates()
  loadCategories()
})
</script>

<style scoped>
.task-templates-container {
  @apply p-6 bg-gray-50 min-h-screen;
}

.template-card:hover {
  @apply transform scale-105;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>