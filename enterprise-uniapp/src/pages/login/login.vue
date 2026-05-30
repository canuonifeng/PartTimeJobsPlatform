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
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (e) {
    uni.showToast({ title: '登录失败，请检查账号密码', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="container">
    <view class="login-box">
      <text class="title">企业职位管理</text>
      <view class="form">
        <input
          v-model="username"
          class="input"
          placeholder="用户名"
          @confirm="handleLogin"
        />
        <input
          v-model="password"
          class="input"
          type="text"
          password
          placeholder="密码"
          @confirm="handleLogin"
        />
        <button class="btn" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </view>
    </view>
  </view>
</template>

<style>
page {
  background-color: #f5f5f5;
}
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 40rpx;
}
.login-box {
  width: 100%;
  max-width: 600rpx;
}
.title {
  display: block;
  text-align: center;
  font-size: 44rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 60rpx;
}
.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
}
.input {
  width: 100%;
  height: 88rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #007aff;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  text-align: center;
  margin-top: 16rpx;
}
.btn[disabled] {
  opacity: 0.6;
}
</style>
