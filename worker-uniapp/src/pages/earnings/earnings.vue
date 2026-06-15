<template>
  <view class="earnings-page">
    <view class="summary-card">
      <view class="summary-top">
        <view>
          <text class="summary-label">累计收入</text>
          <text class="summary-amount">¥{{ money(summary.totalEarnings) }}</text>
        </view>
        <button class="withdraw-btn" @click="navTo('/pages/earnings/withdraw')">提现</button>
      </view>
      <view class="summary-details">
        <view class="detail-item">
          <text class="detail-num">{{ money(summary.monthEarnings) }}</text>
          <text class="detail-label">本月收入</text>
        </view>
        <view class="detail-item">
          <text class="detail-num">{{ money(summary.pendingSettlement) }}</text>
          <text class="detail-label">待结算</text>
        </view>
        <view class="detail-item">
          <text class="detail-num">{{ money(summary.withdrawnAmount) }}</text>
          <text class="detail-label">已提现</text>
        </view>
      </view>
    </view>

    <view class="tabs-card">
      <view v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">收入明细</text>
        <text class="section-count">{{ filteredTransactions.length }}笔</text>
      </view>

      <uni-load-more v-if="loading && page === 1" status="loading" />

      <view v-if="groupedTransactions.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无明细记录</text>
      </view>

      <view v-for="group in groupedTransactions" :key="group.date" class="date-group">
        <view class="date-row">
          <text class="date-title">{{ group.date }}</text>
          <text class="date-count">{{ group.items.length }}笔</text>
        </view>
        <view v-for="item in group.items" :key="item.id" class="transaction-card">
          <view class="tx-icon" :class="item.type"><text>{{ item.type === 'referral' ? '邀' : item.type === 'withdrawal' ? '提' : '收' }}</text></view>
          <view class="tx-left">
            <text class="tx-title">{{ item.title }}</text>
            <template v-if="item.type === 'earning'">
              <text class="tx-date">{{ item.workTime }}</text>
              <text class="tx-location">{{ item.location }}</text>
              <text v-if="item.settlementTime" class="tx-settlement">结算时间：{{ item.settlementTime }}</text>
            </template>
            <text v-else class="tx-date">{{ item.subtitle || item.time || item.createdAt }}</text>
          </view>
          <view class="tx-right">
            <text class="tx-amount" :class="item.type">{{ item.type === 'withdrawal' ? '-' : '+' }}{{ money(item.amount) }}</text>
            <text class="tx-status" :class="item.statusClass">{{ item.status }}</text>
          </view>
        </view>
      </view>

      <uni-load-more v-if="transactions.length > 0" :status="moreStatus" />
    </view>
  </view>
  <LoginSheet />
  <InviteFloat />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { getMyAttendance } from '@/api/attendance'
import { getEarningsSummary, getEarningsTransactions, getReferralRewards } from '@/api/earnings'
import InviteFloat from '@/components/InviteFloat.vue'
import LoginSheet from '@/components/LoginSheet.vue'

interface Summary {
  totalEarnings: number
  monthEarnings: number
  pendingSettlement: number
  withdrawnAmount: number
  availableBalance: number
}

interface Transaction {
  id: number | string
  type: string
  amount: number
  status: string
  statusClass: string
  filterStatus: string
  createdAt: string
  date: string
  time: string
  title: string
  subtitle?: string
  location?: string
  workTime?: string
  settlementTime?: string
  sortAt: string
}

const referralTransactions = ref<Transaction[]>([])

const pageSize = 20
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const activeTab = ref('all')
const summary = ref<Summary>({
  totalEarnings: 0,
  monthEarnings: 0,
  pendingSettlement: 0,
  withdrawnAmount: 0,
  availableBalance: 0
})
const transactions = ref<Transaction[]>([])
const pendingAttendanceTransactions = ref<Transaction[]>([])

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'settled', label: '已结算' },
  { key: 'pending', label: '待结算' },
  { key: 'withdrawn', label: '已提现' }
]

