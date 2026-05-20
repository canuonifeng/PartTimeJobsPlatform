<template>
  <view class="login-page">
    <view class="logo-area">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">找零工</text>
      <text class="app-desc">海量零工机会，随时随地赚钱</text>
    </view>

    <view class="login-btn-wrapper">
      <button class="wechat-btn" type="primary" open-type="getUserInfo" @click="handleLogin" :loading="loading">
        <text class="btn-icon">&#xe601;</text>
        微信登录
      </button>
    </view>

    <view class="agreement">
      <text class="agree-text">登录即表示同意</text>
      <text class="agree-link">《用户协议》</text>
      <text class="agree-text">和</text>
      <text class="agree-link">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const loading = ref(false)
const redirect = ref('')

const TAB_PAGES = new Set([
  '/pages/index/index',
  '/pages/jobs/jobList',
  '/pages/message/message',
  '/pages/profile/profile'
])

onLoad((params) => {
  redirect.value = typeof params?.redirect === 'string' ? decodeURIComponent(params.redirect) : ''
})

function goAfterLogin() {
  if (!redirect.value) {
    uni.switchTab({ url: '/pages/index/index' })
    return
  }
  const targetPath = redirect.value.split('?')[0]
  if (TAB_PAGES.has(targetPath)) {
    uni.switchTab({ url: targetPath })
    return
  }
  uni.redirectTo({ url: redirect.value })
}

async function handleLogin() {
  if (loading.value) return
  loading.value = true
  try {
    await authStore.wechatLogin()
    await authStore.loadWorkerInfo()
    goAfterLogin()
  } catch (err) {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 100vh;
  padding: 100rpx 60rpx 60rpx;
  background: linear-gradient(180deg, #ffffff 0%, #f0fdf4 100%);
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 120rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  margin-bottom: 24rpx;
}

.app-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
}

.app-desc {
  font-size: 26rpx;
  color: #999;
}

.login-btn-wrapper {
  width: 100%;
  margin-bottom: 40rpx;
}

.wechat-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: #07c160;
  border-radius: 48rpx;
  font-size: 32rpx;
  color: #fff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wechat-btn::after {
  border: none;
}

.btn-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.agreement {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #999;
}

.agree-link {
  color: #07c160;
}
</style>
