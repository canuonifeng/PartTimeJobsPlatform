<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listShifts, createShift, cancelShift, listCorrections, approveCorrection, rejectCorrection } from '../../api/schedule'

const activeTab = ref('shifts')

// --- Shift tab ---
const shifts = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 20
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  jobId: '',
  workerId: '',
  shiftDate: '',
  startTime: '',
  endTime: ''
})
const searchForm = ref({
  jobId: '',
  workerName: '',
  date: '',
  status: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await listShifts({ ...searchForm.value, page: page.value, pageSize })
    const data = Array.isArray(res) ? res : (res.records || [])
    shifts.value = data
    total.value = Array.isArray(res) ? res.length : (res.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.value = { jobId: '', workerName: '', date: '', status: '' }
  page.value = 1
  fetchData()
}

function handleAssign() {
  form.value = { jobId: '', workerId: '', shiftDate: '', startTime: '', endTime: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await createShift(form.value)
    ElMessage.success('分配成功')
    dialogVisible.value = false
    fetchData()
  } catch {}
}

function canCancelShift(row) {
  if (!row || row.status === 'CANCELLED') return false
  if (!row.shiftDate || !row.startTime) return false
  return new Date(`${row.shiftDate}T${row.startTime}`).getTime() > Date.now()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定取消该排班？取消后该兼职将无法签到', '提示')
    await cancelShift(row.id)
    ElMessage.success('取消成功')
    fetchData()
  } catch {}
}

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function shiftStatusText(status) {
  const map = { SCHEDULED: '待上岗', ON_DUTY: '工作中', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', EARLY: '早退', LATE_EARLY_LEAVE: '迟到并早退', CANCELLED: '已取消' }
  return map[status] || '待上岗'
}

function attendanceStatusText(status) {
  const map = { CHECKED_IN: '已签到', CHECKED_OUT: '已签退', NORMAL: '正常', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', LATE_EARLY_LEAVE: '迟到并早退', PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消' }
  return map[status] || '未签到'
}

function correctionStatusText(status) {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || '-'
}

function handlePageChange(val) {
  page.value = val
  fetchData()
}

// --- Correction tab ---
const corrections = ref([])
const correctionTotal = ref(0)
const correctionPage = ref(1)
const correctionPageSize = 20
const correctionLoading = ref(false)
const correctionFilter = ref({ status: '', keyword: '', dateFrom: '', dateTo: '' })
const rejectDialogVisible = ref(false)
const rejectTarget = ref(null)
const rejectForm = ref({ rejectReason: '' })

async function fetchCorrections() {
  correctionLoading.value = true
  try {
    const params = { ...correctionFilter.value, page: correctionPage.value, pageSize: correctionPageSize }
    const res = await listCorrections(params)
    corrections.value = res.records || []
    correctionTotal.value = res.total || 0
  } finally {
    correctionLoading.value = false
  }
}

function handleCorrectionSearch() {
  correctionPage.value = 1
  fetchCorrections()
}

function handleCorrectionReset() {
  correctionFilter.value = { status: '', keyword: '', dateFrom: '', dateTo: '' }
  correctionPage.value = 1
  fetchCorrections()
}

async function handleApprove(id) {
  try {
    await approveCorrection(id)
    ElMessage.success('已通过')
    fetchCorrections()
  } catch {
    ElMessage.error('操作失败')
  }
}

function openRejectDialog(row) {
  rejectTarget.value = row
  rejectForm.value = { rejectReason: '' }
  rejectDialogVisible.value = true
}

async function handleReject() {
  if (!rejectForm.value.rejectReason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await rejectCorrection(rejectTarget.value.id, rejectForm.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchCorrections()
  } catch {
    ElMessage.error('操作失败')
  }
}

function handleCorrectionPageChange(val) {
  correctionPage.value = val
  fetchCorrections()
}

function onTabChange(tab) {
  activeTab.value = tab
  if (tab === 'corrections' && corrections.value.length === 0) {
    fetchCorrections()
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="shift-list">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="排班列表" name="shifts">
          <el-form :model="searchForm" inline>
            <el-form-item label="职位">
              <el-input v-model="searchForm.jobId" placeholder="职位ID" clearable style="width: 120px" />
            </el-form-item>
            <el-form-item label="人员">
              <el-input v-model="searchForm.workerName" placeholder="姓名" clearable style="width: 120px" />
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker v-model="searchForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 150px">
                <el-option label="待上岗" value="SCHEDULED" />
                <el-option label="工作中" value="ON_DUTY" />
                <el-option label="迟到" value="LATE" />
                <el-option label="早退" value="EARLY_LEAVE" />
                <el-option label="迟到并早退" value="LATE_EARLY_LEAVE" />
                <el-option label="缺勤" value="ABSENT" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="toolbar">
            <el-button type="primary" @click="handleAssign">分配班次</el-button>
          </div>
          <el-table :data="shifts" v-loading="loading" stripe style="width: 100%">
            <el-table-column prop="jobTitle" label="职位" min-width="160" />
            <el-table-column prop="workerName" label="人员" width="120" />
            <el-table-column prop="workerAge" label="年龄" width="70" />
            <el-table-column prop="shiftDate" label="日期" width="120" />
            <el-table-column label="时段" width="160">
              <template #default="{ row }">
                {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column label="签到" width="160">
              <template #default="{ row }">
                <template v-if="row.attendanceStatus === 'CHECKED_OUT'">
                  <div class="att-row">{{ attendanceStatusText(row.attendanceStatus) }} {{ formatTime(row.checkOutTime) }}</div>
                  <div class="att-row att-sub">签到 {{ formatTime(row.checkInTime) }}</div>
                </template>
                <template v-else-if="row.attendanceStatus === 'CHECKED_IN'">
                  <div class="att-row">{{ attendanceStatusText(row.attendanceStatus) }} {{ formatTime(row.checkInTime) }}</div>
                </template>
                <span v-else class="att-none">{{ attendanceStatusText(row.attendanceStatus) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="排班状态" width="100">
              <template #default="{ row }">
                <el-tag :type="['ABSENT','LATE','EARLY_LEAVE','EARLY','LATE_EARLY_LEAVE','CANCELLED'].includes(row.status) ? 'danger' : row.status === 'COMPLETED' ? 'success' : row.status === 'ON_DUTY' ? 'warning' : 'info'" size="small">{{ shiftStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="canCancelShift(row)" size="small" type="danger" @click="handleDelete(row)">取消排班</el-button>
                <span v-else style="color:#999">{{ row.status === 'CANCELLED' ? '已取消' : '不可取消' }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-if="total > pageSize"
              background
              layout="total, prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              @current-change="handlePageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="补卡申请" name="corrections">
          <el-form :model="correctionFilter" inline>
            <el-form-item label="状态">
              <el-select v-model="correctionFilter.status" clearable placeholder="全部" style="width: 120px">
                <el-option label="待审批" value="PENDING" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="correctionFilter.keyword" placeholder="岗位名称" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCorrectionSearch">搜索</el-button>
              <el-button @click="handleCorrectionReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="corrections" v-loading="correctionLoading" stripe style="width: 100%">
            <el-table-column prop="jobTitle" label="职位" min-width="140" />
            <el-table-column prop="workerName" label="人员" width="120" />
            <el-table-column prop="workerAge" label="年龄" width="70" />
            <el-table-column prop="shiftDate" label="排班日期" width="120" />
            <el-table-column label="时段" width="150">
              <template #default="{ row }">
                {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="补卡原因" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'danger'" size="small">{{ correctionStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 'PENDING'">
                  <el-button size="small" type="primary" @click="handleApprove(row.id)">通过</el-button>
                  <el-button size="small" type="warning" @click="openRejectDialog(row)">拒绝</el-button>
                </template>
                <el-tooltip v-else-if="row.status === 'REJECTED' && row.rejectReason" :content="row.rejectReason">
                  <span style="color:#999;cursor:pointer">查看原因</span>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-if="correctionTotal > correctionPageSize"
              background
              layout="total, prev, pager, next"
              :total="correctionTotal"
              :page-size="correctionPageSize"
              :current-page="correctionPage"
              @current-change="handleCorrectionPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" title="分配班次" width="500px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="职位" prop="jobId" :rules="[{ required: true, message: '请输入职位ID' }]">
          <el-input v-model="form.jobId" placeholder="职位ID" />
        </el-form-item>
        <el-form-item label="人员" prop="workerId" :rules="[{ required: true, message: '请输入人员ID' }]">
          <el-input v-model="form.workerId" placeholder="人员ID" />
        </el-form-item>
        <el-form-item label="日期" prop="shiftDate" :rules="[{ required: true, message: '请选择日期' }]">
          <el-date-picker v-model="form.shiftDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" value-format="HH:mm" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" value-format="HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝补卡" width="400px">
      <el-form :model="rejectForm">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.shift-list {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0 4px;
}
.att-row {
  font-size: 13px;
  line-height: 1.6;
}
.att-sub {
  color: #999;
  font-size: 12px;
}
.att-none {
  color: #999;
}
</style>
