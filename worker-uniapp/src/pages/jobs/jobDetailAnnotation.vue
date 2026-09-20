<template>
  <view class="detail-page">
    <uni-load-more v-if="loading" status="loading" />

    <view v-if="!job && !loading" class="empty-state">
      <text class="empty-text">任务信息加载失败</text>
    </view>

    <template v-if="job && !loading">
      <scroll-view class="detail-content" scroll-y>
        <view class="detail-banner">
          <view class="banner-overlay"></view>
          <view class="banner-info">
            <text class="banner-title">📝 {{ job.title }}</text>
            <view class="banner-tags">
              <text class="banner-tag">标注任务</text>
              <text class="banner-tag">{{ unitPrice }}元/条</text>
              <text class="banner-tag">共{{ totalItems }}条</text>
            </view>
          </view>
        </view>

        <view class="job-info-card">
          <text class="job-info-title">{{ job.title }}</text>
          <view v-if="job.description" class="job-info-desc">
            <text>{{ job.description }}</text>
          </view>
        </view>

        <view class="detail-card pricing-card">
          <view class="card-title">
            <view class="title-icon"><text>💰</text></view>
            <text class="title-text">价格信息</text>
          </view>
          <view class="pricing-row">
            <view class="pricing-item">
              <text class="pricing-value">{{ unitPrice }}元</text>
              <text class="pricing-label">单价/条</text>
            </view>
            <view class="pricing-divider"></view>
            <view class="pricing-item">
              <text class="pricing-value">{{ totalItems }}条</text>
              <text class="pricing-label">总条数</text>
            </view>
            <view class="pricing-divider"></view>
            <view class="pricing-item">
              <text class="pricing-value highlight">¥{{ estimatedEarnings }}</text>
              <text class="pricing-label">预计收入</text>
            </view>
          </view>
        </view>

        <view class="detail-card">
          <view class="card-title">
            <view class="title-icon"><text>📦</text></view>
            <text class="title-text">可抢单批次</text>
          </view>
          <view v-if="batches.length === 0" class="empty-hint">
            <text>暂无可用批次</text>
          </view>
          <view v-for="batch in batches" :key="batch.id" class="batch-card">
            <view class="batch-left">
              <text class="batch-title">批次 #{{ batch.id }}</text>
              <text class="batch-info">共 {{ batch.totalItems }} 条，剩余 {{ batch.remainingItems }} 条</text>
            </view>
            <view class="batch-right">
              <text class="batch-remaining" :class="{ low: batch.remainingItems < 10 }">{{ batch.remainingItems }}条可抢</text>
            </view>
          </view>
        </view>

        <view class="detail-card">
          <view class="card-title">
            <view class="title-icon"><text>📋</text></view>
            <text class="title-text">任务说明</text>
          </view>
          <view v-if="job.responsibilities" class="duty-list">
            <view v-for="(item, index) in dutyItems" :key="index" class="duty-item">
              <view class="duty-dot"></view>
              <text class="duty-text">{{ item }}</text>
            </view>
          </view>
          <view v-else class="duty-list">
            <view class="duty-item">
              <view class="duty-dot"></view>
              <text class="duty-text">按照标注指南完成数据标注</text>
            </view>
            <view class="duty-item">
              <view class="duty-dot"></view>
              <text class="duty-text">保证标注质量和准确性</text>
            </view>
          </view>
        </view>

        <view class="bottom-safe"></view>
      </scroll-view>

      <view class="action-bar">
        <button
          class="apply-btn-main"
          :class="{ disabled: !canGrab }"
          :disabled="!canGrab"
          @click="handleGrab"
        >
          {{ grabButtonText }}
        </button>
      </view>
    </template>

    <SuccessOverlay
      :visible="showSuccess"
      title="抢单成功！"
      description="请尽快开始标注任务，完成后系统将自动结算"
      primary-text="查看我的任务"
      secondary-text="继续找活"
      icon="🎉"
      @primary="goToMyTasks"
      @secondary="closeSuccessAndBack"
    />

    <LoginSheet />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAnnotationJobDetail, grabTaskOrder } from '@/api/jobs'
import { useAuthStore } from '@/store'
import LoginSheet from '@/components/LoginSheet.vue'
import SuccessOverlay from '@/components/SuccessOverlay.vue'
import { openLoginSheet } from '@/utils/loginSheet'

const authStore = useAuthStore()
const job = ref<any>(null)
const loading = ref(true)
const jobId = ref(0)
const showSuccess = ref(false)
const grabbing = ref(false)

