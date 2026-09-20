<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>报表中心</span>
        <el-button type="primary" size="small" @click="exportCSV">导出 CSV</el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="运营概览" name="overview">
        <el-row :gutter="20" style="margin-bottom:20px">
          <el-col :span="4"><el-statistic title="累计职位" :value="overview.totalJobs" /></el-col>
          <el-col :span="4"><el-statistic title="累计报名" :value="overview.totalApplications" /></el-col>
          <el-col :span="4"><el-statistic title="累计工人" :value="overview.totalWorkers" /></el-col>
          <el-col :span="4"><el-statistic title="累计企业" :value="overview.totalEnterprises" /></el-col>
          <el-col :span="4"><el-statistic title="今日新增职位" :value="overview.todayNewJobs" /></el-col>
          <el-col :span="4"><el-statistic title="今日新增报名" :value="overview.todayNewApplications" /></el-col>
        </el-row>
        <el-row :gutter="20" style="margin-bottom:20px">
          <el-col :span="6"><el-statistic title="今日完成考勤" :value="overview.todayCompletedAttendance" /></el-col>
          <el-col :span="6"><el-statistic title="今日结算金额" :value="overview.todaySettlementAmount" prefix="¥" precision="2" /></el-col>
          <el-col :span="6"><el-statistic title="累计结算金额" :value="overview.totalSettlementAmount" prefix="¥" precision="2" /></el-col>
          <el-col :span="6"><el-statistic title="累计服务费" :value="overview.totalServiceFee" prefix="¥" precision="2" /></el-col>
        </el-row>
        <v-chart class="chart" :option="overviewTrendOption" autoresize />
      </el-tab-pane>

      <el-tab-pane label="企业分析" name="enterprise">
        <v-chart class="chart" :option="enterpriseTrendOption" autoresize style="margin-bottom:20px" />
        <el-table :data="enterpriseRanking" stripe style="width:100%">
          <el-table-column prop="rank" label="排名" width="70" />
          <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
          <el-table-column prop="jobCount" label="发布职位数" width="120" />
          <el-table-column prop="applicationCount" label="报名总数" width="120" />
          <el-table-column prop="totalSettlement" label="累计结算" width="130">
            <template #default="{ row }">¥{{ row.totalSettlement }}</template>
          </el-table-column>
          <el-table-column prop="activityLevel" label="活跃度" width="100">
            <template #default="{ row }">
              <el-tag :type="row.activityLevel === 'HIGH' ? 'success' : row.activityLevel === 'MEDIUM' ? 'warning' : 'info'">
                {{ row.activityLevel === 'HIGH' ? '高' : row.activityLevel === 'MEDIUM' ? '中' : '低' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="工人分析" name="worker">
        <v-chart class="chart" :option="workerTrendOption" autoresize style="margin-bottom:20px" />
        <el-table :data="workerRanking" stripe style="width:100%">
          <el-table-column prop="rank" label="排名" width="70" />
          <el-table-column prop="workerName" label="工人姓名" width="120" />
          <el-table-column prop="workerPhone" label="联系方式" width="130" />
          <el-table-column prop="completedJobs" label="完成工作数" width="120" />
          <el-table-column prop="totalEarnings" label="累计收入" width="130">
            <template #default="{ row }">¥{{ row.totalEarnings }}</template>
          </el-table-column>
          <el-table-column prop="activityLevel" label="活跃度" width="100">
            <template #default="{ row }">
              <el-tag :type="row.activityLevel === 'HIGH' ? 'success' : row.activityLevel === 'MEDIUM' ? 'warning' : 'info'">
                {{ row.activityLevel === 'HIGH' ? '高' : row.activityLevel === 'MEDIUM' ? '中' : '低' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="供需分析" name="supplyDemand">
        <el-row :gutter="20" style="margin-bottom:20px">
          <el-col :span="8"><el-statistic title="总需求(招聘人数)" :value="supplyDemand.totalDemand" /></el-col>
          <el-col :span="8"><el-statistic title="总供给(报名人数)" :value="supplyDemand.totalSupply" /></el-col>
          <el-col :span="8"><el-statistic title="匹配率" :value="supplyDemand.matchingRate" suffix="%" /></el-col>
        </el-row>
        <v-chart class="chart" :option="supplyDemandOption" autoresize style="margin-bottom:20px" />
        <el-table :data="supplyDemand.categories" stripe style="width:100%">
          <el-table-column prop="category" label="职位分类" width="200" />
          <el-table-column prop="demand" label="招聘人数" width="120" />
          <el-table-column prop="supply" label="报名人数" width="120" />
          <el-table-column label="供需比" width="120">
            <template #default="{ row }">{{ row.demand > 0 ? (row.supply / row.demand).toFixed(2) : '-' }}:1</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="转化漏斗" name="conversion">
        <v-chart class="chart" :option="funnelOption" autoresize />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, FunnelChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import {
  getReportOverview, getEnterpriseActivity, getWorkerActivity,
  getEnterpriseRanking, getWorkerRanking,
  getSupplyDemandAnalysis, getConversionFunnel
} from '../../api/reports'

use([CanvasRenderer, LineChart, BarChart, FunnelChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const activeTab = ref('overview')
const overview = ref({})
const enterpriseTrend = ref([])
const workerTrend = ref([])
const enterpriseRanking = ref([])
const workerRanking = ref([])
const supplyDemand = ref({ categories: [] })
const funnel = ref({ steps: [] })

async function fetchAll() {
  try { overview.value = await getReportOverview({}) } catch {}
  try { enterpriseTrend.value = (await getEnterpriseActivity({})) || [] } catch {}
  try { workerTrend.value = (await getWorkerActivity({})) || [] } catch {}
  try { enterpriseRanking.value = (await getEnterpriseRanking({})) || [] } catch {}
  try { workerRanking.value = (await getWorkerRanking({})) || [] } catch {}
  try { supplyDemand.value = (await getSupplyDemandAnalysis({})) || { categories: [] } } catch {}
  try { funnel.value = (await getConversionFunnel({})) || { steps: [] } } catch {}
}

const overviewTrendOption = computed(() => ({
  title: { text: '近7日运营趋势' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['新增企业', '活跃企业', '发布职位', '新增工人', '活跃工人', '报名数'] },
  grid: { left: 40, right: 20, bottom: 40 },
  xAxis: { type: 'category', data: enterpriseTrend.value.map(i => i.date) },
  yAxis: { type: 'value' },
  series: [
    { name: '新增企业', type: 'line', smooth: true, data: enterpriseTrend.value.map(i => i.newEnterprises) },
    { name: '活跃企业', type: 'line', smooth: true, data: enterpriseTrend.value.map(i => i.activeEnterprises) },
    { name: '发布职位', type: 'line', smooth: true, data: enterpriseTrend.value.map(i => i.publishedJobs) },
    { name: '新增工人', type: 'line', smooth: true, data: workerTrend.value.map(i => i.newWorkers) },
    { name: '活跃工人', type: 'line', smooth: true, data: workerTrend.value.map(i => i.activeWorkers) },
    { name: '报名数', type: 'line', smooth: true, data: workerTrend.value.map(i => i.applications) }
  ]
}))

const enterpriseTrendOption = computed(() => ({
  title: { text: '近7日企业活跃度' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['新增企业', '活跃企业', '发布职位'] },
  grid: { left: 40, right: 20, bottom: 40 },
  xAxis: { type: 'category', data: enterpriseTrend.value.map(i => i.date) },
  yAxis: { type: 'value' },
  series: [
    { name: '新增企业', type: 'bar', stack: 'a', data: enterpriseTrend.value.map(i => i.newEnterprises) },
    { name: '活跃企业', type: 'line', smooth: true, data: enterpriseTrend.value.map(i => i.activeEnterprises) },
    { name: '发布职位', type: 'line', smooth: true, data: enterpriseTrend.value.map(i => i.publishedJobs) }
  ]
}))

const workerTrendOption = computed(() => ({
  title: { text: '近7日工人活跃度' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['新增工人', '活跃工人', '报名数'] },
  grid: { left: 40, right: 20, bottom: 40 },
  xAxis: { type: 'category', data: workerTrend.value.map(i => i.date) },
  yAxis: { type: 'value' },
  series: [
    { name: '新增工人', type: 'bar', data: workerTrend.value.map(i => i.newWorkers) },
    { name: '活跃工人', type: 'line', smooth: true, data: workerTrend.value.map(i => i.activeWorkers) },
    { name: '报名数', type: 'line', smooth: true, data: workerTrend.value.map(i => i.applications) }
  ]
}))

const supplyDemandOption = computed(() => ({
  title: { text: '分类供需对比' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['招聘人数', '报名人数'] },
  grid: { left: 40, right: 20, bottom: 60 },
  xAxis: { type: 'category', data: (supplyDemand.value.categories || []).map(i => i.category), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value' },
  series: [
    { name: '招聘人数', type: 'bar', data: (supplyDemand.value.categories || []).map(i => i.demand) },
    { name: '报名人数', type: 'bar', data: (supplyDemand.value.categories || []).map(i => i.supply) }
  ]
}))

const funnelOption = computed(() => ({
  title: { text: '转化漏斗' },
  tooltip: { trigger: 'item', formatter: '{b}: {c}' },
  series: [{
    type: 'funnel',
    left: '10%',
    width: '80%',
    label: { formatter: '{b}: {c}' },
    data: (funnel.value.steps || []).map(s => ({ name: s.name, value: s.count }))
  }]
}))

function exportCSV() {
  const rows = [['指标', '值']]
  rows.push(['累计职位', overview.value.totalJobs ?? 0])
  rows.push(['累计报名', overview.value.totalApplications ?? 0])
  rows.push(['累计工人', overview.value.totalWorkers ?? 0])
  rows.push(['累计企业', overview.value.totalEnterprises ?? 0])
  rows.push(['今日新增职位', overview.value.todayNewJobs ?? 0])
  rows.push(['今日新增报名', overview.value.todayNewApplications ?? 0])
  rows.push(['今日完成考勤', overview.value.todayCompletedAttendance ?? 0])
  rows.push(['今日结算金额', overview.value.todaySettlementAmount ?? 0])
  rows.push(['累计结算金额', overview.value.totalSettlementAmount ?? 0])
  rows.push(['累计服务费', overview.value.totalServiceFee ?? 0])
  rows.push([])
  rows.push(['漏斗步骤', '数量'])
  ;(funnel.value.steps || []).forEach(s => rows.push([s.name, s.count]))
  rows.push([])
  rows.push(['分类', '招聘人数', '报名人数'])
  ;(supplyDemand.value.categories || []).forEach(c => rows.push([c.category, c.demand, c.supply]))

  const csv = rows.map(r => r.join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `报表中心_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(() => { fetchAll() })
</script>

<style scoped>
.chart {
  height: 360px;
  width: 100%;
}
</style>
