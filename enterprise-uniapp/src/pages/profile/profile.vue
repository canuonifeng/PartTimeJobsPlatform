<script setup>
import { onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { request } from '@/api/request'
import EnterpriseTabBar from '@/components/EnterpriseTabBar.vue'

const authStore = useAuthStore()
const companyName = ref('')

const managementItems = [
  { name: '工作地点', icon: '⌖', path: '/pages/locations/locationList' },
  { name: '职位模板', icon: '▤', path: '/pages/templates/templateList' },
  { name: '兼职管理', icon: '人', path: '/pages/workers/workerList' },
  { name: '账号管理', icon: 'ID', path: '/pages/accounts/accountList' },
  { name: '账户余额', icon: '¥', path: '/pages/balance/balanceList' },
  { name: '实名认证', icon: '✓', path: '/pages/auth/realName' }
]

const settingItems = [
  { name: '企业资料', desc: '公司名称、联系人、营业信息', icon: '企', path: '/pages/settings/companySettings' },
  { name: '账号安全', desc: '设置姓名、联系电话和密码', icon: '锁', path: '/pages/security/accountSecurity' },
  { name: '资金与流水', desc: '余额、充值、支出记录', icon: '¥', path: '/pages/balance/balanceList' }
]

onShow(() => {
  uni.hideTabBar({ animation: false })
})

onMounted(async () => {
  try {
    const res = await request('GET', '/enterprise')
    companyName.value = res?.companyName || ''
  } catch {}
})

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前企业账号吗？',
    success: (res) => {
      if (res.confirm) authStore.logout()
    }
  })
}
</script>

<template>
  <view class="op-page profile-page">
    <view class="profile-card">
      <view class="avatar">企</view>
      <view class="op-row-main">
        <text class="profile-name">{{ companyName || '企业名称' }}</text>
        <text class="profile-desc">已认证 · 管理员：{{ authStore.displayName || '企业管理员' }}</text>
      </view>
    </view>

    <view class="op-content">
      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">常用管理</text>
        </view>
        <view class="menu-grid">
          <view v-for="item in managementItems" :key="item.name" class="menu-item" @click="navigateTo(item.path)">
            <view class="menu-icon">{{ item.icon }}</view>
            <text class="menu-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">企业设置</text>
        </view>
        <view class="op-card setting-card">
          <view v-for="item in settingItems" :key="item.name" class="setting-row" @click="navigateTo(item.path)">
            <view class="setting-icon">{{ item.icon }}</view>
            <view class="op-row-main">
              <text class="op-row-title">{{ item.name }}</text>
              <text class="op-row-desc">{{ item.desc }}</text>
            </view>
            <text class="setting-arrow">›</text>
          </view>
          <view class="setting-row" @click="handleLogout">
            <view class="setting-icon danger">退</view>
            <view class="op-row-main">
              <text class="op-row-title">退出登录</text>
              <text class="op-row-desc">退出当前企业账号</text>
            </view>
            <text class="setting-arrow">›</text>
          </view>
        </view>
      </view>
    </view>
    <EnterpriseTabBar active="profile" />
  </view>
</template>

<style>
.profile-page { min-height: 100vh; padding-bottom: calc(140rpx + env(safe-area-inset-bottom)); }
.profile-card { display: flex; align-items: center; margin: 24rpx 28rpx 0; padding: 32rpx; border-radius: 32rpx; background: #fff; box-shadow: 0 12rpx 34rpx rgba(23,83,53,.08); box-sizing: border-box; }
.avatar { width: 104rpx; height: 104rpx; margin-right: 22rpx; border-radius: 32rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #18c86b, #047857); color: #fff; font-size: 42rpx; font-weight: 850; flex-shrink: 0; }
.profile-name { display: block; font-size: 34rpx; font-weight: 850; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.profile-desc { display: block; margin-top: 10rpx; font-size: 24rpx; color: #64748b; }
.menu-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; }
.menu-item { padding: 24rpx 8rpx; border-radius: 24rpx; background: #fff; text-align: center; box-shadow: 0 10rpx 24rpx rgba(23,83,53,.06); box-sizing: border-box; }
.menu-icon { width: 72rpx; height: 72rpx; margin: 0 auto 12rpx; border: 2rpx solid rgba(22,163,74,.26); border-radius: 25rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f0fdf4, #dcfce7); color: #16a34a; font-size: 27rpx; font-weight: 900; box-sizing: border-box; box-shadow: inset 0 0 0 5rpx rgba(255,255,255,.52); }
.menu-name { font-size: 24rpx; color: #334155; font-weight: 750; }
.setting-card { padding-top: 4rpx; padding-bottom: 4rpx; }
.setting-row { display: flex; align-items: center; min-width: 0; padding: 22rpx 0; border-bottom: 1rpx solid #edf0f3; }
.setting-row:last-child { border-bottom: none; }
.setting-icon { width: 74rpx; height: 74rpx; margin-right: 18rpx; border: 2rpx solid rgba(22,163,74,.26); border-radius: 25rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f0fdf4, #dcfce7); color: #16a34a; font-size: 27rpx; font-weight: 900; flex-shrink: 0; box-sizing: border-box; box-shadow: inset 0 0 0 5rpx rgba(255,255,255,.52); }
.setting-icon.danger { border-color: rgba(220,38,38,.24); background: linear-gradient(135deg, #fff1f2, #fee2e2); color: #dc2626; }
.setting-arrow { margin-left: 12rpx; color: #98a3b3; font-size: 40rpx; }
</style>
