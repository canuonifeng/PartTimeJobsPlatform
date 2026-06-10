<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store'
import { request } from '@/api/request'

const authStore = useAuthStore()
const companyName = ref('')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

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
      { name: '职位管理', bg: '#07c160', text: '职', path: '/pages/jobs/jobList' },
      { name: '报名管理', bg: '#3b82f6', text: '报', path: '/pages/applications/applicationList' },
      { name: '排班考勤', bg: '#ff9500', text: '班', path: '/pages/schedules/scheduleList' },
      { name: '薪资管理', bg: '#f59e0b', text: '薪', path: '/pages/attendance/attendanceList' }
    ]
  },
  {
    name: '基础管理',
    items: [
      { name: '工作地点', bg: '#06b6d4', text: '地', path: '/pages/locations/locationList' },
      { name: '职位模版', bg: '#8b5cf6', text: '模', path: '/pages/templates/templateList' },
      { name: '兼职管理', bg: '#ec4899', text: '人', path: '/pages/workers/workerList' },
      { name: '账号管理', bg: '#6366f1', text: '号', path: '/pages/accounts/accountList' },
      { name: '企业设置', bg: '#64748b', text: '设', path: '/pages/settings/companySettings' },
      { name: '账户余额', bg: '#f59e0b', text: '余', path: '/pages/balance/balanceList' }
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
  <scroll-view scroll-y class="page">
    <view class="header">
      <view class="header-top">
        <view class="header-info">
          <text class="greeting">{{ greeting }}，{{ authStore.displayName }}</text>
          <text class="company-name">{{ companyName || '企业名称' }}</text>
        </view>
        <view class="logout-btn" @click="handleLogout">
          <text class="logout-text">退出</text>
        </view>
      </view>
    </view>
    <view class="content">
      <view v-for="(sec, si) in sections" :key="si" class="section">
        <view class="section-header">
          <view class="section-dot"></view>
          <text class="section-title">{{ sec.name }}</text>
        </view>
        <view class="card">
          <view class="grid">
            <view v-for="mod in sec.items" :key="mod.path" class="grid-item" @click="navigateTo(mod.path)">
              <view class="icon-box" :style="{ background: mod.bg }">
                <text class="icon-text">{{ mod.text }}</text>
              </view>
              <text class="grid-name">{{ mod.name }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f6f8f7;
}
.header {
  background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%);
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
  padding: 56rpx 32rpx 140rpx;
  position: relative;
  z-index: 1;
}
.header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.header-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.greeting {
  font-size: 44rpx;
  font-weight: 800;
  color: #fff;
}
.company-name {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
}
.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999rpx;
  padding: 10rpx 24rpx;
}
.logout-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 500;
}
.content {
  padding: 0 28rpx 40rpx;
  margin-top: -60rpx;
  position: relative;
  z-index: 2;
}
.section {
  margin-bottom: 28rpx;
}
.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding-top: 8rpx;
}
.section-dot {
  width: 8rpx;
  height: 28rpx;
  background: #08a95a;
  border-radius: 4rpx;
  margin-right: 12rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a2e1e;
}
.card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
  padding: 30rpx;
}
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx 0;
}
.grid-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.icon-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}
.icon-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}
.grid-name {
  font-size: 24rpx;
  color: #333;
  text-align: center;
}
</style>
