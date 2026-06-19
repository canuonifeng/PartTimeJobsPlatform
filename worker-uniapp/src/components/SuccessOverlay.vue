<template>
  <view v-if="visible" class="success-overlay">
    <view class="success-content">
      <view class="success-icon-wrap">
        <view class="success-icon">
          <text class="icon-text">{{ icon }}</text>
        </view>
      </view>
      <text class="success-title">{{ title }}</text>
      <text class="success-desc">{{ description }}</text>
      <view class="success-actions">
        <button class="btn-primary" @click="handlePrimary">{{ primaryText }}</button>
        <button v-if="secondaryText" class="btn-secondary" @click="handleSecondary">{{ secondaryText }}</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  title: string
  description: string
  primaryText: string
  secondaryText?: string
  icon?: string
}>()

const emit = defineEmits<{
  primary: []
  secondary: []
}>()

function handlePrimary() {
  emit('primary')
}

function handleSecondary() {
  emit('secondary')
}
</script>

<style scoped>
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.98);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 60rpx;
  width: 100%;
}

.success-icon-wrap {
  margin-bottom: 48rpx;
}

.success-icon {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #2ad879 0%, #20c26b 40%, #1aab5a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 24rpx 64rpx rgba(32, 194, 107, 0.5),
              0 8rpx 20rpx rgba(0, 0, 0, 0.1),
              inset 0 -8rpx 20rpx rgba(0, 0, 0, 0.1),
              inset 0 6rpx 16rpx rgba(255, 255, 255, 0.3);
  animation: bounceIn 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.success-icon::after {
  content: '';
  position: absolute;
  top: 16rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 80rpx;
  height: 10rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  filter: blur(2rpx);
}

@keyframes bounceIn {
  0% {
    transform: scale(0);
  }
  60% {
    transform: scale(1.15);
  }
  100% {
    transform: scale(1);
  }
}

.icon-text {
  font-size: 80rpx;
}

.success-title {
  font-size: 44rpx;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 20rpx;
  text-align: center;
}

.success-desc {
  font-size: 28rpx;
  color: #888;
  text-align: center;
  line-height: 1.6;
  margin-bottom: 80rpx;
  padding: 0 40rpx;
}

.success-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  align-items: center;
}

.btn-primary {
  width: 100%;
  max-width: 480rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 10rpx 32rpx rgba(16, 185, 129, 0.4);
  margin: 0;
}

.btn-primary::after {
  border: none;
}

.btn-secondary {
  width: 100%;
  max-width: 480rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background: #f3f4f6;
  color: #666;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  margin: 0;
}

.btn-secondary::after {
  border: none;
}
</style>
