<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="hover" class="chart-card">
      <template #header><span>Trend</span></template>
      <v-chart :option="chartOption" style="height:400px" autoresize />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getDashboardStats } from '../api/dashboard'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const statCards = ref([
  { label: 'Companies', value: 0 },
  { label: 'Jobs', value: 0 },
  { label: 'Workers', value: 0 },
  { label: 'Shifts', value: 0 },
  { label: 'Revenue', value: '¥0' }
])

const chartOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['Jobs', 'Workers'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [] },
  yAxis: { type: 'value' },
  series: [
    { name: 'Jobs', type: 'line', smooth: true, data: [] },
    { name: 'Workers', type: 'line', smooth: true, data: [] }
  ]
})

onMounted(async () => {
  try {
    const data = await getDashboardStats()
    if (data) {
      statCards.value = [
        { label: 'Companies', value: data.totalCompanies ?? 0 },
        { label: 'Jobs', value: data.totalJobs ?? 0 },
        { label: 'Workers', value: data.totalWorkers ?? 0 },
        { label: 'Shifts', value: data.totalShifts ?? 0 },
        { label: 'Revenue', value: `¥${data.totalRevenue ?? 0}` }
      ]
      if (data.trendLabels) {
        chartOption.value.xAxis.data = data.trendLabels
        chartOption.value.series[0].data = data.jobTrend ?? []
        chartOption.value.series[1].data = data.workerTrend ?? []
      }
    }
  } catch {
    // handled by interceptor
  }
})
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-card { margin-bottom: 20px; text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }
.chart-card { margin-top: 10px; }
</style>
