<template>
  <el-card>
    <template #header>
      <span class="card-title">企业实名审核</span>
    </template>
    <el-form :model="searchForm" inline>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" v-loading="loading" stripe style="width:100%;margin-top:12px">
      <el-table-column prop="id" label="记录ID" width="80" />
      <el-table-column prop="enterpriseId" label="企业ID" width="80" />
      <el-table-column prop="companyName" label="企业名称" min-width="160" />
      <el-table-column prop="legalPersonName" label="法人姓名" width="120" />
      <el-table-column prop="legalPersonIdCard" label="法人身份证" width="200" />
      <el-table-column prop="unifiedSocialCreditCode" label="统一社会信用代码" width="200" />
      <el-table-column label="营业执照" width="100">
        <template #default="{ row }">
          <el-button size="small" link @click="showImage(row)">查看</el-button>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="拒绝原因" min-width="160" show-overflow-tooltip />
      <el-table-column prop="submittedAt" label="提交时间" width="180" />
      <el-table-column prop="reviewedAt" label="审核时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>

    <el-dialog v-model="imageDialogVisible" title="营业执照" width="600px">
      <el-image v-if="currentRow?.businessLicenseUrl" :src="currentRow.businessLicenseUrl" :preview-src-list="[currentRow.businessLicenseUrl]" fit="contain" style="width:500px;max-height:400px" />
      <p v-else>无</p>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listEnterpriseRealName, approveEnterpriseRealName, rejectEnterpriseRealName } from '../../api/enterpriseRealName'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchForm = ref({ status: 'PENDING' })

const imageDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentRow = ref(null)
const rejectReason = ref('')

const statusMap = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchForm.value.status) params.status = searchForm.value.status
    const res = await listEnterpriseRealName(params)
    records.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }
function handleReset() { searchForm.value = { status: 'PENDING' }; page.value = 1; fetchData() }

function showImage(row) {
  currentRow.value = row
  imageDialogVisible.value = true
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm('确认通过该实名认证?', '确认', { type: 'warning' })
    await approveEnterpriseRealName(row.id)
    ElMessage.success('已通过')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data?.error || '操作失败')
  }
}

function handleReject(row) {
  currentRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function submitReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  try {
    await rejectEnterpriseRealName(currentRow.value.id, rejectReason.value.trim())
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.error || '操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
