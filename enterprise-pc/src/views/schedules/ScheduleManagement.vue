<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { listJobs } from '../../api/job'
import { listManagedSchedules, listScheduleApplicants, updateManagedSchedule, copyManagedSchedule, batchCreateManagedSchedules, exportScheduleApplicants } from '../../api/schedule'
const router = useRouter()

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', startDate: '', endDate: '', page: 1, pageSize: 20 })

function today() { const d = new Date(); return d.toISOString().split('T')[0] }
function tomorrow() { const d = new Date(); d.setDate(d.getDate() + 1); return d.toISOString().split('T')[0] }
function weekStart() { const d = new Date(); const day = d.getDay(); const diff = d.getDate() - day + (day === 0 ? -6 : 1); d.setDate(diff); return d.toISOString().split('T')[0] }
function weekEnd() { const d = new Date(); const day = d.getDay(); const diff = d.getDate() + (7 - day) - (day === 0 ? 0 : 1) + (day === 0 ? 0 : 0); d.setDate(d.getDate() + (7 - day) % 7 || 7); return d.toISOString().split('T')[0] }
function setDateRange(start, end) { query.startDate = start; query.endDate = end; resetPage() }
const applicantVisible = ref(false)
const applicantLoading = ref(false)
const applicants = ref([])
const applicantTotal = ref(0)
const currentSchedule = ref(null)
const applicantQuery = reactive({ status: '', page: 1, pageSize: 20 })
const editVisible = ref(false)
const editForm = reactive({ id: null, scheduleName: '', scheduleDate: '', startTime: '', endTime: '', slotsAvailable: 1, contactName: '', contactPhone: '' })
const copyVisible = ref(false)
const copyForm = reactive({ sourceScheduleId: null, scheduleDate: '', startTime: '', endTime: '' })
const batchVisible = ref(false)
const jobOptions = ref([])
const batchForm = reactive({ jobId: null, startDate: '', endDate: '', weekdays: [], startTime: '', endTime: '' })

