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
    CANCELLED: 'badge-red',
    COMPLETED: 'badge-gray',
    ON_DUTY: 'badge-green',
    LATE: 'badge-green',
    SCHEDULED: 'badge-yellow'
  }
  return map[status] || 'badge-gray'
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
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">排班考勤</text>
    </view>

    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
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
            <text class="worker-name">{{ s.workerName || '-' }}</text>
            <view class="badge" :class="statusClass(s.status)">{{ statusLabel(s.status) }}</view>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="info-label">岗位</text>
              <text class="info-value">{{ s.jobTitle || '-' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">年龄</text>
              <text class="info-value">{{ s.workerAge ?? '-' }}岁</text>
            </view>
            <view class="info-row">
              <text class="info-label">日期</text>
              <text class="info-value">{{ s.shiftDate || s.date || '-' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">时间</text>
              <text class="info-value">{{ s.startTime || '-' }} - {{ s.endTime || '-' }}</text>
            </view>
          </view>

          <view class="card-footer">
            <view
              v-if="s.status !== 'CANCELLED'"
              class="action-pill pill-red"
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
    </scroll-view>
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

.list-scroll {
  height: calc(100vh - 120rpx);
  padding: 24rpx 32rpx;
  overflow: hidden;
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
  gap: 24rpx;
}

.schedule-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
  box-sizing: border-box;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.worker-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2933;
  flex: 1;
  margin-right: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge {
  font-size: 24rpx;
  font-weight: 700;
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
}

.badge-green {
  background: #e7f8ef;
  color: #08a857;
}

.badge-yellow {
  background: #fff7df;
  color: #d28a00;
}

.badge-red {
  background: #feecec;
  color: #df3b30;
}

.badge-gray {
  background: #eef1f0;
  color: #7b8580;
}

.card-body {
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8rpx 0;
}

.info-label {
  font-size: 26rpx;
  color: #98a3b3;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #1f2933;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-left: 16rpx;
}

.card-footer {
  display: flex;
}

.action-pill {
  font-size: 24rpx;
  font-weight: 600;
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid;
}

.pill-red {
  border-color: #ef4444;
  color: #ef4444;
}

.cancelled-label {
  font-size: 24rpx;
  color: #98a3b3;
}

.load-more-wrap {
  padding: 24rpx 0;
}
</style>
