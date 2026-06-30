<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>职位管理</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="草稿" value="DRAFT" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索职位/企业" size="small" style="width:200px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="jobs" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="职位名称" min-width="180" />
      <el-table-column prop="companyName" label="企业名称" width="160" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="headcount" label="招聘人数" width="100" />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'CLOSED' ? 'info' : 'warning'">
            {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'CLOSED' ? '已关闭' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.isTop" @change="handleSetTop(row)" :active-value="true" :inactive-value="false" />
        </template>
      </el-table-column>
      <el-table-column label="推荐" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.isRecommended" @change="handleSetRecommended(row)" :active-value="true" :inactive-value="false" />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'PUBLISHED'" type="warning" size="small" text @click="handleClose(row)">下架</el-button>
          <el-button v-if="row.status === 'CLOSED'" type="success" size="small" text @click="handleReopen(row)">重新上架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, closeJob, reopenJob, setJobTop, setJobRecommended } from '../../api/jobs'

const loading = ref(false)
const jobs = ref([])
const query = ref({ status: '', keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listJobs(query.value)
    jobs.value = res || []
  } finally {
    loading.value = false
  }
}

async function handleClose(row) {
  try {
    await ElMessageBox.confirm(`确定下架职位 "${row.title}"？`, '提示', { type: 'warning' })
    await closeJob(row.id)
    ElMessage.success('已下架')
    await fetchData()
  } catch {}
}

async function handleReopen(row) {
  try {
    await ElMessageBox.confirm(`确定重新上架职位 "${row.title}"？`, '提示', { type: 'warning' })
    await reopenJob(row.id)
    ElMessage.success('已重新上架')
    await fetchData()
  } catch {}
}

async function handleSetTop(row) {
  try {
    await setJobTop(row.id, row.isTop)
    ElMessage.success(row.isTop ? '已置顶' : '已取消置顶')
  } catch {
    row.isTop = !row.isTop
  }
}

async function handleSetRecommended(row) {
  try {
    await setJobRecommended(row.id, row.isRecommended)
    ElMessage.success(row.isRecommended ? '已推荐' : '已取消推荐')
  } catch {
    row.isRecommended = !row.isRecommended
  }
}

function handleDetail(row) {
  ElMessage.info('详情功能待开发')
}

onMounted(() => {
  fetchData()
})
</script>
