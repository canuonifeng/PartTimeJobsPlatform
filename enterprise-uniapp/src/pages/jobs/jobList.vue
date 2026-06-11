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
  <view class="page e-page">
    <view class="header e-header">
      <text class="e-header-title">职位管理</text>
      <text class="e-header-desc">管理发布、报名与职位状态</text>
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

    <view class="content e-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="filteredJobs.length === 0" class="e-empty">
        <text class="e-empty-title">暂无职位</text>
        <text class="e-empty-desc">点击右下角按钮创建新职位</text>
      </view>

      <view v-else class="job-list">
        <view
          v-for="job in filteredJobs"
          :key="job.id"
          class="job-card e-card"
          @click="navigateToDetail(job.id)"
        >
          <view class="e-card-title-row">
            <text class="job-title e-card-title">{{ job.title }}</text>
            <view class="e-badge" :class="'e-' + statusClass(job.status)">{{ statusLabel(job.status) }}</view>
          </view>

          <view class="e-info-grid">
            <view class="e-info-pill">
              <text class="e-info-label">招聘人数</text>
              <text class="e-info-value">{{ job.headcount }}</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">总报名数</text>
              <text class="e-info-value">{{ job.applicationCount ?? 0 }}</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">待审核</text>
              <text class="e-info-value">{{ job.pendingApplicationCount ?? 0 }}</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">截止日期</text>
              <text class="e-info-value">{{ job.deadline || '不限' }}</text>
            </view>
          </view>

          <view class="e-action-row" @click.stop>
            <view class="e-action-pill e-action-blue" @click.stop="navigateToApplications(job)">报名记录</view>
            <view class="e-action-pill e-action-primary" @click.stop="handleShare(job.id)">邀请报名</view>
            <view class="e-action-pill e-action-blue" @click="navigateToEdit(job.id)">编辑</view>
            <view
              v-if="job.status === 'DRAFT'"
              class="e-action-pill e-action-primary"
              @click="handlePublish(job.id)"
            >发布</view>
            <view
              v-if="job.status === 'PUBLISHED'"
              class="e-action-pill e-action-orange"
              @click="handleClose(job.id)"
            >关闭</view>
            <view
              v-if="job.status === 'CLOSED'"
              class="e-action-pill e-action-blue"
              @click="handleReopen(job.id)"
            >重新发布</view>
            <view
              v-if="job.status === 'CLOSED' && (job.applicationCount ?? 0) === 0"
              class="e-action-pill e-action-red"
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
.header {
  box-sizing: border-box;
}

.tabs-wrap {
  background: #fff;
  border-bottom: 2rpx solid #eee;
  box-sizing: border-box;
  white-space: nowrap;
}

.tabs-inner {
  display: flex;
  white-space: nowrap;
  padding: 20rpx 32rpx;
  box-sizing: border-box;
}

.tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 10rpx 28rpx;
  margin-right: 16rpx;
  font-size: 26rpx;
  color: #64748b;
  background: #f1f5f9;
  border-radius: 999rpx;
  box-sizing: border-box;
}

.tab:last-child {
  margin-right: 0;
}

.tab-active {
  color: #fff;
  background: #07c160;
}

.content {
  padding-bottom: 140rpx;
}

.job-list {
  box-sizing: border-box;
}

.job-card {
  width: 100%;
}

.job-title {
  min-width: 0;
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
