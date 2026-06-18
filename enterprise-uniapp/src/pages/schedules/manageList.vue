<template>
  <view class="page">
    <view class="hero"><view><text class="hero-title">班次管理</text><text class="hero-sub">按班次查看报名、容量和考勤概览</text></view><button size="mini" class="hero-btn" @click="openBatch">批量创建</button></view>
    <view class="filters">
      <view v-for="item in statusTabs" :key="item.value" class="pill" :class="{ active: query.status === item.value }" @click="setStatus(item.value)">{{ item.label }}</view>
    </view>
    <view class="date-filter">
      <picker mode="date" :value="query.startDate" @change="setStartDate"><view class="date-picker">{{ query.startDate || '开始日期' }}</view></picker>
      <picker mode="date" :value="query.endDate" @change="setEndDate"><view class="date-picker">{{ query.endDate || '结束日期' }}</view></picker>
      <view v-if="query.startDate || query.endDate" class="clear-date" @click="clearDate">清空</view>
    </view>
    <uni-load-more v-if="loading" status="loading" />
    <view v-if="!loading && schedules.length === 0" class="empty">暂无班次</view>
    <view v-for="item in schedules" :key="item.id" class="card">
      <view class="card-head"><view><text class="title">{{ item.scheduleName || item.jobTitle }}</text><text class="sub">{{ item.jobTitle }}</text></view><text class="tag" :class="statusTagClass(item)">{{ scheduleStatusText(item) }}</text></view>
      <view class="time">{{ item.scheduleDate }} {{ item.startTime }}-{{ item.endTime }}</view>
      <view class="grid"><view><text class="num">{{ item.applicationCount || 0 }}</text><text>报名</text></view><view><text class="num">{{ item.pendingCount || 0 }}</text><text>待审</text></view><view><text class="num">{{ item.acceptedCount || 0 }}/{{ item.slotsAvailable || 0 }}</text><text>名额</text></view><view><text class="num warn">{{ item.exceptionCount || 0 }}</text><text>异常</text></view></view>
      <view class="contact"><text>{{ item.contactName || '暂无联系人' }}</text><text>{{ item.contactPhone || '' }}</text></view>
      <view class="actions"><button size="mini" @click="openApplicants(item)">报名人</button><button size="mini" @click="openEdit(item)">编辑</button><button size="mini" @click="copySchedule(item)">复制</button><button v-if="canCancel(item)" size="mini" @click="cancelSchedule(item)">取消</button></view>
    </view>
    <uni-load-more v-if="schedules.length" :status="moreStatus" @clickLoadMore="loadMore" />
    <view v-if="drawerVisible" class="overlay" @click="drawerVisible = false"><view class="drawer" @click.stop><view class="drawer-title">报名人</view><view class="filters small"><view v-for="item in applicantTabs" :key="item.value" class="pill" :class="{ active: applicantQuery.status === item.value }" @click="setApplicantStatus(item.value)">{{ item.label }}</view></view><view v-for="app in applicants" :key="app.applicationId" class="app-row"><view><text class="name">{{ app.workerName || '未实名' }}</text><text class="phone">{{ app.workerPhone || '-' }}</text></view><view class="right"><text>{{ applicationStatusText(app.applicationStatus) }}</text><text>{{ attendanceStatusText(app.attendanceStatus) || shiftStatusText(app.shiftStatus) }}</text></view></view></view></view>
    <view v-if="editVisible" class="overlay" @click="editVisible = false"><view class="drawer" @click.stop><view class="drawer-title">编辑班次</view><view class="form-row"><text>班次名称</text><input v-model="editForm.scheduleName" /></view><view class="form-row"><text>日期</text><picker mode="date" :value="editForm.scheduleDate" @change="editForm.scheduleDate = $event.detail.value"><view class="picker-value">{{ editForm.scheduleDate || '选择日期' }}</view></picker></view><view class="form-row"><text>开始时间</text><picker mode="time" :value="trimTime(editForm.startTime)" @change="editForm.startTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(editForm.startTime) || '选择时间' }}</view></picker></view><view class="form-row"><text>结束时间</text><picker mode="time" :value="trimTime(editForm.endTime)" @change="editForm.endTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(editForm.endTime) || '选择时间' }}</view></picker></view><view class="form-row"><text>招聘人数</text><input v-model="editForm.slotsAvailable" type="number" /></view><view class="form-row"><text>联系人</text><input v-model="editForm.contactName" /></view><view class="form-row"><text>联系电话</text><input v-model="editForm.contactPhone" /></view><button class="submit-btn" @click="submitEdit">保存</button></view></view>
    <view v-if="copyVisible" class="overlay" @click="copyVisible = false"><view class="drawer" @click.stop><view class="drawer-title">复制班次</view><view class="form-row"><text>日期</text><picker mode="date" :value="copyForm.scheduleDate" @change="copyForm.scheduleDate = $event.detail.value"><view class="picker-value">{{ copyForm.scheduleDate || '选择日期' }}</view></picker></view><view class="form-row"><text>开始时间</text><picker mode="time" :value="trimTime(copyForm.startTime)" @change="copyForm.startTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(copyForm.startTime) || '选择时间' }}</view></picker></view><view class="form-row"><text>结束时间</text><picker mode="time" :value="trimTime(copyForm.endTime)" @change="copyForm.endTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(copyForm.endTime) || '选择时间' }}</view></picker></view><button class="submit-btn" @click="submitCopy">复制</button></view></view>
    <view v-if="batchVisible" class="overlay" @click="batchVisible = false"><view class="drawer" @click.stop><view class="drawer-title">批量创建</view><view class="form-row"><text>岗位</text><picker :range="jobOptions" range-key="title" @change="selectBatchJob"><view class="picker-value">{{ selectedBatchJobTitle || '选择岗位' }}</view></picker></view><view class="form-row"><text>开始日期</text><picker mode="date" :value="batchForm.startDate" @change="batchForm.startDate = $event.detail.value"><view class="picker-value">{{ batchForm.startDate || '选择日期' }}</view></picker></view><view class="form-row"><text>结束日期</text><picker mode="date" :value="batchForm.endDate" @change="batchForm.endDate = $event.detail.value"><view class="picker-value">{{ batchForm.endDate || '选择日期' }}</view></picker></view><view class="weekday-row"><view v-for="day in weekdays" :key="day.value" class="pill" :class="{ active: batchForm.weekdays.includes(day.value) }" @click="toggleWeekday(day.value)">{{ day.label }}</view></view><view class="form-row"><text>开始时间</text><picker mode="time" :value="trimTime(batchForm.startTime)" @change="batchForm.startTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(batchForm.startTime) || '选择时间' }}</view></picker></view><view class="form-row"><text>结束时间</text><picker mode="time" :value="trimTime(batchForm.endTime)" @change="batchForm.endTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(batchForm.endTime) || '选择时间' }}</view></picker></view><button class="submit-btn" @click="submitBatch">创建</button></view></view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listManagedSchedules, listScheduleApplicants, copyManagedSchedule, updateManagedSchedule, batchCreateManagedSchedules } from '@/api/schedules'
