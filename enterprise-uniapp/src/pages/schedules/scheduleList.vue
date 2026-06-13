<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { listScheduleShifts, cancelShift } from '@/api/schedules'

const shifts = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)
const pageSize = 20

onMounted(loadShifts)
onPullDownRefresh(() => {
  page.value = 1
  shifts.value = []
  hasMore.value = true
  loadShifts().finally(() => uni.stopPullDownRefresh())
})

async function loadShifts(append = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await listScheduleShifts({ page: page.value, pageSize })
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

function statusLabel(status) {
  const map = {
    CANCELLED: '已取消',
    COMPLETED: '已完成',
    ON_DUTY: '工作中',
    LATE: '迟到',
    ABSENT: '缺勤',
    SCHEDULED: '待上岗'
  }
  return map[status] || status || '-'
}

function statusClass(status) {
  const map = {
    CANCELLED: 'cancelled',
    COMPLETED: 'completed',
    ON_DUTY: 'on-duty',
    LATE: 'on-duty',
    ABSENT: 'cancelled',
    SCHEDULED: 'pending'
  }
  return map[status] || 'default'
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
      <text class="op-hero-desc">{{ shifts.length }} 条排班 · 及时处理未开始班次</text>
    </view>

    <view class="op-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="shifts.length === 0" class="e-empty">
        <text class="e-empty-title">暂无排班</text>
        <text class="e-empty-desc">还没有安排排班计划</text>
      </view>

      <view v-else class="schedule-list">
        <view v-for="s in shifts" :key="s.id" class="op-card schedule-card">
          <view class="op-row">
            <view class="date-box">
              <text class="date-day">{{ (s.shiftDate || s.date || '').slice(-2) || '-' }}</text>
              <text class="date-month">{{ (s.shiftDate || s.date || '').slice(5, 7) || '' }}月</text>
            </view>
            <view class="op-row-main">
              <text class="op-row-title">{{ s.jobTitle || '-' }}</text>
              <text class="op-row-desc">{{ formatTime(s.startTime) }} - {{ formatTime(s.endTime) }}</text>
            </view>
            <text class="op-pill" :class="statusClass(s.status) === 'cancelled' ? 'op-pill-danger' : statusClass(s.status) === 'pending' ? 'op-pill-warn' : ''">{{ statusLabel(s.status) }}</text>
          </view>

          <view class="worker-box">
            <view class="worker-avatar">{{ initials(s.workerName) }}</view>
            <view class="op-row-main">
              <text class="worker-name">{{ s.workerName || '-' }}</text>
              <text class="worker-desc">{{ genderLabel(s.workerGender) }} · {{ ageLabel(s.workerAge) }} · {{ phoneLabel(s.workerPhone) }}</text>
            </view>
          </view>

          <view class="action-row">
            <view v-if="canCancelShift(s)" class="action-btn danger" @click="handleDelete(s.id)">取消排班</view>
            <view v-else class="action-btn disabled">{{ s.status === 'CANCELLED' ? '已取消' : '不可取消' }}</view>
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
.schedule-list { display: flex; flex-direction: column; gap: 20rpx; }
.schedule-card { overflow: hidden; }
.date-box { width: 78rpx; height: 86rpx; margin-right: 18rpx; border-radius: 24rpx; background: #ecfdf5; color: #16a34a; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; }
.date-day { font-size: 34rpx; font-weight: 850; line-height: 1; }
.date-month { margin-top: 6rpx; font-size: 20rpx; font-weight: 750; }
.worker-box { display: flex; align-items: center; margin-top: 22rpx; padding: 18rpx; border-radius: 20rpx; background: #f8fafc; }
.worker-avatar { width: 64rpx; height: 64rpx; margin-right: 16rpx; border-radius: 22rpx; background: #16a34a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 26rpx; font-weight: 850; flex-shrink: 0; }
.worker-name { display: block; font-size: 27rpx; font-weight: 800; color: #1f2933; }
.worker-desc { display: block; margin-top: 6rpx; font-size: 23rpx; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.action-row { margin-top: 20rpx; }
.action-btn { height: 68rpx; line-height: 68rpx; border-radius: 999rpx; text-align: center; font-size: 26rpx; font-weight: 850; }
.action-btn.danger { background: #fee2e2; color: #dc2626; }
.action-btn.disabled { background: #f1f5f9; color: #98a3b3; }
.load-more-wrap { padding: 16rpx 0 32rpx; }
</style>
