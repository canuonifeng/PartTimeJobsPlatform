<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { getCurrentAccount, updateCurrentAccount } from '@/api/account'

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const form = ref({
  displayName: '',
  phone: ''
})

onShow(loadAccount)

async function loadAccount() {
  loading.value = true
  try {
    const res = await getCurrentAccount()
    form.value.displayName = res?.displayName || authStore.displayName || ''
    form.value.phone = res?.phone || ''
  } catch {
    uni.showToast({ title: '账号信息加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.value.displayName.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  saving.value = true
  try {
    const payload = {
      displayName: form.value.displayName.trim(),
      phone: form.value.phone.trim()
    }
    const res = await updateCurrentAccount(payload)
    authStore.setDisplayName(res?.displayName || payload.displayName)
    uni.showToast({ title: '保存成功', icon: 'success' })
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

function goPassword() {
  uni.navigateTo({ url: '/pages/security/passwordChange' })
}
</script>

<template>
  <view class="op-page security-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">账号安全</text>
      <text class="op-hero-title">设置自己的联系信息</text>
      <text class="op-hero-desc">用于企业端通知接收和身份展示</text>
    </view>

    <view class="op-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>
      <view v-else class="security-card">
        <view class="form-section">
          <text class="section-title">基本信息</text>
          <view class="form-row">
            <text class="form-label">姓名</text>
            <input v-model="form.displayName" class="form-input" placeholder="请输入姓名" />
          </view>
          <view class="form-row">
            <text class="form-label">联系电话</text>
            <input v-model="form.phone" class="form-input" type="number" placeholder="请输入联系电话" />
          </view>
        </view>

        <view class="security-link" @click="goPassword">
          <view class="security-link-icon">密</view>
          <view class="op-row-main">
            <text class="security-link-title">修改密码</text>
            <text class="security-link-desc">单独修改当前登录账号密码</text>
          </view>
          <text class="security-link-arrow">›</text>
        </view>

        <button class="save-btn" :disabled="saving" @click="handleSave">
          <text class="save-btn-text">{{ saving ? '保存中...' : '保存设置' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<style>
.security-page { min-height: 100vh; padding-bottom: 48rpx; }
.top-space { height: 24rpx; }
.security-card { padding: 28rpx; border-radius: 30rpx; background: #fff; box-shadow: 0 12rpx 34rpx rgba(23,83,53,.08); box-sizing: border-box; }
.form-section { padding-bottom: 10rpx; }
.section-title { display: block; font-size: 30rpx; font-weight: 850; color: #1f2933; }
.form-row { margin-top: 24rpx; }
.form-label { display: block; margin-bottom: 12rpx; font-size: 25rpx; font-weight: 800; color: #64748b; }
.form-input { width: 100%; height: 88rpx; padding: 0 22rpx; border-radius: 20rpx; background: #f8fafc; border: 2rpx solid #edf0f3; color: #1f2933; font-size: 28rpx; box-sizing: border-box; }
.save-btn { height: 86rpx; line-height: 86rpx; margin-top: 34rpx; border-radius: 999rpx; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; }
.save-btn[disabled] { background: #a7f3d0; color: #fff; }
.save-btn-text { color: #fff; }
.security-link { display: flex; align-items: center; margin-top: 30rpx; padding: 22rpx; border-radius: 24rpx; background: #f8fafc; }
.security-link-icon { width: 68rpx; height: 68rpx; margin-right: 18rpx; border-radius: 22rpx; display: flex; align-items: center; justify-content: center; background: #ecfdf5; color: #16a34a; font-size: 28rpx; font-weight: 850; flex-shrink: 0; }
.security-link-title { display: block; font-size: 27rpx; font-weight: 850; color: #1f2933; }
.security-link-desc { display: block; margin-top: 8rpx; font-size: 23rpx; color: #64748b; }
.security-link-arrow { margin-left: 12rpx; color: #98a3b3; font-size: 42rpx; }
</style>
