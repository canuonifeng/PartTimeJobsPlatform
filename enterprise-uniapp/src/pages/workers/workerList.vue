<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listWorkers, blacklistWorker, unblacklistWorker } from '@/api/worker'

const workers = ref([])
const loading = ref(false)
const keyword = ref('')

onShow(loadWorkers)
onPullDownRefresh(() => loadWorkers().finally(() => uni.stopPullDownRefresh()))

async function loadWorkers() {
  loading.value = true
  try {
    const res = await listWorkers({ keyword: keyword.value || undefined })
    workers.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadWorkers()
}

function toggleStatus(worker) {
  const active = worker.status === 'ACTIVE'
  uni.showModal({
    title: '确认操作',
    content: active ? '确定拉黑该兼职吗？' : '确定取消拉黑该兼职吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        if (active) await blacklistWorker(worker.id)
        else await unblacklistWorker(worker.id)
        uni.showToast({ title: '操作成功', icon: 'success' })
        loadWorkers()
      } catch {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    }
  })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">兼职管理</text>
    </view>
    <view class="search-row">
      <input v-model="keyword" class="search-input" placeholder="搜索姓名/电话" confirm-type="search" @confirm="handleSearch" />
      <button class="search-btn" @click="handleSearch">搜索</button>
    </view>
    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="workers.length === 0" class="state-msg">暂无兼职</view>
      <view v-else class="list">
        <view v-for="w in workers" :key="w.id || w.workerId" class="card">
          <view class="card-top">
            <text class="card-name">{{ w.name || '未实名' }}</text>
            <text class="badge" :class="w.status === 'ACTIVE' ? 'badge-on' : 'badge-off'">{{ w.status === 'ACTIVE' ? '正常' : '已拉黑' }}</text>
          </view>
          <text class="info">编号：{{ w.workerId || w.id }}</text>
          <text class="info">电话：{{ w.phone || '-' }}</text>
          <text class="info">首次联系：{{ w.firstContactAt || '-' }}</text>
          <text class="info">最近联系：{{ w.lastContactAt || '-' }}</text>
          <view class="card-actions">
            <button class="action-btn" :class="w.status === 'ACTIVE' ? 'warn' : 'ok'" @click="toggleStatus(w)">{{ w.status === 'ACTIVE' ? '拉黑' : '取消拉黑' }}</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; }
.header { padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.search-row { display: flex; gap: 16rpx; padding: 20rpx 32rpx; background: #fff; }
.search-input { flex: 1; height: 64rpx; padding: 0 20rpx; border: 2rpx solid #ddd; border-radius: 8rpx; font-size: 26rpx; }
.search-btn { width: 120rpx; height: 64rpx; line-height: 64rpx; font-size: 26rpx; background: #007aff; color: #fff; border-radius: 8rpx; }
.search-btn::after, .action-btn::after { border: none; }
.content { padding: 24rpx 32rpx; }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.card-name { font-size: 30rpx; font-weight: 500; color: #333; }
.badge { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.badge-on { background: #e8f8e8; color: #34c759; }
.badge-off { background: #fff3e0; color: #ff9500; }
.info { display: block; font-size: 26rpx; color: #666; margin-top: 8rpx; }
.card-actions { display: flex; margin-top: 20rpx; }
.action-btn { flex: 1; height: 64rpx; line-height: 64rpx; font-size: 24rpx; border-radius: 8rpx; background: #fff; }
.warn { border: 2rpx solid #ff9500; color: #ff9500; }
.ok { border: 2rpx solid #34c759; color: #34c759; }
</style>
