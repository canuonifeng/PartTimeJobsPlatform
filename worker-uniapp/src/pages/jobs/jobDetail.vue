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
      </view>

      <view class="map-section" v-if="job.latitude && job.longitude">
        <map :latitude="job.latitude" :longitude="job.longitude" :markers="markers" style="width:100%;height:300rpx;border-radius:16rpx" />
        <text class="map-address">{{ job.province }} {{ job.city }} {{ job.district }} {{ job.address }}</text>
      </view>

      <view class="section">
        <text class="section-title">薪资说明</text>
        <view class="salary-table" v-if="job.rates?.length">
          <view class="salary-row" v-for="rate in job.rates" :key="rate.id">
            <text class="salary-type">{{ rateTypeLabel(rate.type) }}</text>
            <text class="salary-amount">{{ rate.amount }}元/{{ rateUnit(rate.type) }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-title">工作时段</text>
        <view class="schedule-slots" v-if="job.schedules?.length">
          <view class="slot" v-for="slot in job.schedules" :key="slot.id">
            <text class="slot-date">{{ slot.date }}</text>
            <text class="slot-time">{{ slot.startTime }}-{{ slot.endTime }}</text>
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
          :disabled="!!appliedStatus"
          :class="{ applied: !!appliedStatus }"
          @click="handleApply"
        >
          {{ appliedStatus || '立即报名' }}
        </button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getJobDetail, applyJob } from '@/api/jobs'

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
const markers = computed(() => {
  if (!job.value?.latitude || !job.value?.longitude) return []
  return [{
    latitude: job.value.latitude,
    longitude: job.value.longitude,
    title: job.value.title || ''
  }]
})

async function loadDetail() {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const id = page?.options?.id
  if (!id) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    return
  }

  try {
    const res: any = await getJobDetail(id)
    job.value = res
    if (res.applyStatus) {
      appliedStatus.value = res.applyStatus === 'approved' ? '已通过'
        : res.applyStatus === 'rejected' ? '未通过'
        : '已报名'
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function handleApply() {
  if (appliedStatus.value || !job.value) return
  try {
    await applyJob(job.value.id)
    appliedStatus.value = '已报名'
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch {
    uni.showToast({ title: '报名失败', icon: 'none' })
  }
}

onMounted(loadDetail)
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
  justify-content: space-between;
  padding: 16rpx 20rpx;
  background: #f9f9f9;
  border-radius: 8rpx;
}

.slot-date {
  font-size: 26rpx;
  color: #333;
}

.slot-time {
  font-size: 26rpx;
  color: #666;
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
