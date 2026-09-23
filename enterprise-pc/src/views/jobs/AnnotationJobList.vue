<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, publishJob, closeJob, deleteJob } from '../../api/job'

const router = useRouter()
const jobs = ref([])
const total = ref(0)
const loading = ref(false)
const searchForm = ref({
  title: '',
  status: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await listJobs({ ...searchForm.value, taskType: 'ANNOTATION' })
    const data = Array.isArray(res) ? res : (res.records || [])
    jobs.value = data
    total.value = Array.isArray(res) ? res.length : (res.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  searchForm.value = { title: '', status: '' }
  fetchData()
}

function handleCreate() {
  router.push('/jobs/create?taskType=ANNOTATION')
}

function handleEdit(row) {
  router.push(`/jobs/${row.id}/edit?taskType=ANNOTATION`)
}

async function handlePublish(row) {
  try {
    await publishJob(row.id)
    ElMessage.success('发布成功')
    fetchData()
  } catch {}
}

async function handleClose(row) {
  try {
    await closeJob(row.id)
    ElMessage.success('下线成功')
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除职位「${row.title}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteJob(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

function handleBatches(row) {
  router.push(`/jobs/annotation/${row.id}/batches`)
}

const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '发布中' },
  { value: 'CLOSED', label: '已关闭' }
]

const statusMap = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  CLOSED: 'danger'
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="annotation-job-list">
    <el-card>
      <el-form :model="searchForm" inline>
        <el-form-item label="职位名称">
          <el-input v-model="searchForm.title" placeholder="请输入" clearable />
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
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">新建标注职位</el-button>
      </div>
      <el-table :data="jobs" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="title" label="职位名称" min-width="160" />
        <el-table-column label="工作地点" width="180">
          <template #default="{ row }">
            {{ row.location || [row.province, row.city, row.district, row.address].filter(Boolean).join(' ') || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="类别" width="100" />
        <el-table-column prop="headcount" label="招聘人数" width="80" />
        <el-table-column label="报名情况" width="140">
          <template #default="{ row }">
            <div>总数：{{ row.applicationCount ?? 0 }}</div>
            <div>待审：{{ row.pendingApplicationCount ?? 0 }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">
              {{ statusOptions.find(o => o.value === row.status)?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 'PUBLISHED'" size="small" type="warning" @click="handleClose(row)">下线</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button size="small" type="info" @click="handleBatches(row)">批次管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.annotation-job-list {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
</style>
