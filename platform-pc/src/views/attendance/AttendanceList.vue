<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>考勤管理</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="正常" value="NORMAL" />
            <el-option label="迟到" value="LATE" />
            <el-option label="早退" value="EARLY_LEAVE" />
            <el-option label="缺勤" value="ABSENT" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索职位/工人" size="small" style="width:200px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="records" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="jobTitle" label="职位名称" min-width="160" />
      <el-table-column prop="companyName" label="企业名称" width="160" />
      <el-table-column prop="workerName" label="工人姓名" width="100" />
      <el-table-column prop="checkInTime" label="签到时间" width="180" />
      <el-table-column prop="checkOutTime" label="签退时间" width="180" />
      <el-table-column prop="totalHours" label="工时" width="90" />
      <el-table-column prop="payablePay" label="薪资" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'NORMAL' ? 'success' : row.status === 'ABSENT' ? 'danger' : 'warning'">
            {{ row.status === 'NORMAL' ? '正常' : row.status === 'LATE' ? '迟到' : row.status === 'EARLY_LEAVE' ? '早退' : row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="settlementStatus" label="结算状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.settlementStatus === 'PAID' ? 'success' : 'info'">
            {{ row.settlementStatus === 'PAID' ? '已结算' : '待结算' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleMarkNormal(row)">标记正常</el-button>
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAttendance, updateAttendanceStatus } from '../../api/attendance'

const loading = ref(false)
const records = ref([])
const query = ref({ status: '', keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listAttendance(query.value)
    records.value = res || []
  } finally {
    loading.value = false
  }
}

async function handleMarkNormal(row) {
  try {
    await ElMessageBox.confirm(`确定标记该考勤为正常状态？`, '提示', { type: 'warning' })
    await updateAttendanceStatus(row.id, 'NORMAL', '')
    ElMessage.success('已标记为正常')
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
