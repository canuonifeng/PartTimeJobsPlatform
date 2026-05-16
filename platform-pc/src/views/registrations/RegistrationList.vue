<template>
  <div>
    <el-card>
      <template #header><span class="card-title">注册审核</span></template>
      <el-table :data="registrations" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="id" label="编号" width="60" />
        <el-table-column prop="companyName" label="企业名称" min-width="150" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rejectDialog.visible" title="拒绝原因" width="400px">
      <el-input v-model="rejectDialog.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRegistrations, approveRegistration, rejectRegistration } from '../../api/registrations'

const loading = ref(false)
const registrations = ref([])
const rejectDialog = ref({ visible: false, reason: '', targetId: null })

const statusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }

function statusType(status) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getRegistrations()
    registrations.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确认通过 "${row.companyName}" 的注册申请？`, '确认')
    await approveRegistration(row.id)
    ElMessage.success('注册已通过')
    await fetchData()
  } catch {
    // cancelled or error
  }
}

function handleReject(row) {
  rejectDialog.value = { visible: true, reason: '', targetId: row.id }
}

async function confirmReject() {
  await rejectRegistration(rejectDialog.value.targetId, rejectDialog.value.reason)
  ElMessage.success('注册已拒绝')
  rejectDialog.value.visible = false
  await fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
