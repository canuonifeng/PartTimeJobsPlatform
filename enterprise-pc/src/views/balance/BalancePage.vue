<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBalance, topUp, getTransactions } from '../../api/balance'

const balanceInfo = ref({
  balance: 0,
  creditLimit: 0,
  totalTopUp: 0,
  totalSpent: 0,
  usableBalance: 0
})
const transactions = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const topUpDialogVisible = ref(false)
const topUpAmount = ref(0)

async function fetchBalance() {
  try {
    balanceInfo.value = await getBalance()
  } catch {}
}

async function fetchTransactions() {
  loading.value = true
  try {
    const res = await getTransactions({ page: page.value, pageSize: pageSize.value })
    transactions.value = Array.isArray(res) ? res : (res.records || [])
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleTopUp() {
  if (!topUpAmount.value || topUpAmount.value <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  try {
    await ElMessageBox.confirm(`确定充值 ${topUpAmount.value} 元？`, '提示')
    await topUp(topUpAmount.value)
    ElMessage.success('充值成功')
    topUpDialogVisible.value = false
    topUpAmount.value = 0
    fetchBalance()
    fetchTransactions()
  } catch {}
}

const typeLabels = {
  TOP_UP: { label: '充值', color: 'green' },
  SETTLEMENT: { label: '结算支出', color: 'red' },
  SETTLEMENT_REFUND: { label: '结算退款', color: 'orange' }
}

onMounted(() => {
  fetchBalance()
  fetchTransactions()
})
</script>

<template>
  <div class="balance-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">账户余额</div>
            <div class="stat-value" style="color: #409eff">{{ balanceInfo.balance }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">信用额度</div>
            <div class="stat-value" style="color: #67c23a">{{ balanceInfo.creditLimit }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">可用额度</div>
            <div class="stat-value" style="color: #e6a23c">{{ balanceInfo.usableBalance }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">累计充值</div>
            <div class="stat-value" style="color: #909399">{{ balanceInfo.totalTopUp }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <div class="toolbar">
        <el-button type="primary" @click="topUpDialogVisible = true">充值</el-button>
      </div>
      <el-table :data="transactions" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'TOP_UP' ? 'success' : (row.type === 'SETTLEMENT' ? 'danger' : 'warning')" size="small">
              {{ typeLabels[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.amount > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }} 元
            </span>
          </template>
        </el-table-column>
        <el-table-column label="说明">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchTransactions"
        />
      </div>
    </el-card>

    <el-dialog v-model="topUpDialogVisible" title="充值" width="400px">
      <el-form label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number v-model="topUpAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topUpDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTopUp">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.balance-page {
  padding: 20px;
}
.stat-item {
  text-align: center;
  padding: 20px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 8px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
