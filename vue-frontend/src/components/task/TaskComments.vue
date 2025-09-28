<template>
  <div class="task-comments">
    <!-- 评论列表 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载评论中...</p>
    </div>

    <div v-else-if="comments.length === 0" class="empty-state">
      <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
      </svg>
      <p>暂无评论</p>
    </div>

    <div v-else class="comments-list">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <!-- 主评论 -->
        <div class="comment-main">
          <Avatar class="comment-avatar">
            <img
              v-if="comment.user?.user_img"
              :src="comment.user.user_img"
              :alt="comment.user.nickname"
            />
            <span v-else>{{ comment.user?.nickname?.charAt(0) || 'U' }}</span>
          </Avatar>

          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-author">{{ comment.user?.nickname || '未知用户' }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>

            <div class="comment-text" v-html="formatCommentContent(comment.content)"></div>

            <!-- 评论操作 -->
            <div class="comment-actions">
              <Button
                @click="toggleReplyForm(comment.id)"
                size="sm"
                variant="ghost"
                class="action-btn"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"/>
                </svg>
                回复
              </Button>

              <Button
                v-if="canEditComment(comment)"
                @click="editComment(comment)"
                size="sm"
                variant="ghost"
                class="action-btn"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
                编辑
              </Button>

              <Button
                v-if="canDeleteComment(comment)"
                @click="deleteComment(comment)"
                size="sm"
                variant="ghost"
                class="action-btn text-red-600"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
                删除
              </Button>
            </div>

            <!-- 回复表单 -->
            <div v-if="showReplyForm === comment.id" class="reply-form">
              <CommentForm
                :parent-id="comment.id"
                :task-id="taskId"
                :placeholder="`回复 ${comment.user?.nickname}...`"
                @submit="handleReplySubmit"
                @cancel="showReplyForm = null"
              />
            </div>
          </div>
        </div>

        <!-- 回复列表 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
          <div
            v-for="reply in comment.replies"
            :key="reply.id"
            class="reply-item"
          >
            <Avatar class="reply-avatar">
              <img
                v-if="reply.user?.user_img"
                :src="reply.user.user_img"
                :alt="reply.user.nickname"
              />
              <span v-else>{{ reply.user?.nickname?.charAt(0) || 'U' }}</span>
            </Avatar>

            <div class="reply-content">
              <div class="reply-header">
                <span class="reply-author">{{ reply.user?.nickname || '未知用户' }}</span>
                <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
              </div>

              <div class="reply-text" v-html="formatCommentContent(reply.content)"></div>

              <div class="reply-actions">
                <Button
                  v-if="canEditComment(reply)"
                  @click="editComment(reply)"
                  size="sm"
                  variant="ghost"
                  class="action-btn"
                >
                  编辑
                </Button>

                <Button
                  v-if="canDeleteComment(reply)"
                  @click="deleteComment(reply)"
                  size="sm"
                  variant="ghost"
                  class="action-btn text-red-600"
                >
                  删除
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加评论表单 -->
    <div class="add-comment-section">
      <h4>添加评论</h4>
      <CommentForm
        :task-id="taskId"
        placeholder="写下你的评论..."
        @submit="handleCommentSubmit"
      />
    </div>

    <!-- 编辑评论模态框 -->
    <Dialog :open="!!editingComment" @update:open="editingComment = null">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>编辑评论</DialogTitle>
        </DialogHeader>
        <CommentForm
          v-if="editingComment"
          :task-id="taskId"
          :initial-content="editingComment.content"
          :edit-mode="true"
          @submit="handleEditSubmit"
          @cancel="editingComment = null"
        />
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineProps, defineEmits } from 'vue'
import { Button } from '@/components/ui/button'
import { Avatar } from '@/components/ui/avatar'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import CommentForm from './CommentForm.vue'
import { taskApi, type TaskComment } from '@/api/task'
import { useAuthStore } from '@/stores/auth'

