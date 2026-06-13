<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getJobs, publishJob, closeJob, reopenJob, getJobShareLink } from '@/api/jobs'
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

async function handleShare(job) {
  if (job.status !== 'PUBLISHED') {
    uni.showToast({ title: '仅已发布职位可邀请', icon: 'none' })
    return
  }
  try {
    const res = await getJobShareLink(job.id)
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
  <scroll-view scroll-y class="op-page jobs-page">
    <view class="top-space"></view>
    <view class="op-hero jobs-hero">
      <text class="op-hero-kicker">职位运营</text>
      <text class="op-hero-title">管理发布、报名与职位状态</text>
      <text class="op-hero-desc">{{ filteredJobs.length }} 个职位 · {{ tabs[currentTab].name }}视图</text>
    </view>

    <scroll-view scroll-x class="status-tabs" scroll-with-animation>
      <view class="status-tabs-inner">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          class="status-tab"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}
        </view>
      </view>
    </scroll-view>

    <view class="op-content jobs-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="filteredJobs.length === 0" class="e-empty">
        <text class="e-empty-title">暂无职位</text>
        <text class="e-empty-desc">点击右下角按钮创建新职位</text>
      </view>

      <view v-else class="job-list">
        <view v-for="job in filteredJobs" :key="job.id" class="op-card job-card" @click="navigateToDetail(job.id)">
          <view class="op-row">
            <view class="op-row-main">
              <text class="op-row-title">{{ job.title }}</text>
              <text class="op-row-desc">截止 {{ job.deadline || '不限' }}</text>
            </view>
            <text class="op-pill" :class="statusClass(job.status) === 'badge-red' ? 'op-pill-danger' : statusClass(job.status) === 'badge-gray' ? 'pill-gray' : ''">{{ statusLabel(job.status) }}</text>
          </view>

          <view class="metric-grid">
            <view class="metric-item">
              <text class="metric-label">招聘人数</text>
              <text class="metric-value">{{ job.headcount ?? '-' }}</text>
            </view>
            <view class="metric-item">
              <text class="metric-label">总报名</text>
              <text class="metric-value">{{ job.applicationCount ?? 0 }}</text>
            </view>
            <view class="metric-item">
              <text class="metric-label">待审核</text>
              <text class="metric-value warn">{{ job.pendingApplicationCount ?? 0 }}</text>
            </view>
          </view>

          <view class="action-row" @click.stop>
            <view class="action-btn light" @click.stop="navigateToApplications(job)">报名</view>
            <view v-if="job.status === 'PUBLISHED'" class="action-btn light" @click.stop="handleShare(job)">邀请</view>
            <view class="action-btn light" @click.stop="navigateToEdit(job.id)">编辑</view>
            <view v-if="job.status === 'DRAFT'" class="action-btn primary" @click.stop="handlePublish(job.id)">发布</view>
            <view v-if="job.status === 'PUBLISHED'" class="action-btn warn" @click.stop="handleClose(job.id)">关闭</view>
            <view v-if="job.status === 'CLOSED'" class="action-btn primary" @click.stop="handleReopen(job.id)">重发</view>
          </view>
        </view>
      </view>
    </view>

    <view class="fab" @click="navigateToCreate">
      <text class="fab-icon">+</text>
    </view>
  </scroll-view>
</template>

<style scoped>
.jobs-page { height: 100vh; }
.top-space { height: 24rpx; }
.jobs-content { padding-bottom: 160rpx; }
.status-tabs { width: 100%; margin-top: 22rpx; white-space: nowrap; }
.status-tabs-inner { display: flex; padding: 0 28rpx; }
.status-tab { flex-shrink: 0; margin-right: 16rpx; padding: 14rpx 28rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 26rpx; font-weight: 800; box-shadow: 0 8rpx 20rpx rgba(23,83,53,.06); }
.status-tab.active { background: #16a34a; color: #fff; }
.job-list { display: flex; flex-direction: column; gap: 20rpx; }
.job-card { overflow: hidden; }
.pill-gray { color: #64748b; background: #f1f5f9; }
.metric-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin-top: 22rpx; }
.metric-item { min-width: 0; padding: 16rpx; border-radius: 18rpx; background: #f8fafc; }
.metric-label { display: block; font-size: 22rpx; color: #64748b; }
.metric-value { display: block; margin-top: 8rpx; font-size: 30rpx; font-weight: 850; color: #1f2933; }
.metric-value.warn { color: #b45309; }
.action-row { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 22rpx; }
.action-btn { flex: 1 1 120rpx; height: 64rpx; line-height: 64rpx; border-radius: 999rpx; text-align: center; font-size: 25rpx; font-weight: 850; }
.action-btn.light { background: #ecfdf5; color: #16a34a; }
.action-btn.primary { background: #16a34a; color: #fff; }
.action-btn.warn { background: #fffbeb; color: #b45309; }
.fab { position: fixed; right: 32rpx; bottom: 54rpx; width: 96rpx; height: 96rpx; background: linear-gradient(135deg, #18c86b, #08a95a); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 28rpx rgba(7, 193, 96, 0.4); z-index: 100; }
.fab-icon { font-size: 52rpx; line-height: 1; }
</style>
