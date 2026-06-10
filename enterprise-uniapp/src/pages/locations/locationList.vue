<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listLocations, deleteLocation, enableLocation, disableLocation } from '@/api/locations'

const locations = ref([])
const loading = ref(false)

onShow(loadLocations)
onPullDownRefresh(() => loadLocations().finally(() => uni.stopPullDownRefresh()))

async function loadLocations() {
  loading.value = true
  try {
    const res = await listLocations()
    locations.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function openCreate() {
  uni.navigateTo({ url: '/pages/locations/locationForm' })
}

function openEdit(loc) {
  const data = encodeURIComponent(JSON.stringify(loc))
  uni.navigateTo({ url: `/pages/locations/locationForm?id=${loc.id}&data=${data}` })
}

function handleDelete(id) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该地点吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteLocation(id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadLocations()
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

async function toggleEnabled(loc) {
  try {
    if (loc.status === 'ENABLED') {
      await disableLocation(loc.id)
    } else {
      await enableLocation(loc.id)
    }
    loadLocations()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">工作地点</text>
      <text class="add-btn" @click="openCreate">+ 新建</text>
    </view>
    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="locations.length === 0" class="state-msg">暂无地点</view>
      <view v-else class="list">
        <view v-for="loc in locations" :key="loc.id" class="card">
          <view class="card-top">
            <text class="card-name">{{ loc.name }}</text>
            <text class="badge" :class="loc.status === 'ENABLED' ? 'badge-on' : 'badge-off'" @click.stop="toggleEnabled(loc)">{{ loc.status === 'ENABLED' ? '已启用' : '已禁用' }}</text>
          </view>
          <text class="card-addr">{{ [loc.province, loc.city, loc.district, loc.address].filter(Boolean).join(' ') || '暂无地址' }}</text>
          <view class="card-actions">
            <button class="action-btn edit" @click="openEdit(loc)">编辑</button>
            <button class="action-btn delete" @click="handleDelete(loc.id)">删除</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f6f8f7; }
.header { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%); padding: 48rpx 32rpx 28rpx; border-bottom-left-radius: 36rpx; border-bottom-right-radius: 36rpx; }
.header-title { font-size: 36rpx; font-weight: 700; color: #fff; }
.add-btn { font-size: 26rpx; color: #fff; background: rgba(255,255,255,0.25); padding: 8rpx 24rpx; border-radius: 999rpx; }
.content { padding: 24rpx 32rpx; }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 28rpx; box-shadow: 0 12rpx 34rpx rgba(23,83,53,0.08); }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.card-name { font-size: 30rpx; font-weight: 600; color: #1f2933; }
.badge { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.badge-on { background: #e7f8ef; color: #08a857; }
.badge-off { background: #eef1f0; color: #7b8580; }
.card-addr { font-size: 26rpx; color: #64748b; display: block; margin-bottom: 16rpx; }
.card-actions { display: flex; gap: 16rpx; }
.action-btn { flex: 1; height: 64rpx; line-height: 64rpx; font-size: 24rpx; border-radius: 999rpx; border: 2rpx solid #e2e8f0; background: #fff; color: #1f2933; text-align: center; font-weight: 600; }
.action-btn::after { border: none; }
.edit { border-color: #07c160; color: #07c160; }
.delete { border-color: #ef4444; color: #ef4444; }
</style>
