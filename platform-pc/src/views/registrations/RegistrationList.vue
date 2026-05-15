<template>
  <div>
    <el-card>
      <template #header><span class="card-title">Registration Review</span></template>
      <el-table :data="registrations" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="companyName" label="Company Name" min-width="150" />
        <el-table-column prop="contactPerson" label="Contact" width="120" />
        <el-table-column prop="contactPhone" label="Phone" width="130" />
        <el-table-column prop="status" label="Status" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row)">Approve</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleReject(row)">Reject</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="rejectDialog.visible" title="Reject Reason" width="400px">
      <el-input v-model="rejectDialog.reason" type="textarea" :rows="3" placeholder="Enter reject reason" />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">Cancel</el-button>
        <el-button type="danger" @click="confirmReject">Confirm</el-button>
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

function statusType(status) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'danger' : 'warning'
}

async function fetchData() {
  loading.value = true
  try {
    registrations.value = await getRegistrations()
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`Approve registration for "${row.companyName}"?`, 'Confirm')
    await approveRegistration(row.id)
    ElMessage.success('Registration approved')
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
  ElMessage.success('Registration rejected')
  rejectDialog.value.visible = false
  await fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
