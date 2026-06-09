<template>
  <view class="profile-page">
    <view class="profile-header">
      <view class="profile-top">
        <image class="avatar" :src="profile?.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="user-info">
          <view class="name-row">
            <text class="nickname">{{ displayName }}</text>
            <text class="auth-badge" @click="navTo('/pages/auth/realName')">{{ realNameStatus }}</text>
          </view>
          <text class="phone">{{ displayPhone }}</text>
        </view>
      </view>
      <view class="stats-panel">
        <view v-for="item in stats" :key="item.label" class="stat-item">
          <text class="stat-value">{{ item.value }}</text>
          <text class="stat-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <view class="income-card">
      <view class="card-title-row">
        <text class="card-title">本月收入</text>
        <text class="card-link" @click="navTo('/pages/earnings/earnings')">查看明细</text>
      </view>
      <view class="income-main">
        <text class="income-symbol">¥</text>
          <text class="income-amount">{{ moneyText(monthIncome) }}</text>
      </view>
      <view class="income-sub-row">
        <view class="income-sub-item">
          <text class="income-sub-value">¥{{ moneyText(pendingAmount) }}</text>
          <text class="income-sub-label">待结算</text>
        </view>
        <view class="income-divider"></view>
        <view class="income-sub-item">
          <text class="income-sub-value">¥{{ moneyText(settledAmount) }}</text>
          <text class="income-sub-label">已结算</text>
        </view>
      </view>
    </view>

    <view v-for="(group, groupIndex) in menuGroups" :key="groupIndex" class="menu-group">
      <view v-for="item in group" :key="item.title" class="menu-item" @click="navTo(item.url)">
        <view class="menu-left">
          <view class="menu-icon" :class="item.iconClass">
            <text>{{ item.icon }}</text>
          </view>
          <text class="menu-text">{{ item.title }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="logout-area">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
  <InviteFloat />
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '@/store'
import { getProfileDashboard } from '@/api/profile'
import InviteFloat from '@/components/InviteFloat.vue'

const authStore = useAuthStore()
const profile = ref<any>(null)
const dashboardStats = ref<any>({})
const earningsSummary = ref<any>({})
const realNameAuth = ref<any>({})

const tabBarPageUrls = [
  '/pages/index/index',
  '/pages/jobs/jobList',
  '/pages/message/message',
  '/pages/profile/profile'
]

const existingPageUrls = [
  ...tabBarPageUrls,
  '/pages/schedule/schedule',
  '/pages/signup/signup',
  '/pages/attendance/clockIn',
  '/pages/earnings/earnings',
  '/pages/auth/realName',
  '/pages/settings/settings',
  '/pages/referral/referral',
  '/pages/referral/referralRecords'
]

const monthIncome = computed(() => numberValue(dashboardStats.value?.monthIncome))
const monthHours = computed(() => numberValue(dashboardStats.value?.monthHours))
const attendanceDays = computed(() => numberValue(dashboardStats.value?.attendanceDays))
const totalEarned = computed(() => numberValue(earningsSummary.value?.totalEarned))
const pendingAmount = computed(() => numberValue(earningsSummary.value?.pendingWithdrawal))
const settledAmount = computed(() => Math.max(0, totalEarned.value - pendingAmount.value))
const stats = computed(() => [
  { value: moneyText(totalEarned.value), label: '累计收入' },
  { value: decimalText(monthHours.value), label: '本月工时' },
  { value: integerText(attendanceDays.value), label: '出勤天数' }
])

const menuGroups = [
  [
    { title: '我的排班', url: '/pages/schedule/schedule', icon: '排', iconClass: 'icon-green' },
    { title: '我的报名', url: '/pages/signup/signup', icon: '报', iconClass: 'icon-blue' },
    { title: '打卡记录', url: '/pages/attendance/clockIn', icon: '卡', iconClass: 'icon-orange' },
    { title: '收入明细', url: '/pages/earnings/earnings', icon: '收', iconClass: 'icon-gold' }
  ],
  [
    { title: '邀请好友', url: '/pages/referral/referral', icon: '邀', iconClass: 'icon-purple' },
    { title: '设置', url: '/pages/settings/settings', icon: '设', iconClass: 'icon-gray' }
  ]
]

const displayName = computed(() => {
  return profile.value?.name || profile.value?.realName || profile.value?.nickname || '未设置姓名'
})

const displayPhone = computed(() => {
  return profile.value?.phone || profile.value?.mobile || '未绑定手机号'
})

const realNameStatus = computed(() => {
  const status = String(realNameAuth.value?.status || profile.value?.realNameStatus || profile.value?.authStatus || '').toUpperCase()
  if (profile.value?.isRealName || status === 'APPROVED' || status === 'VERIFIED') return '已实名'
  if (status === 'PENDING') return '审核中'
  if (status === 'REJECTED') return '未通过'
  return '未实名'
})

function numberValue(value: any) {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

function moneyText(value: any) {
  return numberValue(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function decimalText(value: any) {
  return numberValue(value).toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

function integerText(value: any) {
  return Math.round(numberValue(value)).toLocaleString('zh-CN')
}

function navTo(url: string) {
  if (!existingPageUrls.includes(url)) {
    uni.showToast({ title: '功能建设中', icon: 'none' })
    return
  }
  if (tabBarPageUrls.includes(url)) {
    uni.switchTab({ url })
    return
  }
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
    const res: any = await getProfileDashboard()
    profile.value = res?.profile || null
    dashboardStats.value = res?.stats || {}
    earningsSummary.value = res?.earningsSummary || {}
    realNameAuth.value = res?.realNameAuth || {}
    if (profile.value) authStore.setWorkerInfo(profile.value)
  } catch {
    profile.value = null
    dashboardStats.value = {}
    earningsSummary.value = {}
    realNameAuth.value = {}
    uni.showToast({ title: '我的页面加载失败', icon: 'none' })
  }
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f6f7f8;
  padding: 24rpx 24rpx 40rpx;
  box-sizing: border-box;
}
.profile-header {
  background: linear-gradient(135deg, #19c876 0%, #08a657 100%);
  border-radius: 0 0 36rpx 36rpx;
  margin: -24rpx -24rpx 24rpx;
  padding: 88rpx 48rpx 36rpx;
  color: #fff;
  box-shadow: 0 12rpx 32rpx rgba(7, 193, 96, 0.24);
}
.profile-top {
  display: flex;
  align-items: center;
}
.avatar {
  width: 132rpx;
  height: 132rpx;
  border-radius: 66rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}
.user-info {
  flex: 1;
  min-width: 0;
  margin-left: 26rpx;
}
.name-row {
  display: flex;
  align-items: center;
}
.nickname {
  max-width: 300rpx;
  font-size: 42rpx;
  font-weight: 700;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.auth-badge {
  margin-left: 16rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.22);
  border: 2rpx solid rgba(255, 255, 255, 0.46);
  font-size: 24rpx;
  line-height: 1.3;
}
.phone {
  display: block;
  margin-top: 14rpx;
  font-size: 28rpx;
  opacity: 0.86;
}
.stats-panel {
  display: flex;
  margin-top: 42rpx;
  padding: 26rpx 0;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.16);
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 2rpx solid rgba(255, 255, 255, 0.22);
}
.stat-item:last-child {
  border-right: none;
}
.stat-value {
  font-size: 38rpx;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  margin-top: 10rpx;
  font-size: 24rpx;
  opacity: 0.82;
}
.income-card,
.menu-group {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 28rpx rgba(26, 35, 48, 0.06);
  overflow: hidden;
}
.income-card {
  padding: 32rpx;
  margin-bottom: 24rpx;
}
.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 32rpx;
  color: #1f2933;
  font-weight: 700;
}
.card-link {
  font-size: 26rpx;
  color: #07c160;
}
.income-main {
  display: flex;
  align-items: baseline;
  margin-top: 30rpx;
}
.income-symbol {
  font-size: 32rpx;
  color: #ff8a00;
  font-weight: 700;
}
.income-amount {
  margin-left: 8rpx;
  font-size: 58rpx;
  color: #ff8a00;
  font-weight: 800;
  line-height: 1;
}
.income-sub-row {
  display: flex;
  align-items: center;
  margin-top: 34rpx;
  padding-top: 28rpx;
  border-top: 2rpx solid #f2f4f6;
}
.income-sub-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.income-sub-value {
  font-size: 30rpx;
  color: #1f2933;
  font-weight: 700;
}
.income-sub-label {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8792a2;
}
.income-divider {
  width: 2rpx;
  height: 48rpx;
  background: #eef1f4;
}
.menu-group {
  margin-bottom: 24rpx;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 32rpx;
  border-bottom: 2rpx solid #f2f4f6;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-left {
  display: flex;
  align-items: center;
}
.menu-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 22rpx;
  font-size: 24rpx;
  color: #fff;
  font-weight: 700;
}
.icon-green {
  background: #19c876;
}
.icon-blue {
  background: #3b82f6;
}
.icon-orange {
  background: #ff8a00;
}
.icon-gold {
  background: #f5b400;
}
.icon-purple {
  background: #8b5cf6;
}
.icon-cyan {
  background: #06b6d4;
}
.icon-red {
  background: #f05252;
}
.icon-gray {
  background: #94a3b8;
}
.menu-text {
  font-size: 30rpx;
  color: #1f2933;
  font-weight: 500;
}
.arrow {
  font-size: 44rpx;
  color: #c6ccd4;
  line-height: 1;
}
.logout-area {
  padding: 8rpx 0 0;
}
.logout-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: #fff;
  color: #ef4444;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 28rpx rgba(26, 35, 48, 0.06);
}
.logout-btn::after {
  border: none;
}
</style>
