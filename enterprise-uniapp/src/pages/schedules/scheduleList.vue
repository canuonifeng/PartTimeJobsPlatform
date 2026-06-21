<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { listScheduleShifts, cancelShift } from '@/api/schedules'

const shifts = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)
const pageSize = 20
const currentConfirmStatus = ref('')
const confirmTabs = [
  { label: '全部', value: '' },
  { label: '待确认', value: 'SCHEDULED' },
  { label: '工作中', value: 'ON_DUTY' },
  { label: '已完成', value: 'COMPLETED' }
]

onLoad((options = {}) => {
  currentConfirmStatus.value = normalizeConfirmStatus(options.status)
  refreshShifts()
})

onPullDownRefresh(() => {
  refreshShifts().finally(() => uni.stopPullDownRefresh())
})

function normalizeConfirmStatus(status) {
  return confirmTabs.some(tab => tab.value === status) ? status : ''
}

function refreshShifts() {
  page.value = 1
  shifts.value = []
  hasMore.value = true
  return loadShifts()
}

async function loadShifts(append = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await listScheduleShifts({ status: currentConfirmStatus.value, page: page.value, pageSize })
    const list = Array.isArray(res) ? res : (res.records || res.data || [])
    shifts.value = append ? shifts.value.concat(list) : list
    hasMore.value = list.length >= pageSize
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  page.value += 1
  loadShifts(true)
}

function switchConfirmStatus(status) {
  if (currentConfirmStatus.value === status || loading.value) return
  currentConfirmStatus.value = status
  refreshShifts()
}

function statusLabel(status) {
  const map = {
    CANCELLED: '已取消',
    COMPLETED: '已完成',
    ON_DUTY: '工作中',
    LATE: '迟到',
    ABSENT: '缺勤',
    EARLY_LEAVE: '早退',
    LATE_EARLY_LEAVE: '迟到并早退',
    EARLY: '早退',
    SCHEDULED: '待上岗'
  }
  return map[status] || status || '-'
}

function statusClass(status) {
  if (isDangerStatus(status)) return 'danger'
  const map = {
    COMPLETED: 'completed',
    ON_DUTY: 'on-duty',
    SCHEDULED: 'pending'
  }
  return map[status] || 'default'
}

