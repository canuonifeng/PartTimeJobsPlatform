<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>结算管理</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="待结算" value="PENDING" />
            <el-option label="已结算" value="PAID" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索工人/企业/职位" size="small" style="width:200px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="settlements" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="settlementNo" label="结算单号" width="180" />
      <el-table-column prop="workerName" label="工人姓名" width="120" />
      <el-table-column prop="workerPhone" label="联系电话" width="130" />
      <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
      <el-table-column prop="jobTitle" label="职位名称" min-width="150" />
      <el-table-column prop="totalHours" label="工时(h)" width="100" />
      <el-table-column prop="basePay" label="基本工资" width="100">
        <template #default="{ row }">¥{{ row.basePay }}</template>
      </el-table-column>
      <el-table-column prop="bonus" label="补贴" width="100">
        <template #default="{ row }">¥{{ row.bonus }}</template>
      </el-table-column>
      <el-table-column prop="deduction" label="扣款" width="100">
        <template #default="{ row }">¥{{ row.deduction }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="应发" width="100">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="serviceFee" label="服务费" width="100">
        <template #default="{ row }">¥{{ row.serviceFee }}</template>
      </el-table-column>
      <el-table-column prop="actualPay" label="实发" width="100">
        <template #default="{ row }">¥{{ row.actualPay }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PAID' ? 'success' : row.status === 'CANCELLED' ? 'info' : 'warning'">
            {{ row.status === 'PAID' ? '已结算' : row.status === 'CANCELLED' ? '已取消' : '待结算' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" text @click="handleConfirm(row)">确认结算</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" text @click="handleCancel(row)">取消结算</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSettlements, confirmSettlement, cancelSettlement } from '../../api/settlements'

const loading = ref(false)
const settlements = ref([])
const query = ref({ status: '', keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listSettlements(query.value)
    settlements.value = res || []
  } finally {
    loading.value = false
  }
}

function handleDetail(row) {
  ElMessage.info(`查看结算 ${row.settlementNo} 详情功能开发中`)
}

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm(`确定确认结算 ${row.settlementNo}？`, '提示', { type: 'warning' })
    await confirmSettlement(row.id)
    ElMessage.success('结算已确认')
    await fetchData()
  } catch {}
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消结算 ${row.settlementNo}？`, '提示', { type: 'warning' })
    await cancelSettlement(row.id)
    ElMessage.success('已取消结算')
    await fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>
