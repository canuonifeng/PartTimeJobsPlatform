<template>
  <view class="page">
    <view class="header">
      <text class="title">邀请好友</text>
      <text class="subtitle">邀请好友加入平台，获得奖励</text>
    </view>

    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalReferees || 0 }}</text>
        <text class="stat-label">邀请人数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalRewardAmount || 0 }}</text>
        <text class="stat-label">累计奖励(元)</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.pendingRewardAmount || 0 }}</text>
        <text class="stat-label">待发放(元)</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">邀请链接</text>
      <view class="link-box">
        <text class="link-text">{{ referralLink }}</text>
        <button class="copy-btn" @click="copyLink">复制</button>
      </view>
    </view>

    <view class="section">
      <text class="section-title">邀请海报</text>
      <image v-if="posterUrl" :src="posterUrl" class="poster-image" mode="aspectFit" />
      <button class="share-btn" @click="sharePoster">分享海报</button>
    </view>

    <view class="section">
      <navigator url="/pages/referral/referralRecords" class="records-link">
        <text>查看邀请记录</text>
        <text class="arrow">›</text>
      </navigator>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getReferralLink, getReferralPoster, getReferralStats } from '@/api/referral'

const referralLink = ref('')
const referralCode = ref('')
const posterUrl = ref('')
const stats = ref({
  totalReferees: 0,
  totalRewardAmount: 0,
  pendingRewardAmount: 0
})

async function fetchReferralInfo() {
  try {
    const linkRes = await getReferralLink()
    referralLink.value = linkRes.link
    referralCode.value = linkRes.code

    const posterRes = await getReferralPoster()
    posterUrl.value = posterRes.posterUrl

    const statsRes = await getReferralStats()
    stats.value = statsRes
  } catch (e) {
    console.error('获取邀请信息失败', e)
  }
}

function copyLink() {
  uni.setClipboardData({
    data: referralLink.value,
    success: () => {
      uni.showToast({ title: '链接已复制', icon: 'success' })
    }
  })
}

function sharePoster() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

onShow(() => {
  fetchReferralInfo()
})
</script>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.header { text-align: center; padding: 40rpx 0; }
.title { font-size: 36rpx; font-weight: 600; color: #333; display: block; }
.subtitle { font-size: 26rpx; color: #999; margin-top: 8rpx; display: block; }
.stats-card { background: #fff; border-radius: 16rpx; padding: 32rpx; display: flex; justify-content: space-around; margin-bottom: 16rpx; }
.stat-item { text-align: center; }
.stat-value { font-size: 40rpx; font-weight: 600; color: #409eff; display: block; }
.stat-label { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.section-title { font-size: 28rpx; font-weight: 500; color: #333; margin-bottom: 16rpx; display: block; }
.link-box { display: flex; align-items: center; gap: 16rpx; }
.link-text { flex: 1; font-size: 24rpx; color: #666; background: #f5f5f5; padding: 16rpx; border-radius: 8rpx; word-break: break-all; }
.copy-btn { background: #409eff; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 32rpx; font-size: 26rpx; }
.poster-image { width: 100%; height: 400rpx; border-radius: 8rpx; margin-bottom: 16rpx; }
.share-btn { background: #67c23a; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 28rpx; }
.records-link { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.arrow { color: #999; }
</style>
