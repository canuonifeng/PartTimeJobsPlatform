<template>
  <view class="task-orders-page">
    <view class="tabs-card">
      <view v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <uni-load-more v-if="loading && page === 1" status="loading" />

    <view v-if="filteredOrders.length === 0 && !loading" class="empty-state">
      <text class="empty-text">暂无任务单记录</text>
    </view>

    <view class="orders-list">
      <view v-for="order in filteredOrders" :key="order.id" class="order-card">
        <view class="order-header">
          <text class="order-title">{{ order.jobTitle }}</text>
          <text class="order-status" :class="statusClass(order.status)">{{ statusText(order.status) }}</text>
        </view>
        <view class="order-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: progressPercent(order) + '%' }"></view>
          </view>
          <text class="progress-text">{{ order.completedItems }}/{{ order.totalItems }}条</text>
        </view>
        <view class="order-footer">
          <text class="order-company">{{ order.companyName || '企业' }}</text>
          <text class="order-earnings">¥{{ money(order.earnings) }}</text>
        </view>
        <button class="platform-btn" @click="goAnnotationPlatform(order)">去 PC 端完成标注</button>
      </view>
    </view>

    <uni-load-more v-if="orders.length > 0" :status="moreStatus" />
  </view>
  <LoginSheet />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onMounted } from 'vue'
import { getTaskOrders } from '@/api/jobs'
import LoginSheet from '@/components/LoginSheet.vue'

interface TaskOrder {
  id: number | string
  jobId: number
  jobTitle: string
  companyName?: string
  status: string
  externalBatchId?: string
  totalItems: number
  completedItems: number
  earnings: number
  createdAt: string
}

const pageSize = 20
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const activeTab = ref('all')
const orders = ref<TaskOrder[]>([])

const ANNOTATION_PLATFORM_URL = 'https://annotation.example.com'

function goAnnotationPlatform(order: TaskOrder) {
  const batchId = order.externalBatchId || ''
  const url = batchId
    ? `${ANNOTATION_PLATFORM_URL}?batchId=${encodeURIComponent(batchId)}`
    : ANNOTATION_PLATFORM_URL
  // #ifdef H5
  window.open(url)
  // #endif
  // #ifndef H5
  uni.setClipboardData({
    data: url,
    success: () => uni.showToast({ title: '链接已复制，请在PC浏览器打开', icon: 'none' })
  })
  // #endif
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'IN_PROGRESS', label: '进行中' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'SETTLED', label: '已结算' }
]

const hasMore = computed(() => orders.value.length < total.value)
const moreStatus = computed(() => {
  if (loading.value || loadingMore.value) return 'loading'
  if (!hasMore.value && orders.value.length > 0) return 'noMore'
  return 'more'
})

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(item => item.status === activeTab.value)
})

function num(value: any) {
  const n = Number(value)
  return Number.isFinite(n) ? n : 0
}

function money(value: any) {
  const n = num(value)
  return n % 1 === 0 ? String(n) : n.toFixed(2)
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    SETTLED: '已结算',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

function statusClass(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'pending',
    IN_PROGRESS: 'pending',
    COMPLETED: 'success',
    SETTLED: 'success',
    CANCELLED: 'failed'
  }
  return map[status] || ''
}

function progressPercent(order: TaskOrder): number {
  const total = num(order.totalItems)
  const completed = num(order.completedItems)
  if (total <= 0) return 0
  return Math.min(100, Math.round((completed / total) * 100))
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

function mapOrder(item: any): TaskOrder {
  return {
    id: item?.id || `order-${Date.now()}`,
    jobId: item?.jobId || 0,
    jobTitle: item?.jobTitle || item?.title || '标注任务',
    companyName: item?.companyName || '',
    status: String(item?.status || 'IN_PROGRESS').toUpperCase(),
    externalBatchId: item?.externalBatchId || '',
    totalItems: num(item?.totalItems || item?.itemsTotal),
    completedItems: num(item?.completedItems || item?.itemsCompleted),
    earnings: num(item?.earnings || item?.amount || item?.payablePay),
    createdAt: item?.createdAt || item?.createTime || ''
  }
}

async function loadOrders(p: number, append: boolean) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res: any = await getTaskOrders({ page: p, pageSize })
    const list = normalizeRecords(res).map(mapOrder)
    if (append) {
      orders.value.push(...list)
      total.value = normalizeTotal(res, orders.value.length)
    } else {
      orders.value = list
      total.value = normalizeTotal(res, list.length)
    }
  } catch {
    if (!append) orders.value = []
    total.value = 0
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  await loadOrders(page.value, true)
}

onMounted(() => {
  loadOrders(1, false)
})
</script>

<style scoped>
.task-orders-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f7f8fa;
  box-sizing: border-box;
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
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 600;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.order-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a2e;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}

.order-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.order-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.order-status.success {
  background: #f6ffed;
  color: #52c41a;
}

.order-status.failed {
  background: #fff1f0;
  color: #f5222d;
}

.order-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 24rpx;
  color: #666;
  font-weight: 600;
  flex-shrink: 0;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-company {
  font-size: 24rpx;
  color: #999;
}

.order-earnings {
  font-size: 32rpx;
  font-weight: 800;
  color: #ff6b35;
}

.platform-btn {
  margin-top: 20rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 14rpx;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 40%, #2563eb 100%);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  padding: 0;
}

.platform-btn::after {
  border: none;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
