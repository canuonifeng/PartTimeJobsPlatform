<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getJobs, publishJob, closeJob, reopenJob, deleteJob, getJobShareLink } from '@/api/jobs'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const jobs = ref([])
const currentTab = ref(0)
const loading = ref(false)

const tabs = [
  { name: '全部', value: '' },
  { name: '草稿', value: 'DRAFT' },
  { name: '已发布', value: 'PUBLISHED' },
  { name: '已关闭', value: 'CLOSED' }
]

const filteredJobs = computed(() => {
  const status = tabs[currentTab.value].value
  if (!status) return jobs.value
  return jobs.value.filter(j => j.status === status)
})

async function loadJobs() {
  loading.value = true
  try {
    const res = await getJobs(authStore.companyId)
    jobs.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(() => loadJobs())

onPullDownRefresh(() => {
  loadJobs().finally(() => uni.stopPullDownRefresh())
})

function switchTab(index) {
  currentTab.value = index
}

function navigateToCreate() {
  uni.navigateTo({ url: '/pages/jobs/jobForm' })
}

function navigateToEdit(id) {
  uni.navigateTo({ url: `/pages/jobs/jobForm?id=${id}` })
}

function navigateToDetail(id) {
  uni.navigateTo({ url: `/pages/jobs/jobDetail?id=${id}` })
}

function navigateToApplications(job) {
  uni.navigateTo({ url: `/pages/jobs/applicationList?jobId=${job.id}&jobTitle=${encodeURIComponent(job.title || '')}` })
}

async function handleShare(id) {
  try {
    const res = await getJobShareLink(id)
    const link = res?.link || res?.data?.link || ''
    if (!link) {
      throw new Error('empty link')
    }
    await new Promise((resolve, reject) => {
      uni.setClipboardData({
        data: link,
        success: resolve,
        fail: reject
      })
    })
    uni.showToast({ title: '链接已复制', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '复制失败', icon: 'none' })
  }
}

async function handlePublish(id) {
  try {
    await publishJob(id)
    uni.showToast({ title: '已发布', icon: 'success' })
    loadJobs()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleClose(id) {
  try {
    await closeJob(id)
    uni.showToast({ title: '已关闭', icon: 'success' })
    loadJobs()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleReopen(id) {
  try {
    await reopenJob(id)
    uni.showToast({ title: '已重新发布', icon: 'success' })
    loadJobs()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function handleDelete(id) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该职位吗？此操作不可恢复。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteJob(id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadJobs()
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

function statusLabel(s) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭' }
  return map[s] || s
}

function statusClass(s) {
  const map = { DRAFT: 'badge-draft', PUBLISHED: 'badge-published', CLOSED: 'badge-closed' }
  return map[s] || ''
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">职位管理</text>
    </view>

    <scroll-view scroll-x class="tabs" scroll-with-animation>
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        {{ tab.name }}
      </view>
    </scroll-view>

    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="filteredJobs.length === 0" class="state-msg">暂无职位</view>
      <view v-else class="job-list">
        <view
          v-for="job in filteredJobs"
          :key="job.id"
          class="job-card"
          @click="navigateToDetail(job.id)"
        >
          <view class="card-header">
            <text class="card-title">{{ job.title }}</text>
            <text class="badge" :class="statusClass(job.status)">{{ statusLabel(job.status) }}</text>
          </view>
          <view class="card-body">
            <text class="info">招聘人数：{{ job.headcount }}</text>
            <text class="info">总报名数：{{ job.applicationCount ?? 0 }}</text>
            <text class="info">待审核：{{ job.pendingApplicationCount ?? 0 }}</text>
            <text class="info">截止日期：{{ job.deadline || '不限' }}</text>
          </view>
          <view class="card-footer" @click.stop>
            <button
              class="action-btn record-btn"
              @click.stop="navigateToApplications(job)"
            >报名记录</button>
            <button
              class="action-btn share-btn"
              @click.stop="handleShare(job.id)"
            >邀请报名</button>
            <button
              class="action-btn edit-btn"
              @click="navigateToEdit(job.id)"
            >编辑</button>
            <button
              v-if="job.status === 'PUBLISHED'"
              class="action-btn close-btn"
              @click="handleClose(job.id)"
            >关闭</button>
            <button
              v-if="job.status === 'CLOSED' && (job.applicationCount ?? 0) === 0"
              class="action-btn delete-btn"
              @click="handleDelete(job.id)"
            >删除</button>
            <button
              v-if="job.status === 'DRAFT'"
              class="action-btn publish-btn"
              @click="handlePublish(job.id)"
            >发布</button>
            <button
              v-if="job.status === 'CLOSED'"
              class="action-btn reopen-btn"
              @click="handleReopen(job.id)"
            >重新发布</button>
          </view>
        </view>
      </view>
    </view>

    <view class="fab" @click="navigateToCreate">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
}
.header-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}
.tabs {
  display: flex;
  white-space: nowrap;
  background: #fff;
  padding: 16rpx 32rpx;
  border-bottom: 2rpx solid #eee;
}
.tab {
  display: inline-block;
  padding: 12rpx 28rpx;
  margin-right: 16rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 8rpx;
  background: #f5f5f5;
}
.tab.active {
  color: #fff;
  background: #007aff;
}
.content {
  padding: 24rpx 32rpx;
}
.state-msg {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}
.job-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  flex: 1;
  margin-right: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
.card-body {
  margin-bottom: 16rpx;
}
.info {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
}
.card-footer {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}
.action-btn {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  border: 2rpx solid #ddd;
  background: #fff;
  color: #333;
  line-height: 1.5;
  min-width: 0;
  height: auto;
}
.action-btn::after {
  border: none;
}
.record-btn {
  border-color: #007aff;
  color: #007aff;
}
.publish-btn {
  border-color: #34c759;
  color: #34c759;
}
.share-btn {
  border-color: #34c759;
  color: #34c759;
}
.close-btn {
  border-color: #ff9500;
  color: #ff9500;
}
.reopen-btn {
  border-color: #007aff;
  color: #007aff;
}
.edit-btn {
  border-color: #007aff;
  color: #007aff;
}
.delete-btn {
  border-color: #ff3b30;
  color: #ff3b30;
}
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 40rpx;
  width: 96rpx;
  height: 96rpx;
  background: #007aff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 122, 255, 0.4);
  z-index: 100;
}
.fab-icon {
  font-size: 52rpx;
  line-height: 1;
}
</style>
