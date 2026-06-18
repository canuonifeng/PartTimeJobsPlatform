<template>
  <el-card>
    <template #header>
      <span class="card-title">兼职管理</span>
      <el-input v-model="keyword" placeholder="搜索姓名/电话" size="small" style="float:right;width:200px;margin-right:8px" clearable @clear="onSearch" @keyup.enter="onSearch" />
    </template>
    <el-table :data="workers" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="workerGender" label="性别" width="90">
        <template #default="{ row }">{{ genderLabel(row.workerGender) }}</template>
      </el-table-column>
      <el-table-column prop="workerAge" label="年龄" width="70" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="realNameStatus" label="实名状态" width="110">
        <template #default="{ row }">
          <el-tag :type="realNameStatusTag(row.realNameStatus)">
            {{ realNameStatusLabel(row.realNameStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已拉黑' }}
          </el-tag>
        </template>
      </el-table-column>
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
    <el-pagination
      v-if="total > 0"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      style="margin-top:16px;justify-content:flex-end"
      @current-change="fetchData"
      @size-change="fetchData"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkers, blacklistWorker, unblacklistWorker } from '../../api/worker'

const loading = ref(false)
const workers = ref([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

function onSearch() {
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const data = await listWorkers({
      keyword: keyword.value || undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    const list = Array.isArray(data) ? data : (data.records || [])
    workers.value = list
    total.value = data?.total ?? list.length
  } finally {
    loading.value = false
  }
}

function handleProfile(row) {
  ElMessage.info('档案功能待开发')
}

function genderLabel(gender) {
  const map = { MALE: '男', FEMALE: '女', OTHER: '其他' }
  return map[gender] || gender || '未知'
}

function realNameStatusLabel(status) {
  const map = { NONE: '未实名', PENDING: '审核中', APPROVED: '已实名', REJECTED: '未通过' }
  return map[status] || status || '未实名'
}

function realNameStatusTag(status) {
  const map = { NONE: 'info', PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
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
