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
    <view class="content">
      <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
        <view v-if="loading" class="state-msg">加载中...</view>
        <view v-else-if="shifts.length === 0" class="state-msg">暂无排班</view>
        <view v-else class="list">
          <view v-for="s in shifts" :key="s.id" class="card">
            <view class="card-row">
              <text class="card-label">工人</text>
              <text class="card-val">{{ s.workerName || '-' }}</text>
            </view>
            <view class="card-row">
              <text class="card-label">年龄</text>
              <text class="card-val">{{ s.workerAge ?? '-' }}</text>
            </view>
            <view class="card-row">
              <text class="card-label">岗位</text>
              <text class="card-val">{{ s.jobTitle || '-' }}</text>
            </view>
            <view class="card-row">
              <text class="card-label">日期</text>
              <text class="card-val">{{ s.shiftDate || s.date || '-' }}</text>
            </view>
            <view class="card-row">
              <text class="card-label">时间</text>
              <text class="card-val">{{ s.startTime || '-' }} - {{ s.endTime || '-' }}</text>
            </view>
            <view class="card-row">
              <text class="card-label">状态</text>
              <text class="card-val" :class="s.status === 'CANCELLED' ? 'status-cancelled' : ''">
                {{ s.status === 'CANCELLED' ? '已取消' : s.status === 'COMPLETED' ? '已完成' : s.status === 'ON_DUTY' || s.status === 'LATE' ? '工作中' : '待上岗' }}
              </text>
            </view>
            <view class="card-actions">
              <button v-if="s.status !== 'CANCELLED'" class="action-btn delete" @click="handleDelete(s.id)">取消排班</button>
              <text v-else class="cancelled-text">已取消</text>
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
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; }
.header { padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.content { padding: 24rpx 32rpx; }
.list-scroll { height: calc(100vh - 120rpx); }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.card-row { display: flex; padding: 6rpx 0; }
.card-label { font-size: 26rpx; color: #999; width: 100rpx; flex-shrink: 0; }
.card-val { font-size: 26rpx; color: #333; flex: 1; }
.card-actions { display: flex; gap: 16rpx; margin-top: 12rpx; }
.action-btn { flex: 1; height: 64rpx; line-height: 64rpx; font-size: 24rpx; border-radius: 8rpx; border: 2rpx solid #ddd; background: #fff; text-align: center; }
.action-btn::after { border: none; }
.delete { border-color: #ff3b30; color: #ff3b30; }
.status-cancelled { color: #ff3b30; }
.cancelled-text { flex: 1; text-align: center; color: #999; font-size: 24rpx; line-height: 64rpx; }
.load-more-wrap { padding-bottom: 24rpx; }
</style>
