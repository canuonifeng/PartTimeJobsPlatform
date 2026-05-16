<template>
  <el-card>
    <template #header>
      <span class="card-title">兼职管理</span>
      <el-input v-model="keyword" placeholder="搜索姓名/电话" size="small" style="float:right;width:200px;margin-right:8px" clearable @clear="fetchData" @keyup.enter="fetchData" />
    </template>
    <el-table :data="workers" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="workerId" label="编号" width="100" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已拉黑' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="firstContactAt" label="首次联系" width="180" />
      <el-table-column prop="lastContactAt" label="最近联系" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleProfile(row)">档案</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text
            @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '拉黑' : '取消拉黑' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkers, blacklistWorker, unblacklistWorker } from '../../api/worker'

const loading = ref(false)
const workers = ref([])
const keyword = ref('')

async function fetchData() {
  loading.value = true
  try {
    const data = await listWorkers({ keyword: keyword.value || undefined })
    workers.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleProfile(row) {
  ElMessage.info('档案功能待开发')
}

async function handleToggleStatus(row) {
  const action = row.status === 'ACTIVE' ? '拉黑' : '取消拉黑'
  try {
    await ElMessageBox.confirm(`确认${action}该兼职？`, '确认')
    if (row.status === 'ACTIVE') {
      await blacklistWorker(row.id)
    } else {
      await unblacklistWorker(row.id)
    }
    ElMessage.success(`已${action}`)
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
