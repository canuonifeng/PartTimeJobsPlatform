<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJob, getRates, getSchedules } from '@/api/jobs'

const job = ref(null)
const rates = ref([])
const schedules = ref([])
const loading = ref(true)

onLoad(async (params) => {
  if (params.id) {
    await loadDetail(params.id)
  } else {
    loading.value = false
    uni.showToast({ title: '参数错误', icon: 'none' })
  }
})

async function loadDetail(id) {
  loading.value = true
  try {
    job.value = await getJob(id)
    try {
      const rateRes = await getRates(id)
      rates.value = Array.isArray(rateRes) ? rateRes : []
    } catch {}
    try {
      const schedRes = await getSchedules(id)
      schedules.value = Array.isArray(schedRes) ? schedRes : []
    } catch {}
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function statusLabel(s) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭' }
  return map[s] || s
}

function rateTypeLabel(t) {
  const map = { HOURLY: '时薪', DAILY: '日薪', PIECEWORK: '计件' }
  return map[t] || t
}
</script>

<template>
  <view class="page">
    <scroll-view scroll-y class="detail-scroll">
      <view v-if="loading" class="state-msg">加载中...</view>

      <view v-else-if="job" class="detail-content">
        <view class="detail-card">
          <view class="detail-header">
            <text class="detail-title">{{ job.title }}</text>
            <text class="badge" :class="'badge-' + (job.status || '').toLowerCase()">
              {{ statusLabel(job.status) }}
            </text>
          </view>
        </view>

        <view class="detail-card">
          <text class="section-title">基本信息</text>
          <view class="info-row">
            <text class="info-label">职位描述</text>
            <text class="info-value">{{ job.description || '暂无' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">工作地点</text>
            <text class="info-value">{{ job.location || '暂无' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">招聘人数</text>
            <text class="info-value">{{ job.headcount }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">截止日期</text>
            <text class="info-value">{{ job.deadline || '不限' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">创建时间</text>
            <text class="info-value">{{ job.createdAt || job.createTime || '暂无' }}</text>
          </view>
        </view>

        <view v-if="rates.length > 0" class="detail-card">
          <text class="section-title">薪资标准</text>
          <view v-for="(rate, i) in rates" :key="i" class="info-row">
            <text class="info-label">{{ rateTypeLabel(rate.type) }}</text>
            <text class="info-value">{{ rate.amount }} {{ rate.currency || 'CNY' }}</text>
          </view>
        </view>

        <view v-if="schedules.length > 0" class="detail-card">
          <text class="section-title">排班时段</text>
          <view v-for="(sched, i) in schedules" :key="i" class="info-row">
            <text class="info-label">{{ sched.date }}</text>
            <text class="info-value">
              {{ sched.startTime }} - {{ sched.endTime }}
            </text>
          </view>
        </view>

      </view>

      <view v-else class="state-msg">暂无数据</view>
    </scroll-view>

  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.detail-scroll {
  padding: 24rpx 32rpx;
}
.state-msg {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}
.detail-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
  margin-right: 16rpx;
}
.badge {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}
.badge-draft {
  background: #f0f0f0;
  color: #999;
}
.badge-published {
  background: #e8f8e8;
  color: #34c759;
}
.badge-closed {
  background: #ffe8e8;
  color: #ff3b30;
}
.section-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}
.info-row {
  display: flex;
  padding: 14rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  font-size: 26rpx;
  color: #999;
  width: 140rpx;
  flex-shrink: 0;
}
.info-value {
  font-size: 26rpx;
  color: #333;
  flex: 1;
}
.application-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.application-item {
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 20rpx;
  background: #fafafa;
}
.application-main {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.application-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.application-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}
.application-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #eef6ff;
  color: #007aff;
}
.application-phone,
.application-time {
  font-size: 24rpx;
  color: #666;
}
.application-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}
.application-actions .action-btn {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  border-radius: 8rpx;
  border: 2rpx solid #ddd;
  background: #fff;
  color: #333;
}
.accept-btn {
  border-color: #34c759;
  color: #34c759;
}
.reject-btn {
  border-color: #ff3b30;
  color: #ff3b30;
}
</style>
