<template>
  <view class="profile-page">
    <view class="profile-header">
      <view class="avatar-wrap">
        <image class="avatar" :src="profile?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="avatar-ring"></view>
      </view>
      <text class="nickname">{{ profile?.name || '未设置' }}</text>
      <text class="phone" v-if="profile?.phone">{{ profile.phone }}</text>
    </view>

    <view class="info-card">
      <view class="info-row">
        <text class="info-label">技能标签</text>
        <view class="skill-tags">
          <text v-for="(skill, i) in profile?.skills" :key="i" class="skill-tag">{{ skill }}</text>
          <text v-if="!profile?.skills?.length" class="empty-tip">暂无</text>
        </view>
      </view>
      <view class="info-row">
        <text class="info-label">可工作日期</text>
        <text class="info-value">{{ profile?.availableDays?.map((d: string) => dayLabelMap[d] || d).join('、') || '暂无' }}</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @click="navTo('/pages/profile/edit')">
        <view class="menu-left">
          <text class="menu-icon">✏️</text>
          <text class="menu-text">编辑资料</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/auth/realName')">
        <view class="menu-left">
          <text class="menu-icon">🪪</text>
          <text class="menu-text">实名认证</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/bank/bankCard')">
        <view class="menu-left">
          <text class="menu-icon">💳</text>
          <text class="menu-text">银行卡</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/schedule/schedule')">
        <view class="menu-left">
          <text class="menu-icon">📅</text>
          <text class="menu-text">我的排班</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/attendance/clockIn')">
        <view class="menu-left">
          <text class="menu-icon">📍</text>
          <text class="menu-text">打卡记录</text>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/earnings/earnings')">
        <view class="menu-left">
          <text class="menu-icon">💰</text>
          <text class="menu-text">我的收入</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="logout-area">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/store'
import { getProfile } from '@/api/profile'

const dayLabelMap: Record<string, string> = {
  Monday: '周一', Tuesday: '周二', Wednesday: '周三', Thursday: '周四',
  Friday: '周五', Saturday: '周六', Sunday: '周日'
}

const authStore = useAuthStore()
const profile = ref<any>(null)

function navTo(url: string) {
  uni.navigateTo({ url })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) authStore.logout()
    }
  })
}

onMounted(async () => {
  try {
    const res: any = await getProfile()
    profile.value = res
    authStore.setWorkerInfo(res)
  } catch {}
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  padding-bottom: 40rpx;
}
.profile-header {
  background: linear-gradient(135deg, #07c160, #059d50);
  margin: -30rpx -30rpx 60rpx;
  padding: 80rpx 30rpx 200rpx;
  color: #fff;
  border-bottom-left-radius: 30rpx;
  border-bottom-right-radius: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-wrap {
  position: relative;
  margin-bottom: 24rpx;
}
.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  position: relative;
  z-index: 1;
}
.avatar-ring {
  position: absolute;
  top: -8rpx;
  left: -8rpx;
  width: 176rpx;
  height: 176rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.4);
}
.nickname {
  font-size: 44rpx;
  font-weight: 700;
  margin-bottom: 10rpx;
}
.phone {
  font-size: 30rpx;
  opacity: 0.9;
}
.info-card {
  background: #fff;
  border-radius: 24rpx;
  margin: -120rpx 24rpx 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  position: relative;
  z-index: 1;
}
.info-row {
  display: flex;
  flex-direction: column;
  padding: 16rpx 0;
}
.info-row + .info-row {
  border-top: 2rpx solid #f5f5f5;
  margin-top: 8rpx;
  padding-top: 24rpx;
}
.info-label {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 14rpx;
}
.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}
.skill-tag {
  padding: 10rpx 24rpx;
  background: #ecfdf5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #07c160;
  font-weight: 500;
}
.empty-tip {
  font-size: 28rpx;
  color: #ccc;
}
.info-value {
  font-size: 30rpx;
  color: #333;
  line-height: 1.5;
}
.menu-list {
  background: #fff;
  border-radius: 24rpx;
  margin: 0 24rpx 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  overflow: hidden;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  border-bottom: 2rpx solid #f5f5f5;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.menu-icon {
  font-size: 36rpx;
}
.menu-text {
  font-size: 34rpx;
  color: #333;
  font-weight: 500;
}
.arrow {
  font-size: 44rpx;
  color: #ccc;
  font-weight: 300;
}
.logout-area {
  padding: 0 24rpx;
}
.logout-btn {
  width: 100%;
  height: 100rpx;
  line-height: 100rpx;
  background: #fff;
  color: #ff3b30;
  border-radius: 50rpx;
  font-size: 34rpx;
  font-weight: 600;
  border: 2rpx solid #ff3b30;
}
</style>
