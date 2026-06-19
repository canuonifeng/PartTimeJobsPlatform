<template>
  <view v-if="visible" class="modal-mask" @click="handleMaskClick">
    <view class="modal-sheet" @click.stop>
      <view class="sheet-handle"></view>
      <text class="sheet-title">确认报名</text>
      <text class="sheet-subtitle">{{ jobTitle }}</text>
      <text class="sheet-location" v-if="location">📍 {{ location }}</text>

      <view class="schedule-list">
        <view v-for="item in schedules" :key="item.id" class="schedule-item">
          <view class="sch-check">✓</view>
          <view class="sch-info">
            <text class="sch-date">{{ item.date }}</text>
            <text class="sch-time">{{ item.time }}</text>
          </view>
          <text class="sch-pay">{{ item.pay }}</text>
        </view>
      </view>

      <view class="total-row">
        <text class="total-label">合计：{{ totalText }}</text>
        <text class="total-amount">{{ totalAmount }}</text>
      </view>

      <view class="warning-box">
        <text class="warning-icon">⚠️</text>
        <text class="warning-text">请确保能按时到岗，无故爽约会影响你的信用分和后续报名资格</text>
      </view>

      <view class="action-row">
        <button class="btn-cancel" @click="handleCancel">取消</button>
        <button class="btn-confirm" :class="{ loading: submitting }" :disabled="submitting" @click="handleConfirm">
          {{ submitting ? '提交中...' : '确认报名' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  jobTitle: string
  location?: string
  schedules: Array<{ id: number | string; date: string; time: string; pay: string }>
  totalText: string
  totalAmount: string
  submitting?: boolean
}>()

const emit = defineEmits<{
  close: []
  confirm: []
}>()

function handleMaskClick() {
  emit('close')
}

function handleCancel() {
  emit('close')
}

function handleConfirm() {
  emit('confirm')
}
</script>

<style scoped>
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.modal-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 20rpx 40rpx 60rpx;
  position: relative;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.sheet-handle {
  width: 64rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: #e5e7eb;
  margin: 0 auto 28rpx;
}

.sheet-title {
  display: block;
  text-align: center;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8rpx;
}

.sheet-subtitle {
  display: block;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 6rpx;
}

.sheet-location {
  display: block;
  text-align: center;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 28rpx;
}

.schedule-list {
  background: #f8f9ff;
  border-radius: 16rpx;
  padding: 8rpx 20rpx;
  margin-bottom: 20rpx;
  max-height: 320rpx;
  overflow-y: auto;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eef0f5;
}

.schedule-item:last-child {
  border-bottom: none;
}

.sch-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2ad879, #1aab5a);
  color: #fff;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16rpx;
  box-shadow: 0 4rpx 10rpx rgba(32, 194, 107, 0.3);
}

.sch-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.sch-date {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
}

.sch-time {
  font-size: 24rpx;
  color: #888;
}

.sch-pay {
  font-size: 28rpx;
  font-weight: 700;
  color: #20c26b;
  flex-shrink: 0;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-top: 1rpx solid #f0f0f0;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 20rpx;
}

.total-label {
  font-size: 26rpx;
  color: #666;
}

.total-amount {
  font-size: 36rpx;
  font-weight: 800;
  color: #20c26b;
}

.warning-box {
  display: flex;
  gap: 12rpx;
  padding: 20rpx;
  background: #fffbeb;
  border-radius: 12rpx;
  margin-bottom: 32rpx;
}

.warning-icon {
  font-size: 28rpx;
  flex-shrink: 0;
}

.warning-text {
  flex: 1;
  font-size: 24rpx;
  color: #b45309;
  line-height: 1.5;
}

.action-row {
  display: flex;
  gap: 20rpx;
}

.btn-cancel {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  background: #f3f4f6;
  color: #666;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  margin: 0;
}

.btn-cancel::after {
  border: none;
}

.btn-confirm {
  flex: 2;
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #2ad879 0%, #20c26b 40%, #1aab5a 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 10rpx 28rpx rgba(32, 194, 107, 0.4);
  margin: 0;
}

.btn-confirm:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(32, 194, 107, 0.3);
}

.btn-confirm::after {
  border: none;
}

.btn-confirm.loading {
  opacity: 0.7;
}

.btn-confirm[disabled] {
  opacity: 0.6;
}
</style>
