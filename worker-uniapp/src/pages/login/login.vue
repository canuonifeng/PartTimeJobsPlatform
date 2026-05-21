<template>
  <view class="login-page">
    <view class="logo-area">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">找零工</text>
      <text class="app-desc">海量零工机会，随时随地赚钱</text>
    </view>

    <view class="tab-bar">
      <text :class="['tab', { active: tab === 'wechat' }]" @click="tab = 'wechat'">微信登录</text>
      <text :class="['tab', { active: tab === 'phone' }]" @click="tab = 'phone'">手机号登录</text>
    </view>

    <view v-if="tab === 'wechat'" class="login-btn-wrapper">
      <button class="wechat-btn" type="primary" open-type="getUserInfo" @click="handleWechatLogin" :loading="wechatLoading">
        微信登录
      </button>
    </view>

    <view v-else class="phone-form">
      <view class="input-row">
        <input class="phone-input" v-model="phone" type="text" maxlength="11" placeholder="请输入手机号" />
      </view>
      <view class="input-row code-row">
        <input class="code-input" v-model="code" type="text" maxlength="6" placeholder="请输入验证码" />
        <button class="code-btn" :disabled="codeSending || countdown > 0" @click="handleSendCode">
          {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
        </button>
      </view>
      <button class="phone-btn" @click="handlePhoneLogin" :loading="phoneLoading">登录</button>
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
const wechatLoading = ref(false)
const phoneLoading = ref(false)
const redirect = ref('')
const tab = ref('wechat')
const phone = ref('')
const code = ref('')
const codeSending = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

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

async function handleWechatLogin() {
  if (wechatLoading.value) return
  wechatLoading.value = true
  try {
    await authStore.wechatLogin()
    await authStore.loadWorkerInfo()
    goAfterLogin()
  } catch (err) {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    wechatLoading.value = false
  }
}

async function handleSendCode() {
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  codeSending.value = true
  try {
    await authStore.sendSmsCode(phone.value)
    uni.showToast({ title: '验证码已发送（开发环境: 123456）', icon: 'none' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    codeSending.value = false
  }
}

async function handlePhoneLogin() {
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  phoneLoading.value = true
  try {
    await authStore.phoneLogin(phone.value, code.value)
    await authStore.loadWorkerInfo()
    goAfterLogin()
  } catch (err) {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    phoneLoading.value = false
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

.tab-bar {
  display: flex;
  justify-content: center;
  gap: 60rpx;
  margin-bottom: 60rpx;
}

.tab {
  font-size: 30rpx;
  color: #999;
  padding-bottom: 8rpx;
  border-bottom: 4rpx solid transparent;
}

.tab.active {
  color: #07c160;
  border-bottom-color: #07c160;
  font-weight: 600;
}

.phone-form {
  width: 100%;
  margin-bottom: 40rpx;
}

.input-row {
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
}

.phone-input, .code-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
}

.code-row {
  padding-right: 0;
}

.code-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  background: #07c160;
  color: #fff;
  border-radius: 32rpx;
  font-size: 24rpx;
  border: none;
  white-space: nowrap;
  flex-shrink: 0;
}

.code-btn[disabled] {
  background: #ccc;
}

.phone-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: #07c160;
  border-radius: 48rpx;
  font-size: 32rpx;
  color: #fff;
  border: none;
  text-align: center;
  margin-top: 16rpx;
}

.phone-btn::after {
  border: none;
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
