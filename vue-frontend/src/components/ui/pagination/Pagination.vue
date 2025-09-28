<template>
  <div class="pagination">
    <Button
      variant="outline"
      size="sm"
      :disabled="currentPage === 1"
      @click="goToPage(currentPage - 1)"
    >
      <ChevronLeft class="w-4 h-4" />
      {{ t('pagination.previous') }}
    </Button>

    <div class="page-numbers">
      <Button
        v-for="page in visiblePages"
        :key="page"
        :variant="page === currentPage ? 'default' : 'outline'"
        size="sm"
        @click="goToPage(page)"
      >
        {{ page }}
      </Button>
    </div>

    <Button
      variant="outline"
      size="sm"
      :disabled="currentPage === totalPages"
      @click="goToPage(currentPage + 1)"
    >
      {{ t('pagination.next') }}
      <ChevronRight class="w-4 h-4" />
    </Button>

    <div class="page-info">
      {{ t('pagination.showing', {
        start: (currentPage - 1) * pageSize + 1,
        end: Math.min(currentPage * pageSize, total),
        total
      }) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

interface Props {
  currentPage: number
  totalPages: number
  total: number
  pageSize: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:currentPage': [page: number]
}>()

const { t } = useI18n()

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, props.currentPage - 2)
  const end = Math.min(props.totalPages, props.currentPage + 2)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const goToPage = (page: number) => {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('update:currentPage', page)
  }
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-info {
  margin-left: auto;
  font-size: 0.875rem;
  color: #6b7280;
}
</style>