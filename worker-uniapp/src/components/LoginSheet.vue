<template>
  <view v-if="visible" class="login-mask" @click="handleClose">
    <view class="login-sheet" @click.stop>
      <view class="sheet-handle"></view>
      <view class="sheet-header">
        <view>
          <text class="sheet-title">登录后继续操作</text>
          <text class="sheet-subtitle">报名、查看排班和收入都需要登录</text>
        </view>
        <text class="sheet-close" @click="handleClose">×</text>
      </view>

      <button
        v-if="mode === 'wechat'"
        class="wechat-phone-btn"
        open-type="getPhoneNumber"
        @getphonenumber="handleWechatPhoneLogin"
        :loading="wechatLoading"
        :disabled="wechatLoading"
      >
        微信一键登录
      </button>

      <view v-else class="phone-form">
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
        <button class="submit-btn" :loading="phoneLoading" :disabled="phoneLoading" @click="handlePhoneLogin">登录</button>
      </view>

      <view class="agreement-row">
        <view class="agree-check" :class="{ checked: agreed }" @click="agreed = !agreed">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
        <text class="agree-text">登录即表示同意</text>
        <text class="agree-link" @click.stop="goProtocol('user_agreement')">《用户协议》</text>
        <text class="agree-text">和</text>
        <text class="agree-link" @click.stop="goProtocol('privacy_policy')">《隐私政策》</text>
      </view>

      <view class="mode-switch" @click="toggleMode">
        <text>{{ mode === 'wechat' ? '使用手机号验证码登录' : '使用微信一键登录' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { useAuthStore } from '@/store'
import { closeLoginSheet, notifyLoginSuccess, onLoginSheetChange } from '@/utils/loginSheet'
import { getWechatPhoneAuthFailureMessage, hasWechatPhoneAuth } from '@/utils/wechatPhoneAuth'

const authStore = useAuthStore()
const visible = ref(false)
const mode = ref<'wechat' | 'phone'>('wechat')
const agreed = ref(false)
const wechatLoading = ref(false)
const phoneLoading = ref(false)
const codeSending = ref(false)
const phone = ref('')
const code = ref('')
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const unsubscribe = onLoginSheetChange((payload) => {
  visible.value = payload.visible
  if (payload.reset) resetForm()
  if (payload.visible) mode.value = 'wechat'
})

onBeforeUnmount(() => {
  unsubscribe()
  clearCountdownTimer()
})

function handleClose() {
  closeLoginSheet()
}

function resetForm() {
  clearCountdownTimer()
  mode.value = 'wechat'
  agreed.value = false
  wechatLoading.value = false
  phoneLoading.value = false
  codeSending.value = false
  phone.value = ''
  code.value = ''
  countdown.value = 0
}

function toggleMode() {
  mode.value = mode.value === 'wechat' ? 'phone' : 'wechat'
}

function goProtocol(key: string) {
  uni.navigateTo({ url: `/pages/common/protocol?key=${key}` })
}

function ensureAgreed() {
  if (agreed.value) return true
  uni.showToast({ title: '请先同意用户协议', icon: 'none' })
  return false
}

async function afterLogin() {
  await authStore.loadWorkerInfo()
  notifyLoginSuccess()
}

async function handleWechatPhoneLogin(event) {
  if (!ensureAgreed()) return
  const phoneCode = event?.detail?.code
  const encryptedData = event?.detail?.encryptedData
  const iv = event?.detail?.iv
  if (!hasWechatPhoneAuth(event)) {
    uni.showToast({ title: getWechatPhoneAuthFailureMessage(event), icon: 'none' })
    return
  }
  if (wechatLoading.value) return
  wechatLoading.value = true
  try {
    await authStore.wechatPhoneLogin({ phoneCode, encryptedData, iv })
    await afterLogin()
  } catch {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    wechatLoading.value = false
  }
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
      if (countdown.value <= 0) clearCountdownTimer()
    }, 1000)
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    codeSending.value = false
  }
}

async function handlePhoneLogin() {
  if (phoneLoading.value || !ensureAgreed()) return
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
    await afterLogin()
  } catch {
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    phoneLoading.value = false
  }
}
</script>

<style scoped>
.login-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: flex-end;
  background: rgba(10, 20, 15, 0.46);
}

.login-sheet {
  width: 100%;
  padding: 18rpx 36rpx calc(40rpx + env(safe-area-inset-bottom));
  border-radius: 36rpx 36rpx 0 0;
  background: #ffffff;
  box-shadow: 0 -18rpx 50rpx rgba(0, 0, 0, 0.12);
  box-sizing: border-box;
}

.sheet-handle {
  width: 72rpx;
  height: 8rpx;
  margin: 0 auto 28rpx;
  border-radius: 99rpx;
  background: #d7e2dc;
}

.sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 34rpx;
}

.sheet-title {
  display: block;
  font-size: 38rpx;
  line-height: 52rpx;
  font-weight: 700;
  color: #12251b;
}

.sheet-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  line-height: 38rpx;
  color: #6b7c72;
}

.sheet-close {
  width: 56rpx;
  height: 56rpx;
  text-align: center;
  line-height: 52rpx;
  font-size: 42rpx;
  color: #9aa8a0;
}

.wechat-phone-btn,
.submit-btn {
  width: 100%;
  height: 92rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #15c46f 0%, #08a85a 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 92rpx;
}

.wechat-phone-btn::after,
.submit-btn::after,
.code-btn::after {
  border: 0;
}

.phone-form {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.input-row {
  height: 96rpx;
  padding: 0 26rpx;
  border-radius: 24rpx;
  background: #f5fbf7;
  border: 2rpx solid #e3f3e9;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.input-label {
  width: 104rpx;
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
  height: 66rpx;
  border-radius: 999rpx;
  background: #e9fff2;
  color: #10b765;
  font-size: 24rpx;
  line-height: 66rpx;
  padding: 0;
}

.agreement-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 26rpx;
  font-size: 24rpx;
  line-height: 36rpx;
}

.agree-check {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  border: 2rpx solid #b7c8bf;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.agree-check.checked {
  border-color: #14bd69;
  background: #14bd69;
}

.check-icon {
  color: #ffffff;
  font-size: 20rpx;
}

.agree-text {
  color: #7c8a82;
}

.agree-link {
  color: #0aa85a;
}

.mode-switch {
  margin-top: 30rpx;
  text-align: center;
  color: #0aa85a;
  font-size: 28rpx;
}
</style>
