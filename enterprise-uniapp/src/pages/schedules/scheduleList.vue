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
  <view class="page">
    <view class="header">
      <text class="header-title">排班考勤</text>
    </view>

    <view class="content">
      <view v-if="loading" class="empty-state">
        <text class="empty-emoji">⏳</text>
        <text class="empty-title">加载中...</text>
      </view>

      <view v-else-if="shifts.length === 0" class="empty-state">
        <text class="empty-emoji">📅</text>
        <text class="empty-title">暂无排班</text>
        <text class="empty-desc">还没有安排排班计划</text>
      </view>

      <view v-else class="schedule-list">
        <view v-for="s in shifts" :key="s.id" class="schedule-card">
          <view class="card-header">
            <view class="worker-info">
              <view class="worker-avatar">{{ initials(s.workerName) }}</view>
              <view class="worker-detail">
                <text class="worker-name">{{ s.workerName || '-' }}</text>
                <text class="worker-meta">{{ genderLabel(s.workerGender) }} · {{ ageLabel(s.workerAge) }} · {{ phoneLabel(s.workerPhone) }}</text>
              </view>
            </view>
            <view class="status-badge" :class="statusClass(s.status)">{{ statusLabel(s.status) }}</view>
          </view>

          <view class="time-block">
            <view class="date-box">
              <text class="date-day">{{ (s.shiftDate || s.date || '').slice(-2) || '-' }}</text>
              <text class="date-month">{{ (s.shiftDate || s.date || '').slice(5, 7) || '' }}月</text>
            </view>
            <view class="time-info">
              <text class="time-label">工作时间</text>
              <text class="time-value">{{ formatTime(s.startTime) }} - {{ formatTime(s.endTime) }}</text>
            </view>
          </view>

          <view class="info-block">
            <text class="info-icon">💼</text>
            <view class="info-text">
              <text class="info-label">岗位</text>
              <text class="info-value">{{ s.jobTitle || '-' }}</text>
            </view>
          </view>

          <view class="card-footer">
            <view
              v-if="s.status !== 'CANCELLED'"
              class="action-btn"
              @click="handleDelete(s.id)"
            >取消排班</view>
            <view v-else class="cancelled-label">已取消</view>
          </view>
        </view>
      </view>

      <view class="load-more-wrap">
        <uni-load-more v-if="loadingMore" status="loading" />
        <uni-load-more v-else-if="hasMore" status="more" />
        <uni-load-more v-else status="noMore" />
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #f6f8f7;
}

.header {
  background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%);
  padding: 48rpx 32rpx 28rpx;
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}

.content {
  padding: 24rpx 32rpx;
  padding-bottom: 80rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-emoji {
  font-size: 64rpx;
  margin-bottom: 20rpx;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2933;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #98a3b3;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.schedule-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.worker-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  margin-right: 16rpx;
}

.worker-avatar {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #06b6d4, #0891b2);
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.worker-detail {
  flex: 1;
  min-width: 0;
}

.worker-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2933;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.worker-meta {
  font-size: 24rpx;
  color: #98a3b3;
  display: block;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  font-size: 22rpx;
  font-weight: 700;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
}

.status-badge.pending {
  background: #fff7df;
  color: #d28a00;
}

.status-badge.completed {
  background: #eef1f0;
  color: #7b8580;
}

.status-badge.on-duty {
  background: #e7f8ef;
  color: #08a857;
}

.status-badge.cancelled {
  background: #feecec;
  color: #df3b30;
}

.status-badge.default {
  background: #eef1f0;
  color: #7b8580;
}

.time-block {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #f8faf9;
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  margin-bottom: 16rpx;
}

.date-box {
  flex-shrink: 0;
  width: 80rpx;
  height: 80rpx;
  border-radius: 18rpx;
  background: #eafaf1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.date-day {
  font-size: 30rpx;
  line-height: 34rpx;
  color: #08a857;
  font-weight: 800;
}

.date-month {
  font-size: 20rpx;
  color: #58b987;
}

.time-info {
  flex: 1;
  min-width: 0;
}

.time-label {
  font-size: 22rpx;
  color: #98a3b3;
  display: block;
  margin-bottom: 6rpx;
}

.time-value {
  font-size: 32rpx;
  color: #1f2933;
  font-weight: 700;
  display: block;
}

.info-block {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 14rpx 16rpx;
  background: #f8faf9;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.info-icon {
  font-size: 26rpx;
  flex-shrink: 0;
}

.info-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.info-label {
  font-size: 22rpx;
  color: #98a3b3;
}

.info-value {
  font-size: 26rpx;
  font-weight: 500;
  color: #1f2933;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 76rpx;
  border: 2rpx solid #ef4444;
  border-radius: 38rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #ef4444;
}

.cancelled-label {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: #98a3b3;
  line-height: 76rpx;
}

.load-more-wrap {
  padding: 24rpx 0;
}
</style>