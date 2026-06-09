<template>
  <view class="page">
    <view class="header-card">
      <text class="header-title">更换手机号</text>
      <text class="header-sub">当前绑定：{{ maskedPhone }}</text>
    </view>

    <view class="form-card">
      <view class="input-row">
        <text class="input-label">新手机号</text>
        <input class="input" v-model="newPhone" type="number" maxlength="11" placeholder="请输入新手机号" placeholder-class="placeholder" />
      </view>
      <view class="input-row code-row">
        <view class="code-input-wrap">
          <text class="input-label">验证码</text>
          <input class="input" v-model="code" type="number" maxlength="6" placeholder="请输入验证码" placeholder-class="placeholder" />
        </view>
        <button class="code-btn" :disabled="countdown > 0" @click="handleSendCode">
          {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
        </button>
      </view>
    </view>

    <button class="submit-btn" :disabled="submitting || !newPhone || !code" @click="handleSubmit">确认更换</button>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store'
import { getProfile, updateProfile } from '@/api/profile'
import { sendSmsCode } from '@/api/auth'

const authStore = useAuthStore()
const currentPhone = ref('')
const newPhone = ref('')
const code = ref('')
const submitting = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const maskedPhone = computed(() => {
  const p = currentPhone.value
  if (!p || p.length < 7) return p || '未绑定'
  return p.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

onMounted(async () => {
  try {
    const res: any = await getProfile()
    currentPhone.value = res?.phone || res?.mobile || ''
  } catch {}
})

function clearTimer() {
  if (timer) { clearInterval(timer); timer = null }
}

async function handleSendCode() {
  if (!/^1\d{10}$/.test(newPhone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  clearTimer()
  try {
    await sendSmsCode(newPhone.value)
    uni.showToast({ title: '验证码已发送', icon: 'none' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearTimer()
    }, 1000)
  } catch {
    uni.showToast({ title: '发送失败', icon: 'none' })
  }
}

async function handleSubmit() {
  if (submitting.value) return
  if (!/^1\d{10}$/.test(newPhone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await updateProfile({ phone: newPhone.value })
    currentPhone.value = newPhone.value
    newPhone.value = ''
    code.value = ''
    uni.showToast({ title: '更换成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '更换失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 30rpx;
  box-sizing: border-box;
}
.header-card {
  background: linear-gradient(135deg, #19c876 0%, #08a657 100%);
  margin: -30rpx -30rpx 30rpx;
  padding: 58rpx 40rpx 46rpx;
  color: #fff;
  border-bottom-left-radius: 34rpx;
  border-bottom-right-radius: 34rpx;
  box-shadow: 0 10rpx 30rpx rgba(7, 193, 96, 0.22);
}
.header-title {
  display: block;
  font-size: 46rpx;
  line-height: 1.2;
  font-weight: 800;
}
.header-sub {
  display: block;
  margin-top: 14rpx;
  font-size: 27rpx;
  opacity: 0.86;
}
.form-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 28rpx rgba(26, 35, 48, 0.06);
}
.input-row {
  height: 104rpx;
  padding: 0 28rpx;
  border-bottom: 2rpx solid #f2f4f6;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}
.input-row:last-child {
  border-bottom: none;
}
.input-label {
  width: 140rpx;
  font-size: 28rpx;
  color: #24382d;
  flex-shrink: 0;
}
.input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #12251b;
}
.placeholder {
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
  color: #fff;
  font-size: 24rpx;
  border: none;
  flex-shrink: 0;
}
.code-btn::after {
  border: none;
}
.code-btn[disabled] {
  background: #c8d6ce;
  color: #fff;
}
.submit-btn {
  margin-top: 40rpx;
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 48rpx;
  background: linear-gradient(90deg, #11bd66 0%, #37d889 100%);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 14rpx 28rpx rgba(18, 197, 108, 0.25);
}
.submit-btn::after {
  border: none;
}
.submit-btn[disabled] {
  background: #c8d6ce;
  box-shadow: none;
}
</style>
