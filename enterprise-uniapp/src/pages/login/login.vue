<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { login } from '@/api/auth'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

onShow(() => {
  const token = uni.getStorageSync('token')
  if (token) {
    uni.reLaunch({ url: '/pages/home/index' })
  }
})

async function handleLogin() {
  if (!username.value || !password.value) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await login(username.value, password.value)
    authStore.setToken(res.token || res.accessToken)
    authStore.setUser(res.user)
    authStore.setDisplayName(username.value)
    if (username.value.includes('@')) {
      authStore.setEmailSuffix(username.value.substring(username.value.indexOf('@') + 1))
    }
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (e) {
    uni.showToast({ title: '登录失败，请检查账号密码', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="bg-gradient">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>
    <view class="content">
      <view class="logo-section">
        <view class="logo-box">
          <image class="logo-img" src="/static/logo.png" mode="aspectFit"></image>
        </view>
        <text class="app-name">找零工</text>
        <text class="subtitle">企业端管理</text>
      </view>
      <view class="form-card">
        <view class="form-title-wrap">
          <text class="form-title">账号登录</text>
          <text class="form-desc">请输入您的账号和密码</text>
        </view>
        <view class="input-group">
          <text class="input-label">用户名</text>
          <view class="input-wrap">
            <input
              v-model="username"
              class="input"
              placeholder="请输入用户名"
              placeholder-class="placeholder"
              @confirm="handleLogin"
            />
          </view>
        </view>
        <view class="input-group">
          <text class="input-label">密码</text>
          <view class="input-wrap">
            <input
              v-model="password"
              class="input"
              type="text"
              password
              placeholder="请输入密码"
              placeholder-class="placeholder"
              @confirm="handleLogin"
            />
          </view>
        </view>
        <button class="login-btn" :disabled="loading" @click="handleLogin">
          <text class="login-btn-text">{{ loading ? '登录中...' : '登 录' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<style>
page {
  background: transparent;
}
.page {
  min-height: 100vh;
  background: #f6f8f7;
  position: relative;
}
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 680rpx;
  background: linear-gradient(180deg, #18c56e 0%, #5fdc9a 42%, #f4fff8 100%);
  overflow: hidden;
}
.circle {
  position: absolute;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
}
.circle-1 {
  width: 320rpx;
  height: 320rpx;
  top: -60rpx;
  right: -80rpx;
}
.circle-2 {
  width: 220rpx;
  height: 220rpx;
  top: 180rpx;
  left: -40rpx;
}
.circle-3 {
  width: 160rpx;
  height: 160rpx;
  top: 80rpx;
  right: 120rpx;
}
.content {
  position: relative;
  z-index: 1;
  padding: 0 40rpx;
  padding-top: 160rpx;
}
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}
.logo-box {
  width: 154rpx;
  height: 154rpx;
  border-radius: 40rpx;
  background: #fff;
  box-shadow: 0 16rpx 48rpx rgba(23, 83, 53, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}
.logo-img {
  width: 100rpx;
  height: 100rpx;
}
.app-name {
  font-size: 48rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10rpx;
}
.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.92);
}
.form-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 16rpx 48rpx rgba(23, 83, 53, 0.12);
  padding: 48rpx 36rpx;
  box-sizing: border-box;
}
.form-title-wrap {
  margin-bottom: 40rpx;
}
.form-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a2e1e;
  margin-bottom: 8rpx;
}
.form-desc {
  font-size: 26rpx;
  color: #9ca3af;
}
.input-group {
  margin-bottom: 28rpx;
}
.input-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12rpx;
}
.input-wrap {
  background: #f8faf9;
  border: 2rpx solid #edf0f3;
  border-radius: 18rpx;
  min-height: 84rpx;
  display: flex;
  align-items: center;
  padding: 0 22rpx;
  box-sizing: border-box;
}
.input {
  width: 100%;
  min-height: 84rpx;
  font-size: 28rpx;
  color: #1f2933;
}
.placeholder {
  color: #9ca3af;
  font-size: 28rpx;
}
.login-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #18c86b, #08a95a);
  border-radius: 48rpx;
  border: none;
  margin-top: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 28rpx rgba(8, 169, 90, 0.35);
}
.login-btn::after {
  border: none;
}
.login-btn[disabled] {
  opacity: 0.6;
}
.login-btn-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}
</style>
