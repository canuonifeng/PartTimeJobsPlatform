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

    <view class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索姓名/电话"
        confirm-type="search"
        @confirm="handleSearch"
      />
      <view class="search-btn" @click="handleSearch">搜索</view>
    </view>

    <view class="content">
      <view v-if="loading" class="empty-state">
        <text class="empty-emoji">⏳</text>
        <text class="empty-title">加载中...</text>
      </view>

      <view v-else-if="workers.length === 0" class="empty-state">
        <text class="empty-emoji">👥</text>
        <text class="empty-title">暂无兼职</text>
        <text class="empty-desc">还没有工人加入平台</text>
      </view>

      <view v-else class="worker-list">
        <view v-for="w in workers" :key="w.id || w.workerId" class="worker-card">
          <view class="card-header">
            <text class="worker-name">{{ w.name || '未实名' }}</text>
            <view
              class="badge"
              :class="w.status === 'ACTIVE' ? 'badge-green' : 'badge-red'"
            >{{ w.status === 'ACTIVE' ? '正常' : '已拉黑' }}</view>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="info-label">编号</text>
              <text class="info-value">{{ w.workerId || w.id }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">电话</text>
              <text class="info-value">{{ w.phone || '-' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">年龄</text>
              <text class="info-value">{{ w.workerAge ?? '-' }}岁</text>
            </view>
            <view class="info-row">
              <text class="info-label">首次联系</text>
              <text class="info-value">{{ w.firstContactAt || '-' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">最近联系</text>
              <text class="info-value">{{ w.lastContactAt || '-' }}</text>
            </view>
          </view>

          <view class="card-footer">
            <view
              v-if="w.status === 'ACTIVE'"
              class="action-pill pill-orange"
              @click="toggleStatus(w)"
            >拉黑</view>
            <view
              v-else
              class="action-pill pill-green"
              @click="toggleStatus(w)"
            >取消拉黑</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #f6f8f7;
}

.header {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
}

.header-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2933;
}

.search-bar {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
}

.search-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 44rpx;
  font-size: 26rpx;
  background: #f8fafc;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 140rpx;
  height: 72rpx;
  background: #07c160;
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 44rpx;
}

.content {
  padding: 24rpx 32rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-emoji {
  font-size: 64rpx;
  margin-bottom: 20rpx;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1f2933;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #98a3b3;
}

.worker-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.worker-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.worker-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2933;
  flex: 1;
  margin-right: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge {
  font-size: 24rpx;
  font-weight: 700;
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
}

.badge-green {
  background: #e7f8ef;
  color: #08a857;
}

.badge-yellow {
  background: #fff7df;
  color: #d28a00;
}

.badge-red {
  background: #feecec;
  color: #df3b30;
}

.badge-gray {
  background: #eef1f0;
  color: #7b8580;
}

.card-body {
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8rpx 0;
}

.info-label {
  font-size: 26rpx;
  color: #98a3b3;
}

.info-value {
  font-size: 26rpx;
  color: #1f2933;
}

.card-footer {
  display: flex;
}

.action-pill {
  font-size: 24rpx;
  font-weight: 600;
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid;
}

.pill-green {
  border-color: #07c160;
  color: #07c160;
}

.pill-orange {
  border-color: #f59e0b;
  color: #f59e0b;
}
</style>
