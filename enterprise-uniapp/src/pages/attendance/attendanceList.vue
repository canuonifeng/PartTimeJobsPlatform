<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listAttendance, batchPay, batchDeleteAttendance } from '@/api/attendance'

const records = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)
const requestSeq = ref(0)
const pageSize = 20
const selectedIds = ref([])

onShow(refreshRecords)
onPullDownRefresh(() => {
  refreshRecords().finally(() => uni.stopPullDownRefresh())
})

function refreshRecords() {
  page.value = 1
  records.value = []
  hasMore.value = true
  selectedIds.value = []
  return loadRecords()
}

async function loadRecords(append = false) {
  const seq = ++requestSeq.value
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await listAttendance({ page: page.value, pageSize })
    if (seq !== requestSeq.value) return
    const list = Array.isArray(res) ? res : (res.records || res.data || [])
    records.value = append ? records.value.concat(list) : list
    hasMore.value = list.length >= pageSize
  } catch {
    if (seq === requestSeq.value) {
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  } finally {
    if (seq === requestSeq.value) {
      loading.value = false
      loadingMore.value = false
    }
  }
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  page.value += 1
  loadRecords(true)
}

function toggleSelect(id) {
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) selectedIds.value.splice(idx, 1)
  else selectedIds.value.push(id)
}

function toggleSelectAll() {
  const allIds = records.value.map(r => r.id)
  if (selectedIds.value.length === allIds.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = [...allIds]
  }
}

async function handleBatchPay() {
  if (selectedIds.value.length === 0) {
    uni.showToast({ title: '请选择要结算的记录', icon: 'none' })
    return
  }
  try {
    await batchPay(selectedIds.value)
    uni.showToast({ title: '结算成功', icon: 'success' })
    refreshRecords()
  } catch {
    uni.showToast({ title: '结算失败', icon: 'none' })
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    uni.showToast({ title: '请选择要删除的记录', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedIds.value.length} 条记录吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await batchDeleteAttendance(selectedIds.value)
          uni.showToast({ title: '已删除', icon: 'success' })
          await refreshRecords()
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

function settlementStatusLabel(s) {
  const map = { UNPAID: '未结算', PAYING: '结算中', PAID: '已结算' }
  return map[s] || s || '-'
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">薪资管理</text>
      <view class="header-actions">
        <button class="header-btn pay-btn" @click="handleBatchPay">结算</button>
        <button class="header-btn del-btn" @click="handleBatchDelete">删除</button>
      </view>
    </view>
    <view class="content">
      <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
        <view v-if="loading" class="state-msg">加载中...</view>
        <view v-else-if="records.length === 0" class="state-msg">暂无考勤记录</view>
        <view v-else class="list">
          <view class="select-all" @click="toggleSelectAll">
            <text class="checkbox" :class="{ checked: selectedIds.length === records.length && records.length > 0 }">{{ selectedIds.length === records.length && records.length > 0 ? '✓' : '' }}</text>
            <text class="select-all-label">全选</text>
          </view>
          <view v-for="r in records" :key="r.id" class="card" @click="toggleSelect(r.id)">
            <view class="card-top">
              <text class="checkbox" :class="{ checked: selectedIds.includes(r.id) }">{{ selectedIds.includes(r.id) ? '✓' : '' }}</text>
              <text class="card-name">{{ r.workerName || '未知' }}</text>
              <text class="badge" :class="r.settlementStatus === 'PAID' ? 'badge-paid' : r.settlementStatus === 'PAYING' ? 'badge-paying' : 'badge-unpaid'">{{ settlementStatusLabel(r.settlementStatus) }}</text>
            </view>
            <view class="card-body">
              <text class="info">日期：{{ r.recordDate || '-' }}</text>
              <text class="info">年龄：{{ r.workerAge ?? '-' }}岁</text>
              <text class="info">工时：{{ r.totalHours ?? '-' }}</text>
              <text class="info">应付：¥{{ r.payablePay ?? r.scheduledPay ?? '0' }}</text>
            </view>
          </view>
        </view>

        <view class="load-more-wrap">
          <uni-load-more v-if="loadingMore" status="loading" />
          <uni-load-more v-else-if="hasMore" status="more" />
          <uni-load-more v-else status="noMore" />
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f6f8f7; }
.header { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%); padding: 48rpx 32rpx 28rpx; border-bottom-left-radius: 36rpx; border-bottom-right-radius: 36rpx; }
.header-title { font-size: 36rpx; font-weight: 700; color: #fff; }
.header-actions { display: flex; gap: 16rpx; }
.header-btn { height: 60rpx; line-height: 60rpx; font-size: 24rpx; padding: 0 24rpx; border-radius: 999rpx; color: #fff; font-weight: 600; }
.header-btn::after { border: none; }
.pay-btn { background: #07c160; }
.del-btn { background: #ff3b30; }
.content { padding: 24rpx 28rpx; }
.list-scroll { height: calc(100vh - 140rpx); }
.state-msg { text-align: center; padding: 120rpx 0; color: #98a3b3; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.select-all { display: flex; align-items: center; padding: 12rpx 0; }
.checkbox { width: 40rpx; height: 40rpx; border: 2rpx solid #d1d5db; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #fff; margin-right: 12rpx; flex-shrink: 0; }
.checkbox.checked { background: #07c160; border-color: #07c160; }
.select-all-label { font-size: 26rpx; color: #64748b; }
.card { background: #fff; border-radius: 24rpx; padding: 28rpx; box-shadow: 0 12rpx 34rpx rgba(23,83,53,0.08); }
.card-top { display: flex; align-items: center; margin-bottom: 16rpx; }
.card-name { font-size: 30rpx; font-weight: 600; color: #1f2933; flex: 1; margin-left: 8rpx; }
.badge { font-size: 22rpx; padding: 6rpx 18rpx; border-radius: 999rpx; font-weight: 700; flex-shrink: 0; }
.badge-paid { background: #e7f8ef; color: #08a857; }
.badge-paying { background: #fff7df; color: #d28a00; }
.badge-unpaid { background: #eef1f0; color: #7b8580; }
.card-body { margin-top: 8rpx; }
.info { display: block; font-size: 26rpx; color: #64748b; line-height: 1.8; }
.load-more-wrap { padding-bottom: 24rpx; }
</style>
