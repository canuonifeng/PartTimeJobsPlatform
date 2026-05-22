<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAttendanceHours,
  updateAttendanceHours,
  batchPayAttendanceHours,
  batchDeleteAttendanceHours
} from '../../api/attendanceHours'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const selectedIds = ref([])
const statusLabels = {
  UNPAID: { label: '未结算', type: 'info' },
  PAYING: { label: '结算中', type: 'warning' },
  PAID: { label: '已结算', type: 'success' }
}

const searchForm = ref({
  workerName: '',
  dateFrom: '',
  dateTo: '',
  settlementStatus: ''
})
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  totalHours: 0,
  scheduledPay: 0
})

const salaryTypeMap = {
  HOURLY: '元/小时',
  DAILY: '元/天'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchForm.value.workerName) params.workerName = searchForm.value.workerName
    if (searchForm.value.dateFrom) params.dateFrom = searchForm.value.dateFrom
    if (searchForm.value.dateTo) params.dateTo = searchForm.value.dateTo
    if (searchForm.value.settlementStatus !== '' && searchForm.value.settlementStatus != null) params.settlementStatus = searchForm.value.settlementStatus
    const res = await listAttendanceHours(params)
    records.value = Array.isArray(res) ? res : (res.records || [])
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.value = { workerName: '', dateFrom: '', dateTo: '', settlementStatus: '' }
  page.value = 1
  fetchData()
}

function handleSelectionChange(val) {
  selectedIds.value = val.filter(r => r.settlementStatus !== 'PAID').map(r => r.id)
}

function handleEdit(row) {
  editForm.value = {
    id: row.id,
    totalHours: row.totalHours ?? 0,
    scheduledPay: row.scheduledPay ?? 0,
    payablePay: row.payablePay ?? null
  }
  editDialogVisible.value = true
}

async function handleEditSave() {
  try {
    const payload = {
      totalHours: editForm.value.totalHours,
      scheduledPay: editForm.value.scheduledPay
    }
    if (editForm.value.payablePay != null) payload.payablePay = editForm.value.payablePay
    await updateAttendanceHours(editForm.value.id, payload)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    fetchData()
  } catch {}
}

async function handlePay(row) {
  try {
    await ElMessageBox.confirm(`确定结算 ${row.workerName} 的薪资 ${row.payablePay ?? row.scheduledPay} 元？`, '提示')
    await batchPayAttendanceHours([row.id])
    ElMessage.success('结算成功')
    fetchData()
  } catch {}
}

async function handleBatchPay() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要结算的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定批量结算 ${selectedIds.value.length} 条记录？`, '提示')
    await batchPayAttendanceHours(selectedIds.value)
    ElMessage.success('批量结算成功')
    selectedIds.value = []
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.workerName} 的考勤记录？`, '提示', { type: 'warning' })
    await batchDeleteAttendanceHours([row.id])
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定批量删除 ${selectedIds.value.length} 条记录？此操作不可恢复`, '提示', { type: 'warning' })
    await batchDeleteAttendanceHours(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchData()
  } catch {}
}

function n(val) {
  return val ?? '-'
}

function formatSalary(row) {
  if (row.salaryAmount == null) return '-'
  const unit = salaryTypeMap[row.salaryType] || ''
  return row.salaryAmount + unit
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="attendance-hours">
    <el-card>
      <el-form :model="searchForm" inline>
        <el-form-item label="工人姓名">
          <el-input v-model="searchForm.workerName" placeholder="搜索" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.dateFrom" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.dateTo" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item label="结算状态">
          <el-select v-model="searchForm.settlementStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="未结算" value="UNPAID" />
            <el-option label="结算中" value="PAYING" />
            <el-option label="已结算" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <div class="toolbar">
        <el-button type="success" :disabled="selectedIds.length === 0" @click="handleBatchPay">批量结算</el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      </div>
      <el-table
        :data="records"
        v-loading="loading"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column label="排班时间" width="180">
          <template #default="{ row }">
            {{ row.shiftDate }} {{ row.startTime }}~{{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="workerName" label="姓名" width="120" />
        <el-table-column label="工时" width="80">
          <template #default="{ row }">{{ n(row.totalHours) }}</template>
        </el-table-column>
        <el-table-column label="薪资标准" width="120">
          <template #default="{ row }">{{ formatSalary(row) }}</template>
        </el-table-column>
        <el-table-column label="排班薪资" width="100">
          <template #default="{ row }">{{ n(row.scheduledPay) }}</template>
        </el-table-column>
        <el-table-column label="应付薪资" width="100">
          <template #default="{ row }">{{ n(row.payablePay) }}</template>
        </el-table-column>
        <el-table-column label="结算状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(statusLabels[row.settlementStatus] || statusLabels.UNPAID).type" size="small">
              {{ (statusLabels[row.settlementStatus] || statusLabels.UNPAID).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.settlementStatus !== 'PAID'">
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="success" @click="handlePay(row)">结算</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <span v-else style="color: #999; font-size: 12px">--</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑考勤管理" width="400px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="工时">
          <el-input-number v-model="editForm.totalHours" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排班薪资">
          <el-input-number v-model="editForm.scheduledPay" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="应付薪资">
          <el-input-number v-model="editForm.payablePay" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.attendance-hours {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
