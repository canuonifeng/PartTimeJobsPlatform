<template>
  <view class="detail-page">
    <uni-load-more v-if="loading" status="loading" />

    <template v-if="job">
      <view class="banner-wrap">
        <image v-if="job.imageUrl" class="job-banner" :src="job.imageUrl" mode="aspectFill" />
        <image v-else class="job-banner placeholder-banner" />
        <view class="banner-overlay">
          <text class="banner-title">{{ job.title }}</text>
          <text class="banner-pay">{{ job.rates?.[0]?.amount ?? '-' }}元/{{ rateUnit(job.rates?.[0]?.type) }}</text>
        </view>
      </view>

      <view class="body-wrap">
        <view class="card">
          <view class="card-header">
            <text class="card-icon">📅</text>
            <text class="card-title">工作时间</text>
          </view>
          <view class="schedule-grid" v-if="job.schedules?.length">
            <view class="schedule-block" :class="scheduleBlockClass(slot.id)" v-for="slot in job.schedules" :key="slot.id" @click="!isScheduleApplied(slot.id) && toggleSchedule(slot.id)">
              <text v-if="isScheduleApplied(slot.id)" class="block-badge">已报名</text>
              <text v-else-if="pendingScheduleIds.includes(slot.id)" class="block-badge selected">已选</text>
              <text class="block-date">{{ slot.date?.slice(5) }}</text>
              <text class="block-time">{{ slot.startTime }}-{{ slot.endTime }}</text>
            </view>
          </view>
          <view v-else class="empty-hint">暂无可用排班</view>
        </view>

        <view class="card">
          <view class="card-header">
            <text class="card-icon">📍</text>
            <text class="card-title">工作地点</text>
          </view>
          <view class="company-row">
            <text class="company-name">{{ job.companyName }}</text>
          </view>
          <view class="location-row" @click="handleOpenLocation">
            <text class="location-text">{{ job.location }}</text>
            <view class="location-tag">
              <text class="location-arrow">导航 ›</text>
            </view>
          </view>
        </view>

        <view class="card">
          <view class="card-header">
            <text class="card-icon">📝</text>
            <text class="card-title">职位描述</text>
          </view>
          <text class="job-desc">{{ job.description || '暂无描述' }}</text>
        </view>
      </view>

      <view class="action-bar">
        <button v-if="job.status === 'CLOSED'" class="btn-disabled" disabled>已关闭</button>
        <button v-else-if="!canApplyMore && pendingScheduleIds.length === 0" class="btn-disabled" disabled>已报名</button>
        <button v-else class="btn-primary" @click="handleApply">立即报名</button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJobDetail, applyJob } from '@/api/jobs'

const job = ref<any>(null)
const loading = ref(true)
const jobId = ref(0)
const pendingScheduleIds = ref<number[]>([])
const appliedScheduleIds = ref<number[]>([])

const canApplyMore = computed(() => {
  if (!job.value) return false
  return job.value.status !== 'CLOSED'
})

function scheduleBlockClass(scheduleId: number) {
  if (appliedScheduleIds.value.includes(scheduleId)) return 'block-applied'
  if (pendingScheduleIds.value.includes(scheduleId)) return 'block-selected'
  return ''
}

function isScheduleApplied(id: number) {
  return appliedScheduleIds.value.includes(id)
}

function toggleSchedule(id: number) {
  const idx = pendingScheduleIds.value.indexOf(id)
  if (idx >= 0) {
    pendingScheduleIds.value.splice(idx, 1)
  } else {
    pendingScheduleIds.value.push(id)
  }
}

function rateUnit(type) {
  const map = { HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' }
  return map[type] || '小时'
}

function handleOpenLocation() {
  if (!job.value) return
  uni.openLocation({
    latitude: job.value.latitude || 39.9,
    longitude: job.value.longitude || 116.4,
    name: job.value.title,
    address: job.value.location
  })
}

async function handleApply() {
  if (pendingScheduleIds.value.length === 0) {
    uni.showToast({ title: '请先选择排班', icon: 'none' })
    return
  }
  try {
    await applyJob(jobId.value, { scheduleIds: pendingScheduleIds.value })
    uni.showToast({ title: '报名成功', icon: 'success' })
    pendingScheduleIds.value = []
    loadJob()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '报名失败', icon: 'none' })
  }
}

