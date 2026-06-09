<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getEnterpriseInfo, updateCompanyLogo } from '@/api/enterprise'

const loading = ref(false)
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
  try {
    await updateCompanyLogo(logoUrl.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
    info.value.companyLogo = logoUrl.value
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
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
      <view v-else class="card">
        <view class="form-row">
          <text class="label">企业名称</text>
          <text class="value">{{ info.companyName || '-' }}</text>
        </view>
        <view class="form-row column">
          <text class="label">企业Logo</text>
          <image v-if="logoUrl" class="logo" :src="logoUrl" mode="aspectFill" />
          <input v-model="logoUrl" class="input" placeholder="请输入Logo图片URL" />
        </view>
        <button class="save-btn" @click="saveLogo">保存</button>
        <view class="menu-item" @click="goRealName">
          <text class="menu-label">企业实名认证</text>
          <text class="menu-arrow">›</text>
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
  padding: 48rpx 32rpx 40rpx;
  background: linear-gradient(135deg, #07c160, #08a95a);
}
.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}
.content {
  padding: 0 32rpx;
  margin-top: -20rpx;
}
.state-msg {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}
.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}
.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #f2f2f2;
}
.column {
  align-items: flex-start;
  flex-direction: column;
  gap: 16rpx;
}
.label {
  font-size: 28rpx;
  color: #64748b;
}
.value {
  font-size: 28rpx;
  color: #1f2933;
}
.logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 14rpx;
  background: #f2f2f2;
}
.input {
  width: 100%;
  height: 78rpx;
  padding: 0 20rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  box-sizing: border-box;
  font-size: 28rpx;
}
.save-btn {
  margin-top: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: linear-gradient(135deg, #18c86b, #08a95a);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 700;
  border: none;
}
.save-btn::after {
  border: none;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 2rpx solid #f2f2f2;
  margin-top: 24rpx;
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
