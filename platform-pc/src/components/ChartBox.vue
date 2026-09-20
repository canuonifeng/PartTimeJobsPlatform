<template>
  <v-chart :option="chartOption" :style="{ height }" autoresize />
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart, FunnelChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, PieChart, BarChart, FunnelChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps({
  type: { type: String, default: 'line' },
  title: { type: String, default: '' },
  labels: { type: Array, default: () => [] },
  series: { type: Array, default: () => [] },
  height: { type: String, default: '400px' }
})

const option = ref(null)

const chartOption = computed(() => buildOption())

function buildOption() {
  const base = {
    title: props.title ? { text: props.title, left: 'center' } : undefined,
    tooltip: { trigger: props.type === 'pie' || props.type === 'funnel' ? 'item' : 'axis' },
    legend: { bottom: 0 }
  }
  if (props.type === 'pie') {
    return {
      ...base,
      series: [{ type: 'pie', radius: '60%', data: buildPieData() }]
    }
  }
  if (props.type === 'funnel') {
    return {
      ...base,
      series: [{ type: 'funnel', left: '10%', width: '80%', data: buildPieData() }]
    }
  }
  return {
    ...base,
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: props.type === 'bar', data: props.labels },
    yAxis: { type: 'value' },
    series: props.series.map((s) => ({
      name: s.name,
      type: props.type,
      smooth: props.type === 'line',
      data: s.data
    }))
  }
}

function buildPieData() {
  if (!props.series.length) return []
  const s = props.series[0]
  const arr = s.data || []
  return arr.map((v, i) => ({ name: props.labels[i] || String(i), value: v }))
}

defineExpose({})
</script>
