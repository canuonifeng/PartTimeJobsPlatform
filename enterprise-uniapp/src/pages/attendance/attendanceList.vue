<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listAttendance, batchPay } from '@/api/attendance'
import { getBalance } from '@/api/balance'
import { request } from '@/api/request'

const records = ref([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)
const requestSeq = ref(0)
const pageSize = 20
const currentSettlementStatus = ref('')
const balance = ref(null)
const settlementTabs = [
  { label: '全部', value: '' },
  { label: '未结算', value: 'UNPAID' },
  { label: '已结算', value: 'PAID' }
]

onLoad((options = {}) => {
  currentSettlementStatus.value = normalizeSettlementStatus(options.settlementStatus)
})

onShow(() => { refreshRecords(); loadBalance() })
onPullDownRefresh(() => {
  refreshRecords().finally(() => uni.stopPullDownRefresh())
})

async function loadBalance() {
  try {
    balance.value = await getBalance(request)
  } catch {}
}

function isBalanceInsufficient() {
  if (!balance.value) return false
  const bal = typeof balance.value === 'object' ? (balance.value.amount || balance.value.balance || 0) : Number(balance.value)
  return bal <= 0
}

function normalizeSettlementStatus(status) {
  return settlementTabs.some(tab => tab.value === status) ? status : ''
}

function refreshRecords() {
  page.value = 1
  records.value = []
  hasMore.value = true
  return loadRecords()
}

async function loadRecords(append = false) {
  const seq = ++requestSeq.value
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await listAttendance({ settlementStatus: currentSettlementStatus.value, page: page.value, pageSize })
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
  return record?.settlementStatus === 'UNPAID'
}

function switchSettlementStatus(status) {
  if (currentSettlementStatus.value === status || loading.value) return
  currentSettlementStatus.value = status
  refreshRecords()
}

async function handlePay(record) {
  if (!canOperate(record)) return
  try {
    await batchPay([record.id])
    uni.showToast({ title: '结算成功', icon: 'success' })
    refreshRecords()
  } catch {
    uni.showToast({ title: '结算失败', icon: 'none' })
  }
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
  return `${formatClock(record.startTime)}-${formatClock(record.endTime)}`
}

function formatClock(value) {
  if (!value) return '-'
  return String(value).slice(0, 5)
}

function beijingTime(value) {
  if (!value) return '-'
  const raw = String(value).replace('T', ' ')
  const hasTimezone = /Z$|[+-]\d{2}:?\d{2}$/.test(String(value))
  if (!hasTimezone) return raw.slice(0, 16)
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return raw.slice(0, 16)
  const beijing = new Date(date.getTime() + 8 * 60 * 60 * 1000)
  return `${beijing.getUTCFullYear()}-${pad2(beijing.getUTCMonth() + 1)}-${pad2(beijing.getUTCDate())} ${pad2(beijing.getUTCHours())}:${pad2(beijing.getUTCMinutes())}`
}

function pad2(value) {
  return String(value).padStart(2, '0')
}

function navigateToBalance() {
  uni.navigateTo({ url: '/pages/balance/balanceList' })
}
</script>

<template>
  <scroll-view scroll-y class="op-page salary-page" @scrolltolower="loadMore">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">薪资结算</text>
      <text class="op-hero-title">确认工时并逐条结算</text>
      <text class="op-hero-desc">{{ records.length }} 条记录 · 按结算状态筛选处理</text>
    </view>

    <view class="op-content">
      <view class="balance-card" @click="navigateToBalance">
        <view class="balance-left">
          <text class="balance-label">企业余额</text>
          <text class="balance-amount">¥{{ balance?.amount ?? balance?.balance ?? '-' }}</text>
        </view>
        <view class="balance-right">
          <text class="balance-action">去充值</text>
          <text class="balance-arrow">›</text>
        </view>
      </view>

      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-value">{{ records.length }}</text>
          <text class="summary-label">记录数</text>
        </view>
        <view class="summary-item">
          <text class="summary-value selected">{{ records.filter(canOperate).length }}</text>
          <text class="summary-label">可结算</text>
        </view>
      </view>

      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view class="filter-row">
          <view v-for="tab in settlementTabs" :key="tab.value || 'ALL'" class="filter-pill" :class="{ active: currentSettlementStatus === tab.value }" @click="switchSettlementStatus(tab.value)">{{ tab.label }}</view>
        </view>
      </scroll-view>

      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="records.length === 0" class="e-empty">
        <text class="e-empty-title">暂无考勤记录</text>
      </view>

      <view v-else class="salary-list">
        <view v-for="r in records" :key="r.id" class="op-card operation-card salary-card">
          <view class="op-row">
            <view class="op-row-main">
              <text class="op-row-title title-wrap">{{ r.workerName || '未知' }}</text>
              <text class="op-row-desc desc-wrap">{{ r.jobTitle || '-' }} · {{ r.shiftDate || '-' }}</text>
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
              <text class="salary-value">{{ beijingTime(r.checkInTime) }} / {{ beijingTime(r.checkOutTime) }}</text>
            </view>
            <view class="salary-info wide">
              <text class="salary-label">排班时间</text>
              <text class="salary-value">{{ timeRange(r) }}</text>
            </view>
          </view>

          <view class="action-row">
            <view v-if="canOperate(r) && isBalanceInsufficient()" class="action-btn insufficient" @click="navigateToBalance">余额不足，去充值</view>
            <view v-else-if="canOperate(r)" class="action-btn pay" @click="handlePay(r)">结算</view>
            <view v-else class="action-btn disabled">{{ settlementStatusLabel(r.settlementStatus) }}</view>
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
.balance-card { display: flex; align-items: center; justify-content: space-between; padding: 28rpx 24rpx; margin-bottom: 20rpx; border-radius: 24rpx; background: linear-gradient(135deg, #059669, #16a34a); color: #fff; box-shadow: 0 12rpx 30rpx rgba(22,163,74,.2); }
.balance-left { }
.balance-label { display: block; font-size: 22rpx; opacity: .82; }
.balance-amount { display: block; margin-top: 6rpx; font-size: 40rpx; font-weight: 850; line-height: 1; }
.balance-right { display: flex; align-items: center; gap: 6rpx; opacity: .9; }
.balance-action { font-size: 24rpx; font-weight: 700; }
.balance-arrow { font-size: 32rpx; line-height: 1; }
.summary-card { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin-bottom: 20rpx; }
.summary-item { padding: 24rpx; border-radius: 24rpx; background: #fff; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); }
.summary-value { display: block; font-size: 40rpx; font-weight: 850; color: #16a34a; line-height: 1; }
.summary-value.selected { color: #b45309; }
.summary-label { display: block; margin-top: 10rpx; font-size: 23rpx; color: #64748b; }
.filter-scroll { margin-bottom: 20rpx; white-space: nowrap; }
.filter-row { display: inline-flex; gap: 14rpx; padding-right: 8rpx; }
.filter-pill { display: inline-flex; align-items: center; justify-content: center; height: 62rpx; padding: 0 28rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 25rpx; font-weight: 800; box-shadow: 0 10rpx 24rpx rgba(23,83,53,.06); }
.filter-pill.active { background: #16a34a; color: #fff; box-shadow: 0 12rpx 28rpx rgba(22,163,74,.2); }
.salary-list { display: flex; flex-direction: column; gap: 20rpx; }
.operation-card { position: relative; overflow: hidden; }
.operation-card::before { content: ''; position: absolute; left: 0; top: 28rpx; bottom: 28rpx; width: 8rpx; border-radius: 0 999rpx 999rpx 0; background: linear-gradient(180deg, #18c86b, #047857); }
.salary-card { overflow: hidden; }
.title-wrap, .desc-wrap { white-space: normal; overflow: visible; text-overflow: clip; line-height: 1.35; }
.pill-gray { color: #64748b; background: #f1f5f9; }
.salary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin-top: 22rpx; }
.salary-info { min-width: 0; padding: 16rpx; border-radius: 18rpx; background: #f8fafc; }
.salary-info.wide { grid-column: span 2; }
.salary-label { display: block; font-size: 22rpx; color: #64748b; }
.salary-value { display: block; margin-top: 8rpx; font-size: 25rpx; font-weight: 750; color: #1f2933; white-space: normal; overflow: visible; text-overflow: clip; line-height: 1.35; word-break: break-all; }
.salary-value.money { color: #16a34a; font-size: 30rpx; font-weight: 850; }
.action-row { margin-top: 20rpx; }
.action-btn { height: 68rpx; line-height: 68rpx; border-radius: 999rpx; text-align: center; font-size: 26rpx; font-weight: 850; }
.action-btn.pay { background: #16a34a; color: #fff; }
.action-btn.insufficient { background: #fef3c7; color: #b45309; }
.action-btn.disabled { background: #f1f5f9; color: #98a3b3; }
.load-more-wrap { padding: 16rpx 0 32rpx; }
</style>
