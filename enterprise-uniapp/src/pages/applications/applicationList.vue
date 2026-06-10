<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getApplications, acceptApplication, rejectApplication } from '@/api/jobs'

const applications = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

onLoad(() => {
  page.value = 1
  applications.value = []
  hasMore.value = true
  loadApplications()
})

async function loadApplications(append = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await getApplications(null, { page: page.value, pageSize })
    const list = Array.isArray(res) ? res : (res.records || res.data || [])
    applications.value = append ? applications.value.concat(list) : list
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
  loadApplications(true)
}

function statusLabel(status) {
  const map = { PENDING: '待审核', ACCEPTED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status || '-'
}

function statusClass(status) {
  const map = { PENDING: 'badge-yellow', ACCEPTED: 'badge-green', REJECTED: 'badge-red' }
  return map[status] || 'badge-gray'
}

async function handleAccept(applicationId) {
  try {
    await acceptApplication(applicationId)
    uni.showToast({ title: '已通过', icon: 'success' })
    page.value = 1
    applications.value = []
    hasMore.value = true
    loadApplications()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleReject(applicationId) {
  try {
    await rejectApplication(applicationId)
    uni.showToast({ title: '已拒绝', icon: 'success' })
    page.value = 1
    applications.value = []
    hasMore.value = true
    loadApplications()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">应聘管理</text>
    </view>

    <scroll-view class="list-scroll" scroll-y @scrolltolower="loadMore">
      <view v-if="loading" class="empty-state">
        <text class="empty-emoji">⏳</text>
        <text class="empty-title">加载中...</text>
      </view>

      <view v-else-if="applications.length === 0" class="empty-state">
        <text class="empty-emoji">📝</text>
        <text class="empty-title">暂无报名记录</text>
        <text class="empty-desc">等待工人投递简历</text>
      </view>

      <view v-else class="application-list">
        <view v-for="app in applications" :key="app.applicationId" class="application-card">
          <view class="card-header">
            <view class="app-info">
              <text class="worker-name">{{ app.workerName || '未知姓名' }}</text>
              <text class="job-title">{{ app.jobTitle || '' }}</text>
            </view>
            <view class="badge" :class="statusClass(app.status)">{{ statusLabel(app.status) }}</view>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="info-label">联系电话</text>
              <text class="info-value">{{ app.workerPhone || '暂无手机号' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">排班时间</text>
              <text class="info-value">{{ app.scheduleDate || '' }} {{ app.startTime || '' }}-{{ app.endTime || '' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">申请时间</text>
              <text class="info-value">{{ app.appliedAt || '暂无申请时间' }}</text>
            </view>
          </view>

          <view v-if="app.status === 'PENDING'" class="card-footer">
            <view class="action-accept" @click="handleAccept(app.applicationId)">通过</view>
            <view class="action-reject" @click="handleReject(app.applicationId)">拒绝</view>
          </view>
        </view>

        <view class="load-more-wrap">
          <uni-load-more v-if="loadingMore" status="loading" />
          <uni-load-more v-else-if="hasMore" status="more" />
          <uni-load-more v-else status="noMore" />
        </view>
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

.application-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.application-card {
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
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.app-info {
  flex: 1;
  margin-right: 16rpx;
}

.worker-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2933;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-title {
  font-size: 24rpx;
  color: #98a3b3;
  display: block;
  margin-top: 4rpx;
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
  gap: 20rpx;
}

.action-accept {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80rpx;
  background: #07c160;
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 44rpx;
}

.action-reject {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80rpx;
  background: #fff;
  color: #1f2933;
  font-size: 28rpx;
  font-weight: 600;
  border-radius: 44rpx;
  border: 2rpx solid #e2e8f0;
}

.load-more-wrap {
  padding: 24rpx 0;
}
</style>
