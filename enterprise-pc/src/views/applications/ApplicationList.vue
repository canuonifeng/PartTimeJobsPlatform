<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApplications, acceptApplication, rejectApplication } from '../../api/application'

const applications = ref([])
const total = ref(0)
const loading = ref(false)
const searchForm = ref({
  jobTitle: '',
  status: ''
})

const statusOptions = [
  { value: 'PENDING', label: '待处理' },
  { value: 'ACCEPTED', label: '已通过' },
  { value: 'REJECTED', label: '已拒绝' }
]

const statusMap = {
  PENDING: 'warning',
  ACCEPTED: 'success',
  REJECTED: 'danger'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listApplications(searchForm.value)
    applications.value = res.data.records || res.data || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  searchForm.value = { jobTitle: '', status: '' }
  fetchData()
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm('确定通过该应聘申请？', '提示')
    await acceptApplication(row.id)
    ElMessage.success('操作成功')
    fetchData()
  } catch {}
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm('确定拒绝该应聘申请？', '提示')
    await rejectApplication(row.id)
    ElMessage.success('操作成功')
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="application-list">
    <el-card>
      <el-form :model="searchForm" inline>
        <el-form-item label="职位名称">
          <el-input v-model="searchForm.jobTitle" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="applications" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="jobTitle" label="职位" min-width="160" />
        <el-table-column prop="workerName" label="应聘者" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">
              {{ statusOptions.find(o => o.value === row.status)?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="handleAccept(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            <el-tag v-if="row.status !== 'PENDING'" :type="statusMap[row.status]">
              {{ statusOptions.find(o => o.value === row.status)?.label }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.application-list {
  padding: 20px;
}
</style>
