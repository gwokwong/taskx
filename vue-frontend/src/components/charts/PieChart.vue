<template>
  <div class="relative">
    <canvas ref="chartRef"></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  Chart as ChartJS,
  ArcElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'

ChartJS.register(
  ArcElement,
  Title,
  Tooltip,
  Legend
)

interface Props {
  data: {
    labels: string[]
    datasets: {
      data: number[]
      backgroundColor?: string[]
      borderColor?: string[]
      borderWidth?: number
    }[]
  }
  options?: any
  type?: 'pie' | 'doughnut'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'doughnut'
})

const chartRef = ref<HTMLCanvasElement>()
let chart: ChartJS | null = null

const defaultOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const,
    },
    title: {
      display: false,
    },
  },
}

const createChart = () => {
  if (!chartRef.value) return

  const ctx = chartRef.value.getContext('2d')
  if (!ctx) return

  chart = new ChartJS(ctx, {
    type: props.type,
    data: props.data,
    options: {
      ...defaultOptions,
      ...props.options,
    },
  })
}

const updateChart = () => {
  if (!chart) return

  chart.data = props.data
  chart.update()
}

watch(
  () => props.data,
  () => {
    updateChart()
  },
  { deep: true }
)

onMounted(() => {
  createChart()
})

onUnmounted(() => {
  if (chart) {
    chart.destroy()
  }
})
</script>