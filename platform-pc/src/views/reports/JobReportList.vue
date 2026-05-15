<template>
  <el-card>
    <template #header><span class="card-title">Job Report Review</span></template>
    <el-table :data="reports" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="jobTitle" label="Job Title" min-width="150" />
      <el-table-column prop="reportedBy" label="Reported By" width="120" />
      <el-table-column prop="reason" label="Reason" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="Status" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="warning" size="small" @click="handleDismiss(row)">Dismiss</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleBan(row)">Ban Job</el-button>
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

function statusType(status) {
  return status === 'DISMISSED' ? 'info' : status === 'BANNED' ? 'danger' : 'warning'
}

async function fetchData() {
  loading.value = true
  try {
    reports.value = await getJobReports()
  } finally {
    loading.value = false
  }
}

async function handleDismiss(row) {
  try {
    await ElMessageBox.confirm(`Dismiss report #${row.id}?`, 'Confirm')
    await dismissReport(row.id)
    ElMessage.success('Report dismissed')
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleBan(row) {
  try {
    await ElMessageBox.confirm(`Ban job "${row.jobTitle}"? This will remove the job.`, 'Confirm', { confirmButtonClass: 'el-button--danger' })
    await banJob(row.id)
    ElMessage.success('Job banned')
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