const hasMore = computed(() => transactions.value.length < total.value)
const moreStatus = computed(() => {
  if (loading.value || loadingMore.value) return 'loading'
  if (!hasMore.value && transactions.value.length > 0) return 'noMore'
  return 'more'
})
const filteredTransactions = computed(() => {
  const list = activeTab.value === 'all' ? transactions.value : transactions.value.filter(item => item.filterStatus === activeTab.value)
  return [...list].sort((a, b) => b.sortAt.localeCompare(a.sortAt))
})
const groupedTransactions = computed(() => {
  const groups: { date: string; items: Transaction[] }[] = []
  filteredTransactions.value.forEach(item => {
    let group = groups.find(g => g.date === item.date)
    if (!group) {
      group = { date: item.date, items: [] }
      groups.push(group)
    }
    group.items.push(item)
  })
  return groups
})
function navTo(url: string) {
  uni.navigateTo({ url })
}

function num(value: any) {
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

function money(value: any) {
  const n = num(value)
  return n % 1 === 0 ? String(n) : n.toFixed(2)
}

function normalizeDate(value: any) {
  const text = String(value || '')
  return text ? text.slice(0, 10) : '今日'
}

function normalizeTime(value: any) {
  const text = String(value || '')
  return text.length >= 16 ? text.slice(11, 16) : ''
}

function workLocation(item: any) {
  return item?.location || item?.workLocation || item?.jobLocation || '暂无地点'
}

function workTimeText(item: any) {
  const date = item?.shiftDate || item?.date
  const start = normalizeTime(item?.startTime)
  const end = normalizeTime(item?.endTime)
  const parts = []
  if (date) parts.push(String(date).slice(0, 10))
  if (start && end) parts.push(`${start}-${end}`)
  return parts.join(' ')
}

function settlementFilter(status: any) {
  const rawStatus = String(status || '').toUpperCase()
  if (rawStatus.includes('UNPAID') || rawStatus.includes('PENDING') || rawStatus.includes('WAIT') || rawStatus.includes('PROCESS')) return 'pending'
  return 'settled'
}

function mapTx(item: any): Transaction {
  const rawType = String(item?.type || item?.transactionType || '').toUpperCase()
  const isWithdrawal = rawType.includes('WITHDRAW')
  const createdAt = item?.createdAt || item?.createTime || item?.time || new Date().toISOString().replace('T', ' ').slice(0, 19)
  const filterStatus = isWithdrawal ? 'withdrawn' : settlementFilter(item?.settlementStatus || item?.status)
  const statusMap: Record<string, string> = { withdrawn: '已提现', pending: '待结算', settled: '已结算' }
  const shiftDate = item?.shiftDate || item?.date || createdAt
  const startTime = normalizeTime(item?.startTime)
  const sortAt = isWithdrawal ? createdAt : `${normalizeDate(shiftDate)} ${startTime || normalizeTime(createdAt) || '00:00'}:00`
  return {
    id: item?.id || `${rawType || 'tx'}-${createdAt}-${item?.amount || 0}`,
    type: isWithdrawal ? 'withdrawal' : 'earning',
    amount: Math.abs(num(item?.amount || item?.payablePay || item?.scheduledPay || item?.money || item?.value)),
    status: statusMap[filterStatus],
    statusClass: filterStatus === 'pending' ? 'pending' : 'success',
    filterStatus,
    createdAt,
    date: normalizeDate(isWithdrawal ? createdAt : shiftDate),
    time: normalizeTime(createdAt),
    title: isWithdrawal ? (item?.title || item?.description || '余额提现') : (item?.jobTitle || item?.positionName || item?.jobName || '工作收入'),
    subtitle: isWithdrawal ? normalizeTime(createdAt) : undefined,
    location: isWithdrawal ? undefined : workLocation(item),
    workTime: isWithdrawal ? undefined : workTimeText(item),
    settlementTime: !isWithdrawal && filterStatus === 'settled' ? createdAt : undefined,
    sortAt
  }
}

function mapAttendanceTx(item: any): Transaction | null {
  const amount = num(item?.payablePay || item?.scheduledPay || item?.earning || item?.amount || item?.salary)
  if (amount <= 0) return null
  const tx = mapTx({ ...item, id: `attendance-${item?.attendanceId || item?.id || item?.shiftId}`, type: 'EARNINGS', amount, settlementStatus: item?.settlementStatus || 'UNPAID' })
  tx.filterStatus = 'pending'
  tx.status = '待结算'
  tx.statusClass = 'pending'
  return tx
}

function normalizeRecords(res: any) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.records)) return res.records
  if (Array.isArray(res?.list)) return res.list
  if (Array.isArray(res?.data)) return res.data
  return []
}