interface Props {
  taskId: number
}

interface Emits {
  (e: 'comment-updated'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const authStore = useAuthStore()

const comments = ref<TaskComment[]>([])
const loading = ref(false)
const showReplyForm = ref<number | null>(null)
const editingComment = ref<TaskComment | null>(null)

onMounted(() => {
  loadComments()
})

const loadComments = async () => {
  loading.value = true
  try {
    const response = await taskApi.getTaskComments(props.taskId)
    comments.value = response.data
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCommentSubmit = async () => {
  await loadComments()
  emit('comment-updated')
}

const handleReplySubmit = async () => {
  showReplyForm.value = null
  await loadComments()
  emit('comment-updated')
}

const handleEditSubmit = async (content: string) => {
  if (!editingComment.value) return

  try {
    await taskApi.updateTaskComment(props.taskId, editingComment.value.id, { content })
    editingComment.value = null
    await loadComments()
    emit('comment-updated')
  } catch (error) {
    console.error('更新评论失败:', error)
  }
}

const toggleReplyForm = (commentId: number) => {
  showReplyForm.value = showReplyForm.value === commentId ? null : commentId
}

const editComment = (comment: TaskComment) => {
  editingComment.value = comment
}

const deleteComment = async (comment: TaskComment) => {
  if (confirm('确定要删除这条评论吗？')) {
    try {
      await taskApi.deleteTaskComment(props.taskId, comment.id)
      await loadComments()
      emit('comment-updated')
    } catch (error) {
      console.error('删除评论失败:', error)
    }
  }
}

const canEditComment = (comment: TaskComment): boolean => {
  return authStore.currentUser?.userid === comment.userId
}

const canDeleteComment = (comment: TaskComment): boolean => {
  return authStore.currentUser?.userid === comment.userId ||
         authStore.currentUser?.role === 'admin'
}

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN')
}

const formatCommentContent = (content: string): string => {
  // 处理换行
  let formatted = content.replace(/\n/g, '<br>')

  // 处理@提及（如果需要）
  formatted = formatted.replace(/@(\w+)/g, '<span class="mention">@$1</span>')

  // 处理链接
  const urlRegex = /(https?:\/\/[^\s]+)/g
  formatted = formatted.replace(urlRegex, '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>')

  return formatted
}
</script>

<style scoped>
.task-comments {
  width: 100%;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #6b7280;
}

.loading-spinner {
  width: 2rem;
  height: 2rem;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  width: 3rem;
  height: 3rem;
  margin-bottom: 1rem;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.comment-item {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 1.5rem;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-main {
  display: flex;
  gap: 0.75rem;
}

.comment-avatar {
  flex-shrink: 0;
  width: 2.5rem;
  height: 2.5rem;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.comment-author {
  font-weight: 500;
  color: #374151;
}

.comment-time {
  font-size: 0.875rem;
  color: #6b7280;
}

.comment-text {
  color: #374151;
  line-height: 1.6;
  margin-bottom: 0.75rem;
  word-wrap: break-word;
}

.comment-text :deep(.mention) {
  color: #3b82f6;
  font-weight: 500;
}

.comment-text :deep(a) {
  color: #3b82f6;
  text-decoration: underline;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.action-btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.reply-form {
  margin-top: 1rem;
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 6px;
}

.replies-list {
  margin-top: 1rem;
  margin-left: 3.25rem;
  padding-left: 1rem;
  border-left: 2px solid #e5e7eb;
}

.reply-item {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar {
  flex-shrink: 0;
  width: 2rem;
  height: 2rem;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.reply-author {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.reply-time {
  font-size: 0.75rem;
  color: #6b7280;
}

.reply-text {
  color: #374151;
  line-height: 1.6;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  word-wrap: break-word;
}

.reply-actions {
  display: flex;
  gap: 0.5rem;
}

.add-comment-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.add-comment-section h4 {
  margin: 0 0 1rem 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
}
</style>