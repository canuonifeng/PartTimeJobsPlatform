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
    <view class="header">
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
.page { min-height: 100vh; background: #f5f5f5; }
.header { padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.content { padding: 24rpx 32rpx; }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 28rpx; }
.form-row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 2rpx solid #f2f2f2; }
.column { align-items: flex-start; flex-direction: column; gap: 16rpx; }
.label { font-size: 28rpx; color: #666; }
.value { font-size: 28rpx; color: #333; }
.logo { width: 120rpx; height: 120rpx; border-radius: 12rpx; background: #f2f2f2; }
.input { width: 100%; height: 76rpx; padding: 0 20rpx; border: 2rpx solid #ddd; border-radius: 8rpx; box-sizing: border-box; font-size: 28rpx; }
.save-btn { margin-top: 32rpx; height: 76rpx; line-height: 76rpx; background: #007aff; color: #fff; border-radius: 8rpx; font-size: 28rpx; }
.save-btn::after { border: none; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 28rpx 0; border-bottom: 2rpx solid #f2f2f2; margin-top: 24rpx; }
.menu-label { font-size: 28rpx; color: #333; }
.menu-arrow { font-size: 32rpx; color: #ccc; }
</style>