function normalizeTotal(res: any, listLength: number) {
  return num(res?.total || res?.totalCount || res?.count || listLength)
}

function mapReferralReward(item: any): Transaction {
  const status = String(item?.status || '').toUpperCase()
  const isGranted = status === 'GRANTED'
  const createdAt = item?.createdAt || item?.createTime || new Date().toISOString().replace('T', ' ').slice(0, 19)
  return {
    id: `referral-${item?.id || createdAt}`,
    type: 'referral',
    amount: Math.abs(num(item?.rewardAmount || item?.amount || 0)),
    status: isGranted ? '已发放' : (status === 'PENDING' || status === 'AUDITING') ? '待审核' : '审核中',
    statusClass: isGranted ? 'success' : 'pending',
    filterStatus: isGranted ? 'settled' : 'pending',
    createdAt,
    date: normalizeDate(createdAt),
    time: normalizeTime(createdAt),
    title: item?.refereeName ? `${item.refereeName} 邀请奖励` : '邀请好友奖励',
    subtitle: item?.refereePhone || undefined,
    sortAt: createdAt
  }
}

async function loadReferralRewards() {
  try {
    const res: any = await getReferralRewards({ page: 1, pageSize: 100 })
    const list = normalizeRecords(res).map(mapReferralReward)
    referralTransactions.value = list
  } catch {
    referralTransactions.value = []
  }
}

async function loadTxPage(p: number, append: boolean) {
  const res: any = await getEarningsTransactions({ page: p, pageSize })
  const list = normalizeRecords(res).map(mapTx)
    if (append) {
      transactions.value.push(...list)
      total.value = normalizeTotal(res, transactions.value.length)
    } else {
      transactions.value = [...list, ...pendingAttendanceTransactions.value, ...referralTransactions.value]
      total.value = normalizeTotal(res, list.length) + pendingAttendanceTransactions.value.length + referralTransactions.value.length
    }
}