function isDangerStatus(status) {
  return ['ABSENT', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE', 'EARLY', 'CANCELLED'].includes(status)
}

function genderLabel(gender) {
  const map = { MALE: '男', FEMALE: '女', OTHER: '其他' }
  return map[gender] || gender || '未知'
}

function ageLabel(age) {
  return age == null ? '年龄未知' : `${age}岁`
}

function phoneLabel(phone) {
  return phone || '暂无手机号'
}

function canCancelShift(shift) {
  if (!shift || shift.status === 'CANCELLED') return false
  const date = shift.shiftDate || shift.date
  const startTime = shift.startTime
  if (!date || !startTime) return false
  return new Date(`${date}T${formatTime(startTime)}`).getTime() > Date.now()
}

function handleDelete(id) {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该排班吗？取消后该兼职将无法签到',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelShift(id)
          uni.showToast({ title: '已取消', icon: 'success' })
          page.value = 1
          shifts.value = []
          hasMore.value = true
          loadShifts()
        } catch {
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

function formatTime(t) {
  if (!t) return '-'
  return String(t).slice(0, 5)
}

function dateLabel(value) {
  return value || '-'
}

function shiftTimeLabel(shift) {
  return `${formatTime(shift.startTime)}-${formatTime(shift.endTime)}`
}

function initials(name) {
  return (name || '工').slice(0, 1)
}
</script>

<template>
  <scroll-view scroll-y class="op-page schedules-page" @scrolltolower="loadMore">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">排班考勤</text>
      <text class="op-hero-title">跟进班次人员与签到状态</text>
      <text class="op-hero-desc">{{ shifts.length }} 条排班 · 及时处理待确认班次</text>
    </view>

    <view class="op-content">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view class="filter-row">
          <view v-for="tab in confirmTabs" :key="tab.value || 'ALL'" class="filter-pill" :class="{ active: currentConfirmStatus === tab.value }" @click="switchConfirmStatus(tab.value)">{{ tab.label }}</view>
        </view>
      </scroll-view>

      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="shifts.length === 0" class="e-empty">
        <text class="e-empty-title">暂无排班</text>
        <text class="e-empty-desc">当前筛选下暂无排班记录</text>
      </view>

      <view v-else class="schedule-list">
        <view v-for="s in shifts" :key="s.id" class="op-card operation-card schedule-card">
          <view class="op-row">
            <view class="date-box">
              <text class="date-day">{{ (s.shiftDate || s.date || '').slice(-2) || '-' }}</text>
              <text class="date-month">{{ (s.shiftDate || s.date || '').slice(5, 7) || '' }}月</text>
            </view>
            <view class="op-row-main">
              <text class="op-row-title title-wrap">{{ s.jobTitle || '-' }}</text>
              <text class="op-row-desc desc-wrap">{{ dateLabel(s.shiftDate || s.date) }} · {{ shiftTimeLabel(s) }}</text>
            </view>
            <text class="op-pill" :class="statusClass(s.status) === 'danger' ? 'op-pill-danger' : statusClass(s.status) === 'pending' ? 'op-pill-warn' : ''">{{ statusLabel(s.status) }}</text>
            <text v-if="s.settlementStatus === 'PAID'" class="op-pill settlement-paid">已结算</text>
          </view>

          <view class="worker-box">
            <view class="worker-avatar">{{ initials(s.workerName) }}</view>
            <view class="op-row-main">
              <text class="worker-name">{{ s.workerName || '-' }}</text>
              <text class="worker-desc">{{ genderLabel(s.workerGender) }} · {{ ageLabel(s.workerAge) }} · {{ phoneLabel(s.workerPhone) }}</text>
            </view>
          </view>

          <view v-if="canCancelShift(s)" class="action-row">
            <view class="action-btn danger" @click="handleDelete(s.id)">取消排班</view>
          </view>
        </view>
      </view>

      <view class="load-more-wrap">
        <uni-load-more v-if="loadingMore" status="loading" />
        <uni-load-more v-else-if="hasMore" status="more" />
        <uni-load-more v-else status="noMore" />
      </view>
    </view>
  </scroll-view>
</template>

<style>
.schedules-page { height: 100vh; }
.top-space { height: 24rpx; }
.filter-scroll { margin-bottom: 20rpx; white-space: nowrap; }
.filter-row { display: inline-flex; gap: 14rpx; padding-right: 8rpx; }
.filter-pill { display: inline-flex; align-items: center; justify-content: center; height: 62rpx; padding: 0 28rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 25rpx; font-weight: 800; box-shadow: 0 10rpx 24rpx rgba(23,83,53,.06); }
.filter-pill.active { background: #16a34a; color: #fff; box-shadow: 0 12rpx 28rpx rgba(22,163,74,.2); }
.schedule-list { display: flex; flex-direction: column; gap: 20rpx; }
.operation-card { position: relative; overflow: hidden; }
.operation-card::before { content: ''; position: absolute; left: 0; top: 28rpx; bottom: 28rpx; width: 8rpx; border-radius: 0 999rpx 999rpx 0; background: linear-gradient(180deg, #18c86b, #047857); }
.schedule-card { overflow: hidden; }
.title-wrap, .desc-wrap { white-space: normal; overflow: visible; text-overflow: clip; line-height: 1.35; }
.date-box { width: 78rpx; height: 86rpx; margin-right: 18rpx; border-radius: 24rpx; background: #ecfdf5; color: #16a34a; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; }
.date-day { font-size: 34rpx; font-weight: 850; line-height: 1; }
.date-month { margin-top: 6rpx; font-size: 20rpx; font-weight: 750; }
.worker-box { display: flex; align-items: center; margin-top: 22rpx; padding: 18rpx; border-radius: 20rpx; background: #f8fafc; }
.worker-avatar { width: 64rpx; height: 64rpx; margin-right: 16rpx; border-radius: 22rpx; background: #16a34a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 26rpx; font-weight: 850; flex-shrink: 0; }
.worker-name { display: block; font-size: 27rpx; font-weight: 800; color: #1f2933; }
.worker-desc { display: block; margin-top: 6rpx; font-size: 23rpx; color: #64748b; line-height: 1.4; white-space: normal; overflow: visible; text-overflow: clip; }
.action-row { margin-top: 20rpx; }
.action-btn { height: 68rpx; line-height: 68rpx; border-radius: 999rpx; text-align: center; font-size: 26rpx; font-weight: 850; }
.action-btn.danger { background: #fee2e2; color: #dc2626; }
.load-more-wrap { padding: 16rpx 0 32rpx; }
.settlement-paid { margin-left: 8rpx; color: #12834a; background: #ecfdf5; }
</style>
