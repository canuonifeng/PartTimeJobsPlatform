<template>
  <view class="register-page">
    <view class="page-bg page-bg-top"></view>
    <view class="page-bg page-bg-bottom"></view>

    <view class="logo-area">
      <view class="logo-box">
        <image class="logo" src="/static/logo.png" mode="aspectFit" />
      </view>
      <text class="app-name">老登e站</text>
      <text class="app-desc">海量零工机会，随时随地赚钱</text>
    </view>

    <view class="register-card">
      <view class="card-title-row">
        <view>
          <text class="card-title">注册账号</text>
          <text class="card-desc">创建账号开始赚钱之旅</text>
        </view>
      </view>

      <view class="input-row">
        <text class="input-label">姓名</text>
        <input class="input-field" v-model="name" placeholder="请输入姓名" placeholder-class="input-placeholder" />
      </view>

      <view class="input-row">
        <text class="input-label">手机号</text>
        <input class="input-field" v-model="phone" type="number" maxlength="11" placeholder="请输入手机号" placeholder-class="input-placeholder" />
      </view>

      <view class="input-row code-row">
        <view class="code-input-wrap">
          <text class="input-label">验证码</text>
          <input class="code-input" v-model="code" type="number" maxlength="6" placeholder="请输入验证码" placeholder-class="input-placeholder" />
        </view>
        <button class="code-btn" :disabled="codeSending || countdown > 0" @click="handleSendCode">
          {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
        </button>
      </view>

      <view class="input-row" v-if="referralCode">
        <text class="input-label">邀请码</text>
        <input class="input-field" v-model="referralCode" placeholder="邀请码已自动填入" placeholder-class="input-placeholder" disabled />
      </view>

      <button class="register-btn" @click="handleRegister" :loading="registerLoading" :disabled="registerLoading">注册</button>

      <view class="login-entry">
        <text class="entry-text">已有账号？</text>
        <text class="entry-link" @click="goLogin">去登录</text>
      </view>
    </view>

    <view class="agreement">
      <text class="agree-text">注册即表示同意</text>
      <text class="agree-link">《用户协议》</text>
      <text class="agree-text">和</text>
      <text class="agree-link">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import request from '@/api/request'

const name = ref('')
const phone = ref('')
const code = ref('')
const referralCode = ref('')
const registerLoading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

onLoad((options) => {
  if (options?.code) {
    referralCode.value = decodeURIComponent(options.code)
  }
})

onUnload(() => {
  clearCountdownTimer()
})

function clearCountdownTimer() {
  if (!timer) return
  clearInterval(timer)
  timer = null
}

async function handleSendCode() {
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  clearCountdownTimer()
  codeSending.value = true
  try {
    await request({
      url: '/auth/send-code',
      method: 'POST',
      data: { phone: phone.value }
    })
    uni.showToast({ title: '验证码已发送（开发环境: 123456）', icon: 'none' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearCountdownTimer()
      }
    }, 1000)
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  if (registerLoading.value) return
  if (!name.value.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  if (!/^1\d{10}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }

  registerLoading.value = true
  try {
    const data = await request({
      url: '/auth/register',
      method: 'POST',
      data: {
        name: name.value,
        phone: phone.value,
        referralCode: referralCode.value || undefined
      }
    })
    uni.setStorageSync('token', data.token)
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (err: any) {
    uni.showToast({ title: err?.message || '注册失败，请重试', icon: 'none' })
  } finally {
    registerLoading.value = false
  }
}

function goLogin() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.switchTab({ url: '/pages/jobs/jobList' })
}
</script>

<style scoped>
.register-page {
  position: relative;
  min-height: 100vh;
  padding: 96rpx 44rpx 48rpx;
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
  margin-bottom: 56rpx;
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

.register-card {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 42rpx 34rpx 36rpx;
  border-radius: 36rpx;
  background: #ffffff;
  box-shadow: 0 24rpx 60rpx rgba(6, 101, 54, 0.15);
  box-sizing: border-box;
}

.card-title-row {
  margin-bottom: 34rpx;
}

.card-title {
  display: block;
  font-size: 36rpx;
  line-height: 50rpx;
  font-weight: 700;
  color: #12251b;
  margin-bottom: 8rpx;
}

.card-desc {
  display: block;
  font-size: 24rpx;
  line-height: 34rpx;
  color: #7d8b84;
}

.input-row {
  height: 104rpx;
  padding: 0 28rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  background: #f5fbf7;
  border: 2rpx solid #e3f3e9;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.input-label {
  width: 108rpx;
  font-size: 28rpx;
  color: #24382d;
  flex-shrink: 0;
}

.input-field {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #12251b;
}

.input-placeholder {
  color: #b6c4bc;
}

.code-row {
  padding-right: 12rpx;
}

.code-input-wrap {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
}

.code-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #12251b;
}

.code-btn {
  width: 176rpx;
  height: 68rpx;
  line-height: 68rpx;
  padding: 0;
  margin: 0;
  border-radius: 34rpx;
  background: #20c56f;
  color: #ffffff;
  font-size: 24rpx;
  border: none;
  flex-shrink: 0;
}

.code-btn::after,
.register-btn::after {
  border: none;
}

.code-btn[disabled],
.register-btn[disabled] {
  background: #c8d6ce;
  color: #ffffff;
  box-shadow: none;
}

.register-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  margin: 36rpx 0 30rpx;
  border-radius: 48rpx;
  background: linear-gradient(90deg, #11bd66 0%, #37d889 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 14rpx 28rpx rgba(18, 197, 108, 0.25);
}

.login-entry {
  display: flex;
  align-items: center;
  justify-content: center;
}

.entry-text {
  font-size: 26rpx;
  color: #7b8b82;
}

.entry-link {
  font-size: 26rpx;
  color: #12a960;
  margin-left: 8rpx;
}

.agreement {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 34rpx;
  font-size: 24rpx;
  line-height: 36rpx;
  color: #7b8b82;
}

.agree-text {
  color: #7b8b82;
}

.agree-link {
  color: #12a960;
}
</style>
