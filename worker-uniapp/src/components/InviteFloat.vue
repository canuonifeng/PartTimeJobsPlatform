<template>
  <view class="invite-float" @click="goInvite">
    <text class="invite-icon"> invite </text>
    <text class="invite-text">邀请好友</text>
  </view>
</template>

<script setup lang="ts">
function goInvite() {
  uni.navigateTo({ url: '/pages/referral/referral' })
}
</script>

<style scoped>
.invite-float {
  position: fixed;
  right: 30rpx;
  bottom: 200rpx;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  box-shadow: 0 8rpx 24rpx rgba(109, 40, 217, 0.35);
}

.invite-icon {
  font-size: 20rpx;
  color: #fff;
  margin-bottom: 2rpx;
}

.invite-text {
  font-size: 18rpx;
  color: #fff;
  font-weight: 600;
}
</style>
