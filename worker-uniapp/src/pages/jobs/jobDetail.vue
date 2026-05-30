<template>
  <view class="detail-page">
    <uni-load-more v-if="loading" status="loading" />

    <template v-if="job">
      <view class="detail-header">
        <text class="job-title">{{ job.title }}</text>
        <text class="job-pay">{{ job.rates?.[0]?.amount ?? '-' }}元/{{ rateUnit(job.rates?.[0]?.type) }}</text>
      </view>

      <view class="info-section">
        <view class="info-row">
          <text class="info-label">工作地点</text>
          <text class="info-value">{{ job.location }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">招聘人数</text>
          <text class="info-value">{{ job.headcount }}人</text>
        </view>
        <view class="info-row">
          <text class="info-label">报名截止</text>
          <text class="info-value">{{ job.deadline }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">发布企业</text>
          <text class="info-value">{{ job.companyName }}</text>
        </view>
        <view v-if="applyStatusText" class="info-row">
          <text class="info-label">报名状态</text>
          <text class="info-value">{{ displayApplyStatus(applyStatusText) }}</text>
        </view>
      </view>

      <view class="map-section" v-if="job.latitude && job.longitude">
        <map :latitude="job.latitude" :longitude="job.longitude" :markers="markers" style="width:100%;height:300rpx;border-radius:16rpx" />
        <text class="map-address">{{ job.province }} {{ job.city }} {{ job.district }} {{ job.address }}</text>
        <view class="map-meta">
          <text class="map-coords">{{ job.latitude }}, {{ job.longitude }}</text>
          <text class="map-link" @click="handleOpenLocation">定位</text>
        </view>
      </view>

      <view class="section">
        <text class="section-title">
          选择排班时段
          <span v-if="canApplyMore" class="selected-count">（已选 {{ pendingScheduleIds.length }} 个）</span>
          <span v-if="appliedScheduleIds.length" class="applied-count">（已报名 {{ appliedScheduleIds.length }} 个）</span>
        </text>
        <view class="schedule-slots" v-if="job.schedules?.length">
          <view
            class="slot"
            :class="slotClass(slot.id)"
            v-for="slot in job.schedules"
            :key="slot.id"
            @click="!isScheduleApplied(slot.id) && toggleSchedule(slot.id)"
          >
            <text class="slot-check">{{ slotCheckText(slot.id) }}</text>
            <text class="slot-date">{{ slot.date }}</text>
            <text class="slot-time">{{ slot.startTime }}-{{ slot.endTime }}</text>
            <text v-if="isScheduleApplied(slot.id)" class="slot-badge">已报名</text>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-title">职位描述</text>
        <text class="job-desc">{{ job.description }}</text>
      </view>

      <view class="bottom-bar">
        <button
          class="apply-btn"
          type="primary"
          :disabled="isJobFull || (!canApplyMore && authStore.isLoggedIn)"
          @click="handleApply"
        >
          {{ applyButtonText }}
        </button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJobDetail, applyJob } from '@/api/jobs'
import { useAuthStore } from '@/store'

function rateUnit(type) {
  const map = { HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' }
  return map[type] || '小时'
}

function rateTypeLabel(type) {
  const map = { HOURLY: '时薪', DAILY: '日薪', PIECEWORK: '计件', PIECE: '计件', MONTHLY: '月薪' }
  return map[type] || type || '-'
}

const job = ref<any>(null)
const loading = ref(true)
const appliedStatus = ref<string | null>(null)
const appliedScheduleIds = ref<number[]>([])
const selectedScheduleIds = ref<number[]>([])
const authStore = useAuthStore()
const markers = computed(() => {
  if (!job.value?.latitude || !job.value?.longitude) return []
  return [{
    id: 1,
    latitude: job.value.latitude,
    longitude: job.value.longitude,
    title: job.value.title || '',
    width: 30,
    height: 30
  }]
})

const applyStatusText = computed(() => job.value?.applyStatus || appliedStatus.value || '')

const pendingScheduleIds = computed(() =>
  selectedScheduleIds.value.filter(id => !appliedScheduleIds.value.includes(id))
)

const canApplyMore = computed(() =>
  !!authStore.isLoggedIn && job.value?.schedules?.length !== appliedScheduleIds.value.length
)

const isJobFull = computed(() => {
  if (!job.value) return false
  return job.value.acceptedCount >= job.value.headcount
})

const applyButtonText = computed(() => {
  if (isJobFull.value) return '已招满'
  if (!authStore.isLoggedIn) return '登录报名'
  if (!appliedScheduleIds.value.length) return '立即报名'
  if (canApplyMore.value) return '补报名'
  return '已报名全部排班'
})

function buildRedirectUrl() {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const route = page?.route ? `/${page.route}` : '/pages/jobs/jobDetail'
  const options = page?.options || {}
  const query = Object.entries(options)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return `${route}${query ? `?${query}` : ''}`
}

function goToLogin() {
  uni.navigateTo({
    url: `/pages/login/login?redirect=${encodeURIComponent(buildRedirectUrl())}`
  })
}

async function loadDetail() {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const id = page?.options?.id || page?.options?.scene
  if (!id) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    return
  }

  try {
    const res: any = await getJobDetail(id)
    job.value = res
    if (res.applyStatus) {
      appliedStatus.value = res.applyStatus
    }
    appliedScheduleIds.value = res.appliedScheduleIds ? [...res.appliedScheduleIds] : []
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function isScheduleApplied(id: number) {
  return appliedScheduleIds.value.includes(id)
}

function slotClass(id: number) {
  if (isScheduleApplied(id)) return 'applied'
  if (selectedScheduleIds.value.includes(id)) return 'selected'
  return ''
}

function slotCheckText(id: number) {
  if (isScheduleApplied(id)) return '✓'
  if (selectedScheduleIds.value.includes(id)) return '✓'
  return ''
}

function toggleSchedule(scheduleId: number) {
  if (isScheduleApplied(scheduleId)) return
  const idx = selectedScheduleIds.value.indexOf(scheduleId)
  if (idx >= 0) {
    selectedScheduleIds.value.splice(idx, 1)
  } else {
    selectedScheduleIds.value.push(scheduleId)
  }
}

async function handleApply() {
  if (!job.value) return
  if (!authStore.isLoggedIn) {
    goToLogin()
    return
  }
  if (isJobFull.value) {
    uni.showToast({ title: '该岗位已招满', icon: 'none' })
    return
  }
  const idsToSubmit = [...pendingScheduleIds.value]
  if (idsToSubmit.length === 0) {
    uni.showToast({ title: '请选择未报名的排班时段', icon: 'none' })
    return
  }
  try {
    await applyJob(job.value.id, {
      jobId: job.value.id,
      scheduleIds: idsToSubmit
    })
    appliedScheduleIds.value = [...appliedScheduleIds.value, ...idsToSubmit]
    appliedStatus.value = '已报名'
    job.value.applyStatus = '已报名'
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch (err: any) {
    const msg = err.message || err.errMsg || ''
    if (msg.includes('已招满')) {
      uni.showToast({ title: '该岗位已招满', icon: 'none' })
    } else {
      uni.showToast({ title: '报名失败', icon: 'none' })
    }
  }
}

function displayApplyStatus(status?: string) {
  if (status === 'PENDING') return '已报名'
  if (status === 'ACCEPTED') return '已通过'
  if (status === 'REJECTED') return '未通过'
  return status || ''
}

function handleOpenLocation() {
  if (!job.value?.latitude || !job.value?.longitude) {
    uni.showToast({ title: '暂无定位信息', icon: 'none' })
    return
  }
  uni.openLocation({
    latitude: Number(job.value.latitude),
    longitude: Number(job.value.longitude),
    name: job.value.title || '岗位地点',
    address: [job.value.province, job.value.city, job.value.district, job.value.address].filter(Boolean).join(' ')
  })
}

onLoad(loadDetail)
</script>

<style scoped>
.detail-page {
  padding: 30rpx;
  padding-bottom: 140rpx;
}

.detail-header {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.job-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.job-pay {
  font-size: 32rpx;
  color: #f60;
  font-weight: 600;
}

.info-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #999;
}

.info-value {
  font-size: 28rpx;
  color: #333;
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.salary-table {
  border: 1rpx solid #f0f0f0;
  border-radius: 8rpx;
}

.salary-row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.salary-row:last-child {
  border-bottom: none;
}

.salary-type {
  font-size: 26rpx;
  color: #666;
}

.salary-amount {
  font-size: 26rpx;
  color: #f60;
  font-weight: 500;
}

.schedule-slots {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.slot {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 20rpx;
  background: #f9f9f9;
  border-radius: 8rpx;
  border: 1rpx solid transparent;
  transition: all 0.2s;
}

.slot.selected {
  background: #e8f8ee;
  border-color: #07c160;
}

.slot-check {
  width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  border-radius: 50%;
  background: #e0e0e0;
  color: #fff;
  font-size: 20rpx;
  flex-shrink: 0;
}

.slot.selected .slot-check {
  background: #07c160;
}

.slot.applied .slot-check {
  background: #07c160;
}

.slot.applied {
  background: #e8f8ee;
  border-color: #07c160;
  opacity: 0.8;
}

.slot-badge {
  font-size: 20rpx;
  color: #07c160;
  background: #d4f5e0;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  white-space: nowrap;
  flex-shrink: 0;
}

.slot-date {
  font-size: 26rpx;
  color: #333;
  flex: 1;
}

.slot-time {
  font-size: 26rpx;
  color: #666;
}

.selected-count {
  font-size: 22rpx;
  color: #07c160;
  font-weight: 400;
}

.applied-count {
  font-size: 22rpx;
  color: #999;
  font-weight: 400;
}

.job-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.map-section {
  margin-bottom: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}
.map-address {
  display: block;
  padding: 12rpx 16rpx;
  background: #fff;
  font-size: 24rpx;
  color: #666;
}

.map-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16rpx 12rpx;
  background: #fff;
}

.map-coords {
  font-size: 22rpx;
  color: #999;
}

.map-link {
  font-size: 24rpx;
  color: #07c160;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.apply-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  border-radius: 44rpx;
  font-size: 32rpx;
  color: #fff;
  border: none;
}

.apply-btn.applied {
  background: #ccc;
}

.apply-btn[disabled] {
  background: #ccc;
}
</style>