async function loadData() {
  loading.value = true
  page.value = 1
  try {
    const earningsRes = await getEarningsSummary()
    const earned = num(earningsRes?.totalEarned ?? earningsRes?.totalEarnings ?? earningsRes?.totalIncome)
    const pending = num(earningsRes?.pendingSettlement ?? earningsRes?.pendingEarnings ?? earningsRes?.pendingAmount)
    const available = num(earningsRes?.pendingWithdrawal ?? earningsRes?.availableBalance ?? earningsRes?.balance)
    const withdrawn = num(earningsRes?.withdrawnAmount ?? earningsRes?.totalWithdrawn ?? earningsRes?.withdrawn)

    summary.value = {
      totalEarnings: earned,
      monthEarnings: num(earningsRes?.monthEarnings ?? earningsRes?.monthlyEarnings ?? earningsRes?.currentMonthEarnings),
      pendingSettlement: pending || available,
      withdrawnAmount: withdrawn,
      availableBalance: available
    }

    try {
      const attendanceRes = await getMyAttendance({ page: 1, pageSize: 100 })
      const attrs = normalizeRecords(attendanceRes)
      if (Array.isArray(attrs) && attrs.length > 0) {
        pendingAttendanceTransactions.value = attrs
          .filter((item: any) => settlementFilter(item?.settlementStatus) === 'pending')
          .map(mapAttendanceTx)
          .filter(Boolean) as Transaction[]
        if (summary.value.monthEarnings === 0) {
          const month = new Date().toISOString().slice(0, 7)
          summary.value.monthEarnings = attrs.reduce((sum: number, item: any) => {
            const date = String(item?.shiftDate || item?.date || item?.createdAt || item?.checkInTime || '')
            return date.slice(0, 7) === month ? sum + num(item?.payablePay ?? item?.scheduledPay ?? item?.earning ?? item?.amount ?? item?.salary) : sum
          }, 0)
        }
      } else {
        pendingAttendanceTransactions.value = []
      }
    } catch {
      pendingAttendanceTransactions.value = []
    }

    await loadTxPage(1, false)
    await loadReferralRewards()
    transactions.value = [...transactions.value.filter((t: any) => t.type !== 'referral'), ...referralTransactions.value]
    total.value = transactions.value.length
  } catch {
    summary.value = { totalEarnings: 0, monthEarnings: 0, pendingSettlement: 0, withdrawnAmount: 0, availableBalance: 0 }
    transactions.value = []
    pendingAttendanceTransactions.value = []
    total.value = 0
    uni.showToast({ title: '收入明细加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  page.value++
  try {
    await loadTxPage(page.value, true)
  } catch {
    page.value--
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingMore.value = false
  }
}

onReachBottom(loadMore)

onMounted(loadData)
onMounted(() => { uni.$on('earningsRefresh', loadData) })
onUnmounted(() => { uni.$off('earningsRefresh', loadData) })
</script>

<style scoped>
.earnings-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}

.summary-card {
  background: linear-gradient(135deg, #ff9f2d, #ff6a00);
  border-radius: 28rpx;
  padding: 36rpx 30rpx 30rpx;
  margin-bottom: 24rpx;
  color: #fff;
  box-shadow: 0 12rpx 30rpx rgba(255, 106, 0, 0.18);
}

.summary-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 34rpx;
}

.summary-label {
  font-size: 26rpx;
  opacity: 0.86;
  margin-bottom: 14rpx;
  display: block;
}

.summary-amount {
  font-size: 68rpx;
  line-height: 76rpx;
  font-weight: 700;
  display: block;
}

.summary-details {
  display: flex;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 20rpx;
  padding: 24rpx 0;
}

.detail-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1rpx solid rgba(255, 255, 255, 0.24);
}

.detail-item:last-child {
  border-right: none;
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
  width: 142rpx;
  height: 62rpx;
  line-height: 62rpx;
  background: #fff;
  border-radius: 34rpx;
  font-size: 28rpx;
  color: #ff6a00;
  margin: 0;
  padding: 0;
}

.tabs-card {
  display: flex;
  background: #fff;
  border-radius: 18rpx;
  padding: 10rpx;
  margin-bottom: 24rpx;
}

.tab-item {
  flex: 1;
  height: 62rpx;
  line-height: 62rpx;
  text-align: center;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
}

.tab-item.active {
  background: #fff3e8;
  color: #ff6a00;
  font-weight: 600;
}

.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #222;
}

.section-count,
.date-count {
  font-size: 24rpx;
  color: #999;
}

.date-group {
  margin-bottom: 16rpx;
}

.date-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 0 8rpx;
}

.date-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
}

.data-notice {
  padding: 18rpx 22rpx;
  margin-bottom: 16rpx;
  border-radius: 12rpx;
  background: #fff7e6;
  color: #d46b08;
  font-size: 24rpx;
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
  border-bottom: 1rpx solid #f1f1f1;
}

.tx-icon {
  width: 58rpx;
  height: 58rpx;
  line-height: 58rpx;
  text-align: center;
  border-radius: 50%;
  font-size: 24rpx;
  margin-right: 18rpx;
}

.tx-icon.earning {
  color: #ff6a00;
  background: #fff3e8;
}

.tx-icon.withdrawal {
  color: #2f80ed;
  background: #eaf3ff;
}

.tx-icon.referral {
  color: #8b5cf6;
  background: #f3e8ff;
}

.transaction-card:last-child {
  border-bottom: none;
}

.tx-left {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tx-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 6rpx;
}

.tx-date,
.tx-location {
  font-size: 22rpx;
  color: #999;
  line-height: 34rpx;
}

.tx-location {
  margin-top: 4rpx;
}

.tx-settlement {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #bbb;
  line-height: 34rpx;
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
  color: #ff6a00;
}

.tx-amount.withdrawal {
  color: #2f80ed;
}

.tx-amount.referral {
  color: #8b5cf6;
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
