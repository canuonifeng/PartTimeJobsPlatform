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
      <template #header><span>趋势</span></template>
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
  { label: '企业数', value: 0 },
  { label: '岗位数', value: 0 },
  { label: '工人数', value: 0 },
  { label: '班次数', value: 0 },
  { label: '营收', value: '¥0' }
])

const chartOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['岗位', '工人'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [] },
  yAxis: { type: 'value' },
  series: [
    { name: '岗位', type: 'line', smooth: true, data: [] },
    { name: '工人', type: 'line', smooth: true, data: [] }
  ]
})

onMounted(async () => {
  try {
    const data = await getDashboardStats()
    if (data) {
      statCards.value = [
        { label: '企业数', value: data.totalCompanies ?? 0 },
        { label: '岗位数', value: data.totalJobs ?? 0 },
        { label: '工人数', value: data.totalWorkers ?? 0 },
        { label: '班次数', value: data.totalShifts ?? 0 },
        { label: '营收', value: `¥${data.totalRevenue ?? 0}` }
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
