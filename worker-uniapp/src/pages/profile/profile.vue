<template>
  <view class="profile-page">
    <view class="profile-header">
      <image class="avatar" :src="profile?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
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
        <text>编辑资料</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/auth/realName')">
        <text>实名认证</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/schedule/schedule')">
        <text>我的排班</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/attendance/clockIn')">
        <text>打卡记录</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/earnings/earnings')">
        <text>我的收入</text>
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
      if (res.confirm) {
        authStore.logout()
      }
    }
  })
}

onMounted(async () => {
  try {
    const res: any = await getProfile()
    profile.value = res
    authStore.setWorkerInfo(res)
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.profile-page {
  padding: 30rpx;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  margin-bottom: 20rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.phone {
  font-size: 26rpx;
  color: #999;
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.info-row {
  display: flex;
  flex-direction: column;
  padding: 16rpx 0;
}

.info-label {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.skill-tag {
  padding: 8rpx 20rpx;
  background: #e8f8ee;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #07c160;
}

.empty-tip {
  font-size: 24rpx;
  color: #ccc;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.menu-list {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
  font-size: 28rpx;
  color: #333;
}

.menu-item:last-child {
  border-bottom: none;
}

.arrow {
  color: #ccc;
  font-size: 28rpx;
}

.logout-area {
  padding: 20rpx 0;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #fff;
  border: 1rpx solid #e0e0e0;
  border-radius: 44rpx;
  font-size: 28rpx;
  color: #f60;
}
</style>
