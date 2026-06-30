<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>报名审核</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="ACCEPTED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索职位/工人" size="small" style="width:200px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="applications" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="jobTitle" label="职位名称" min-width="160" />
      <el-table-column prop="companyName" label="企业名称" width="160" />
      <el-table-column prop="workerName" label="工人姓名" width="100" />
      <el-table-column prop="workerPhone" label="联系方式" width="130" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACCEPTED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
            {{ row.status === 'ACCEPTED' ? '已通过' : row.status === 'REJECTED' ? '已拒绝' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="appliedAt" label="申请时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" text @click="handleAccept(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" text @click="handleReject(row)">拒绝</el-button>
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApplications, acceptApplication, rejectApplication } from '../../api/applications'

const loading = ref(false)
const applications = ref([])
const query = ref({ status: '', keyword: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listApplications(query.value)
    applications.value = res || []
  } finally {
    loading.value = false
  }
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm(`确定通过 ${row.workerName} 的报名申请？`, '提示', { type: 'warning' })
    await acceptApplication(row.id)
    ElMessage.success('已通过')
    await fetchData()
  } catch {}
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm(`确定拒绝 ${row.workerName} 的报名申请？`, '提示', { type: 'warning' })
    await rejectApplication(row.id)
    ElMessage.success('已拒绝')
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
