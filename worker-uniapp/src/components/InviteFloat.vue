<template>
  <view
    class="invite-float"
    :style="{ right: pos.x + 'px', bottom: pos.y + 'px' }"
    @touchstart="onTouchStart"
    @touchmove.prevent="onTouchMove"
    @touchend="onTouchEnd"
    @click="goInvite"
  >
    <text class="invite-icon"> invite </text>
    <text class="invite-text">邀请好友</text>
  </view>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

const pos = reactive({ x: 16, y: 30 })
let startX = 0
let startY = 0
let startPosX = 0
let startPosY = 0
let moved = false

function onTouchStart(e: TouchEvent) {
  const touch = e.touches[0]
  startX = touch.clientX
  startY = touch.clientY
  startPosX = pos.x
  startPosY = pos.y
  moved = false
}

function onTouchMove(e: TouchEvent) {
  const touch = e.touches[0]
  const dx = touch.clientX - startX
  const dy = touch.clientY - startY
  if (Math.abs(dx) > 2 || Math.abs(dy) > 2) moved = true
  const sysInfo = uni.getSystemInfoSync()
  const maxX = sysInfo.windowWidth - 100
  const maxY = sysInfo.windowHeight - 100
  pos.x = Math.max(0, Math.min(maxX, startPosX - dx))
  pos.y = Math.max(0, Math.min(maxY, startPosY - dy))
}

function onTouchEnd() {
  if (!moved) return
  const sysInfo = uni.getSystemInfoSync()
  const half = sysInfo.windowWidth / 2
  pos.x = pos.x < half ? 16 : sysInfo.windowWidth - 116
}

function goInvite() {
  if (moved) return
  uni.navigateTo({ url: '/pages/referral/referral' })
}
</script>

<style scoped>
.invite-float {
  position: fixed;
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
