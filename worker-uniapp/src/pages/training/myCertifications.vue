<template>
  <view class="certs-page">
    <uni-load-more v-if="loading" status="loading" />

    <view v-if="!loading && certs.length === 0" class="empty-state">
      <text class="empty-icon">🎓</text>
      <text class="empty-text">还没有获得技能认证</text>
      <button class="empty-btn" @click="goTraining">去培训</button>
    </view>

    <view v-else class="cert-list">
      <view v-for="cert in certs" :key="cert.certificationId" class="cert-card">
        <view class="cert-top">
          <view class="cert-badge" :class="{ expired: cert.status === 'EXPIRED' }">
            <text class="badge-text">{{ cert.status === 'EXPIRED' ? '已过期' : '已认证' }}</text>
          </view>
          <text class="cert-name">{{ cert.name }}</text>
        </view>
        <view class="cert-meta">
          <text class="meta-row">编码：{{ cert.code }}</text>
          <text class="meta-row">适用：{{ taskTypeText(cert.taskType) }}</text>
          <text class="meta-row">获得：{{ formatTime(cert.grantedAt) }}</text>
          <text class="meta-row">有效期：{{ cert.expiresAt ? '至 ' + formatTime(cert.expiresAt) : '永久有效' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyCertifications } from '@/api/training'

const certs = ref<any[]>([])
const loading = ref(true)

function taskTypeText(type: string) {
  if (type === 'ANNOTATION') return '标注任务'
  if (type === 'WORK') return '普通工作'
  return type || '通用'
}

function formatTime(value: string) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function goTraining() {
  uni.navigateTo({ url: '/pages/training/trainingList' })
}

async function load() {
  loading.value = true
  try {
    const res: any = await getMyCertifications()
    certs.value = Array.isArray(res) ? res : []
  } catch {
    certs.value = []
    uni.showToast({ title: '认证列表加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onShow(load)
</script>

<style scoped>
.certs-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 24rpx;
  box-sizing: border-box;
}

.cert-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.cert-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.cert-top {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.cert-badge {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: #d1fae5;
}

.badge-text {
  font-size: 22rpx;
  color: #059669;
  font-weight: 600;
}

.cert-badge.expired {
  background: #fee2e2;
}

.cert-badge.expired .badge-text {
  color: #dc2626;
}

.cert-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.cert-meta {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.meta-row {
  font-size: 26rpx;
  color: #6b7280;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  gap: 20rpx;
}

.empty-icon {
  font-size: 90rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.empty-btn {
  margin-top: 20rpx;
  padding: 0 60rpx;
  height: 76rpx;
  line-height: 76rpx;
  border-radius: 38rpx;
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: #fff;
  font-size: 28rpx;
  border: none;
}

.empty-btn::after {
  border: none;
}
</style>
