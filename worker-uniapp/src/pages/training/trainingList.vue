<template>
  <view class="training-page">
    <view class="header-banner">
      <view class="banner-title">培训中心</view>
      <view class="banner-sub">完成培训并通过考试，获取技能认证后可抢标注任务</view>
      <view class="banner-link" @click="goMyCerts">我的技能认证 ›</view>
    </view>

    <uni-load-more v-if="loading" status="loading" />

    <view v-if="!loading && courses.length === 0" class="empty-state">
      <text class="empty-text">暂无培训课程</text>
    </view>

    <view v-else class="course-list">
      <view v-for="course in courses" :key="course.id" class="course-card" @click="goDetail(course)">
        <view class="course-top">
          <view class="course-title-wrap">
            <text class="course-title">{{ course.title }}</text>
          </view>
          <text class="course-status" :class="statusClass(course)">{{ statusText(course) }}</text>
        </view>
        <text v-if="course.summary" class="course-summary">{{ course.summary }}</text>
        <view class="course-bottom">
          <view class="course-cert" v-if="course.certificationName">
            <text class="cert-dot"></text>
            <text class="cert-text">认证：{{ course.certificationName }}</text>
          </view>
          <text class="course-pass">及格 {{ course.passScore }}分</text>
        </view>
        <view class="course-action" :class="{ done: course.certified }">
          {{ course.certified ? '已认证' : (course.myStatus === 'COMPLETED' ? '重新学习' : (course.myStatus === 'FAILED' ? '重新考试' : '去学习')) }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTrainingCourses } from '@/api/training'

const courses = ref<any[]>([])
const loading = ref(true)

function statusText(course: any) {
  if (course.certified) return '已认证'
  if (course.myStatus === 'COMPLETED') return '已完成'
  if (course.myStatus === 'FAILED') return '未通过'
  if (course.myStatus === 'IN_PROGRESS') return '学习中'
  return '未开始'
}

function statusClass(course: any) {
  if (course.certified) return 's-certified'
  if (course.myStatus === 'FAILED') return 's-failed'
  if (course.myStatus === 'IN_PROGRESS' || course.myStatus === 'COMPLETED') return 's-progress'
  return 's-new'
}

function goDetail(course: any) {
  uni.navigateTo({ url: `/pages/training/trainingDetail?id=${course.id}` })
}

function goMyCerts() {
  uni.navigateTo({ url: '/pages/training/myCertifications' })
}

async function load() {
  loading.value = true
  try {
    const res: any = await getTrainingCourses()
    courses.value = Array.isArray(res) ? res : []
  } catch {
    courses.value = []
    uni.showToast({ title: '培训课程加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onShow(load)
</script>

<style scoped>
.training-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.header-banner {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  padding: 50rpx 32rpx 60rpx;
  color: #fff;
  position: relative;
}

.banner-title {
  font-size: 44rpx;
  font-weight: 800;
  margin-bottom: 12rpx;
}

.banner-sub {
  font-size: 26rpx;
  opacity: 0.92;
  line-height: 1.6;
}

.banner-link {
  position: absolute;
  right: 32rpx;
  bottom: 30rpx;
  font-size: 26rpx;
  opacity: 0.95;
  padding: 8rpx 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 28rpx;
}

.course-list {
  padding: 24rpx;
  margin-top: -24rpx;
}

.course-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  position: relative;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.course-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.course-title-wrap {
  flex: 1;
  margin-right: 16rpx;
}

.course-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.course-status {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.s-certified {
  color: #059669;
  background: #d1fae5;
}

.s-progress {
  color: #2563eb;
  background: #dbeafe;
}

.s-failed {
  color: #dc2626;
  background: #fee2e2;
}

.s-new {
  color: #6b7280;
  background: #f3f4f6;
}

.course-summary {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.6;
  display: block;
  margin-bottom: 16rpx;
}

.course-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.course-cert {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.cert-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #10b981;
}

.cert-text {
  font-size: 24rpx;
  color: #374151;
}

.course-pass {
  font-size: 24rpx;
  color: #9ca3af;
}

.course-action {
  text-align: center;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}

.course-action.done {
  background: #e5e7eb;
  color: #6b7280;
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
