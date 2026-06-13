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

function canOperate(record) {
  return record?.settlementStatus !== 'PAID'
}

function toggleSelect(record) {
  if (!canOperate(record)) return
  const id = record.id
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) selectedIds.value.splice(idx, 1)
  else selectedIds.value.push(id)
}

function toggleSelectAll() {
  const allIds = records.value.filter(canOperate).map(r => r.id)
  if (selectedIds.value.length === allIds.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = [...allIds]
  }
}

async function handleBatchPay() {
  selectedIds.value = selectedIds.value.filter(id => records.value.some(r => r.id === id && canOperate(r)))
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
  selectedIds.value = selectedIds.value.filter(id => records.value.some(r => r.id === id && canOperate(r)))
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

function salaryLabel(record) {
  if (record.salaryAmount == null) return '-'
  const unitMap = { HOURLY: '元/小时', DAILY: '元/天' }
  return `${record.salaryAmount}${unitMap[record.salaryType] || ''}`
}

function moneyLabel(value) {
  return value == null ? '-' : `¥${value}`
}

function timeRange(record) {
  if (!record.startTime && !record.endTime) return '-'
  return `${record.startTime || '-'}~${record.endTime || '-'}`
}
</script>

<template>
  <scroll-view scroll-y class="op-page salary-page" @scrolltolower="loadMore">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">薪资结算</text>
      <text class="op-hero-title">确认工时并批量结算</text>
      <text class="op-hero-desc">{{ records.length }} 条记录 · 已选 {{ selectedIds.length }} 条</text>
    </view>

    <view class="op-content">
      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-value">{{ records.length }}</text>
          <text class="summary-label">记录数</text>
        </view>
        <view class="summary-item">
          <text class="summary-value selected">{{ selectedIds.length }}</text>
          <text class="summary-label">已选择</text>
        </view>
      </view>

      <view class="batch-bar">
        <view class="select-all" @click="toggleSelectAll">
          <text class="checkbox" :class="{ checked: selectedIds.length === records.filter(canOperate).length && records.filter(canOperate).length > 0 }">{{ selectedIds.length === records.filter(canOperate).length && records.filter(canOperate).length > 0 ? '✓' : '' }}</text>
          <text class="select-all-label">全选可操作记录</text>
        </view>
        <view class="batch-actions">
          <view class="batch-btn pay" @click="handleBatchPay">结算</view>
          <view class="batch-btn delete" @click="handleBatchDelete">删除</view>
        </view>
      </view>

      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="records.length === 0" class="e-empty">
        <text class="e-empty-title">暂无考勤记录</text>
      </view>

      <view v-else class="salary-list">
        <view v-for="r in records" :key="r.id" class="op-card salary-card" @click="toggleSelect(r)">
          <view class="op-row">
            <text class="checkbox" :class="{ checked: selectedIds.includes(r.id), disabled: !canOperate(r) }">{{ selectedIds.includes(r.id) ? '✓' : '' }}</text>
            <view class="op-row-main">
              <text class="op-row-title">{{ r.workerName || '未知' }}</text>
              <text class="op-row-desc">{{ r.jobTitle || '-' }} · {{ r.shiftDate || '-' }}</text>
            </view>
            <text class="op-pill" :class="r.settlementStatus === 'PAID' ? '' : r.settlementStatus === 'PAYING' ? 'op-pill-warn' : 'pill-gray'">{{ settlementStatusLabel(r.settlementStatus) }}</text>
          </view>

          <view class="salary-grid">
            <view class="salary-info">
              <text class="salary-label">应付薪资</text>
              <text class="salary-value money">{{ moneyLabel(r.payablePay ?? r.scheduledPay) }}</text>
            </view>
            <view class="salary-info">
              <text class="salary-label">工时</text>
              <text class="salary-value">{{ r.totalHours ?? '-' }}</text>
            </view>
            <view class="salary-info">
              <text class="salary-label">薪资标准</text>
              <text class="salary-value">{{ salaryLabel(r) }}</text>
            </view>
            <view class="salary-info">
              <text class="salary-label">签到签退</text>
              <text class="salary-value">{{ r.checkInTime || '-' }} / {{ r.checkOutTime || '-' }}</text>
            </view>
            <view class="salary-info wide">
              <text class="salary-label">排班时间</text>
              <text class="salary-value">{{ timeRange(r) }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="load-more-wrap">
        <uni-load-more v-if="loadingMore" status="loading" />
        <uni-load-more v-else-if="hasMore" status="more" />
        <uni-load-more v-else status="noMore" />
      </view>
    </view>
  </scroll-view>
</template>

<style>
.salary-page { height: 100vh; }
.top-space { height: 24rpx; }
.summary-card { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin-bottom: 20rpx; }
.summary-item { padding: 24rpx; border-radius: 24rpx; background: #fff; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); }
.summary-value { display: block; font-size: 40rpx; font-weight: 850; color: #16a34a; line-height: 1; }
.summary-value.selected { color: #b45309; }
.summary-label { display: block; margin-top: 10rpx; font-size: 23rpx; color: #64748b; }
.batch-bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20rpx; padding: 18rpx; border-radius: 24rpx; background: #fff; box-shadow: 0 10rpx 24rpx rgba(23,83,53,.06); }
.select-all { display: flex; align-items: center; min-width: 0; }
.checkbox { width: 42rpx; height: 42rpx; margin-right: 12rpx; border: 2rpx solid #d1d5db; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #fff; flex-shrink: 0; }
.checkbox.checked { background: #16a34a; border-color: #16a34a; }
.checkbox.disabled { background: #eef1f0; border-color: #eef1f0; }
.select-all-label { font-size: 24rpx; color: #64748b; }
.batch-actions { display: flex; gap: 12rpx; flex-shrink: 0; }
.batch-btn { min-width: 96rpx; height: 58rpx; line-height: 58rpx; border-radius: 999rpx; text-align: center; font-size: 24rpx; font-weight: 850; }
.batch-btn.pay { background: #16a34a; color: #fff; }
.batch-btn.delete { background: #fee2e2; color: #dc2626; }
.salary-list { display: flex; flex-direction: column; gap: 20rpx; }
.salary-card { overflow: hidden; }
.pill-gray { color: #64748b; background: #f1f5f9; }
.salary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin-top: 22rpx; }
.salary-info { min-width: 0; padding: 16rpx; border-radius: 18rpx; background: #f8fafc; }
.salary-info.wide { grid-column: span 2; }
.salary-label { display: block; font-size: 22rpx; color: #64748b; }
.salary-value { display: block; margin-top: 8rpx; font-size: 25rpx; font-weight: 750; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.salary-value.money { color: #16a34a; font-size: 30rpx; font-weight: 850; }
.load-more-wrap { padding: 16rpx 0 32rpx; }
</style>
