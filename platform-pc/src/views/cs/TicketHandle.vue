<template>
  <el-card>
    <template #header><span>工单处理</span></template>

    <el-form :inline="true" :model="query" @submit.prevent>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width:150px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="工单标题/编号" clearable @keyup.enter="fetchList" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchList">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="工单标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="类型" width="110" />
      <el-table-column prop="reporterName" label="提交人" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'danger' : row.status === 'PROCESSING' ? 'warning' : 'info'" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status !== 'CLOSED'" type="warning" link size="small" @click="openHandle(row)">处理</el-button>
          <el-button v-if="row.status !== 'CLOSED'" type="danger" link size="small" @click="handleClose(row)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="工单详情" width="640px">
      <el-descriptions :column="1" border v-if="detail">
        <el-descriptions-item label="工单标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.category }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ detail.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="详情描述">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ detail.handleResult || '暂未处理' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="handleVisible" title="处理工单" width="560px">
      <el-form label-position="top">
        <el-form-item label="处理结果/回复">
          <el-input v-model="handleForm.result" type="textarea" :rows="5" placeholder="请输入处理结果或回复用户" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">提交处理</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listComplaints, getComplaintDetail, handleComplaint, closeComplaint } from '../../api/complaintAdmin'

const query = reactive({ status: '', keyword: '' })
const list = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const handleVisible = ref(false)
const saving = ref(false)
const handleForm = reactive({ id: null, result: '' })

function statusText(s) {
  return s === 'PENDING' ? '待处理' : s === 'PROCESSING' ? '处理中' : '已关闭'
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listComplaints({ ...query })
    list.value = res || []
  } catch {} finally {
    loading.value = false
  }
}

function handleReset() {
  query.status = ''
  query.keyword = ''
  fetchList()
}

async function openDetail(row) {
  try {
    const res = await getComplaintDetail(row.id)
    detail.value = res || row
    detailVisible.value = true
  } catch {}
}

function openHandle(row) {
  handleForm.id = row.id
  handleForm.result = ''
  handleVisible.value = true
}

async function submitHandle() {
  if (!handleForm.result.trim()) {
    ElMessage.warning('请填写处理结果')
    return
  }
  saving.value = true
  try {
    await handleComplaint({ id: handleForm.id, handleResult: handleForm.result })
    ElMessage.success('已提交处理')
    handleVisible.value = false
    fetchList()
  } catch {} finally {
    saving.value = false
  }
}

async function handleClose(row) {
  try {
    await ElMessageBox.confirm(`确认关闭工单「${row.title}」？`, '提示', { type: 'warning' })
    await closeComplaint({ id: row.id, closeReason: '客服关闭' })
    ElMessage.success('工单已关闭')
    fetchList()
  } catch {}
}

onMounted(fetchList)
</script>
