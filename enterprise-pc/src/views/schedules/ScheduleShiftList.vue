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
  date: ''
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
  searchForm.value = { jobId: '', workerName: '', date: '' }
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
                  <div class="att-row">签退 {{ formatTime(row.checkOutTime) }}</div>
                  <div class="att-row att-sub">签到 {{ formatTime(row.checkInTime) }}</div>
                </template>
                <template v-else-if="row.attendanceStatus === 'CHECKED_IN'">
                  <div class="att-row">签到 {{ formatTime(row.checkInTime) }}</div>
                </template>
                <span v-else class="att-none">未签到</span>
              </template>
            </el-table-column>
            <el-table-column label="排班状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'CANCELLED'" type="danger" size="small">已取消</el-tag>
                <el-tag v-else-if="row.status === 'COMPLETED'" type="success" size="small">已完成</el-tag>
                <el-tag v-else-if="row.status === 'ABSENT'" type="danger" size="small">缺勤</el-tag>
                <el-tag v-else-if="row.status === 'ON_DUTY' || row.status === 'LATE'" type="warning" size="small">工作中</el-tag>
                <el-tag v-else type="info" size="small">待上岗</el-tag>
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
                <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待审批</el-tag>
                <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
                <el-tag v-else type="danger" size="small">已拒绝</el-tag>
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