import { getJobs } from '@/api/jobs'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()

const schedules = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ status: '', startDate: '', endDate: '', page: 1, pageSize: 10 })
const drawerVisible = ref(false)
const editVisible = ref(false)
const copyVisible = ref(false)
const batchVisible = ref(false)
const jobOptions = ref([])
const selectedBatchJobTitle = ref('')
const currentSchedule = ref(null)
const applicants = ref([])
const applicantQuery = reactive({ status: '', page: 1, pageSize: 20 })
const editForm = reactive({ id: null, scheduleName: '', scheduleDate: '', startTime: '', endTime: '', slotsAvailable: 1, contactName: '', contactPhone: '' })
const copyForm = reactive({ sourceScheduleId: null, scheduleDate: '', startTime: '', endTime: '' })
const batchForm = reactive({ jobId: '', startDate: '', endDate: '', weekdays: [], startTime: '', endTime: '' })
const statusTabs = [{ label: '全部', value: '' }, { label: '可报名', value: 'ACTIVE' }, { label: '已取消', value: 'CANCELLED' }]
const applicantTabs = [{ label: '全部', value: '' }, { label: '待审核', value: 'PENDING' }, { label: '已通过', value: 'ACCEPTED' }, { label: '已拒绝', value: 'REJECTED' }]
const weekdays = [{ label: '一', value: 1 }, { label: '二', value: 2 }, { label: '三', value: 3 }, { label: '四', value: 4 }, { label: '五', value: 5 }, { label: '六', value: 6 }, { label: '日', value: 7 }]
const moreStatus = computed(() => schedules.value.length >= total.value ? 'noMore' : 'more')
function isExpiredSchedule(item) { return item.scheduleDate && item.endTime && new Date(`${item.scheduleDate}T${item.endTime}`).getTime() < Date.now() }
function scheduleStatusText(item) { if (item.status === 'CANCELLED') return '已取消'; if (isExpiredSchedule(item)) return '已过期'; if (Number(item.remainingSlots || 0) <= 0) return '已满员'; return '可报名' }
function statusTagClass(item) { return item.status === 'CANCELLED' ? 'cancelled' : isExpiredSchedule(item) ? 'expired' : Number(item.remainingSlots || 0) <= 0 ? 'full' : 'active' }
function applicationStatusText(status) { return { PENDING: '待审核', ACCEPTED: '已通过', REJECTED: '已拒绝' }[status] || status || '-' }
function shiftStatusText(status) { return { SCHEDULED: '待上岗', ON_DUTY: '工作中', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', EARLY: '早退', LATE_EARLY_LEAVE: '迟到并早退', CANCELLED: '已取消' }[status] || '-' }
function attendanceStatusText(status) { return { CHECKED_IN: '已签到', CHECKED_OUT: '已签退', NORMAL: '正常', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', EARLY: '早退', LATE_EARLY_LEAVE: '迟到并早退', PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消' }[status] || '' }
async function loadData(reset = false) {
  if (reset) query.page = 1
  loading.value = true
  try {
    const res = await listManagedSchedules(query)
    const list = res.records || []
    schedules.value = query.page === 1 ? list : schedules.value.concat(list)
    total.value = res.total || list.length
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}
function setStatus(status) { query.status = status; loadData(true) }
function setStartDate(e) { query.startDate = e.detail.value; loadData(true) }
function setEndDate(e) { query.endDate = e.detail.value; loadData(true) }
function clearDate() { query.startDate = ''; query.endDate = ''; loadData(true) }
function loadMore() { if (schedules.value.length < total.value) { query.page += 1; loadData() } }
async function openApplicants(item) { currentSchedule.value = item; drawerVisible.value = true; applicantQuery.status = ''; await loadApplicants() }
async function loadApplicants() { const res = await listScheduleApplicants(currentSchedule.value.id, applicantQuery); applicants.value = res.records || [] }
function setApplicantStatus(status) { applicantQuery.status = status; loadApplicants() }
function trimTime(value) { return value ? String(value).slice(0, 5) : '' }
function withSeconds(value) { return value && value.length === 5 ? `${value}:00` : value }
function openEdit(item) { Object.assign(editForm, { id: item.id, scheduleName: item.scheduleName || item.jobTitle || '', scheduleDate: item.scheduleDate || '', startTime: item.startTime || '', endTime: item.endTime || '', slotsAvailable: item.slotsAvailable || 1, contactName: item.contactName || '', contactPhone: item.contactPhone || '' }); editVisible.value = true }
async function submitEdit() { await updateManagedSchedule({ ...editForm, slotsAvailable: Number(editForm.slotsAvailable || 1) }); uni.showToast({ title: '保存成功', icon: 'success' }); editVisible.value = false; loadData(true) }
function copySchedule(item) { Object.assign(copyForm, { sourceScheduleId: item.id, scheduleDate: item.scheduleDate, startTime: item.startTime, endTime: item.endTime }); copyVisible.value = true }
async function submitCopy() { await copyManagedSchedule(copyForm); uni.showToast({ title: '复制成功', icon: 'success' }); copyVisible.value = false; loadData(true) }
async function openBatch() { batchVisible.value = true; if (jobOptions.value.length === 0) { const res = await getJobs(authStore.companyId); jobOptions.value = Array.isArray(res) ? res : (res.records || res.data || []) } }
function selectBatchJob(e) { const job = jobOptions.value[Number(e.detail.value)]; if (!job) return; batchForm.jobId = job.id; selectedBatchJobTitle.value = job.title }
function toggleWeekday(value) { const index = batchForm.weekdays.indexOf(value); index >= 0 ? batchForm.weekdays.splice(index, 1) : batchForm.weekdays.push(value) }
async function submitBatch() { if (!batchForm.jobId || !batchForm.startDate || !batchForm.endDate || !batchForm.startTime || !batchForm.endTime) { uni.showToast({ title: '请填写完整班次信息', icon: 'none' }); return } await batchCreateManagedSchedules({ jobId: Number(batchForm.jobId), startDate: batchForm.startDate, endDate: batchForm.endDate, weekdays: [...batchForm.weekdays], startTime: batchForm.startTime, endTime: batchForm.endTime }); uni.showToast({ title: '创建成功', icon: 'success' }); batchVisible.value = false; loadData(true) }
function isFutureSchedule(item) { return item.scheduleDate && item.startTime && new Date(`${item.scheduleDate}T${item.startTime}`).getTime() > Date.now() }
function canCancel(item) { return item.status !== 'CANCELLED' && isFutureSchedule(item) }
async function cancelSchedule(item) { await updateManagedSchedule({ id: item.id, status: 'CANCELLED' }); uni.showToast({ title: '已取消', icon: 'success' }); loadData(true) }
onLoad(() => loadData(true))
onPullDownRefresh(() => loadData(true))
onReachBottom(loadMore)
</script>

<style scoped>
.page { min-height: 100vh; padding: 24rpx; background: #f6f8f7; box-sizing: border-box; }
.hero { display: flex; justify-content: space-between; align-items: center; gap: 20rpx; padding: 30rpx; border-radius: 28rpx; background: linear-gradient(135deg, #16a34a, #22c55e); color: #fff; }
.hero-title { display: block; font-size: 40rpx; font-weight: 800; }
.hero-sub { display: block; margin-top: 10rpx; font-size: 24rpx; opacity: .9; }
.hero-btn { margin: 0; border: 0; border-radius: 999rpx; background: rgba(255,255,255,.22); color: #fff; }
.filters { display: flex; gap: 16rpx; margin: 24rpx 0; }
.filters.small { margin: 12rpx 0 20rpx; }
.date-filter { display: flex; align-items: center; gap: 14rpx; margin-bottom: 20rpx; }
.date-picker, .clear-date { padding: 12rpx 22rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 24rpx; }
.clear-date { color: #16a34a; background: #dcfce7; }
.pill { padding: 12rpx 24rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 24rpx; }
.pill.active { background: #dcfce7; color: #16a34a; font-weight: 700; }
.card { margin-bottom: 20rpx; padding: 24rpx; border-radius: 24rpx; background: #fff; box-shadow: 0 8rpx 24rpx rgba(15,23,42,.05); }
.card-head { display: flex; justify-content: space-between; gap: 16rpx; }
.title { display: block; font-size: 32rpx; font-weight: 800; color: #111827; }
.sub, .time, .contact { display: block; margin-top: 8rpx; font-size: 24rpx; color: #64748b; }
.tag { padding: 8rpx 16rpx; border-radius: 999rpx; background: #dcfce7; color: #16a34a; font-size: 22rpx; height: 32rpx; }
.tag.active { background: #dcfce7; color: #16a34a; }
.tag.cancelled { background: #fee2e2; color: #dc2626; }
.tag.expired { background: #e5e7eb; color: #64748b; }
.tag.normal { background: #e5e7eb; color: #4b5563; }
.tag.full { background: #fef3c7; color: #d97706; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); margin: 22rpx 0; padding: 18rpx; border-radius: 18rpx; background: #f8fafc; text-align: center; color: #64748b; font-size: 22rpx; }
.num { display: block; font-size: 28rpx; font-weight: 800; color: #111827; }
.warn { color: #ef4444; }
.actions { display: flex; justify-content: flex-end; gap: 16rpx; }
.empty { padding: 80rpx 0; text-align: center; color: #94a3b8; }
.overlay { position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: flex-end; z-index: 99; }
.drawer { width: 100%; max-height: 82vh; overflow: auto; padding: 28rpx; border-radius: 32rpx 32rpx 0 0; background: #fff; box-sizing: border-box; }
.drawer-title { font-size: 34rpx; font-weight: 800; }
.app-row { display: flex; justify-content: space-between; padding: 22rpx 0; border-bottom: 1rpx solid #eef2f7; }
.name { display: block; font-weight: 700; color: #111827; }
.phone, .right { display: block; font-size: 24rpx; color: #64748b; text-align: right; }
.form-row { margin-top: 22rpx; }
.form-row text { display: block; margin-bottom: 10rpx; color: #475569; font-size: 24rpx; }
.form-row input, .picker-value { min-height: 72rpx; padding: 0 22rpx; border-radius: 18rpx; background: #f8fafc; color: #111827; font-size: 28rpx; line-height: 72rpx; box-sizing: border-box; }
.weekday-row { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 22rpx; }
.submit-btn { margin-top: 28rpx; border: 0; border-radius: 999rpx; background: #16a34a; color: #fff; }
</style>