function isExpiredSchedule(row) {
  if (!row.scheduleDate || !row.endTime) return false
  return new Date(`${row.scheduleDate}T${row.endTime}`).getTime() < Date.now()
}
function scheduleStatusText(row) {
  if (row.status === 'CANCELLED') return '已取消'
  if (isExpiredSchedule(row)) return '已过期'
  if (Number(row.remainingSlots || 0) <= 0) return '已满员'
  return '可报名'
}
function scheduleStatusTagType(row) {
  if (row.status === 'CANCELLED') return 'danger'
  if (isExpiredSchedule(row)) return 'info'
  if (Number(row.remainingSlots || 0) <= 0) return 'warning'
  return 'success'
}
function applicationStatusText(status) {
  const map = { PENDING: '待审核', ACCEPTED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status || '-'
}
function shiftStatusText(status) {
  const map = { SCHEDULED: '待上岗', ON_DUTY: '工作中', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', LATE_EARLY_LEAVE: '迟到并早退', CANCELLED: '已取消' }
  return map[status] || status || '-'
}
function attendanceStatusText(status) {
  const map = { CHECKED_IN: '已签到', CHECKED_OUT: '已签退', NORMAL: '正常', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', EARLY: '早退', LATE_EARLY_LEAVE: '迟到并早退', PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消' }
  return map[status] || '-'
}
function correctionStatusText(status) {
  const map = { PENDING: '审批中', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || '-'
}
function settlementStatusText(status) {
  const map = { UNSETTLED: '未结算', UNPAID: '未结算', PAYING: '结算中', PENDING: '待结算', SETTLED: '已结算', PAID: '已结算' }
  return map[status] || '-'
}
async function loadData() {
  loading.value = true
  try {
    const res = await listManagedSchedules(query)
    rows.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function resetPage() {
  query.page = 1
  loadData()
}
async function openApplicants(row) {
  currentSchedule.value = row
  applicantVisible.value = true
  applicantQuery.status = ''
  applicantQuery.page = 1
  await loadApplicants()
}
async function loadApplicants() {
  if (!currentSchedule.value) return
  applicantLoading.value = true
  try {
    const res = await listScheduleApplicants(currentSchedule.value.id, applicantQuery)
    applicants.value = res.records || []
    applicantTotal.value = res.total || 0
  } finally {
    applicantLoading.value = false
  }
}
function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    scheduleName: row.scheduleName || '',
    scheduleDate: row.scheduleDate || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    slotsAvailable: row.slotsAvailable || 1,
    contactName: row.contactName || '',
    contactPhone: row.contactPhone || ''
  })
  editVisible.value = true
}
async function submitEdit() {
  await updateManagedSchedule(editForm)
  ElMessage.success('班次已更新，已通过/已排班人员不受影响')
  editVisible.value = false
  loadData()
}
function openCopy(row) {
  Object.assign(copyForm, { sourceScheduleId: row.id, scheduleDate: row.scheduleDate, startTime: row.startTime, endTime: row.endTime })
  copyVisible.value = true
}
async function submitCopy() {
  await copyManagedSchedule(copyForm)
  ElMessage.success('复制成功')
  copyVisible.value = false
  loadData()
}
async function openBatch() {
  batchVisible.value = true
  if (jobOptions.value.length === 0) {
    const res = await listJobs({ page: 1, pageSize: 200 })
    jobOptions.value = res.records || []
  }
}
async function submitBatch() {
  await batchCreateManagedSchedules(batchForm)
  ElMessage.success('批量创建成功')
  batchVisible.value = false
  loadData()
}
async function exportCurrentApplicants() {
  if (!currentSchedule.value) return
  const res = await exportScheduleApplicants(currentSchedule.value.id, { status: applicantQuery.status })
  downloadCsv(res, currentSchedule.value.id)
}
async function exportSchedule(row) {
  const res = await exportScheduleApplicants(row.id)
  downloadCsv(res, row.id)
}
function downloadCsv(res, scheduleId) {
  const blob = new Blob([res.content || ''], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = res.filename || `schedule-${scheduleId}-applicants.csv`
  link.click()
  URL.revokeObjectURL(url)
}
function isFutureSchedule(row) {
  if (!row.scheduleDate || !row.startTime) return false
  return new Date(`${row.scheduleDate}T${row.startTime}`).getTime() > Date.now()
}
function canCancel(row) {
  return row.status !== 'CANCELLED' && isFutureSchedule(row)
}
async function cancelSchedule(row) {
  await updateManagedSchedule({ id: row.id, status: 'CANCELLED' })
  ElMessage.success('班次已取消')
  loadData()
}
function navigateRelated(cmd, row) {
  const base = { scheduleId: row.id, jobId: row.jobId, jobTitle: row.jobTitle }
  const paths = {
    applications: `/applications?scheduleId=${base.scheduleId}`,
    shifts: `/schedules/shifts?scheduleId=${base.scheduleId}`,
    hours: `/attendance/hours?scheduleId=${base.scheduleId}`
  }
  router.push(paths[cmd])
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div><h2>班次管理</h2><p>围绕每个可报名班次查看容量、报名和考勤概览</p></div>
      <el-button type="primary" @click="openBatch">批量创建班次</el-button>
    </div>
    <el-card shadow="never" class="filter-card">
      <el-input v-model="query.keyword" clearable placeholder="搜索岗位/班次" style="width: 200px" @keyup.enter="resetPage" />
      <div class="date-quick-btns">
        <el-button :type="query.startDate === today() && query.endDate === today() ? 'primary' : 'default'" size="small" @click="setDateRange(today(), today())">今天</el-button>
        <el-button :type="query.startDate === tomorrow() && query.endDate === tomorrow() ? 'primary' : 'default'" size="small" @click="setDateRange(tomorrow(), tomorrow())">明天</el-button>
        <el-button :type="query.startDate === weekStart() && query.endDate === weekEnd() ? 'primary' : 'default'" size="small" @click="setDateRange(weekStart(), weekEnd())">本周</el-button>
      </div>
      <el-date-picker v-model="query.startDate" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 140px" @change="resetPage" />
      <el-date-picker v-model="query.endDate" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 140px" @change="resetPage" />
      <el-select v-model="query.status" clearable placeholder="班次状态" style="width: 130px" @change="resetPage">
        <el-option label="可报名" value="ACTIVE" />
        <el-option label="已满员" value="FULL" />
        <el-option label="已过期" value="EXPIRED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="resetPage">查询</el-button>
    </el-card>
    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="scheduleName" label="班次名称" min-width="190" />
        <el-table-column prop="jobTitle" label="岗位" min-width="150" />
        <el-table-column label="日期/时间" min-width="170"><template #default="{ row }"><div>{{ row.scheduleDate }}</div><div class="muted">{{ row.startTime }} - {{ row.endTime }}</div></template></el-table-column>
        <el-table-column label="联系人" min-width="150"><template #default="{ row }"><div>{{ row.contactName || '-' }}</div><div class="muted">{{ row.contactPhone || '-' }}</div></template></el-table-column>
        <el-table-column label="报名" width="120"><template #default="{ row }"><el-button link type="primary" @click="openApplicants(row)">{{ row.applicationCount || 0 }} 人</el-button><div class="muted">待审 {{ row.pendingCount || 0 }}</div></template></el-table-column>
        <el-table-column label="名额" width="130"><template #default="{ row }">{{ row.acceptedCount || 0 }}/{{ row.slotsAvailable || 0 }}<div class="muted">剩余 {{ row.remainingSlots || 0 }}</div></template></el-table-column>
        <el-table-column label="考勤概览" width="140"><template #default="{ row }">排班 {{ row.shiftCount || 0 }}<div class="muted danger">异常 {{ row.exceptionCount || 0 }}</div></template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="scheduleStatusTagType(row)">{{ scheduleStatusText(row) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="330" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link @click="openCopy(row)">复制</el-button><el-button link @click="exportSchedule(row)">导出</el-button><el-button v-if="canCancel(row)" link type="danger" @click="cancelSchedule(row)">取消</el-button><el-dropdown trigger="click" @command="(cmd) => navigateRelated(cmd, row)"><el-button link type="primary" style="margin-left:4px">相关 <el-icon><ArrowDown /></el-icon></el-button><template #dropdown><el-dropdown-menu><el-dropdown-item command="applications">报名审核</el-dropdown-item><el-dropdown-item command="shifts">考勤确认</el-dropdown-item><el-dropdown-item command="hours">薪资结算</el-dropdown-item></el-dropdown-menu></template></el-dropdown></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>
    <el-drawer v-model="applicantVisible" title="报名人列表" size="70%">
      <el-radio-group v-model="applicantQuery.status" @change="() => { applicantQuery.page = 1; loadApplicants() }">
        <el-radio-button label="">全部</el-radio-button><el-radio-button label="PENDING">待审核</el-radio-button><el-radio-button label="ACCEPTED">已通过</el-radio-button><el-radio-button label="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
      <el-button style="margin-left: 12px" type="primary" plain @click="exportCurrentApplicants">导出当前列表</el-button>
      <el-table v-loading="applicantLoading" :data="applicants" style="margin-top: 16px">
        <el-table-column prop="workerName" label="姓名" /><el-table-column prop="workerPhone" label="手机号" />
        <el-table-column label="实名"><template #default="{ row }"><el-tag :type="row.realNamed ? 'success' : 'info'">{{ row.realNamed ? '已实名' : '未实名' }}</el-tag></template></el-table-column>
        <el-table-column label="审核"><template #default="{ row }">{{ applicationStatusText(row.applicationStatus) }}</template></el-table-column>
        <el-table-column label="排班状态"><template #default="{ row }">{{ shiftStatusText(row.shiftStatus) }}</template></el-table-column>
        <el-table-column label="考勤状态"><template #default="{ row }">{{ attendanceStatusText(row.attendanceStatus) }}</template></el-table-column>
        <el-table-column label="补卡状态"><template #default="{ row }">{{ correctionStatusText(row.correctionStatus) }}</template></el-table-column>
        <el-table-column label="结算状态"><template #default="{ row }">{{ settlementStatusText(row.settlementStatus) }}</template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="applicantQuery.page" :total="applicantTotal" layout="total, prev, pager, next" @current-change="loadApplicants" />
    </el-drawer>
    <el-dialog v-model="editVisible" title="编辑班次" width="520px">
      <el-form :model="editForm" label-width="90px"><el-form-item label="班次名称"><el-input v-model="editForm.scheduleName" /></el-form-item><el-form-item label="日期"><el-date-picker v-model="editForm.scheduleDate" value-format="YYYY-MM-DD" /></el-form-item><el-form-item label="时间"><el-time-picker v-model="editForm.startTime" value-format="HH:mm:ss" /> <el-time-picker v-model="editForm.endTime" value-format="HH:mm:ss" /></el-form-item><el-form-item label="招聘人数"><el-input-number v-model="editForm.slotsAvailable" :min="1" /></el-form-item><el-form-item label="联系人"><el-input v-model="editForm.contactName" /></el-form-item><el-form-item label="电话"><el-input v-model="editForm.contactPhone" /></el-form-item></el-form>
      <template #footer><el-button @click="editVisible = false">取消</el-button><el-button type="primary" @click="submitEdit">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="copyVisible" title="复制班次" width="480px">
      <el-form :model="copyForm" label-width="80px"><el-form-item label="日期"><el-date-picker v-model="copyForm.scheduleDate" value-format="YYYY-MM-DD" /></el-form-item><el-form-item label="时间"><el-time-picker v-model="copyForm.startTime" value-format="HH:mm:ss" /> <el-time-picker v-model="copyForm.endTime" value-format="HH:mm:ss" /></el-form-item></el-form>
      <template #footer><el-button @click="copyVisible = false">取消</el-button><el-button type="primary" @click="submitCopy">复制</el-button></template>
    </el-dialog>
    <el-dialog v-model="batchVisible" title="批量创建班次" width="560px">
      <el-form :model="batchForm" label-width="90px">
        <el-form-item label="岗位"><el-select v-model="batchForm.jobId" filterable placeholder="请选择岗位" style="width: 100%"><el-option v-for="job in jobOptions" :key="job.id" :label="job.title" :value="job.id" /></el-select></el-form-item>
        <el-form-item label="日期范围"><el-date-picker v-model="batchForm.startDate" value-format="YYYY-MM-DD" placeholder="开始日期" /> <el-date-picker v-model="batchForm.endDate" value-format="YYYY-MM-DD" placeholder="结束日期" /></el-form-item>
        <el-form-item label="周几"><el-checkbox-group v-model="batchForm.weekdays"><el-checkbox-button v-for="day in [1,2,3,4,5,6,7]" :key="day" :label="day">周{{ ['一','二','三','四','五','六','日'][day - 1] }}</el-checkbox-button></el-checkbox-group></el-form-item>
        <el-form-item label="时间"><el-time-picker v-model="batchForm.startTime" value-format="HH:mm:ss" /> <el-time-picker v-model="batchForm.endTime" value-format="HH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="batchVisible = false">取消</el-button><el-button type="primary" @click="submitBatch">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
h2 { margin: 0; font-size: 24px; }
p { margin: 6px 0 0; color: #6b7280; }
.filter-card { margin-bottom: 16px; }
.filter-card :deep(.el-card__body) { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.date-quick-btns { display: flex; gap: 4px; }
.muted { color: #8a94a6; font-size: 12px; line-height: 20px; }
.danger { color: #ef4444; }
</style>
