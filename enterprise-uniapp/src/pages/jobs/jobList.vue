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
  const map = { DRAFT: 'badge-gray', PUBLISHED: 'badge-green', CLOSED: 'badge-red' }
  return map[s] || ''
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">职位管理</text>
    </view>

    <scroll-view scroll-x class="tabs-wrap" scroll-with-animation>
      <view class="tabs-inner">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          class="tab"
          :class="{ 'tab-active': currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}
        </view>
      </view>
    </scroll-view>

    <view class="content">
      <view v-if="loading" class="empty-state">
        <text class="empty-emoji">⏳</text>
        <text class="empty-title">加载中...</text>
      </view>

      <view v-else-if="filteredJobs.length === 0" class="empty-state">
        <text class="empty-emoji">📋</text>
        <text class="empty-title">暂无职位</text>
        <text class="empty-desc">点击右下角按钮创建新职位</text>
      </view>

      <view v-else class="job-list">
        <view
          v-for="job in filteredJobs"
          :key="job.id"
          class="job-card"
          @click="navigateToDetail(job.id)"
        >
          <view class="card-header">
            <text class="card-title">{{ job.title }}</text>
            <view class="badge" :class="statusClass(job.status)">{{ statusLabel(job.status) }}</view>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="info-label">招聘人数</text>
              <text class="info-value">{{ job.headcount }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">总报名数</text>
              <text class="info-value">{{ job.applicationCount ?? 0 }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">待审核</text>
              <text class="info-value">{{ job.pendingApplicationCount ?? 0 }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">截止日期</text>
              <text class="info-value">{{ job.deadline || '不限' }}</text>
            </view>
          </view>

          <view class="card-footer" @click.stop>
            <view class="action-pill pill-blue" @click.stop="navigateToApplications(job)">报名记录</view>
            <view class="action-pill pill-green" @click.stop="handleShare(job.id)">邀请报名</view>
            <view class="action-pill pill-blue" @click="navigateToEdit(job.id)">编辑</view>
            <view
              v-if="job.status === 'DRAFT'"
              class="action-pill pill-green"
              @click="handlePublish(job.id)"
            >发布</view>
            <view
              v-if="job.status === 'PUBLISHED'"
              class="action-pill pill-orange"
              @click="handleClose(job.id)"
            >关闭</view>
            <view
              v-if="job.status === 'CLOSED'"
              class="action-pill pill-blue"
              @click="handleReopen(job.id)"
            >重新发布</view>
            <view
              v-if="job.status === 'CLOSED' && (job.applicationCount ?? 0) === 0"
              class="action-pill pill-red"
              @click="handleDelete(job.id)"
            >删除</view>
          </view>
        </view>
      </view>
    </view>

    <view class="fab" @click="navigateToCreate">
      <text class="fab-icon">+</text>
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

.tabs-wrap {
  background: #fff;
  border-bottom: 2rpx solid #eee;
}

.tabs-inner {
  display: flex;
  white-space: nowrap;
  padding: 20rpx 32rpx;
  gap: 16rpx;
}

.tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 28rpx;
  font-size: 26rpx;
  color: #64748b;
  background: #f1f5f9;
  border-radius: 999rpx;
  flex-shrink: 0;
}

.tab-active {
  color: #fff;
  background: #07c160;
}

.content {
  padding: 24rpx 32rpx;
  padding-bottom: 140rpx;
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

.job-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.job-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
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

.badge-red {
  background: #feecec;
  color: #df3b30;
}

.badge-gray {
  background: #eef1f0;
  color: #7b8580;
}

.badge-yellow {
  background: #fff7df;
  color: #d28a00;
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
}

.info-value {
  font-size: 26rpx;
  color: #1f2933;
}

.card-footer {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.action-pill {
  font-size: 24rpx;
  font-weight: 600;
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid;
}

.pill-green {
  border-color: #07c160;
  color: #07c160;
}

.pill-blue {
  border-color: #3b82f6;
  color: #3b82f6;
}

.pill-orange {
  border-color: #f59e0b;
  color: #f59e0b;
}

.pill-red {
  border-color: #ef4444;
  color: #ef4444;
}

.fab {
  position: fixed;
  right: 32rpx;
  bottom: 40rpx;
  width: 96rpx;
  height: 96rpx;
  background: linear-gradient(135deg, #18c86b, #08a95a);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 28rpx rgba(7, 193, 96, 0.4);
  z-index: 100;
}

.fab-icon {
  font-size: 52rpx;
  line-height: 1;
}
</style>
