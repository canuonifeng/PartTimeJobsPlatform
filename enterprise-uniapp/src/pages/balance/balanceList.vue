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
  return amount > 0 ? '#07c160' : '#ff3b30'
}

function typeLabel(type) {
  const map = { TOP_UP: '充值', SETTLEMENT: '结算支出', SETTLEMENT_REFUND: '结算退款' }
  return map[type] || type
}
</script>

<template>
  <view class="page">
    <view class="header-gradient">
      <text class="header-title">企业资金</text>
    </view>
    <view class="balance-cards">
      <view class="card-row">
        <view class="card balance-card">
          <text class="card-label">账户余额</text>
          <text class="card-value primary">{{ balanceInfo.balance }}</text>
        </view>
        <view class="card">
          <text class="card-label">信用额度</text>
          <text class="card-value">{{ balanceInfo.creditLimit }}</text>
        </view>
      </view>
      <view class="card-row">
        <view class="card">
          <text class="card-label">可用额度</text>
          <text class="card-value">{{ balanceInfo.usableBalance }}</text>
        </view>
        <view class="card">
          <text class="card-label">累计充值</text>
          <text class="card-value">{{ balanceInfo.totalTopUp }}</text>
        </view>
      </view>
    </view>

    <view class="section-title-row">
      <view class="section-accent"></view>
      <text class="section-title">账户流水</text>
    </view>

    <view class="txn-list">
      <view v-for="item in transactions" :key="item.id" class="txn-item">
        <view class="txn-left">
          <text class="txn-type">{{ typeLabel(item.type) }}</text>
          <text class="txn-desc">{{ item.description || '-' }}</text>
          <text class="txn-time">{{ item.createdAt }}</text>
        </view>
        <text class="txn-amount" :style="{ color: amountColor(item.amount) }">{{ formatAmount(item.amount) }}</text>
      </view>
    </view>

    <view v-if="total > pageSize && page * pageSize < total" class="load-more" @click="page++; fetchTransactions()">
      <text>加载更多</text>
    </view>
  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f6f8f7;
}
.header-gradient {
  background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%);
  padding: 48rpx 32rpx 28rpx;
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}
.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}
.balance-cards {
  margin: 24rpx 32rpx 0;
}
.card-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.card {
  flex: 1;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx 24rpx;
  text-align: center;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}
.card-label {
  font-size: 26rpx;
  color: #64748b;
  display: block;
}
.card-value {
  font-size: 40rpx;
  font-weight: 600;
  margin-top: 8rpx;
  display: block;
  color: #1f2933;
}
.card-value.primary {
  color: #07c160;
}
.section-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 0 32rpx;
}
.section-accent {
  width: 3rpx;
  height: 28rpx;
  background: #07c160;
  border-radius: 2rpx;
  margin-right: 12rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2933;
}
.txn-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 0 32rpx;
}
.txn-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}
.txn-left {
  flex: 1;
}
.txn-type {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2933;
  display: block;
}
.txn-desc {
  font-size: 24rpx;
  color: #64748b;
  margin-top: 4rpx;
  display: block;
}
.txn-time {
  font-size: 22rpx;
  color: #ccc;
  margin-top: 4rpx;
  display: block;
}
.txn-amount {
  font-size: 32rpx;
  font-weight: 600;
}
.load-more {
  text-align: center;
  padding: 24rpx 32rpx;
  color: #07c160;
  font-size: 26rpx;
}
</style>
