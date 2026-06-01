<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getApplications, acceptApplication, rejectApplication } from '@/api/jobs'

const jobId = ref(null)
const jobTitle = ref('')
const applications = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

onLoad((params) => {
  jobId.value = params?.jobId ? Number(params.jobId) : null
  jobTitle.value = decodeSafe(params?.jobTitle)
  page.value = 1
  applications.value = []
  hasMore.value = true
  loadApplications()
})

function decodeSafe(value) {
  if (!value) return ''
  try {
    return decodeURIComponent(String(value))
  } catch {
    return String(value)
  }
}

async function loadApplications(append = false) {
  if (!jobId.value) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    return
  }
  if (!append) loading.value = true
  else loadingMore.value = true

  try {
    const res = await getApplications(jobId.value, { page: page.value, pageSize })
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

onMounted(() => {
  if (!jobId.value) {
    uni.showToast({ title: '参数错误', icon: 'none' })
  }
})
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">报名记录</text>
      <text class="subtitle" v-if="jobTitle">{{ jobTitle }}</text>
    </view>

    <scroll-view class="list-scroll" scroll-y @scrolltolower="loadMore">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="applications.length === 0" class="state-msg">暂无报名记录</view>
      <view v-else class="application-list">
        <view v-for="app in applications" :key="app.applicationId" class="application-card">
          <view class="application-top">
            <text class="name">{{ app.workerName || '未知姓名' }}</text>
            <text class="status">{{ statusLabel(app.status) }}</text>
          </view>
          <text class="phone">{{ app.workerPhone || '暂无手机号' }}</text>
          <text class="time">排班：{{ app.scheduleDate || '' }} {{ app.startTime || '' }}-{{ app.endTime || '' }}</text>
          <text class="time">{{ app.appliedAt || '暂无申请时间' }}</text>
          <view v-if="app.status === 'PENDING'" class="actions">
            <button class="btn accept" @click="handleAccept(app.applicationId)">通过</button>
            <button class="btn reject" @click="handleReject(app.applicationId)">拒绝</button>
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
  background: #f5f5f5;
}

.header {
  padding: 24rpx 32rpx 16rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.title {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}

.list-scroll {
  height: calc(100vh - 110rpx);
  padding: 20rpx 24rpx 32rpx;
}

.state-msg {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}

.application-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.application-top {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  flex: 1;
}

.status {
  font-size: 22rpx;
  color: #07c160;
  flex-shrink: 0;
}

.phone,
.time {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #666;
}

.actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
}

.accept {
  background: #07c160;
  color: #fff;
}

.reject {
  background: #f5f5f5;
  color: #333;
}

.load-more-wrap {
  padding-bottom: 24rpx;
}
</style>
