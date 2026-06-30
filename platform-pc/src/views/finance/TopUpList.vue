<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>企业充值审核</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索企业" size="small" style="width:180px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="records" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
      <el-table-column prop="amount" label="充值金额" width="120">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="paymentMethod" label="支付方式" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'APPROVED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
            {{ row.status === 'APPROVED' ? '已通过' : row.status === 'REJECTED' ? '已拒绝' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditor" label="审核人" width="100" />
      <el-table-column prop="createdAt" label="申请时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" text @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" text @click="handleReject(row)">拒绝</el-button>
          <el-button type="primary" size="small" text @click="handleViewReceipt(row)">查看凭证</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="viewReceiptDialog.visible" title="充值凭证" width="500px">
    <el-image :src="viewReceiptDialog.url" fit="contain" style="width:100%;height:400px" preview-src-list="[viewReceiptDialog.url]" />
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTopUp, approveTopUp, rejectTopUp } from '../../api/topUp'

const loading = ref(false)
const records = ref([])
const query = ref({ status: '', keyword: '' })
const viewReceiptDialog = ref({ visible: false, url: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listTopUp(query.value)
    records.value = res || []
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定通过该企业的充值申请？`, '提示', { type: 'warning' })
    await approveTopUp(row.id)
    ElMessage.success('已通过')
    await fetchData()
  } catch {}
}

async function handleReject(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请')
    await rejectTopUp(row.id, value)
    ElMessage.success('已拒绝')
    await fetchData()
  } catch {}
}

function handleViewReceipt(row) {
  viewReceiptDialog.value = { visible: true, url: row.receiptUrl || 'https://picsum.photos/400/400' }
}

onMounted(() => {
  fetchData()
})
</script>
