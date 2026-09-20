<template>
  <el-card>
    <template #header>
      <span>财务对账</span>
    </template>

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6">
        <el-statistic title="今日企业充值总额" :value="stats.todayTopUp" precision="2" prefix="¥" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日工人提现总额" :value="stats.todayWithdrawal" precision="2" prefix="¥" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日平台服务费收入" :value="stats.todayServiceFee" precision="2" prefix="¥" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日结算笔数" :value="stats.todaySettlementCount" />
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="每日对账" name="daily">
        <el-table :data="dailyRecords" v-loading="loading" stripe style="width:100%">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="totalTopUp" label="充值总额" width="120">
            <template #default="{ row }">¥{{ row.totalTopUp }}</template>
          </el-table-column>
          <el-table-column prop="totalWithdrawal" label="提现总额" width="120">
            <template #default="{ row }">¥{{ row.totalWithdrawal }}</template>
          </el-table-column>
          <el-table-column prop="totalServiceFee" label="服务费收入" width="120">
            <template #default="{ row }">¥{{ row.totalServiceFee }}</template>
          </el-table-column>
          <el-table-column prop="settlementCount" label="结算笔数" width="100" />
          <el-table-column prop="status" label="对账状态" width="100">
            <template #default="{ row }">
              <el-tag type="success">已对账</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="handleViewDetail(row)">查看详情</el-button>
              <el-button type="primary" size="small" text @click="handleExport(row)">导出</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="服务费统计" name="serviceFee">
        <el-row :gutter="20" style="margin-bottom:16px">
          <el-col :span="6">
            <el-statistic title="累计服务费" :value="feeSummary.totalServiceFee" precision="2" prefix="¥" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="今日服务费" :value="feeSummary.todayServiceFee" precision="2" prefix="¥" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="本周服务费" :value="feeSummary.weekServiceFee" precision="2" prefix="¥" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="本月服务费" :value="feeSummary.monthServiceFee" precision="2" prefix="¥" />
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="14">
            <ChartBox type="line" title="近7日资金趋势" :labels="trendLabels" :series="trendSeries" height="320px" />
          </el-col>
          <el-col :span="10">
            <ChartBox type="bar" title="分类服务费" :labels="categoryLabels" :series="categorySeries" height="320px" />
          </el-col>
        </el-row>

        <el-table :data="categoryStats" stripe style="width:100%;margin-top:16px">
          <el-table-column prop="category" label="职位分类" width="200" />
          <el-table-column prop="amount" label="服务费金额" width="150">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ChartBox from '../../components/ChartBox.vue'
import { getDailySummary, getServiceFeeStats } from '../../api/financeAdmin'

const loading = ref(false)
const activeTab = ref('daily')
const dailyRecords = ref([])
const categoryStats = ref([])
const feeSummary = ref({
  totalServiceFee: 0,
  todayServiceFee: 0,
  weekServiceFee: 0,
  monthServiceFee: 0
})
const stats = ref({
  todayTopUp: 0,
  todayWithdrawal: 0,
  todayServiceFee: 0,
  todaySettlementCount: 0
})

const trendLabels = computed(() => dailyRecords.value.map(r => r.date))
const trendSeries = computed(() => [
  { name: '充值', data: dailyRecords.value.map(r => Number(r.totalTopUp) || 0) },
  { name: '提现', data: dailyRecords.value.map(r => Number(r.totalWithdrawal) || 0) },
  { name: '服务费', data: dailyRecords.value.map(r => Number(r.totalServiceFee) || 0) }
])
const categoryLabels = computed(() => categoryStats.value.map(r => r.category))
const categorySeries = computed(() => [
  { name: '服务费金额', data: categoryStats.value.map(r => Number(r.amount) || 0) }
])

function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

async function fetchDailyData() {
  loading.value = true
  try {
    const res = await getDailySummary({})
    dailyRecords.value = res || []
    const today = dailyRecords.value.find(r => r.date === todayStr()) || dailyRecords.value[dailyRecords.value.length - 1]
    if (today) {
      stats.value = {
        todayTopUp: Number(today.totalTopUp) || 0,
        todayWithdrawal: Number(today.totalWithdrawal) || 0,
        todayServiceFee: Number(today.totalServiceFee) || 0,
        todaySettlementCount: Number(today.settlementCount) || 0
      }
    }
  } finally {
    loading.value = false
  }
}

async function fetchServiceFeeStats() {
  try {
    const res = await getServiceFeeStats({})
    categoryStats.value = res.byCategory || []
    feeSummary.value = {
      totalServiceFee: Number(res.totalServiceFee) || 0,
      todayServiceFee: Number(res.todayServiceFee) || 0,
      weekServiceFee: Number(res.weekServiceFee) || 0,
      monthServiceFee: Number(res.monthServiceFee) || 0
    }
  } catch {}
}

function handleViewDetail(row) {
  ElMessage.info('查看对账详情功能开发中')
}

function handleExport(row) {
  ElMessage.info('导出对账报表功能开发中')
}

onMounted(() => {
  fetchDailyData()
  fetchServiceFeeStats()
})
</script>
