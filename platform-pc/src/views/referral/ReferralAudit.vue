<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditList, approveReward, rejectReward } from '../../api/referral'

const auditList = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const rejectDialogVisible = ref(false)
const currentRewardId = ref(null)
const rejectRemark = ref('')

async function fetchAuditList() {
  loading.value = true
  try {
    const res = await getAuditList({ page: page.value, pageSize: pageSize.value })
    auditList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleApprove(id) {
  try {
    await ElMessageBox.confirm('确定审核通过？', '提示')
    await approveReward(id, '审核通过')
    ElMessage.success('审核通过')
    fetchAuditList()
  } catch {}
}

function openRejectDialog(id) {
  currentRewardId.value = id
  rejectRemark.value = ''
  rejectDialogVisible.value = true
}

async function handleReject() {
  if (!rejectRemark.value) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  try {
    await rejectReward(currentRewardId.value, rejectRemark.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchAuditList()
  } catch {}
}

onMounted(() => {
  fetchAuditList()
})
</script>

<template>
  <div class="audit-page" v-loading="loading">
    <el-card>
      <template #header>
        <span>奖励审核</span>
      </template>

      <el-table :data="auditList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="amount" label="奖励金额" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="openRejectDialog(row.id)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchAuditList"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.audit-page { padding: 20px; }
</style>
