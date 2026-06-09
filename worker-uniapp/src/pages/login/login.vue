<template>
  <view class="login-page">
    <view class="page-bg page-bg-top"></view>
    <view class="page-bg page-bg-bottom"></view>

    <view class="logo-area">
      <view class="logo-box">
        <image class="logo" src="/static/logo.png" mode="aspectFit" />
      </view>
      <text class="app-name">找零工</text>
      <text class="app-desc">海量零工机会，随时随地赚钱</text>
    </view>

    <view class="login-card">
      <button class="wechat-phone-btn" @click="handleWechatPhoneLogin" :loading="wechatLoading" :disabled="wechatLoading">
        <text class="wechat-phone-text">微信一键登录</text>
      </button>

      <view class="agreement-row">
        <view class="agree-check" :class="{ checked: agreed }" @click="agreed = !agreed">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
        <text class="agree-text">登录即表示同意</text>
        <text class="agree-link" @click.stop="goProtocol('user_agreement')">《用户协议》</text>
        <text class="agree-text">和</text>
        <text class="agree-link" @click.stop="goProtocol('privacy_policy')">《隐私政策》</text>
      </view>

      <view class="other-login-row">
        <view class="entry-line"></view>
        <text class="entry-text">其他登录方式</text>
        <view class="entry-line"></view>
      </view>

      <view class="other-icons">
        <view class="icon-item" @click="goPhoneLogin">
          <view class="icon-circle">
            <text class="icon-phone">📱</text>
          </view>
          <text class="icon-label">手机号</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const wechatLoading = ref(false)
const agreed = ref(false)

const REGISTERED_PAGES = new Set([
  '/pages/index/index',
  '/pages/jobs/jobList',
  '/pages/jobs/jobDetail',
  '/pages/jobs/applyConfirm',
  '/pages/message/message',
  '/pages/profile/profile',
  '/pages/profile/edit',
  '/pages/settings/settings',
  '/pages/auth/realName',
  '/pages/bank/bankCard',
  '/pages/schedule/schedule',
  '/pages/attendance/clockIn',
  '/pages/earnings/earnings',
  '/pages/earnings/withdraw',
  '/pages/login/login'
])

const TAB_PAGES = new Set([
  '/pages/index/index',
  '/pages/jobs/jobList',
  '/pages/message/message',
  '/pages/profile/profile'
])

onLoad(() => {})

function goProtocol(key: string) {
  uni.navigateTo({ url: `/pages/common/protocol?key=${key}` })
}

function goPhoneLogin() {
  uni.navigateTo({ url: '/pages/login/phoneLogin' })
}

async function handleWechatPhoneLogin() {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }
  if (wechatLoading.value) return
  wechatLoading.value = true
  try {
    await authStore.wechatPhoneLogin()
    await authStore.loadWorkerInfo()
    uni.switchTab({ url: '/pages/index/index' })
  } catch {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    wechatLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  padding: 0 44rpx 48rpx;
  background: linear-gradient(180deg, #18c56e 0%, #5fdc9a 42%, #f4fff8 100%);
  overflow: hidden;
  box-sizing: border-box;
}

.page-bg {
  position: absolute;
  border-radius: 999rpx;
  opacity: 0.28;
  background: #ffffff;
}

.page-bg-top {
  top: -180rpx;
  right: -160rpx;
  width: 420rpx;
  height: 420rpx;
}

.page-bg-bottom {
  top: 330rpx;
  left: -220rpx;
  width: 460rpx;
  height: 460rpx;
}

.logo-area {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 160rpx;
  margin-bottom: 60rpx;
}

.logo-box {
  width: 154rpx;
  height: 154rpx;
  border-radius: 40rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 18rpx 44rpx rgba(3, 119, 57, 0.18);
  margin-bottom: 26rpx;
}

.logo {
  width: 116rpx;
  height: 116rpx;
  border-radius: 28rpx;
}

.app-name {
  font-size: 48rpx;
  line-height: 66rpx;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.app-desc {
  font-size: 26rpx;
  line-height: 38rpx;
  color: rgba(255, 255, 255, 0.92);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 48rpx 36rpx 40rpx;
  border-radius: 36rpx;
  background: #ffffff;
  box-shadow: 0 24rpx 60rpx rgba(6, 101, 54, 0.15);
  box-sizing: border-box;
}

.wechat-phone-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  margin: 0 0 24rpx;
  border-radius: 48rpx;
  background: linear-gradient(90deg, #07c160 0%, #10ad62 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 14rpx 28rpx rgba(7, 193, 96, 0.25);
}

.wechat-phone-btn::after {
  border: none;
}

.wechat-phone-btn[disabled] {
  background: #95d6a8;
  color: #ffffff;
  box-shadow: none;
}

.wechat-phone-text {
  color: #ffffff;
}

.agreement-row {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  font-size: 22rpx;
  line-height: 32rpx;
}

.agree-check {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  border: 2rpx solid #c8d6ce;
  margin-right: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agree-check.checked {
  background: #10a75b;
  border-color: #10a75b;
}

.check-icon {
  color: #ffffff;
  font-size: 18rpx;
  line-height: 1;
}

.agree-text {
  color: #7b8b82;
}

.agree-link {
  color: #12a960;
}

.other-login-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.entry-line {
  flex: 1;
  height: 2rpx;
  background: #edf2ef;
}

.entry-text {
  padding: 0 20rpx;
  font-size: 24rpx;
  color: #9aa8a0;
}

.other-icons {
  display: flex;
  justify-content: center;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 40rpx;
}

.icon-circle {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: #f5fbf7;
  border: 2rpx solid #e3f3e9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.icon-phone {
  font-size: 40rpx;
}

.icon-label {
  font-size: 22rpx;
  color: #7d8b84;
}
</style>
