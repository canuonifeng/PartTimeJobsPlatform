<template>
  <view class="phone-login-page">
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
      <view class="input-row">
        <text class="input-label">手机号</text>
        <input class="phone-input" v-model="phone" type="number" maxlength="11" placeholder="请输入手机号" placeholder-class="input-placeholder" />
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

      <button class="submit-btn" @click="handlePhoneLogin" :loading="phoneLoading" :disabled="phoneLoading">登录</button>

      <view class="agreement-row">
        <view class="agree-check" :class="{ checked: agreed }" @click="agreed = !agreed">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
        <text class="agree-text">登录即表示同意</text>
        <text class="agree-link" @click.stop="goProtocol('user_agreement')">《用户协议》</text>
        <text class="agree-text">和</text>
        <text class="agree-link" @click.stop="goProtocol('privacy_policy')">《隐私政策》</text>
      </view>

      <view class="switch-entry" @click="goWechatLogin">
        <text class="switch-text">切换到微信登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { goAfterLogin, normalizeLoginRedirect } from '@/utils/loginRedirect'

const authStore = useAuthStore()
const phoneLoading = ref(false)
const phone = ref('')
const code = ref('')
const agreed = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const redirectUrl = ref('')
let timer: ReturnType<typeof setInterval> | null = null

onLoad((params) => {
  redirectUrl.value = normalizeLoginRedirect(params?.redirect)
})

onUnload(() => {
  clearCountdownTimer()
})

function goProtocol(key: string) {
  uni.navigateTo({ url: `/pages/common/protocol?key=${key}` })
}

function goWechatLogin() {
  uni.navigateBack()
}

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
    await authStore.sendSmsCode(phone.value)
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

async function handlePhoneLogin() {
  if (phoneLoading.value) return
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
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
  phoneLoading.value = true
  try {
    await authStore.phoneLogin(phone.value, code.value)
    await authStore.loadWorkerInfo()
    goAfterLogin(redirectUrl.value)
  } catch {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    phoneLoading.value = false
  }
}
</script>

<style scoped>
.phone-login-page {
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

.phone-input,
.code-input {
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
.submit-btn::after {
  border: none;
}

.code-btn[disabled],
.submit-btn[disabled] {
  background: #c8d6ce;
  color: #ffffff;
  box-shadow: none;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  margin: 12rpx 0 0;
  border-radius: 48rpx;
  background: linear-gradient(90deg, #11bd66 0%, #37d889 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 14rpx 28rpx rgba(18, 197, 108, 0.25);
}

.agreement-row {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 24rpx;
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

.switch-entry {
  text-align: center;
  margin-top: 32rpx;
  padding: 16rpx 0;
}

.switch-text {
  font-size: 26rpx;
  color: #10a75b;
}
</style>
