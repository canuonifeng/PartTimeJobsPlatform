<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>交易流水</span>
        <div>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" style="width:280px;margin-right:10px" />
          <el-select v-model="query.type" placeholder="交易类型" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="企业充值" value="ENTERPRISE_TOPUP" />
            <el-option label="工人提现" value="WORKER_WITHDRAWAL" />
            <el-option label="工资结算" value="SETTLEMENT" />
            <el-option label="平台服务费" value="SERVICE_FEE" />
            <el-option label="退款" value="REFUND" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" size="small" style="width:100px;margin-right:8px" clearable @change="fetchData">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="处理中" value="PENDING" />
            <el-option label="失败" value="FAILED" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索名称/单号" size="small" style="width:180px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6">
        <el-statistic title="今日充值总额" :value="overview.todayTopUpAmount" precision="2" prefix="¥" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日提现总额" :value="overview.todayWithdrawalAmount" precision="2" prefix="¥" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日结算总额" :value="overview.todaySettlementAmount" precision="2" prefix="¥" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="今日服务费收入" :value="overview.todayServiceFeeAmount" precision="2" prefix="¥" />
      </el-col>
    </el-row>

    <el-table :data="transactions" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="transactionNo" label="交易单号" width="180" />
      <el-table-column prop="typeName" label="交易类型" width="120" />
      <el-table-column prop="relatedName" label="关联方" width="150" />
      <el-table-column prop="relatedPhone" label="联系方式" width="130" />
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">
          <span :style="{color:['ENTERPRISE_TOPUP','SERVICE_FEE'].includes(row.type) ? '#67C23A' : '#F56C6C'}">
            {{['ENTERPRISE_TOPUP','SERVICE_FEE'].includes(row.type) ? '+' : '-'}}¥{{row.amount}}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="balanceAfter" label="余额" width="120">
        <template #default="{ row }">¥{{ row.balanceAfter }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'" size="small">
            {{ row.status === 'SUCCESS' ? '成功' : row.status === 'FAILED' ? '失败' : '处理中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="createdAt" label="交易时间" width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listTransactions, getTransactionOverview } from '../../api/transactions'

const loading = ref(false)
const transactions = ref([])
const query = ref({ type: '', status: '', keyword: '' })
const dateRange = ref([])
const overview = ref({
  todayTopUpAmount: 0,
  todayWithdrawalAmount: 0,
  todaySettlementAmount: 0,
  todayServiceFeeAmount: 0
})

async function fetchData() {
  loading.value = true
  try {
    const res = await listTransactions(query.value)
    transactions.value = res || []
  } finally {
    loading.value = false
  }
}

async function fetchOverview() {
  try {
    const res = await getTransactionOverview()
    overview.value = res
  } catch {}
}

onMounted(() => {
  fetchData()
  fetchOverview()
})
</script>
