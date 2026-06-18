<template>
  <view class="page">
    <view class="hero">
      <view class="hero-bg hero-bg-one"></view>
      <view class="hero-bg hero-bg-two"></view>
      <view class="hero-top">
        <view>
          <text class="hero-title">班次管理</text>
          <text class="hero-sub">围绕班次处理报名、容量和考勤</text>
        </view>
        <button size="mini" class="hero-btn" @click="openBatch">批量创建</button>
      </view>
      <view class="stats">
        <view class="stat-card">
          <text class="stat-num">{{ todayScheduleCount }}</text>
          <text class="stat-label">今日班次</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ pendingReviewCount }}</text>
          <text class="stat-label">待审核</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ exceptionScheduleCount }}</text>
          <text class="stat-label">异常考勤</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-x class="status-scroll" show-scrollbar="false">
      <view class="status-tabs">
        <view v-for="item in statusTabs" :key="item.value" class="pill" :class="{ active: uiStatus === item.value }" @click="setStatus(item.value)">{{ item.label }}</view>
      </view>
    </scroll-view>

    <view class="datebar">
      <view v-for="item in dateModes" :key="item.value" class="date-item" :class="{ active: dateMode === item.value }" @click="setDateMode(item.value)">{{ item.label }}</view>
    </view>
    <view v-if="dateMode === 'custom'" class="custom-date">
      <picker mode="date" :value="query.startDate" @change="setStartDate"><view class="date-picker">{{ query.startDate || '开始日期' }}</view></picker>
      <picker mode="date" :value="query.endDate" @change="setEndDate"><view class="date-picker">{{ query.endDate || '结束日期' }}</view></picker>
      <view v-if="query.startDate || query.endDate" class="clear-date" @click="clearDate">清空</view>
    </view>

    <uni-load-more v-if="loading" status="loading" />
    <view v-if="!loading && visibleSchedules.length === 0" class="empty">暂无班次</view>

    <view v-for="item in visibleSchedules" :key="item.id" class="card" :class="{ cancelled: item.status === 'CANCELLED' }">
      <view class="card-head">
        <view class="card-title-wrap">
          <text class="title">{{ item.scheduleName || item.jobTitle }}</text>
          <text class="sub"><text class="sub-icon">🏷</text>{{ item.jobTitle || '岗位' }}</text>
        </view>
        <text class="tag" :class="statusTagClass(item)">{{ scheduleStatusText(item) }}</text>
      </view>

      <view class="meta-list">
        <view class="meta-line"><text class="meta-icon">⏰</text><text>{{ item.scheduleDate || '-' }}　{{ trimTime(item.startTime) || '--:--' }} - {{ trimTime(item.endTime) || '--:--' }}</text></view>
        <view class="meta-line"><text class="meta-icon">📍</text><text>{{ item.location || '暂无地点' }}</text></view>
        <view class="meta-line"><text class="meta-icon">👤</text><text>{{ item.contactName || '暂无联系人' }}　{{ maskPhone(item.contactPhone) }}</text></view>
      </view>

      <view class="progress-head">
        <text>报名进度</text>
        <text class="progress-text">{{ item.acceptedCount || 0 }} / {{ item.slotsAvailable || 0 }}</text>
      </view>
      <view class="progress-bar"><view class="progress-fill" :class="{ full: Number(item.remainingSlots || 0) <= 0 }" :style="{ width: progressPercent(item) + '%' }"></view></view>

      <view class="metrics">
        <view class="metric"><text class="metric-num">{{ item.applicationCount || 0 }}</text><text class="metric-label">报名</text></view>
        <view class="metric"><text class="metric-num">{{ item.pendingCount || 0 }}</text><text class="metric-label">待审</text></view>
        <view class="metric"><text class="metric-num">{{ item.remainingSlots || 0 }}</text><text class="metric-label">剩余</text></view>
        <view class="metric danger"><text class="metric-num">{{ item.exceptionCount || 0 }}</text><text class="metric-label">异常</text></view>
      </view>

      <view class="actions">
        <button size="mini" class="primary-action" @click="openApplicants(item)">报名人</button>
        <button size="mini" class="more-action" @click="openMore(item)">更多 ▾</button>
      </view>
    </view>

    <uni-load-more v-if="schedules.length" :status="moreStatus" @clickLoadMore="loadMore" />
    <button class="fab" @click="openBatch">＋</button>

    <view v-if="drawerVisible" class="overlay" @click="drawerVisible = false">
      <view class="drawer" @click.stop>
        <view class="grab"></view>
        <view class="drawer-title-row">
          <view>
            <text class="drawer-title">报名人</text>
            <text class="drawer-sub">{{ currentSchedule?.scheduleName || currentSchedule?.jobTitle || '' }}</text>
          </view>
          <text class="drawer-close" @click="drawerVisible = false">×</text>
        </view>
        <scroll-view scroll-x class="applicant-scroll" show-scrollbar="false">
          <view class="status-tabs small-tabs">
            <view v-for="item in applicantTabs" :key="item.value" class="pill" :class="{ active: applicantQuery.status === item.value }" @click="setApplicantStatus(item.value)">{{ item.label }}</view>
          </view>
        </scroll-view>
        <view v-if="applicants.length === 0" class="drawer-empty">暂无报名人</view>
        <view v-for="app in applicants" :key="app.applicationId" class="app-row">
          <view>
            <text class="name">{{ app.workerName || '未实名' }}</text>
            <text class="phone">{{ app.workerPhone || '-' }}</text>
          </view>
          <view class="app-right">
            <text class="app-status">{{ applicationStatusText(app.applicationStatus) }}</text>
            <text class="app-attendance">{{ attendanceStatusText(app.attendanceStatus) || shiftStatusText(app.shiftStatus) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="editVisible" class="overlay" @click="editVisible = false">
      <view class="drawer" @click.stop>
        <view class="grab"></view>
        <view class="drawer-title-row"><text class="drawer-title">编辑班次</text><text class="drawer-close" @click="editVisible = false">×</text></view>
        <view class="form-row"><text>班次名称</text><input v-model="editForm.scheduleName" /></view>
        <view class="form-row"><text>日期</text><picker mode="date" :value="editForm.scheduleDate" @change="editForm.scheduleDate = $event.detail.value"><view class="picker-value">{{ editForm.scheduleDate || '选择日期' }}</view></picker></view>
        <view class="form-row"><text>开始时间</text><picker mode="time" :value="trimTime(editForm.startTime)" @change="editForm.startTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(editForm.startTime) || '选择时间' }}</view></picker></view>
        <view class="form-row"><text>结束时间</text><picker mode="time" :value="trimTime(editForm.endTime)" @change="editForm.endTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(editForm.endTime) || '选择时间' }}</view></picker></view>
        <view class="form-row"><text>招聘人数</text><input v-model="editForm.slotsAvailable" type="number" /></view>
        <view class="form-row"><text>联系人</text><input v-model="editForm.contactName" /></view>
        <view class="form-row"><text>联系电话</text><input v-model="editForm.contactPhone" /></view>
        <button class="submit-btn" @click="submitEdit">保存</button>
      </view>
    </view>

    <view v-if="copyVisible" class="overlay" @click="copyVisible = false">
      <view class="drawer" @click.stop>
        <view class="grab"></view>
        <view class="drawer-title-row"><text class="drawer-title">复制班次</text><text class="drawer-close" @click="copyVisible = false">×</text></view>
        <view class="form-row"><text>日期</text><picker mode="date" :value="copyForm.scheduleDate" @change="copyForm.scheduleDate = $event.detail.value"><view class="picker-value">{{ copyForm.scheduleDate || '选择日期' }}</view></picker></view>
        <view class="form-row"><text>开始时间</text><picker mode="time" :value="trimTime(copyForm.startTime)" @change="copyForm.startTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(copyForm.startTime) || '选择时间' }}</view></picker></view>
        <view class="form-row"><text>结束时间</text><picker mode="time" :value="trimTime(copyForm.endTime)" @change="copyForm.endTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(copyForm.endTime) || '选择时间' }}</view></picker></view>
        <button class="submit-btn" @click="submitCopy">复制</button>
      </view>
    </view>

    <view v-if="batchVisible" class="overlay" @click="batchVisible = false">
      <view class="drawer" @click.stop>
        <view class="grab"></view>
        <view class="drawer-title-row">
          <view><text class="drawer-title">批量创建班次</text><text class="drawer-sub">岗位默认带入联系人</text></view>
          <text class="drawer-close" @click="batchVisible = false">×</text>
        </view>
        <view class="form-row"><text>岗位</text><picker :range="jobOptions" range-key="title" @change="selectBatchJob"><view class="picker-value">{{ selectedBatchJobTitle || '选择岗位' }}</view></picker></view>
        <view class="form-row"><text>开始日期</text><picker mode="date" :value="batchForm.startDate" @change="batchForm.startDate = $event.detail.value"><view class="picker-value">{{ batchForm.startDate || '选择日期' }}</view></picker></view>
        <view class="form-row"><text>结束日期</text><picker mode="date" :value="batchForm.endDate" @change="batchForm.endDate = $event.detail.value"><view class="picker-value">{{ batchForm.endDate || '选择日期' }}</view></picker></view>
        <view class="weekday-row"><view v-for="day in weekdays" :key="day.value" class="weekday" :class="{ active: batchForm.weekdays.includes(day.value) }" @click="toggleWeekday(day.value)">{{ day.label }}</view></view>
        <view class="form-row"><text>开始时间</text><picker mode="time" :value="trimTime(batchForm.startTime)" @change="batchForm.startTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(batchForm.startTime) || '选择时间' }}</view></picker></view>
        <view class="form-row"><text>结束时间</text><picker mode="time" :value="trimTime(batchForm.endTime)" @change="batchForm.endTime = withSeconds($event.detail.value)"><view class="picker-value">{{ trimTime(batchForm.endTime) || '选择时间' }}</view></picker></view>
        <button class="submit-btn" @click="submitBatch">创建班次</button>
      </view>
    </view>
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
const uiStatus = ref('')
const dateMode = ref('today')
const query = reactive({ status: '', startDate: '', endDate: '', page: 1, pageSize: 10 })
const drawerVisible = ref(false)
const editVisible = ref(false)
const copyVisible = ref(false)
const batchVisible = ref(false)
const currentSchedule = ref(null)
const applicants = ref([])
const jobOptions = ref([])
const selectedBatchJobTitle = ref('')
const applicantQuery = reactive({ status: '', page: 1, pageSize: 20 })
const editForm = reactive({ id: null, scheduleName: '', scheduleDate: '', startTime: '', endTime: '', slotsAvailable: 1, contactName: '', contactPhone: '' })
const copyForm = reactive({ sourceScheduleId: null, scheduleDate: '', startTime: '', endTime: '' })
const batchForm = reactive({ jobId: '', startDate: '', endDate: '', weekdays: [], startTime: '', endTime: '' })
const statusTabs = [{ label: '全部', value: '' }, { label: '可报名', value: 'ACTIVE' }, { label: '已满员', value: 'FULL' }, { label: '已取消', value: 'CANCELLED' }, { label: '已过期', value: 'EXPIRED' }]
const applicantTabs = [{ label: '全部', value: '' }, { label: '待审核', value: 'PENDING' }, { label: '已通过', value: 'ACCEPTED' }, { label: '已拒绝', value: 'REJECTED' }]
const dateModes = [{ label: '今天', value: 'today' }, { label: '明天', value: 'tomorrow' }, { label: '本周', value: 'week' }, { label: '自定义', value: 'custom' }]
const weekdays = [{ label: '一', value: 1 }, { label: '二', value: 2 }, { label: '三', value: 3 }, { label: '四', value: 4 }, { label: '五', value: 5 }, { label: '六', value: 6 }, { label: '日', value: 7 }]

const visibleSchedules = computed(() => schedules.value.filter(item => {
  if (uiStatus.value === 'FULL') return scheduleStatusText(item) === '已满员'
  if (uiStatus.value === 'EXPIRED') return scheduleStatusText(item) === '已过期'
  return true
}))
const moreStatus = computed(() => schedules.value.length >= total.value ? 'noMore' : 'more')
const todayScheduleCount = computed(() => schedules.value.filter(item => item.scheduleDate === formatDate(new Date())).length)
const pendingReviewCount = computed(() => schedules.value.reduce((sum, item) => sum + Number(item.pendingCount || 0), 0))
const exceptionScheduleCount = computed(() => schedules.value.reduce((sum, item) => sum + Number(item.exceptionCount || 0), 0))

function pad(value) { return String(value).padStart(2, '0') }
function formatDate(date) { return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` }
function addDays(date, days) { const next = new Date(date); next.setDate(next.getDate() + days); return next }
function getWeekEnd(date) { const next = new Date(date); next.setDate(next.getDate() + (7 - next.getDay()) % 7); return next }
function isExpiredSchedule(item) { return item.scheduleDate && item.endTime && new Date(`${item.scheduleDate}T${item.endTime}`).getTime() < Date.now() }
function scheduleStatusText(item) { if (item.status === 'CANCELLED') return '已取消'; if (isExpiredSchedule(item)) return '已过期'; if (Number(item.remainingSlots || 0) <= 0) return '已满员'; return '可报名' }
function statusTagClass(item) { return item.status === 'CANCELLED' ? 'cancelled' : isExpiredSchedule(item) ? 'expired' : Number(item.remainingSlots || 0) <= 0 ? 'full' : 'active' }
function applicationStatusText(status) { return { PENDING: '待审核', ACCEPTED: '已通过', REJECTED: '已拒绝' }[status] || status || '-' }
function shiftStatusText(status) { return { SCHEDULED: '待上岗', ON_DUTY: '工作中', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', EARLY: '早退', LATE_EARLY_LEAVE: '迟到并早退', CANCELLED: '已取消' }[status] || '' }
function attendanceStatusText(status) { return { CHECKED_IN: '已签到', CHECKED_OUT: '已签退', NORMAL: '正常', COMPLETED: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退', EARLY: '早退', LATE_EARLY_LEAVE: '迟到并早退', PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消' }[status] || '' }
function trimTime(value) { return value ? String(value).slice(0, 5) : '' }
function withSeconds(value) { return value && value.length === 5 ? `${value}:00` : value }
function maskPhone(phone) { if (!phone) return ''; const text = String(phone); return text.length >= 7 ? `${text.slice(0, 3)}****${text.slice(-4)}` : text }
function progressPercent(item) { const totalCount = Number(item.slotsAvailable || 0); if (totalCount <= 0) return 0; return Math.min(100, Math.round(Number(item.acceptedCount || 0) * 100 / totalCount)) }

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
function setStatus(status) {
  uiStatus.value = status
  query.status = status === 'ACTIVE' || status === 'CANCELLED' ? status : ''
  loadData(true)
}
function setDateMode(mode) {
  dateMode.value = mode
  const now = new Date()
  if (mode === 'today') {
    query.startDate = formatDate(now)
    query.endDate = formatDate(now)
  } else if (mode === 'tomorrow') {
    const tomorrow = addDays(now, 1)
    query.startDate = formatDate(tomorrow)
    query.endDate = formatDate(tomorrow)
  } else if (mode === 'week') {
    query.startDate = formatDate(now)
    query.endDate = formatDate(getWeekEnd(now))
  }
  if (mode !== 'custom') loadData(true)
}
function setStartDate(e) { query.startDate = e.detail.value; loadData(true) }
function setEndDate(e) { query.endDate = e.detail.value; loadData(true) }
function clearDate() { query.startDate = ''; query.endDate = ''; loadData(true) }
function loadMore() { if (schedules.value.length < total.value) { query.page += 1; loadData() } }
async function openApplicants(item) { currentSchedule.value = item; drawerVisible.value = true; applicantQuery.status = ''; await loadApplicants() }
async function loadApplicants() { const res = await listScheduleApplicants(currentSchedule.value.id, applicantQuery); applicants.value = res.records || [] }
function setApplicantStatus(status) { applicantQuery.status = status; loadApplicants() }
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
function openMore(item) {
  const itemList = canCancel(item) ? ['编辑班次', '复制班次', '取消班次'] : ['编辑班次', '复制班次']
  uni.showActionSheet({
    itemList,
    success: ({ tapIndex }) => {
      if (tapIndex === 0) openEdit(item)
      if (tapIndex === 1) copySchedule(item)
      if (tapIndex === 2) cancelSchedule(item)
    }
  })
}

onLoad(() => { setDateMode('today') })
onPullDownRefresh(() => loadData(true))
onReachBottom(loadMore)
</script>

<style scoped>
.page { position: relative; min-height: 100vh; padding: 24rpx 24rpx 140rpx; background: #f4f7f5; box-sizing: border-box; }
.hero { position: relative; overflow: hidden; padding: 36rpx 32rpx; border-radius: 36rpx; background: linear-gradient(135deg, #059669 0%, #16a34a 52%, #86efac 130%); color: #fff; box-shadow: 0 22rpx 52rpx rgba(22, 163, 74, .22); }
.hero-bg { position: absolute; border-radius: 999rpx; background: rgba(255,255,255,.16); }
.hero-bg-one { width: 240rpx; height: 240rpx; right: -70rpx; top: -78rpx; }
.hero-bg-two { width: 130rpx; height: 130rpx; right: 80rpx; bottom: -54rpx; }
.hero-top { position: relative; z-index: 1; display: flex; align-items: flex-start; justify-content: space-between; gap: 24rpx; }
.hero-title { display: block; font-size: 46rpx; line-height: 1.15; font-weight: 900; letter-spacing: 1rpx; }
.hero-sub { display: block; margin-top: 12rpx; font-size: 24rpx; opacity: .9; }
.hero-btn { flex-shrink: 0; height: 60rpx; margin: 0; padding: 0 24rpx; border: 0; border-radius: 999rpx; background: rgba(255,255,255,.22); color: #fff; font-size: 24rpx; font-weight: 800; line-height: 60rpx; backdrop-filter: blur(10rpx); }
.hero-btn::after, .primary-action::after, .more-action::after, .submit-btn::after, .fab::after { border: 0; }
.stats { position: relative; z-index: 1; display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin-top: 34rpx; }
.stat-card { padding: 20rpx 18rpx; border: 1rpx solid rgba(255,255,255,.2); border-radius: 24rpx; background: rgba(255,255,255,.18); }
.stat-num { display: block; font-size: 42rpx; line-height: 1; font-weight: 900; }
.stat-label { display: block; margin-top: 10rpx; font-size: 21rpx; opacity: .86; }
.status-scroll { margin-top: 28rpx; white-space: nowrap; }
.status-tabs { display: flex; gap: 14rpx; }
.pill { flex: 0 0 auto; padding: 16rpx 26rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 24rpx; box-shadow: 0 8rpx 24rpx rgba(15,23,42,.04); }
.pill.active { background: #dcfce7; color: #15803d; font-weight: 900; box-shadow: inset 0 0 0 1rpx rgba(22,163,74,.16); }
.datebar { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12rpx; margin-top: 22rpx; }
.date-item { height: 68rpx; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; background: #fff; color: #64748b; font-size: 24rpx; }
.date-item.active { background: #0f172a; color: #fff; font-weight: 900; }
.custom-date { display: flex; align-items: center; gap: 14rpx; margin-top: 18rpx; }
.date-picker, .clear-date { padding: 14rpx 24rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 24rpx; }
.clear-date { color: #16a34a; background: #dcfce7; }
.card { margin-top: 24rpx; padding: 30rpx; border: 1rpx solid rgba(226,232,240,.85); border-radius: 34rpx; background: #fff; box-shadow: 0 18rpx 42rpx rgba(15,23,42,.07); }
.card.cancelled { opacity: .72; }
.card-head { display: flex; justify-content: space-between; gap: 18rpx; }
.card-title-wrap { min-width: 0; }
.title { display: block; font-size: 34rpx; line-height: 1.22; font-weight: 900; color: #0f172a; }
.sub { display: block; margin-top: 10rpx; color: #64748b; font-size: 23rpx; }
.sub-icon { margin-right: 8rpx; }
.tag { flex-shrink: 0; height: 48rpx; padding: 0 18rpx; border-radius: 999rpx; font-size: 22rpx; line-height: 48rpx; font-weight: 900; }
.tag.active { background: #dcfce7; color: #15803d; }
.tag.cancelled { background: #fee2e2; color: #dc2626; }
.tag.expired { background: #e5e7eb; color: #475569; }
.tag.full { background: #fef3c7; color: #b45309; }
.meta-list { display: grid; gap: 14rpx; margin-top: 24rpx; color: #475569; font-size: 24rpx; }
.meta-line { display: flex; align-items: center; gap: 12rpx; line-height: 1.45; }
.meta-icon { width: 42rpx; height: 42rpx; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; background: #f1f5f9; font-size: 24rpx; }
.progress-head { display: flex; justify-content: space-between; align-items: center; margin-top: 28rpx; font-size: 24rpx; font-weight: 900; color: #0f172a; }
.progress-text { color: #64748b; font-weight: 800; }
.progress-bar { overflow: hidden; height: 16rpx; margin-top: 16rpx; border-radius: 999rpx; background: #e2e8f0; }
.progress-fill { height: 100%; border-radius: 999rpx; background: linear-gradient(90deg, #16a34a, #86efac); }
.progress-fill.full { background: linear-gradient(90deg, #f59e0b, #fde68a); }
.metrics { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12rpx; margin-top: 24rpx; }
.metric { padding: 16rpx 6rpx; border-radius: 22rpx; background: #f8fafc; text-align: center; }
.metric-num { display: block; font-size: 31rpx; line-height: 1; font-weight: 900; color: #0f172a; }
.metric-label { display: block; margin-top: 9rpx; color: #64748b; font-size: 21rpx; }
.metric.danger .metric-num { color: #ef4444; }
.actions { display: flex; justify-content: space-between; align-items: center; margin-top: 26rpx; }
.primary-action { width: 210rpx; height: 70rpx; margin: 0; border: 0; border-radius: 999rpx; background: #16a34a; color: #fff; font-size: 26rpx; line-height: 70rpx; font-weight: 900; box-shadow: 0 12rpx 26rpx rgba(22,163,74,.22); }
.more-action { width: 150rpx; height: 70rpx; margin: 0; border: 0; border-radius: 999rpx; background: #f1f5f9; color: #334155; font-size: 26rpx; line-height: 70rpx; font-weight: 900; }
.fab { position: fixed; right: 32rpx; bottom: 42rpx; z-index: 10; width: 104rpx; height: 104rpx; border: 0; border-radius: 38rpx; background: #16a34a; color: #fff; font-size: 48rpx; line-height: 104rpx; box-shadow: 0 20rpx 42rpx rgba(22,163,74,.32); }
.empty { padding: 100rpx 0; text-align: center; color: #94a3b8; font-size: 26rpx; }
.overlay { position: fixed; inset: 0; z-index: 99; display: flex; align-items: flex-end; background: rgba(15,23,42,.38); }
.drawer { width: 100%; max-height: 82vh; overflow: auto; padding: 20rpx 28rpx 36rpx; border-radius: 42rpx 42rpx 0 0; background: #fff; box-sizing: border-box; box-shadow: 0 -20rpx 54rpx rgba(15,23,42,.18); }
.grab { width: 76rpx; height: 8rpx; margin: 0 auto 24rpx; border-radius: 999rpx; background: #cbd5e1; }
.drawer-title-row { display: flex; justify-content: space-between; align-items: flex-start; gap: 20rpx; }
.drawer-title { display: block; color: #0f172a; font-size: 36rpx; font-weight: 900; }
.drawer-sub { display: block; margin-top: 8rpx; color: #64748b; font-size: 23rpx; }
.drawer-close { color: #94a3b8; font-size: 44rpx; line-height: 1; }
.applicant-scroll { margin-top: 22rpx; white-space: nowrap; }
.small-tabs { gap: 12rpx; }
.drawer-empty { padding: 56rpx 0; text-align: center; color: #94a3b8; font-size: 25rpx; }
.app-row { display: flex; justify-content: space-between; gap: 18rpx; padding: 24rpx 0; border-bottom: 1rpx solid #eef2f7; }
.name { display: block; color: #0f172a; font-size: 28rpx; font-weight: 800; }
.phone { display: block; margin-top: 8rpx; color: #64748b; font-size: 24rpx; }
.app-right { text-align: right; }
.app-status { display: block; color: #15803d; font-size: 25rpx; font-weight: 800; }
.app-attendance { display: block; margin-top: 8rpx; color: #64748b; font-size: 23rpx; }
.form-row { margin-top: 24rpx; }
.form-row text { display: block; margin-bottom: 12rpx; color: #64748b; font-size: 23rpx; font-weight: 800; }
.form-row input, .picker-value { min-height: 82rpx; padding: 0 24rpx; border-radius: 22rpx; background: #f8fafc; color: #0f172a; font-size: 27rpx; line-height: 82rpx; box-sizing: border-box; }
.weekday-row { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 24rpx; }
.weekday { width: 58rpx; height: 58rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; background: #f1f5f9; color: #64748b; font-size: 24rpx; font-weight: 900; }
.weekday.active { background: #dcfce7; color: #15803d; }
.submit-btn { height: 84rpx; margin-top: 32rpx; border: 0; border-radius: 999rpx; background: #16a34a; color: #fff; font-size: 29rpx; line-height: 84rpx; font-weight: 900; }
</style>
