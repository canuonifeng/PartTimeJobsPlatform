<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>提现审核</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="records" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="workerName" label="工人姓名" width="120" />
      <el-table-column prop="workerPhone" label="手机号" width="130" />
      <el-table-column prop="amount" label="提现金额" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
            {{ row.status === 'APPROVED' ? '已通过' : row.status === 'REJECTED' ? '已拒绝' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" text @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" text @click="handleReject(row)">拒绝</el-button>
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWithdrawals, approveWithdrawal, rejectWithdrawal } from '../../api/withdrawals'

const loading = ref(false)
const records = ref([])
const query = ref({ status: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listWithdrawals(query.value)
    records.value = res || []
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定通过该提现申请？`, '提示', { type: 'warning' })
    await approveWithdrawal(row.id)
    ElMessage.success('已通过')
    await fetchData()
  } catch {}
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm(`确定拒绝该提现申请？`, '提示', { type: 'warning' })
    await rejectWithdrawal(row.id)
    ElMessage.success('已拒绝')
    await fetchData()
  } catch {}
}

function handleDetail(row) {
  ElMessage.info('详情弹窗待开发')
}

onMounted(() => {
  fetchData()
})
</script>
