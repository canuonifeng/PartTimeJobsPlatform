<template>
  <el-card>
    <template #header><span class="card-title">职位举报审核</span></template>
    <el-table :data="reports" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="60" />
      <el-table-column prop="jobTitle" label="职位名称" min-width="150" />
      <el-table-column prop="reportedBy" label="举报人" width="120" />
      <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="warning" size="small" @click="handleDismiss(row)">驳回</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleBan(row)">封禁职位</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobReports, dismissReport, banJob } from '../../api/reports'

const loading = ref(false)
const reports = ref([])

const statusMap = { PENDING: '待处理', DISMISSED: '已驳回', BANNED: '已封禁' }

function statusType(status) {
  return status === 'DISMISSED' ? 'info' : status === 'BANNED' ? 'danger' : 'warning'
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getJobReports()
    reports.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

async function handleDismiss(row) {
  try {
    await ElMessageBox.confirm(`确认驳回举报 #${row.id}？`, '确认')
    await dismissReport(row.id)
    ElMessage.success('举报已驳回')
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleBan(row) {
  try {
    await ElMessageBox.confirm(`确认封禁职位 "${row.jobTitle}"？该职位将被删除。`, '确认', { confirmButtonClass: 'el-button--danger' })
    await banJob(row.id)
    ElMessage.success('职位已封禁')
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