const title = computed(() => job.value?.title || '')
const unitPrice = computed(() => {
  if (job.value?.unitPrice) return job.value.unitPrice
  if (job.value?.rates && job.value.rates.length > 0) return job.value.rates[0].amount
  return 0
})
const totalItems = computed(() => job.value?.totalItems || 0)
const estimatedEarnings = computed(() => {
  const price = Number(unitPrice.value) || 0
  const items = Number(totalItems.value) || 0
  const result = price * items
  return result % 1 === 0 ? result : result.toFixed(2)
})
const batches = computed(() => {
  if (Array.isArray(job.value?.jobSchedules)) return job.value.jobSchedules
  if (Array.isArray(job.value?.batches)) return job.value.batches
  return []
})
const dutyItems = computed(() => {
  const text = job.value?.responsibilities || job.value?.description || ''
  if (!text) return []
  return String(text).split(/\n|。|；|;/).map((item: string) => item.trim()).filter(Boolean)
})

const canGrab = computed(() => {
  if (!job.value || job.value.status === 'CLOSED') return false
  if (batches.value.length === 0) return false
  return batches.value.some((b: any) => b.remainingItems > 0)
})

const grabButtonText = computed(() => {
  if (!job.value) return '加载中'
  if (job.value.status === 'CLOSED') return '已关闭'
  if (batches.value.length === 0) return '暂无可用批次'
  if (grabbing.value) return '抢单中...'
  return '立即抢单'
})

function num(value: any) {
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

async function handleGrab() {
  if (!authStore.token) {
    openLoginSheet({ success: handleGrab })
    return
  }
  if (!canGrab.value || grabbing.value) return

  grabbing.value = true
  try {
    await grabTaskOrder(jobId.value, {})
    showSuccess.value = true
  } catch (err: any) {
    uni.showToast({ title: err?.data?.error || err?.message || '抢单失败', icon: 'none' })
  } finally {
    grabbing.value = false
  }
}

function goToMyTasks() {
  showSuccess.value = false
  uni.navigateTo({ url: '/pages/earnings/myTaskOrders' })
}

function closeSuccessAndBack() {
  showSuccess.value = false
}

async function loadJob() {
  loading.value = true
  if (jobId.value <= 0) {
    job.value = null
    loading.value = false
    uni.showToast({ title: '任务信息无效', icon: 'none' })
    return
  }
  try {
    const res: any = await getAnnotationJobDetail(jobId.value)
    job.value = res || null
  } catch {
    job.value = null
    uni.showToast({ title: '任务信息加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((params: any) => {
  const id = Number(params.id)
  jobId.value = Number.isFinite(id) && id > 0 ? id : 0
  loadJob()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f6fa;
  position: relative;
}

.detail-banner {
  position: relative;
  height: 320rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 55%, #93c5fd 100%);
  padding: 60rpx 32rpx 32rpx;
  color: #fff;
  overflow: hidden;
}

.detail-banner::before {
  content: '';
  position: absolute;
  top: -80rpx;
  right: -40rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.banner-info {
  position: relative;
  z-index: 5;
  padding-top: 20rpx;
}

.banner-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 16rpx;
}

.banner-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 12rpx;
}

.banner-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.25);
  font-size: 22rpx;
  font-weight: 500;
}

.job-info-card {
  background: #fff;
  margin: 30rpx 24rpx 16rpx;
  border-radius: 20rpx;
  padding: 28rpx 28rpx 24rpx;
  position: relative;
  z-index: 5;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.job-info-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
  margin-bottom: 16rpx;
}

.job-info-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.detail-content {
  height: calc(100vh - 140rpx);
}

.detail-card {
  margin: 20rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.05), 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.title-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #e6f0ff, #c6d9f7);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.pricing-row {
  display: flex;
  align-items: center;
}

.pricing-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pricing-value {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1a2e;
  line-height: 1.2;
}

.pricing-value.highlight {
  color: #3b82f6;
}

.pricing-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.pricing-divider {
  width: 2rpx;
  height: 60rpx;
  background: #f0f0f0;
}

.batch-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
}

.batch-card:last-child {
  margin-bottom: 0;
}

.batch-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.batch-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
}

.batch-info {
  font-size: 24rpx;
  color: #999;
}

.batch-right {
  flex-shrink: 0;
}

.batch-remaining {
  font-size: 26rpx;
  font-weight: 600;
  color: #3b82f6;
  padding: 6rpx 16rpx;
  background: #eff6ff;
  border-radius: 12rpx;
}

.batch-remaining.low {
  color: #ef4444;
  background: #fee2e2;
}

.duty-list {
  padding: 0;
}

.duty-item {
  display: flex;
  gap: 16rpx;
  padding: 10rpx 0;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
}

.duty-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #3b82f6;
  flex-shrink: 0;
  margin-top: 16rpx;
}

.duty-text {
  flex: 1;
}

.empty-hint {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

.bottom-safe {
  height: 40rpx;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 140rpx;
  padding: 20rpx 24rpx 40rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  align-items: center;
  z-index: 50;
}

.apply-btn-main {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 40%, #2563eb 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 10rpx 28rpx rgba(59, 130, 246, 0.45);
  margin: 0;
}

.apply-btn-main:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.apply-btn-main::after {
  border: none;
}

.apply-btn-main.disabled {
  background: #d1d5db;
  color: #9ca3af;
  box-shadow: none;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
