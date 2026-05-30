<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/store'
import { request } from '@/api/request'

const authStore = useAuthStore()
const companyName = ref('')

onMounted(async () => {
  try {
    const res = await request('GET', '/enterprise')
    companyName.value = res?.companyName || ''
  } catch {}
})

const sections = [
  {
    name: '招聘管理',
    items: [
      { name: '发布职位', icon: '📋', path: '/pages/jobs/jobList' },
      { name: '报名管理', icon: '👥', path: '/pages/applications/applicationList' },
      { name: '排班考勤', icon: '🗓', path: '/pages/schedules/scheduleList' },
      { name: '薪资管理', icon: '💵', path: '/pages/attendance/attendanceList' }
    ]
  },
  {
    name: '基础管理',
    items: [
      { name: '工作地点', icon: '📍', path: '/pages/locations/locationList' },
      { name: '职位模版', icon: '📄', path: '/pages/templates/templateList' },
      { name: '账户余额', icon: '🏦', path: '/pages/balance/balanceList' }
    ]
  }
]

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) authStore.logout()
    }
  })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <view class="header-info">
        <text class="company-name">{{ companyName || '企业名称' }}</text>
        <text class="user-name">{{ authStore.displayName }}</text>
      </view>
      <text class="logout-btn" @click="handleLogout">退出</text>
    </view>
    <view class="sections">
      <view v-for="(sec, si) in sections" :key="si" class="section">
        <text class="section-title">{{ sec.name }}</text>
        <view class="grid">
          <view v-for="mod in sec.items" :key="mod.path" class="grid-item" @click="navigateTo(mod.path)">
            <text class="grid-icon">{{ mod.icon }}</text>
            <text class="grid-name">{{ mod.name }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
}
.header-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.company-name {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}
.user-name {
  font-size: 26rpx;
  color: #999;
}
.logout-btn {
  font-size: 26rpx;
  color: #007aff;
  padding: 8rpx 16rpx;
}
.sections {
  padding: 16rpx 0;
}
.section {
  margin-bottom: 16rpx;
}
.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #666;
  padding: 16rpx 32rpx 8rpx;
}
.grid {
  display: flex;
  flex-wrap: wrap;
  padding: 8rpx 16rpx;
  gap: 16rpx;
}
.grid-item {
  width: calc(33.33% - 16rpx);
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}
.grid-icon {
  font-size: 52rpx;
  margin-bottom: 12rpx;
}
.grid-name {
  font-size: 26rpx;
  color: #333;
}
</style>
