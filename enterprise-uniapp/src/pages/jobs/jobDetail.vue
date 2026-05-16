<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJob, publishJob, closeJob, reopenJob, deleteJob, getRates, getSchedules } from '@/api/jobs'

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

async function handlePublish() {
  try {
    await publishJob(job.value.id)
    uni.showToast({ title: '已发布', icon: 'success' })
    loadDetail(job.value.id)
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleClose() {
  try {
    await closeJob(job.value.id)
    uni.showToast({ title: '已关闭', icon: 'success' })
    loadDetail(job.value.id)
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleReopen() {
  try {
    await reopenJob(job.value.id)
    uni.showToast({ title: '已重新发布', icon: 'success' })
    loadDetail(job.value.id)
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function handleDelete() {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该职位吗？此操作不可恢复。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteJob(job.value.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

function handleEdit() {
  uni.navigateTo({ url: `/pages/jobs/jobForm?id=${job.value.id}` })
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
              {{ sched.startTime }} - {{ sched.endTime }}（{{ sched.slots }}人）
            </text>
          </view>
        </view>
      </view>

      <view v-else class="state-msg">暂无数据</view>
    </scroll-view>

    <view v-if="job && !loading" class="bottom-actions">
      <button
        v-if="job.status === 'DRAFT'"
        class="bottom-btn primary-btn"
        @click="handlePublish"
      >发布</button>
      <button
        v-if="job.status === 'PUBLISHED'"
        class="bottom-btn warning-btn"
        @click="handleClose"
      >关闭</button>
      <button
        v-if="job.status === 'CLOSED'"
        class="bottom-btn primary-btn"
        @click="handleReopen"
      >重新发布</button>
      <button
        class="bottom-btn outline-btn"
        @click="handleEdit"
      >编辑</button>
      <button
        class="bottom-btn danger-btn"
        @click="handleDelete"
      >删除</button>
    </view>
  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
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
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 16rpx 32rpx;
  display: flex;
  gap: 16rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}
.bottom-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  border-radius: 12rpx;
  text-align: center;
  min-width: 0;
}
.bottom-btn::after {
  border: none;
}
.primary-btn {
  background: #007aff;
  color: #fff;
}
.warning-btn {
  background: #ff9500;
  color: #fff;
}
.outline-btn {
  background: #fff;
  color: #007aff;
  border: 2rpx solid #007aff;
}
.danger-btn {
  background: #fff;
  color: #ff3b30;
  border: 2rpx solid #ff3b30;
}
</style>
