<script setup>
import { ref } from 'vue'
import { updateCurrentPassword } from '@/api/account'

const saving = ref(false)
const form = ref({ newPassword: '', confirmPassword: '' })

async function handleSave() {
  if (!form.value.newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  if (form.value.newPassword.length < 6) {
    uni.showToast({ title: '密码至少 6 位', icon: 'none' })
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await updateCurrentPassword({ newPassword: form.value.newPassword })
    uni.showToast({ title: '密码已修改', icon: 'success' })
    form.value.newPassword = ''
    form.value.confirmPassword = ''
    setTimeout(() => uni.navigateBack(), 500)
  } catch {
    uni.showToast({ title: '修改失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="op-page password-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">修改密码</text>
      <text class="op-hero-title">单独设置当前账号密码</text>
      <text class="op-hero-desc">修改后请使用新密码登录企业端</text>
    </view>
    <view class="op-content">
      <view class="password-card">
        <view class="form-row">
          <text class="form-label">新密码</text>
          <input v-model="form.newPassword" class="form-input" password placeholder="请输入新密码" />
        </view>
        <view class="form-row">
          <text class="form-label">确认密码</text>
          <input v-model="form.confirmPassword" class="form-input" password placeholder="请再次输入新密码" />
        </view>
        <button class="save-btn" :disabled="saving" @click="handleSave">
          <text class="save-btn-text">{{ saving ? '保存中...' : '修改密码' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<style>
.password-page { min-height: 100vh; padding-bottom: 48rpx; }
.top-space { height: 24rpx; }
.password-card { padding: 28rpx; border-radius: 30rpx; background: #fff; box-shadow: 0 12rpx 34rpx rgba(23,83,53,.08); box-sizing: border-box; }
.form-row { margin-bottom: 24rpx; }
.form-label { display: block; margin-bottom: 12rpx; font-size: 25rpx; font-weight: 800; color: #64748b; }
.form-input { width: 100%; height: 88rpx; padding: 0 22rpx; border-radius: 20rpx; background: #f8fafc; border: 2rpx solid #edf0f3; color: #1f2933; font-size: 28rpx; box-sizing: border-box; }
.save-btn { height: 86rpx; line-height: 86rpx; margin-top: 10rpx; border-radius: 999rpx; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; }
.save-btn[disabled] { background: #a7f3d0; color: #fff; }
.save-btn-text { color: #fff; }
</style>
