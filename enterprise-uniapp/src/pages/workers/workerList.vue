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

function genderLabel(gender) {
  const map = { MALE: '男', FEMALE: '女', OTHER: '其他' }
  return map[gender] || gender || '未知'
}

function realNameStatusLabel(status) {
  const map = { NONE: '未实名', PENDING: '审核中', APPROVED: '已实名', REJECTED: '未通过' }
  return map[status] || status || '未实名'
}

function realNameStatusClass(status) {
  if (status === 'APPROVED') return 'value-green'
  if (status === 'PENDING') return 'value-warn'
  if (status === 'REJECTED') return 'value-red'
  return ''
}
</script>

<template>
  <view class="page e-page">
    <view class="header e-header">
      <text class="e-header-title">兼职管理</text>
      <text class="e-header-desc">查看已联系兼职，维护黑名单状态</text>
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

    <view class="content e-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="workers.length === 0" class="e-empty">
        <text class="e-empty-title">暂无兼职</text>
        <text class="e-empty-desc">还没有工人加入平台</text>
      </view>

      <view v-else>
        <view v-for="w in workers" :key="w.id || w.workerId" class="e-card">
          <view class="e-card-title-row">
            <view class="worker-title">
              <text class="e-card-title">{{ w.name || '未实名' }}</text>
              <text class="gender-pill">{{ genderLabel(w.workerGender || w.gender) }}</text>
            </view>
            <view class="e-badge" :class="w.status === 'ACTIVE' ? 'e-badge-green' : 'e-badge-red'">{{ w.status === 'ACTIVE' ? '正常' : '已拉黑' }}</view>
          </view>

          <view class="e-info-grid">
            <view class="e-info-pill">
              <text class="e-info-label">电话</text>
              <text class="e-info-value">{{ w.phone || '-' }}</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">年龄</text>
              <text class="e-info-value">{{ w.workerAge ?? '-' }}岁</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">实名</text>
              <text class="e-info-value" :class="realNameStatusClass(w.realNameStatus)">{{ realNameStatusLabel(w.realNameStatus) }}</text>
            </view>
          </view>

          <view class="e-action-row">
            <view
              v-if="w.status === 'ACTIVE'"
              class="e-action-pill e-action-orange"
              @click="toggleStatus(w)"
            >拉黑</view>
            <view
              v-else
              class="e-action-pill e-action-primary"
              @click="toggleStatus(w)"
            >启用</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.search-bar {
  display: flex;
  padding: 20rpx 28rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
  box-sizing: border-box;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 72rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 44rpx;
  font-size: 26rpx;
  background: #f8fafc;
  box-sizing: border-box;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 140rpx;
  height: 72rpx;
  margin-left: 16rpx;
  background: #07c160;
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  border-radius: 44rpx;
}

.worker-title {
  display: flex;
  align-items: center;
  min-width: 0;
}

.gender-pill {
  flex-shrink: 0;
  margin-left: 12rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #ecfdf5;
  color: #16a34a;
  font-size: 22rpx;
  font-weight: 750;
}

.value-green {
  color: #16a34a;
}

.value-warn {
  color: #d97706;
}

.value-red {
  color: #dc2626;
}
</style>
