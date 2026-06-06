<template>
  <view class="settings-page">
    <view class="header-card">
      <text class="header-title">设置</text>
      <text class="header-sub">管理账号安全、通知隐私与通用偏好</text>
    </view>

    <view v-for="group in settingGroups" :key="group.title" class="setting-group">
      <text class="group-title">{{ group.title }}</text>
      <view class="group-card">
        <view v-for="item in group.items" :key="item.title" class="setting-item" @click="handleItem(item)">
          <view class="item-left">
            <view class="item-icon" :class="item.iconClass">
              <text>{{ item.icon }}</text>
            </view>
            <view class="item-text-wrap">
              <text class="item-title">{{ item.title }}</text>
              <text v-if="item.desc" class="item-desc">{{ item.desc }}</text>
            </view>
          </view>
          <switch v-if="item.type === 'switch'" :checked="switchValues[item.key]" color="#07c160" @change.stop="toggleSwitch(item.key, $event)" />
          <view v-else class="item-right">
            <text v-if="item.value" class="item-value">{{ item.value }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/store'
import { getProfile } from '@/api/profile'
import { getBankCard } from '@/api/bankCard'
import { getRealNameStatus } from '@/api/realName'
import { getSettings, updateSettings } from '@/api/settings'

const authStore = useAuthStore()
const profile = ref({})
const bankCard = ref(null)
const realName = ref(null)
const savingKey = ref('')

const switchValues = reactive({
  push: true,
  location: true,
  quiet: false
})

const settingGroups = computed(() => [
  {
    title: '账号与安全',
    items: [
      { title: '手机号', value: profile.value?.phone || profile.value?.mobile || '未绑定', icon: '手', iconClass: 'icon-green' },
      { title: '实名认证', value: realNameStatusText.value, url: '/pages/auth/realName', icon: '实', iconClass: 'icon-orange' },
      { title: '银行卡管理', value: bankCard.value?.cardNumber ? '已绑定' : '未绑定', url: '/pages/bank/bankCard', icon: '卡', iconClass: 'icon-purple' }
    ]
  },
  {
    title: '通知与隐私',
    items: [
      { title: '推送通知', type: 'switch', key: 'push', icon: '通', iconClass: 'icon-green' },
      { title: '位置服务', type: 'switch', key: 'location', icon: '位', iconClass: 'icon-cyan' },
      { title: '免打扰', type: 'switch', key: 'quiet', icon: '扰', iconClass: 'icon-gray' }
    ]
  },
  {
    title: '通用',
    items: [
      { title: '语言', value: '简体中文', icon: '语', iconClass: 'icon-blue' }
    ]
  }
])

const realNameStatusText = computed(() => {
  const status = String(realName.value?.status || profile.value?.realNameStatus || '').toUpperCase()
  if (status === 'APPROVED' || status === 'VERIFIED') return '已实名'
  if (status === 'PENDING') return '审核中'
  if (status === 'REJECTED') return '未通过'
  return '未实名'
})

function handleItem(item) {
  if (item.type === 'switch') return
  if (item.url) uni.navigateTo({ url: item.url })
}

async function toggleSwitch(key, event) {
  const previous = switchValues[key]
  const next = event.detail.value
  switchValues[key] = next
  savingKey.value = key
  try {
    await updateSettings({
      pushEnabled: switchValues.push,
      locationEnabled: switchValues.location,
      quietEnabled: switchValues.quiet
    })
  } catch (err) {
    switchValues[key] = previous
    uni.showToast({ title: err?.message || '设置保存失败', icon: 'none' })
  } finally {
    savingKey.value = ''
  }
}

async function loadSettingsPage() {
  try {
    const [profileRes, settingsRes, bankRes, realNameRes] = await Promise.allSettled([
      getProfile(),
      getSettings(),
      getBankCard(),
      getRealNameStatus()
    ])
    if (profileRes.status === 'fulfilled') profile.value = profileRes.value || {}
    if (settingsRes.status === 'fulfilled') {
      switchValues.push = settingsRes.value?.pushEnabled !== false
      switchValues.location = settingsRes.value?.locationEnabled !== false
      switchValues.quiet = settingsRes.value?.quietEnabled === true
    }
    if (bankRes.status === 'fulfilled') bankCard.value = bankRes.value
    if (realNameRes.status === 'fulfilled') realName.value = realNameRes.value
  } catch {
    uni.showToast({ title: '设置加载失败', icon: 'none' })
  }
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: ({ confirm }) => {
      if (confirm) authStore.logout()
    }
  })
}

onMounted(loadSettingsPage)
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 30rpx;
  padding-bottom: 48rpx;
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
.setting-group {
  margin-bottom: 26rpx;
}
.group-title {
  display: block;
  margin: 0 8rpx 16rpx;
  font-size: 26rpx;
  color: #8a95a3;
  font-weight: 600;
}
.group-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 28rpx rgba(26, 35, 48, 0.06);
}
.setting-item {
  min-height: 104rpx;
  padding: 24rpx 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid #f2f4f6;
  box-sizing: border-box;
}
.setting-item:last-child {
  border-bottom: none;
}
.item-left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
}
.item-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 16rpx;
  margin-right: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
  flex-shrink: 0;
}
.item-text-wrap {
  min-width: 0;
  flex: 1;
}
.item-title {
  display: block;
  font-size: 30rpx;
  color: #1f2933;
  font-weight: 500;
}
.item-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #98a2b3;
}
.item-right {
  display: flex;
  align-items: center;
  margin-left: 20rpx;
}
.item-value {
  font-size: 26rpx;
  color: #98a2b3;
}
.arrow {
  margin-left: 12rpx;
  font-size: 44rpx;
  color: #c6ccd4;
  line-height: 1;
}
.icon-green {
  background: #19c876;
}
.icon-blue {
  background: #3b82f6;
}
.icon-orange {
  background: #ff8a00;
}
.icon-purple {
  background: #8b5cf6;
}
.icon-cyan {
  background: #06b6d4;
}
.icon-red {
  background: #f05252;
}
.icon-gray {
  background: #94a3b8;
}
.logout-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  margin-top: 8rpx;
  background: #fff;
  color: #ef4444;
  border-radius: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 28rpx rgba(26, 35, 48, 0.06);
}
.logout-btn::after {
  border: none;
}
</style>
