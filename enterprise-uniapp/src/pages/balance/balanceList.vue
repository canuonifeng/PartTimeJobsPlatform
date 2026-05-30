<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/api/request'
import { getBalance, getTransactions } from '@/api/balance'

const balanceInfo = ref({
  balance: 0,
  creditLimit: 0,
  usableBalance: 0,
  totalTopUp: 0,
  totalSpent: 0
})
const transactions = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

async function fetchBalance() {
  try {
    balanceInfo.value = await getBalance(request)
  } catch {}
}

async function fetchTransactions() {
  try {
    const res = await getTransactions(request, { page: page.value, pageSize: pageSize.value })
    transactions.value = Array.isArray(res) ? res : (res.records || [])
    total.value = res.total || 0
  } catch {}
}

onShow(() => {
  page.value = 1
  fetchBalance()
  fetchTransactions()
})

function formatAmount(amount) {
  const prefix = amount > 0 ? '+' : ''
  return prefix + amount + ' 元'
}

function amountColor(amount) {
  return amount > 0 ? '#67c23a' : '#f56c6c'
}

function typeLabel(type) {
  const map = { TOP_UP: '充值', SETTLEMENT: '结算支出', SETTLEMENT_REFUND: '结算退款' }
  return map[type] || type
}
</script>

<template>
  <view class="page">
    <view class="balance-cards">
      <view class="card-row">
        <view class="card">
          <text class="card-label">账户余额</text>
          <text class="card-value primary">{{ balanceInfo.balance }}</text>
        </view>
        <view class="card">
          <text class="card-label">信用额度</text>
          <text class="card-value success">{{ balanceInfo.creditLimit }}</text>
        </view>
      </view>
      <view class="card-row">
        <view class="card">
          <text class="card-label">可用额度</text>
          <text class="card-value warning">{{ balanceInfo.usableBalance }}</text>
        </view>
        <view class="card">
          <text class="card-label">累计充值</text>
          <text class="card-value info">{{ balanceInfo.totalTopUp }}</text>
        </view>
      </view>
    </view>

    <view class="section-title">账户流水</view>

    <view v-for="item in transactions" :key="item.id" class="txn-item">
      <view class="txn-left">
        <text class="txn-type">{{ typeLabel(item.type) }}</text>
        <text class="txn-desc">{{ item.description || '-' }}</text>
        <text class="txn-time">{{ item.createdAt }}</text>
      </view>
      <text class="txn-amount" :style="{ color: amountColor(item.amount) }">{{ formatAmount(item.amount) }}</text>
    </view>

    <view v-if="total > pageSize && page * pageSize < total" class="load-more" @click="page++; fetchTransactions()">
      <text>加载更多</text>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.balance-cards { padding: 16rpx 0; }
.card-row { display: flex; gap: 16rpx; margin-bottom: 16rpx; }
.card { flex: 1; background: #fff; border-radius: 16rpx; padding: 32rpx 24rpx; text-align: center; }
.card-label { font-size: 26rpx; color: #999; display: block; }
.card-value { font-size: 40rpx; font-weight: 600; margin-top: 8rpx; display: block; }
.primary { color: #409eff; }
.success { color: #67c23a; }
.warning { color: #e6a23c; }
.info { color: #909399; }
.section-title { font-size: 28rpx; color: #666; padding: 16rpx 0 8rpx; }
.txn-item { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 8rpx; display: flex; justify-content: space-between; align-items: center; }
.txn-left { flex: 1; }
.txn-type { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.txn-desc { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.txn-time { font-size: 22rpx; color: #ccc; margin-top: 4rpx; display: block; }
.txn-amount { font-size: 32rpx; font-weight: 600; }
.load-more { text-align: center; padding: 24rpx; color: #409eff; font-size: 26rpx; }
</style>