async function loadJob() {
  loading.value = true
  try {
    const res: any = await getJobDetail(jobId.value)
    job.value = res
    if (res.appliedScheduleIds) {
      appliedScheduleIds.value = res.appliedScheduleIds
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((params: any) => {
  if (params.id) {
    jobId.value = Number(params.id)
    loadJob()
  }
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 200rpx;
}

/* Banner */
.banner-wrap {
  position: relative;
  width: 100%;
  height: 420rpx;
}
.job-banner {
  width: 100%;
  height: 100%;
}
.placeholder-banner {
  background: linear-gradient(135deg, #07c160, #059d50);
}
.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 60rpx 32rpx 32rpx;
  background: linear-gradient(transparent, rgba(0,0,0,0.65));
}
.banner-title {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #fff;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.3);
}
.banner-pay {
  display: inline-block;
  font-size: 36rpx;
  color: #fff;
  font-weight: 700;
  background: rgba(255, 102, 0, 0.85);
  padding: 10rpx 28rpx;
  border-radius: 16rpx;
}

/* Body */
.body-wrap {
  padding: 24rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
}

/* Card */
.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.06);
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}
.card-icon {
  font-size: 36rpx;
}
.card-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

/* Schedule */
.schedule-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.schedule-block {
  position: relative;
  width: calc(50% - 8rpx);
  padding: 28rpx 20rpx;
  border-radius: 18rpx;
  background: #f8faf9;
  border: 2rpx solid #e8eaed;
  text-align: center;
  transition: all 0.2s;
}
.schedule-block:active {
  transform: scale(0.96);
}
.block-badge {
  position: absolute;
  top: 0;
  right: 0;
  font-size: 22rpx;
  color: #999;
  padding: 6rpx 16rpx;
  border-radius: 0 18rpx 0 14rpx;
  background: #e8eaed;
}
.block-badge.selected {
  color: #fff;
  background: #07c160;
}
.block-applied .block-badge {
  color: #fff;
  background: #059d50;
}
.block-applied {
  background: #ecfdf5;
  border-color: #a7f3d0;
  opacity: 0.75;
}
.block-selected {
  background: #ecfdf5;
  border-color: #07c160;
  box-shadow: 0 0 0 2rpx rgba(16, 185, 129, 0.3);
}
.block-date {
  display: block;
  font-size: 30rpx;
  color: #222;
  font-weight: 700;
  margin-bottom: 8rpx;
}
.block-time {
  display: block;
  font-size: 28rpx;
  color: #666;
}
.empty-hint {
  text-align: center;
  padding: 40rpx 0;
  font-size: 28rpx;
  color: #999;
}

/* Location */
.company-row {
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f0f0f0;
}
.company-name {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
}
.location-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 20rpx;
  border-radius: 14rpx;
  background: #f0fdf4;
}
.location-text {
  flex: 1;
  font-size: 30rpx;
  color: #444;
  line-height: 1.4;
}
.location-tag {
  background: #07c160;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  flex-shrink: 0;
}
.location-arrow {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
}

/* Description */
.job-desc {
  display: block;
  font-size: 30rpx;
  color: #555;
  line-height: 1.8;
}

/* Action bar */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx 40rpx;
  background: #fff;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
}
.btn-primary, .btn-disabled {
  width: 100%;
  height: 100rpx;
  line-height: 100rpx;
  text-align: center;
  border-radius: 50rpx;
  font-size: 36rpx;
  font-weight: 700;
  border: none;
}
.btn-primary {
  background: linear-gradient(135deg, #07c160, #059d50);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(16, 185, 129, 0.4);
}
.btn-disabled {
  background: #e5e5e5;
  color: #999;
}
</style>
