<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApplications, acceptApplication, rejectApplication } from '../../api/application'

const route = useRoute()
const applications = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const searchForm = ref({
  jobId: undefined,
  jobTitle: '',
  scheduleId: undefined,
  status: ''
})

const jobTitleHint = ref('')

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
    const res = await listApplications({
      ...searchForm.value,
      page: page.value,
      pageSize
    })
    const data = Array.isArray(res) ? res : (res.records || [])
    applications.value = page.value === 1 ? data : applications.value.concat(data)
    hasMore.value = data.length >= pageSize
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  page.value += 1
  fetchData()
}

function handleSearch() {
  page.value = 1
  hasMore.value = true
  fetchData()
}

function handleReset() {
  searchForm.value = { jobId: route.query.jobId ? Number(route.query.jobId) : undefined, jobTitle: '', status: '' }
  page.value = 1
  hasMore.value = true
  fetchData()
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm('确定通过该应聘申请？', '提示')
    await acceptApplication(row.applicationId)
    ElMessage.success('操作成功')
    fetchData()
  } catch {}
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm('确定拒绝该应聘申请？', '提示')
    await rejectApplication(row.applicationId)
    ElMessage.success('操作成功')
    fetchData()
  } catch {}
}

onMounted(() => {
  searchForm.value.jobId = route.query.jobId ? Number(route.query.jobId) : undefined
  searchForm.value.jobTitle = route.query.jobTitle ? String(route.query.jobTitle) : ''
  searchForm.value.scheduleId = route.query.scheduleId ? Number(route.query.scheduleId) : undefined
  jobTitleHint.value = searchForm.value.jobTitle
  fetchData()
})
</script>

<template>
  <div class="application-list">
    <el-card>
      <div v-if="jobTitleHint" class="job-hint">当前职位：{{ jobTitleHint }}</div>
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
        <el-table-column prop="jobTitle" label="职位" min-width="140" />
        <el-table-column prop="workerName" label="应聘者" width="100" />
        <el-table-column prop="workerAge" label="年龄" width="70" />
        <el-table-column prop="workerPhone" label="手机号" width="130" />
        <el-table-column label="排班日期" width="100">
          <template #default="{ row }">
            {{ row.scheduleDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="排班时段" width="140">
          <template #default="{ row }">
            {{ row.startTime || '-' }} - {{ row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">
              {{ statusOptions.find(o => o.value === row.status)?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appliedAt" label="申请时间" width="180" />
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
      <div class="load-more-wrap">
        <el-button v-if="hasMore" :loading="loadingMore" @click="loadMore">加载更多</el-button>
        <el-tag v-else type="info">没有更多了</el-tag>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.application-list {
  padding: 20px;
}

.job-hint {
  margin-bottom: 12px;
  color: #666;
}

.load-more-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0 4px;
}
</style>
