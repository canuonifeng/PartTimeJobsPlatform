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
.page { min-height: 100vh; background: #f5f5f5; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.add-btn { font-size: 28rpx; color: #007aff; }
.content { padding: 24rpx 32rpx; }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.card-name { font-size: 30rpx; font-weight: 500; color: #333; }
.badge { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.badge-on { background: #e8f8e8; color: #34c759; }
.badge-off { background: #f0f0f0; color: #999; }
.card-addr { font-size: 26rpx; color: #666; display: block; margin-bottom: 16rpx; }
.card-actions { display: flex; gap: 16rpx; }
.action-btn { flex: 1; height: 64rpx; line-height: 64rpx; font-size: 24rpx; border-radius: 8rpx; border: 2rpx solid #ddd; background: #fff; color: #333; text-align: center; }
.action-btn::after { border: none; }
.edit { border-color: #007aff; color: #007aff; }
.delete { border-color: #ff3b30; color: #ff3b30; }
</style>
