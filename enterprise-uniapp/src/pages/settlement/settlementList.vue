<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { listSettlementBills, unsettle } from '@/api/settlement'

const bills = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)
const pageSize = 20

onMounted(loadBills)
onPullDownRefresh(() => {
  page.value = 1
  bills.value = []
  hasMore.value = true
  loadBills().finally(() => uni.stopPullDownRefresh())
})

async function loadBills(append = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await listSettlementBills({ page: page.value, pageSize })
    const list = Array.isArray(res) ? res : (res.records || res.data || [])
    bills.value = append ? bills.value.concat(list) : list
    hasMore.value = list.length >= pageSize
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  page.value += 1
  loadBills(true)
}

function handleUnsettle(attendanceRecordId) {
  uni.showModal({
    title: '确认撤回',
    content: '确定要撤回该笔结算吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await unsettle(attendanceRecordId)
          uni.showToast({ title: '已撤回', icon: 'success' })
          page.value = 1
          bills.value = []
          hasMore.value = true
          loadBills()
        } catch {
          uni.showToast({ title: '撤回失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">结算账单</text>
    </view>
    <view class="content">
      <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
        <view v-if="loading" class="state-msg">加载中...</view>
        <view v-else-if="bills.length === 0" class="state-msg">暂无账单</view>
        <view v-else class="list">
          <view v-for="b in bills" :key="b.id" class="card">
            <view class="card-top">
              <text class="card-name">{{ b.workerName || '未知' }}</text>
              <text class="card-amount">¥{{ b.amount || '0' }}</text>
            </view>
            <view class="card-body">
              <text class="info">岗位：{{ b.jobTitle || '-' }}</text>
              <text class="info">日期：{{ b.settlementDate || b.createdAt || '-' }}</text>
              <text class="info">考勤记录ID：{{ b.attendanceRecordId || '-' }}</text>
            </view>
            <view class="card-actions">
              <button class="action-btn unsettle" @click="handleUnsettle(b.attendanceRecordId)">撤回</button>
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
.page { min-height: 100vh; background: #f5f5f5; }
.header { padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.content { padding: 24rpx 32rpx; }
.list-scroll { height: calc(100vh - 120rpx); }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.card-name { font-size: 30rpx; font-weight: 500; color: #333; }
.card-amount { font-size: 36rpx; font-weight: 600; color: #ff3b30; }
.card-body { margin-bottom: 12rpx; }
.info { display: block; font-size: 26rpx; color: #666; line-height: 1.8; }
.card-actions { display: flex; gap: 16rpx; }
.action-btn { flex: 1; height: 64rpx; line-height: 64rpx; font-size: 24rpx; border-radius: 8rpx; border: 2rpx solid #ddd; background: #fff; text-align: center; }
.action-btn::after { border: none; }
.unsettle { border-color: #ff9500; color: #ff9500; }
.load-more-wrap { padding-bottom: 24rpx; }
</style>
