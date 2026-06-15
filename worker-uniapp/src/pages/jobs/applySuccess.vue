<template>
  <view class="success-page">
    <view class="success-card">
      <view class="success-icon">✓</view>
      <text class="success-title">报名成功</text>
      <text class="success-desc">报名信息已提交给招聘方，请保持手机畅通，等待企业审核或联系。</text>
      <button class="primary-btn" @click="backToDetail">返回职位详情</button>
      <button class="secondary-btn" @click="goSchedule">查看我的排班</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const jobId = ref(0)

onLoad((params: any) => {
  const id = Number(params?.jobId)
  jobId.value = Number.isFinite(id) && id > 0 ? id : 0
})

function backToDetail() {
  if (jobId.value > 0) {
    uni.redirectTo({ url: `/pages/jobs/jobDetail?id=${jobId.value}` })
    return
  }
  uni.navigateBack({ delta: 1 })
}

function goSchedule() {
  uni.navigateTo({ url: '/pages/schedule/schedule' })
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  padding: 120rpx 32rpx 48rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #ecfdf5 0%, #f8fafc 42%, #ffffff 100%);
}

.success-card {
  padding: 72rpx 40rpx 48rpx;
  border-radius: 36rpx;
  background: #ffffff;
  box-shadow: 0 18rpx 48rpx rgba(15, 23, 42, 0.08);
  text-align: center;
}

.success-icon {
  width: 132rpx;
  height: 132rpx;
  line-height: 132rpx;
  margin: 0 auto 32rpx;
  border-radius: 66rpx;
  background: linear-gradient(135deg, #10b981, #059669);
  color: #ffffff;
  font-size: 72rpx;
  font-weight: 900;
}

.success-title {
  display: block;
  color: #0f172a;
  font-size: 44rpx;
  line-height: 60rpx;
  font-weight: 800;
}

.success-desc {
  display: block;
  margin: 22rpx 0 56rpx;
  color: #64748b;
  font-size: 28rpx;
  line-height: 44rpx;
}

.primary-btn,
.secondary-btn {
  height: 92rpx;
  line-height: 92rpx;
  margin: 0;
  padding: 0;
  border-radius: 46rpx;
  font-size: 32rpx;
  font-weight: 800;
}

.primary-btn {
  background: #10b981;
  color: #ffffff;
}

.secondary-btn {
  margin-top: 24rpx;
  background: #f1f5f9;
  color: #0f172a;
}

button::after {
  border: none;
}
</style>
