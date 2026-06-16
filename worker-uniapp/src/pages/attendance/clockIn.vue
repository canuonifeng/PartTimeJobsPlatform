<template>
  <view class="clockin-page">
    <view class="top-panel">
      <view class="date-header">
        <view><text class="date-title">{{ todayDate }}</text><text class="date-subtitle">{{ todayName }}</text></view>
        <view class="date-pill">{{ stats.total }} 个班次</view>
      </view>
      <view class="stats-card">
        <view class="stat-item"><text class="stat-value">{{ stats.total }}</text><text class="stat-label">今日排班</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ stats.checkedIn }}</text><text class="stat-label">已签到</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ stats.completed }}</text><text class="stat-label">已完成</text></view>
      </view>
      <view class="notice-card"><text class="notice-dot"></text><text class="notice-text">请在岗位地点附近完成签到签退，系统将获取当前位置用于打卡校验。</text></view>
    </view>
    <uni-load-more v-if="loading && page === 1" status="loading" />
    <scroll-view class="shift-scroll" scroll-y @scrolltolower="loadMore">
      <view class="section-row"><text class="section-title">打卡记录</text><text class="section-subtitle">{{ allShifts.length }} 条记录</text></view>
      <view v-if="allShifts.length === 0 && !loading" class="empty-state"><text class="empty-text">暂无打卡记录</text></view>
      <view v-for="shift in allShifts" :key="shift.id" class="shift-card">
        <view class="shift-header">
          <view class="job-left"><view class="job-icon"><text>岗</text></view><view class="job-info"><text class="shift-title">{{ shift.jobTitle }}</text><text class="shift-location">{{ shift.location }}</text></view></view>
          <view class="status-badge" :class="statusClass(shift.status, shift)">{{ statusText(shift.status, shift) }}</view>
        </view>
        <view class="time-box"><view><text class="time-label">工作时间</text><text class="time-value">{{ shift.startTime }} - {{ shift.endTime }}</text></view><view class="date-box"><text class="date-day">{{ shift.date.slice(8) }}</text><text class="date-month">{{ shift.date.slice(5, 7) }}月</text></view></view>
        <view class="record-row"><view class="record-item"><text class="record-label">签到</text><text class="record-value">{{ shift.checkInTime || '未签到' }}</text></view><view class="record-item"><text class="record-label">签退</text><text class="record-value">{{ shift.checkOutTime || '未签退' }}</text></view></view>
      </view>
      <uni-load-more v-if="allShifts.length > 0" :status="moreStatus" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyShifts } from '@/api/schedule'

interface Shift { id: number; jobTitle: string; location: string; startTime: string; endTime: string; date: string; status: string; checkedIn: boolean; checkedOut: boolean; checkInTime?: string; checkOutTime?: string; attendanceId?: number }

const PAGE_SIZE = 20
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const total = ref(0)
const allShifts = ref<Shift[]>([])
const weekDayNames = ['日', '一', '二', '三', '四', '五', '六']
const checkedInStatuses = ['ON_DUTY', 'COMPLETED', 'LATE', 'EARLY_LEAVE']
const checkedOutStatuses = ['COMPLETED', 'EARLY_LEAVE']

const todayDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${String(d.getMonth() + 1).padStart(2, '0')}月${String(d.getDate()).padStart(2, '0')}日`
})
const todayName = computed(() => `星期${weekDayNames[new Date().getDay()]}`)
const hasMore = computed(() => allShifts.value.length < total.value)
const moreStatus = computed(() => {
  if (loadingMore.value) return 'loading'
  if (!hasMore.value && allShifts.value.length > 0) return 'noMore'
  return 'more'
})
const stats = computed(() => {
  const today = formatFullDate(new Date())
  const todayShifts = allShifts.value.filter(s => s.date === today)
  return { total: todayShifts.length, checkedIn: todayShifts.filter(hasCheckedIn).length, completed: todayShifts.filter(hasCheckedOut).length }
})

function isCheckedOutAfterShiftEnd(shift: Shift): boolean {
  if (!shift.checkOutTime || !shift.date || !shift.endTime) return false
  return parseDateTime(shift.date, shift.checkOutTime).getTime() >= parseDateTime(shift.date, shift.endTime).getTime()
}

function formatFullDate(d: Date): string { return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }
function normalizeTime(value: any): string {
  const str = String(value || '')
  if (!str) return ''
  if (str.length >= 16 && str.indexOf(' ') > -1) return str.slice(11, 16)
  if (str.length >= 16 && str.indexOf('T') > -1) return str.slice(11, 16)
  return str.slice(0, 5)
}
function normalizeDate(value: any): string {
  const str = String(value || '')
  if (!str) return formatFullDate(new Date())
  const match = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (match) return `${match[1]}-${match[2].padStart(2, '0')}-${match[3].padStart(2, '0')}`
  const parsed = new Date(str)
  return isNaN(parsed.getTime()) ? formatFullDate(new Date()) : formatFullDate(parsed)
}
function normalizeShift(s: any, index: number): Shift {
  const status = s.status || s.attendanceStatus || 'SCHEDULED'
  const checkOutTime = normalizeTime(s.checkOutTime || s.clockOutTime || s.signOutTime)
  return { id: Number(s.id ?? s.shiftId ?? index + 1), jobTitle: s.jobTitle || s.title || s.positionName || s.jobName || '临时岗位', location: s.location || s.locationName || s.jobLocation || s.address || '暂无地点', startTime: normalizeTime(s.startTime || s.beginTime) || '09:00', endTime: normalizeTime(s.endTime || s.finishTime) || '18:00', date: normalizeDate(s.date || s.shiftDate), status, checkedIn: checkedInStatuses.includes(status), checkedOut: checkedOutStatuses.includes(status), checkInTime: normalizeTime(s.checkInTime || s.clockInTime || s.signInTime), checkOutTime, attendanceId: s.attendanceId }
}
function hasCheckedIn(shift: Shift): boolean { return shift.checkedIn || checkedInStatuses.includes(shift.status) }
function hasCheckedOut(shift: Shift): boolean { return shift.checkedOut || isCheckedOutAfterShiftEnd(shift) || checkedOutStatuses.includes(shift.status) }
function statusText(status: string, shift?: Shift): string {
  if (shift && isCheckedOutAfterShiftEnd(shift)) return '已完成'
  const map: Record<string, string> = { SCHEDULED: '待上岗', ON_DUTY: '工作中', COMPLETED: '已完成', LATE: '迟到', EARLY_LEAVE: '早退', ABSENT: '缺勤' }
  return map[status] || status || '待上岗'
}
function statusClass(status: string, shift?: Shift): string {
  if (shift && isCheckedOutAfterShiftEnd(shift)) return 'completed'
  if (['COMPLETED'].includes(status)) return 'completed'
  if (['ON_DUTY', 'LATE'].includes(status)) return 'active'
  if (['EARLY_LEAVE', 'ABSENT'].includes(status)) return 'warning'
  return 'pending'
}
async function loadShifts(p: number) {
  if (p === 1) loading.value = true
  else loadingMore.value = true
  try {
    const dateStr = formatFullDate(new Date())
    const res: any = await getMyShifts({ endDate: dateStr, page: p, pageSize: PAGE_SIZE })
    const list = Array.isArray(res) ? res : (res?.records || res?.list || [])
    if (p === 1) {
      allShifts.value = list.map(normalizeShift)
      total.value = res?.total ?? res?.totalCount ?? list.length
    } else {
      allShifts.value.push(...list.map(normalizeShift))
      total.value = res?.total ?? res?.totalCount ?? allShifts.value.length
    }
  } catch (err: any) {
    if (p === 1) allShifts.value = []
    uni.showToast({ title: err?.message || '打卡记录加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}
async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  await loadShifts(page.value)
}

onMounted(() => loadShifts(1))
</script>

<style scoped>
.clockin-page { display: flex; flex-direction: column; min-height: 100vh; background: #f5f7fb; }
.top-panel { padding: 28rpx 24rpx 22rpx; background: linear-gradient(180deg, #eaf8f0 0%, #f5f7fb 100%); }
.date-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 22rpx; }
.date-title { display: block; font-size: 40rpx; line-height: 56rpx; font-weight: 700; color: #1f2d3d; }
.date-subtitle { display: block; margin-top: 6rpx; font-size: 24rpx; color: #6b7785; }
.date-pill { padding: 12rpx 22rpx; border-radius: 28rpx; background: #ffffff; color: #07c160; font-size: 24rpx; font-weight: 600; box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.12); }
.stats-card { display: flex; align-items: center; padding: 28rpx 12rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 10rpx 30rpx rgba(43, 72, 99, 0.08); }
.stat-item { flex: 1; text-align: center; }
.stat-value { display: block; font-size: 42rpx; line-height: 50rpx; font-weight: 700; color: #1f2d3d; }
.stat-label { display: block; margin-top: 8rpx; font-size: 22rpx; color: #8b98a7; }
.stat-line { width: 1rpx; height: 54rpx; background: #edf0f5; }
.notice-card { display: flex; align-items: center; margin-top: 20rpx; padding: 18rpx 22rpx; border-radius: 18rpx; background: #ffffff; }
.notice-dot { width: 12rpx; height: 12rpx; margin-right: 14rpx; border-radius: 50%; background: #07c160; }
.notice-text { flex: 1; font-size: 24rpx; line-height: 34rpx; color: #637083; }
.shift-scroll { flex: 1; padding: 22rpx 24rpx 34rpx; box-sizing: border-box; }
.section-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18rpx; }
.section-title { font-size: 32rpx; font-weight: 700; color: #1f2d3d; }
.section-subtitle { font-size: 24rpx; color: #8b98a7; }
.shift-card { margin-bottom: 22rpx; padding: 28rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 24rpx rgba(43, 72, 99, 0.06); }
.shift-header { display: flex; align-items: center; justify-content: space-between; }
.job-left { flex: 1; display: flex; align-items: center; min-width: 0; }
.job-icon { width: 64rpx; height: 64rpx; line-height: 64rpx; margin-right: 18rpx; border-radius: 20rpx; text-align: center; color: #ffffff; font-size: 26rpx; font-weight: 700; background: #07c160; }
.job-info { flex: 1; min-width: 0; }
.shift-title { display: block; font-size: 32rpx; line-height: 42rpx; font-weight: 700; color: #1f2d3d; }
.shift-location { display: block; margin-top: 8rpx; font-size: 24rpx; line-height: 32rpx; color: #7b8794; }
.status-badge { margin-left: 18rpx; padding: 8rpx 18rpx; border-radius: 24rpx; font-size: 22rpx; line-height: 28rpx; white-space: nowrap; }
.status-badge.pending { background: #fff7e6; color: #fa8c16; }
.status-badge.active { background: #e6f7ff; color: #1890ff; }
.status-badge.completed { background: #f0fbf4; color: #07c160; }
.status-badge.warning { background: #fff1f0; color: #f5222d; }
.time-box { display: flex; align-items: center; justify-content: space-between; margin-top: 24rpx; padding: 22rpx 24rpx; border-radius: 18rpx; background: #f7fafc; }
.time-label { display: block; font-size: 22rpx; color: #8b98a7; }
.time-value { display: block; margin-top: 8rpx; font-size: 34rpx; line-height: 42rpx; font-weight: 700; color: #07c160; }
.date-box { width: 86rpx; height: 86rpx; border-radius: 22rpx; background: #eafaf1; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.date-day { font-size: 32rpx; line-height: 36rpx; color: #08a857; font-weight: 800; }
.date-month { font-size: 22rpx; color: #58b987; }
.record-row { display: flex; margin-top: 22rpx; }
.record-item { flex: 1; padding: 18rpx 20rpx; border-radius: 16rpx; background: #fbfcfe; }
.record-item:first-child { margin-right: 18rpx; }
.record-label { display: block; font-size: 22rpx; color: #8b98a7; }
.record-value { display: block; margin-top: 8rpx; font-size: 28rpx; font-weight: 600; color: #1f2d3d; }
.empty-state { padding: 100rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: #98a2b3; }
</style>
