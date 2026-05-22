<template>
  <view class="earnings-page">
    <view class="summary-card">
      <text class="summary-label">累计收入</text>
      <text class="summary-amount">{{ summary.totalEarnings || 0 }}<text class="unit">元</text></text>
      <view class="summary-details">
        <view class="detail-item">
          <text class="detail-num">{{ summary.completedShifts || 0 }}</text>
          <text class="detail-label">已完成班次</text>
        </view>
        <view class="detail-item">
          <text class="detail-num">{{ summary.totalHours || 0 }}</text>
          <text class="detail-label">总工时(小时)</text>
        </view>
        <view class="detail-item">
          <text class="detail-num">{{ summary.availableBalance || 0 }}</text>
          <text class="detail-label">可提现(元)</text>
        </view>
      </view>
      <button class="withdraw-btn" @click="navTo('/pages/earnings/withdraw')">
        提现
      </button>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">交易记录</text>
      </view>

      <uni-load-more v-if="loading" status="loading" />

      <view v-if="transactions.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无交易记录</text>
      </view>

      <view v-for="item in transactions" :key="item.id" class="transaction-card">
        <view class="tx-left">
          <text class="tx-title">{{ item.type === 'earning' ? '工作收入' : '提现' }}</text>
          <text class="tx-date">{{ item.createdAt }}</text>
        </view>
        <view class="tx-right">
          <text class="tx-amount" :class="item.type">{{ item.type === 'earning' ? '+' : '-' }}{{ item.amount }}元</text>
          <text class="tx-status" :class="item.statusClass">{{ item.status }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getMyAttendance } from '@/api/attendance'
import { getEarningsSummary, getEarningsTransactions } from '@/api/earnings'

interface Transaction {
  id: number
  type: string
  amount: number
  status: string
  statusClass: string
  createdAt: string
}

const loading = ref(false)
const summary = ref({
  totalEarnings: 0,
  completedShifts: 0,
  totalHours: 0,
  availableBalance: 0
})
const transactions = ref<Transaction[]>([])

const statusMap: Record<string, { text: string; cls: string }> = {
  CHECKED_OUT: { text: '已完成', cls: 'success' },
  CHECKED_IN: { text: '已签到', cls: 'pending' },
  COMPLETED: { text: '已提现', cls: 'success' },
  PROCESSING: { text: '处理中', cls: 'pending' },
  PENDING: { text: '待处理', cls: 'pending' },
  FAILED: { text: '失败', cls: 'failed' }
}

function getTxStatus(item: { status: string }): { text: string; cls: string } {
  return statusMap[item.status] || { text: item.status, cls: '' }
}

function navTo(url: string) {
  uni.navigateTo({ url })
}

async function loadData() {
  loading.value = true
  try {
    const [attendanceRes, earningsRes, txRes] = await Promise.all([
      getMyAttendance(),
      getEarningsSummary(),
      getEarningsTransactions()
    ])

    const attrs = Array.isArray(attendanceRes) ? attendanceRes : (attendanceRes.list || [])

    summary.value = {
      totalEarnings: earningsRes.totalEarned || 0,
      completedShifts: attrs.filter((r: any) => r.status === 'CHECKED_OUT').length,
      totalHours: attrs.reduce((s: number, r: any) => s + (r.totalHours || 0), 0),
      availableBalance: earningsRes.pendingWithdrawal || 0
    }

    const txTypeMap: Record<string, { type: string; status: string; cls: string }> = {
      EARNINGS: { type: 'earning', status: '已完成', cls: 'success' },
      WITHDRAWAL: { type: 'withdrawal', status: '已提现', cls: 'success' },
      REFUND: { type: 'refund', status: '已撤回', cls: 'failed' }
    }
    const txList: Transaction[] = (Array.isArray(txRes) ? txRes : []).map((item: any) => {
      const map = txTypeMap[item.type] || { type: 'earning', status: item.type, cls: '' }
      return {
        id: item.id,
        type: map.type,
        amount: Math.abs(item.amount || 0),
        status: map.status,
        statusClass: map.cls,
        createdAt: item.createdAt
      }
    })
    txList.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    transactions.value = txList
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
onMounted(() => { uni.$on('earningsRefresh', loadData) })
onUnmounted(() => { uni.$off('earningsRefresh', loadData) })
</script>

<style scoped>
.earnings-page {
  padding: 30rpx;
}

.summary-card {
  background: linear-gradient(135deg, #07c160, #06ad56);
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 30rpx;
  color: #fff;
}

.summary-label {
  font-size: 26rpx;
  opacity: 0.8;
  margin-bottom: 12rpx;
  display: block;
}

.summary-amount {
  font-size: 64rpx;
  font-weight: 700;
  margin-bottom: 30rpx;
  display: block;
}

.unit {
  font-size: 28rpx;
  font-weight: 400;
  opacity: 0.8;
  margin-left: 8rpx;
}

.summary-details {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
  margin-bottom: 24rpx;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.detail-num {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 6rpx;
}

.detail-label {
  font-size: 22rpx;
  opacity: 0.8;
}

.withdraw-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 40rpx;
  font-size: 30rpx;
  color: #fff;
  margin: 0;
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 60rpx 0;
}

.empty-text {
  font-size: 26rpx;
  color: #ccc;
}

.transaction-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.transaction-card:last-child {
  border-bottom: none;
}

.tx-left {
  display: flex;
  flex-direction: column;
}

.tx-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 6rpx;
}

.tx-date {
  font-size: 22rpx;
  color: #ccc;
}

.tx-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.tx-amount {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 6rpx;
}

.tx-amount.earning {
  color: #07c160;
}

.tx-amount.withdrawal {
  color: #f60;
}

.tx-amount.refund {
  color: #f60;
}

.tx-status {
  font-size: 22rpx;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.tx-status.pending {
  color: #fa8c16;
  background: #fff7e6;
}

.tx-status.success {
  color: #52c41a;
  background: #f6ffed;
}

.tx-status.failed {
  color: #f5222d;
  background: #fff1f0;
}
</style>
