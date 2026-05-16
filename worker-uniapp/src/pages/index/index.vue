<template>
  <view class="home-page">
    <view class="header-banner">
      <view class="greeting">
        <text class="greeting-text">{{ greeting }},</text>
        <text class="user-name">{{ authStore.workerInfo?.name || '工人' }}</text>
      </view>
      <text class="sub-text">今日宜接单</text>
    </view>

    <view class="stats-grid">
      <view class="stat-card" @click="navTo('/pages/jobs/jobList')">
        <text class="stat-num">找活</text>
        <text class="stat-label">浏览最新零工</text>
      </view>
      <view class="stat-card" @click="navTo('/pages/schedule/schedule')">
        <text class="stat-num">排班</text>
        <text class="stat-label">查看我的排班</text>
      </view>
      <view class="stat-card" @click="navTo('/pages/attendance/clockIn')">
        <text class="stat-num">打卡</text>
        <text class="stat-label">今日打卡</text>
      </view>
      <view class="stat-card" @click="navTo('/pages/earnings/earnings')">
        <text class="stat-num">收入</text>
        <text class="stat-label">查看收入</text>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">热门零工</text>
        <text class="section-more" @click="navTo('/pages/jobs/jobList')">查看更多 ›</text>
      </view>
      <view class="hot-jobs">
        <view v-for="job in hotJobs" :key="job.id" class="hot-job-card" @click="navTo(`/pages/jobs/jobDetail?id=${job.id}`)">
          <text class="hot-job-title">{{ job.title }}</text>
          <text class="hot-job-pay">{{ job.minRate }}-{{ job.maxRate }}元/{{ job.rateUnit || '小时' }}</text>
          <text class="hot-job-location">{{ job.location }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/store'
import { getJobs } from '@/api/jobs'

const authStore = useAuthStore()
const hotJobs = ref<any[]>([])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

function navTo(url: string) {
  uni.navigateTo({ url })
}

onMounted(async () => {
  try {
    const res: any = await getJobs({ page: 1, pageSize: 4 })
    hotJobs.value = res.list || []
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.home-page {
  padding: 30rpx;
}

.header-banner {
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #07c160, #06ad56);
  border-radius: 20rpx;
  margin-bottom: 30rpx;
  color: #fff;
}

.greeting {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.greeting-text {
  font-size: 28rpx;
  opacity: 0.9;
  margin-right: 12rpx;
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
}

.sub-text {
  font-size: 26rpx;
  opacity: 0.8;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.stat-num {
  font-size: 32rpx;
  font-weight: 600;
  color: #07c160;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.section {
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 26rpx;
  color: #999;
}

.hot-jobs {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.hot-job-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.hot-job-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
}

.hot-job-pay {
  font-size: 26rpx;
  color: #f60;
  margin-bottom: 8rpx;
}

.hot-job-location {
  font-size: 24rpx;
  color: #999;
}
</style>
