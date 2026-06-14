<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getEnterpriseInfo, updateCompanyLogo } from '@/api/enterprise'

const loading = ref(false)
const saving = ref(false)
const info = ref({ companyName: '', companyLogo: '' })
const logoUrl = ref('')

onShow(loadInfo)

async function loadInfo() {
  loading.value = true
  try {
    const res = await getEnterpriseInfo()
    info.value = res || {}
    logoUrl.value = res?.companyLogo || ''
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function saveLogo() {
  if (saving.value) return
  saving.value = true
  try {
    await updateCompanyLogo(logoUrl.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
    info.value.companyLogo = logoUrl.value
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

function goRealName() {
  uni.navigateTo({ url: '/pages/auth/realName' })
}
</script>

<template>
  <view class="page">
    <view class="header-gradient">
      <text class="header-title">企业设置</text>
    </view>
    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else>
        <view class="e-form-section">
          <text class="e-section-title">公司信息</text>
          <view class="e-form-row">
            <text class="e-form-label">企业名称</text>
            <text class="value">{{ info.companyName || '-' }}</text>
          </view>
        </view>
        <view class="e-form-section">
          <text class="e-section-title">企业Logo</text>
          <image v-if="logoUrl" class="logo" :src="logoUrl" mode="aspectFill" />
          <input v-model="logoUrl" class="e-input" placeholder="请输入Logo图片URL" />
          <button class="btn-primary save-btn" :loading="saving" :disabled="saving" @click="saveLogo">{{ saving ? '保存中' : '保存' }}</button>
        </view>
        <view class="e-form-section">
          <text class="e-section-title">认证信息</text>
          <view class="menu-item" @click="goRealName">
            <view class="menu-left">
              <view class="menu-icon">✓</view>
              <text class="menu-label">企业实名认证</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f6f8f7;
}
.header-gradient {
  background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%);
  padding: 48rpx 32rpx 28rpx;
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}
.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}
.content {
  padding: 24rpx 32rpx 40rpx;
  box-sizing: border-box;
}
.state-msg {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}
.value {
  font-size: 28rpx;
  color: #1f2933;
}
.logo {
  display: block;
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
  border-radius: 14rpx;
  background: #f2f2f2;
}
.save-btn {
  margin-top: 32rpx;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4rpx 0;
}
.menu-left {
  display: flex;
  align-items: center;
  min-width: 0;
}
.menu-icon {
  width: 68rpx;
  height: 68rpx;
  margin-right: 18rpx;
  border: 2rpx solid rgba(22,163,74,.26);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  color: #16a34a;
  font-size: 28rpx;
  font-weight: 900;
  box-sizing: border-box;
}
.menu-label {
  font-size: 28rpx;
  color: #1f2933;
}
.menu-arrow {
  font-size: 32rpx;
  color: #ccc;
}
</style>
