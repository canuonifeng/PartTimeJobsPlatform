<template>
  <view class="signup-page">
    <view class="header-card">
      <text class="header-title">我的报名</text>
      <text class="header-sub">查看已提交的岗位报名与审核状态</text>
    </view>

    <uni-load-more v-if="loading && page === 1" status="loading" />

    <scroll-view class="signup-scroll" scroll-y :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh" @scrolltolower="loadMore">
      <view v-if="signups.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无报名记录</text>
      </view>

      <view v-for="item in signups" :key="item.applicationId || `${item.scheduleId}-${item.appliedAt}`" class="signup-card">
        <view class="card-header">
          <view class="job-info">
            <text class="job-title">{{ item.jobTitle || '岗位' }}</text>
            <text class="company-name">{{ item.companyName || '企业' }}</text>
          </view>
          <text class="status-badge" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>
        <view class="detail-row"><text class="detail-label">工作日期</text><text class="detail-value">{{ item.workDate || '-' }}</text></view>
        <view class="detail-row"><text class="detail-label">工作时间</text><text class="detail-value">{{ timeRange(item) }}</text></view>
        <view class="detail-row"><text class="detail-label">工作地点</text><text class="detail-value">{{ item.location || '暂无地点' }}</text></view>
        <view class="detail-row"><text class="detail-label">薪资</text><text class="detail-value pay">{{ payText(item) }}</text></view>
        <text class="applied-time">报名时间：{{ formatDateTime(item.appliedAt) }}</text>
      </view>

      <uni-load-more v-if="signups.length > 0" :status="moreStatus" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getMySignups } from '@/api/jobs'

type Signup = {
  applicationId?: number
  jobId?: number
  scheduleId?: number
  jobTitle?: string
  companyName?: string
  status?: string
  workDate?: string
  startTime?: string
  endTime?: string
  location?: string
  payAmount?: number | string
  payType?: string
  appliedAt?: string
}

const pageSize = 10
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const signups = ref<Signup[]>([])
const hasMore = computed(() => signups.value.length < total.value)
const moreStatus = computed(() => loadingMore.value ? 'loading' : (hasMore.value ? 'more' : 'noMore'))

function recordsOf(res: any): Signup[] {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.records)) return res.records
  if (Array.isArray(res?.list)) return res.list
  return []
}

function totalOf(res: any, listLength: number) {
  const value = Number(res?.total ?? res?.count ?? listLength)
  return Number.isFinite(value) ? value : listLength
}

function normalizeTime(value: any) {
  const text = String(value || '')
  if (!text) return ''
  if (text.includes('T') || text.includes(' ')) return text.slice(11, 16)
  return text.slice(0, 5)
}

function timeRange(item: Signup) {
  const start = normalizeTime(item.startTime)
  const end = normalizeTime(item.endTime)
  return start && end ? `${start} - ${end}` : '-'
}

function formatDateTime(value: any) {
  const text = String(value || '')
  return text ? text.replace('T', ' ').slice(0, 16) : '-'
}

function statusText(status: any) {
  const map: Record<string, string> = { PENDING: '待审核', ACCEPTED: '已通过', APPROVED: '已通过', REJECTED: '未通过', CANCELLED: '已取消' }
  const key = String(status || '').toUpperCase()
  return map[key] || '待审核'
}

function statusClass(status: any) {
  const key = String(status || '').toUpperCase()
  if (['ACCEPTED', 'APPROVED'].includes(key)) return 'success'
  if (key === 'REJECTED') return 'danger'
  if (key === 'CANCELLED') return 'muted'
  return 'pending'
}

function payText(item: Signup) {
  const amount = item.payAmount == null ? '' : String(item.payAmount)
  const unitMap: Record<string, string> = { HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' }
  const unit = unitMap[String(item.payType || '').toUpperCase()] || '小时'
  return amount ? `${amount}元/${unit}` : '-'
}

async function fetchSignups(targetPage: number, append = false) {
  if (append) loadingMore.value = true
  else loading.value = true
  try {
    const res = await getMySignups({ page: targetPage, pageSize })
    const list = recordsOf(res)
    total.value = totalOf(res, append ? signups.value.length + list.length : list.length)
    signups.value = append ? [...signups.value, ...list] : list
  } catch (err: any) {
    if (!append) signups.value = []
    total.value = signups.value.length
    uni.showToast({ title: err?.message || '报名记录加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
    refreshing.value = false
  }
}

function onRefresh() {
  refreshing.value = true
  page.value = 1
  fetchSignups(1)
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value += 1
  fetchSignups(page.value, true)
}

onMounted(() => fetchSignups(1))
</script>

<style scoped>
.signup-page { display: flex; flex-direction: column; min-height: 100vh; background: #f6f7fb; }
.header-card { padding: 56rpx 32rpx 36rpx; background: linear-gradient(135deg, #19c876 0%, #08a657 100%); color: #fff; }
.header-title { display: block; font-size: 44rpx; font-weight: 800; }
.header-sub { display: block; margin-top: 12rpx; font-size: 26rpx; opacity: 0.86; }
.signup-scroll { flex: 1; padding: 24rpx; box-sizing: border-box; }
.signup-card { margin-bottom: 22rpx; padding: 28rpx; border-radius: 24rpx; background: #fff; box-shadow: 0 8rpx 24rpx rgba(43, 72, 99, 0.06); }
.card-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 22rpx; }
.job-info { flex: 1; min-width: 0; }
.job-title { display: block; font-size: 34rpx; line-height: 44rpx; font-weight: 700; color: #1f2d3d; }
.company-name { display: block; margin-top: 8rpx; font-size: 24rpx; color: #7b8794; }
.status-badge { margin-left: 18rpx; padding: 8rpx 18rpx; border-radius: 24rpx; font-size: 22rpx; white-space: nowrap; }
.status-badge.pending { background: #fff7e6; color: #fa8c16; }
.status-badge.success { background: #f0fbf4; color: #07c160; }
.status-badge.danger { background: #fff1f0; color: #f5222d; }
.status-badge.muted { background: #eef1f5; color: #8b98a7; }
.detail-row { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid #f1f3f5; }
.detail-label { font-size: 26rpx; color: #8b98a7; }
.detail-value { max-width: 460rpx; font-size: 26rpx; color: #1f2d3d; text-align: right; }
.detail-value.pay { color: #ff6a00; font-weight: 700; }
.applied-time { display: block; margin-top: 18rpx; font-size: 24rpx; color: #98a2b3; }
.empty-state { padding: 120rpx 0; text-align: center; }
.empty-text { font-size: 28rpx; color: #98a2b3; }
</style>
