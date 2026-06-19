<template>
  <view class="clockin-page">
    <view class="page-header">
      <text class="page-title">打卡记录</text>
      <view class="month-tabs">
        <text class="month-current">{{ currentMonthText }}</text>
      </view>
    </view>

    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-num">{{ monthStats.totalHours }}</text>
        <text class="stat-label">总工时(h)</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ monthStats.attendanceDays }}</text>
        <text class="stat-label">出勤天数</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">¥{{ monthStats.estimatedIncome }}</text>
        <text class="stat-label">预计收入</text>
      </view>
    </view>

    <uni-load-more v-if="loading && page === 1" status="loading" />

    <scroll-view class="record-scroll" scroll-y @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="record-list">
        <view v-if="allShifts.length === 0 && !loading" class="empty-state">
          <text class="empty-text">暂无打卡记录</text>
        </view>

        <view v-for="shift in allShifts" :key="shift.id" class="record-card">
          <view class="record-top">
            <view class="record-date">
              <text class="date-day">{{ formatDay(shift.date) }}</text>
              <text class="date-month">{{ formatMonth(shift.date) }}月</text>
            </view>
            <view class="record-main">
              <text class="record-title">{{ shift.jobTitle }}</text>
              <view class="record-time-row">
                <text class="record-time">{{ shift.startTime }} - {{ shift.endTime }}</text>
                <text class="record-status" :class="statusClass(shift.status, shift)">{{ statusText(shift.status, shift) }}</text>
              </view>
            </view>
          </view>
          <view class="record-details">
            <view class="detail-item">
              <text class="detail-label">签到</text>
              <text class="detail-value" :class="{ 'is-null': !shift.checkInTime }">{{ shift.checkInTime || '未签到' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">签退</text>
              <text class="detail-value" :class="{ 'is-null': !shift.checkOutTime }">{{ shift.checkOutTime || '未签退' }}</text>
            </view>
            <view class="detail-item" v-if="shift.location">
              <text class="detail-label">地点</text>
              <text class="detail-value location">{{ shift.location }}</text>
            </view>
          </view>
        </view>

        <uni-load-more v-if="hasMore" :status="loadingMore ? 'loading' : 'more'" />
        <uni-load-more v-if="!hasMore && allShifts.length > 0" status="noMore" />

        <view class="bottom-pad"></view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyShifts } from '@/api/schedule'

interface Shift {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
  checkedIn: boolean
  checkedOut: boolean
  checkInTime?: string
  checkOutTime?: string
  attendanceId?: number
  pay?: number
}

const PAGE_SIZE = 20
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const total = ref(0)
const allShifts = ref<Shift[]>([])
const refreshing = ref(false)

const hasMore = computed(() => allShifts.value.length < total.value)

const currentMonthText = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const monthStats = computed(() => {
  const now = new Date()
  const monthStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const monthShifts = allShifts.value.filter(s => s.date.startsWith(monthStr))
  let totalHours = 0
  let days = 0
  let income = 0
  const daySet = new Set<string>()
  monthShifts.forEach((shift) => {
    if (hasCheckedOut(shift)) {
      const hrs = calculateHours(shift.startTime, shift.checkOutTime || shift.endTime)
      totalHours += hrs
      if (!daySet.has(shift.date)) {
        daySet.add(shift.date)
        days++
      }
      if (shift.pay) income += shift.pay
    }
  })
  return {
    totalHours: totalHours.toFixed(1),
    attendanceDays: days,
    estimatedIncome: income > 0 ? income.toFixed(0) : '--'
  }
})

function calculateHours(start: string, end: string): number {
  if (!start || !end) return 0
  const [sh, sm] = start.split(':').map(Number)
  const [eh, em] = end.split(':').map(Number)
  const diff = (eh * 60 + em) - (sh * 60 + sm)
  return Math.max(0, diff / 60)
}

function formatDay(dateStr: string): string {
  if (!dateStr) return '--'
  const d = String(dateStr)
  return d.length >= 10 ? d.slice(8, 10) : d.slice(-2)
}

function formatMonth(dateStr: string): string {
  if (!dateStr) return '--'
  const d = String(dateStr)
  const m = d.length >= 7 ? d.slice(5, 7) : ''
  return m ? Number(m).toString() : '--'
}

function isCheckedOutAfterShiftEnd(shift: Shift): boolean {
  if (!shift.checkOutTime || !shift.date || !shift.endTime) return false
  const [sh, sm] = shift.endTime.split(':').map(Number)
  const [oh, om] = shift.checkOutTime.split(':').map(Number)
  return oh > sh || (oh === sh && om >= sm)
}

function normalizeTime(value: any): string {
  const str = String(value || '')
  if (!str) return ''
  const match = str.match(/(\d{2}:\d{2})/)
  return match ? match[1] : str.slice(0, 5)
}

function normalizeDate(value: any): string {
  const str = String(value || '')
  if (!str) return ''
  const match = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (match) return `${match[1]}-${String(match[2]).padStart(2, '0')}-${String(match[3]).padStart(2, '0')}`
  return ''
}

const checkedInStatuses = ['ON_DUTY', 'COMPLETED', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE']
const checkedOutStatuses = ['COMPLETED', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE']

function hasCheckedInByStatus(status: string): boolean {
  return checkedInStatuses.includes(status)
}

function hasCheckedOutByStatus(status: string): boolean {
  return checkedOutStatuses.includes(status)
}

function normalizeShift(s: any, index: number): Shift {
  const status = s.status || s.attendanceStatus || 'SCHEDULED'
  return {
    id: Number(s.id ?? s.shiftId ?? index + 1),
    jobTitle: s.jobTitle || s.title || s.positionName || s.jobName || '临时岗位',
    location: s.location || s.locationName || s.jobLocation || s.address || '',
    startTime: normalizeTime(s.startTime || s.beginTime) || '09:00',
    endTime: normalizeTime(s.endTime || s.finishTime) || '18:00',
    date: normalizeDate(s.date || s.shiftDate),
    status,
    checkedIn: hasCheckedInByStatus(status),
    checkedOut: hasCheckedOutByStatus(status),
    checkInTime: normalizeTime(s.checkInTime || s.clockInTime || s.signInTime),
    checkOutTime: normalizeTime(s.checkOutTime || s.clockOutTime || s.signOutTime),
    attendanceId: s.attendanceId,
    pay: Number(s.pay || s.salary || 0) || undefined
  }
}

function hasCheckedOut(shift: Shift): boolean {
  if (checkedOutStatuses.includes(shift.status)) return true
  if (isCheckedOutAfterShiftEnd(shift)) return true
  return false
}

function hasCheckedIn(shift: Shift): boolean {
  return checkedInStatuses.includes(shift.status) || !!shift.checkInTime
}

function statusText(status: string, shift?: Shift): string {
  const map: Record<string, string> = {
    SCHEDULED: '待上岗',
    ON_DUTY: '工作中',
    COMPLETED: '已完成',
    LATE: '迟到',
    EARLY_LEAVE: '早退',
    LATE_EARLY_LEAVE: '迟到早退',
    ABSENT: '缺勤'
  }
  if (shift && isCheckedOutAfterShiftEnd(shift) && !checkedOutStatuses.includes(status)) return '已完成'
  return map[status] || status || '待上岗'
}

function statusClass(status: string, shift?: Shift): string {
  if (['LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE', 'ABSENT'].includes(status)) return 'abnormal'
  if (shift && isCheckedOutAfterShiftEnd(shift)) return 'done'
  if (status === 'COMPLETED') return 'done'
  if (status === 'ON_DUTY') return 'active'
  return 'pending'
}

async function loadShifts(p: number) {
  if (p === 1) loading.value = true
  else loadingMore.value = true
  try {
    const res: any = await getMyShifts({ endDate: formatFullDate(new Date()), page: p, pageSize: PAGE_SIZE })
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
    uni.showToast({ title: err?.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
    refreshing.value = false
  }
}

function formatFullDate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  loadShifts(page.value)
}

function onRefresh() {
  refreshing.value = true
  page.value = 1
  loadShifts(1)
}

onMounted(() => loadShifts(1))
</script>

<style scoped>
.clockin-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6fa;
}

.page-header {
  position: relative;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 55%, #169950 100%);
  padding: 100rpx 32rpx 100rpx;
  color: #fff;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: -60rpx;
  right: -40rpx;
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.page-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.month-tabs {
  display: flex;
  align-items: center;
}

.month-current {
  font-size: 26rpx;
  opacity: 0.85;
}

.stats-card {
  display: flex;
  align-items: center;
  margin: -30rpx 24rpx 20rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx 0;
  box-shadow: 0 10rpx 32rpx rgba(0, 0, 0, 0.08), 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-num {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1a2e;
}

.stat-label {
  font-size: 22rpx;
  color: #888;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: #f0f0f0;
}

.record-scroll {
  flex: 1;
}

.record-list {
  padding: 0 24rpx 20rpx;
}

.record-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05), 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease;
}

.record-card:active {
  transform: scale(0.99);
}

.record-top {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.record-date {
  width: 80rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #eefbf3;
  border-radius: 12rpx;
  padding: 12rpx 0;
}

.date-day {
  font-size: 36rpx;
  font-weight: 800;
  color: #20c26b;
  line-height: 1.1;
}

.date-month {
  font-size: 20rpx;
  color: #20c26b;
  margin-top: 4rpx;
}

.record-main {
  flex: 1;
  min-width: 0;
}

.record-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-time-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-time {
  font-size: 26rpx;
  color: #666;
}

.record-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.record-status.active {
  background: #e6f8ee;
  color: #20c26b;
}

.record-status.pending {
  background: #f3f4f6;
  color: #9ca3af;
}

.record-status.done {
  background: #eefbf3;
  color: #20c26b;
}

.record-status.abnormal {
  background: #fee2e2;
  color: #ef4444;
}

.record-details {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
}

.detail-item {
  min-width: 160rpx;
}

.detail-label {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 6rpx;
}

.detail-value {
  font-size: 26rpx;
  color: #333;
}

.detail-value.is-null {
  color: #ccc;
}

.detail-value.location {
  font-size: 24rpx;
  color: #666;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 160rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.bottom-pad {
  height: 40rpx;
}
</style>
