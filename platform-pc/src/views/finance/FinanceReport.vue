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
        <el-table :data="categoryStats" stripe style="width:100%">
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDailySummary, getServiceFeeStats } from '../../api/finance'

const loading = ref(false)
const activeTab = ref('daily')
const dailyRecords = ref([])
const categoryStats = ref([])
const stats = ref({
  todayTopUp: 0,
  todayWithdrawal: 0,
  todayServiceFee: 0,
  todaySettlementCount: 0
})

async function fetchDailyData() {
  loading.value = true
  try {
    const res = await getDailySummary({})
    dailyRecords.value = res || []
    if (dailyRecords.value.length > 0) {
      const today = dailyRecords.value[0]
      stats.value = {
        todayTopUp: today.totalTopUp || 0,
        todayWithdrawal: today.totalWithdrawal || 0,
        todayServiceFee: today.totalServiceFee || 0,
        todaySettlementCount: today.settlementCount || 0
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
