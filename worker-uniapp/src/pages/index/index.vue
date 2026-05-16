<template>
  <view class="home-page">
    <view class="header-banner">
      <view class="greeting">
        <text class="greeting-text">{{ greeting }},</text>
        <text class="user-name">{{ authStore.workerInfo?.name || '工人' }}</text>
      </view>
      <text class="sub-text">{{ todayLabel }} · {{ todayWeekday }} · {{ todayShiftSummary }}</text>
    </view>

    <view v-if="loadError" class="empty-card">
      <text class="empty-title">排班加载失败</text>
      <text class="empty-desc">{{ loadError }}</text>
      <button class="primary-btn" @click="loadShifts">重试</button>
    </view>

    <view v-else-if="!authStore.workerInfo" class="empty-card">
      <text class="empty-title">请先登录</text>
      <text class="empty-desc">登录后可以看到今日排班和签到状态</text>
      <button class="primary-btn" @click="navTo('/pages/login/login')">去登录</button>
    </view>

    <view v-else-if="loading" class="empty-card">
      <text class="empty-title">正在加载排班</text>
      <text class="empty-desc">请稍等，首页正在整理今日和未来排班</text>
    </view>

    <template v-else>
      <view class="section">
        <view class="section-header">
          <view>
            <text class="section-title">今日排班</text>
            <text class="section-sub">签到状态和签到操作放在最前面</text>
          </view>
          <text class="status-pill" :class="todayShift ? statusClass(todayShift.status) : 'status-wait'">{{ todayShift ? labelForStatus(todayShift.status) : '无排班' }}</text>
        </view>

        <view v-if="todayShift" class="today-card">
          <view class="today-badge">今日主班次</view>
          <text class="today-job">{{ todayShift.jobTitle }}</text>
          <view class="today-meta">
            <view class="meta-item">
              <text class="meta-label">时间</text>
              <text class="meta-value">{{ todayShift.startTime }} - {{ todayShift.endTime }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">地点</text>
              <text class="meta-value">{{ todayShift.location }}</text>
            </view>
          </view>
          <button class="primary-btn" @click="navTo('/pages/attendance/clockIn')">
            {{ isCheckedOut(todayShift.status) ? '已签退' : isCheckedIn(todayShift.status) ? '去签退' : '去签到' }}
          </button>
          <button class="secondary-btn" @click="navTo('/pages/schedule/schedule')">查看今日全部排班</button>
        </view>

        <view v-else class="empty-inline">
          <text class="empty-title">今天没有排班</text>
          <text class="empty-desc">可以先看看接下来的排班</text>
          <button class="secondary-btn" @click="navTo('/pages/schedule/schedule')">查看排班</button>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <view>
            <text class="section-title">我的未来排班</text>
            <text class="section-sub">最近 5 条</text>
          </view>
          <text class="section-more" @click="navTo('/pages/schedule/schedule')">查看全部</text>
        </view>
        <view v-if="futureShifts.length" class="future-list">
          <view v-for="shift in futureShifts" :key="shift.id" class="future-item">
            <view class="future-top">
              <text class="future-job">{{ shift.jobTitle }}</text>
              <text class="future-time">{{ shift.date }} {{ shift.startTime }}-{{ shift.endTime }}</text>
            </view>
            <text class="future-location">{{ shift.location }}</text>
            <text class="future-status">{{ labelForStatus(shift.status) }}</text>
          </view>
        </view>
        <view v-else class="empty-inline">
          <text class="empty-title">暂无未来排班</text>
          <text class="empty-desc">排班信息会在这里展示</text>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <view>
            <text class="section-title">快捷入口</text>
            <text class="section-sub">只保留工作流相关入口</text>
          </view>
        </view>
        <view class="quick-grid">
          <view class="quick-item" @click="navTo('/pages/schedule/schedule')">
            <text class="quick-title">我的排班</text>
            <text class="quick-desc">查看本周和未来排班</text>
          </view>
          <view class="quick-item" @click="navTo('/pages/attendance/clockIn')">
            <text class="quick-title">打卡</text>
            <text class="quick-desc">进入今日签到/签退</text>
          </view>
          <view class="quick-item" @click="navTo('/pages/earnings/earnings')">
            <text class="quick-title">我的收入</text>
            <text class="quick-desc">查看工时与工资</text>
          </view>
          <view class="quick-item" @click="navTo('/pages/message/message')">
            <text class="quick-title">消息</text>
            <text class="quick-desc">查看通知和提醒</text>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/store'
import { getMyShifts } from '@/api/schedule'

const authStore = useAuthStore()
const loading = ref(false)
const loadError = ref('')
const shifts = ref<any[]>([])

type Shift = {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayLabel = computed(() => {
  const d = new Date()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}月${day}日`
})

const todayWeekday = computed(() => {
  return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date().getDay()]
})

function formatDateKey(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function normalizeShift(shift: any): Shift {
  return {
    id: shift.id ?? shift.shiftId,
    jobTitle: shift.jobTitle || shift.title || '排班',
    location: shift.jobLocation || shift.location || shift.locationName || '',
    startTime: shift.startTime || '',
    endTime: shift.endTime || '',
    date: shift.date || shift.shiftDate || '',
    status: shift.status || 'SCHEDULED'
  }
}

function labelForStatus(status?: string) {
  if (status === 'CHECKED_IN') return '已签到'
  if (status === 'CHECKED_OUT') return '已签退'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
  return '待签到'
}

function statusClass(status?: string) {
  if (status === 'CHECKED_IN') return 'status-in'
  if (status === 'CHECKED_OUT') return 'status-out'
  return 'status-wait'
}

function isCheckedIn(status?: string) {
  return status === 'CHECKED_IN' || status === 'CHECKED_OUT'
}

function isCheckedOut(status?: string) {
  return status === 'CHECKED_OUT'
}

const sortedShifts = computed(() => {
  return [...shifts.value].sort((a, b) => {
    const dateDiff = String(a.date).localeCompare(String(b.date))
    if (dateDiff !== 0) return dateDiff
    return String(a.startTime).localeCompare(String(b.startTime))
  })
})

const todayShifts = computed(() => {
  const today = formatDateKey(new Date())
  return sortedShifts.value.filter((shift) => shift.date === today)
})

const futureShifts = computed(() => {
  const today = formatDateKey(new Date())
  return sortedShifts.value
    .filter((shift) => shift.date > today)
    .slice(0, 5)
})

const todayShift = computed(() => todayShifts.value[0] || null)

const todayShiftSummary = computed(() => {
  if (!authStore.workerInfo) return '登录后查看排班'
  if (!todayShifts.value.length) return '今天暂无排班'
  return `今天 ${todayShifts.value.length} 个班次`
})

async function loadShifts() {
  loading.value = true
  loadError.value = ''
  try {
    const startDate = formatDateKey(new Date())
    const endDate = formatDateKey(new Date(Date.now() + 6 * 24 * 60 * 60 * 1000))
    const res: any = await getMyShifts({ startDate, endDate })
    const list = Array.isArray(res) ? res : (res.list || [])
    shifts.value = list.map(normalizeShift)
  } catch (err: any) {
    loadError.value = err?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function navTo(url: string) {
  uni.navigateTo({ url })
}

onMounted(loadShifts)
</script>

<style scoped>
.home-page {
  padding: 30rpx;
}

.header-banner {
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #07c160, #06ad56);
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  color: #fff;
}

.greeting {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.greeting-text {
  font-size: 28rpx;
  opacity: 0.9;
  margin-right: 12rpx;
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
}

.sub-text {
  font-size: 26rpx;
  opacity: 0.8;
}

.empty-card,
.empty-inline,
.today-card,
.future-item,
.quick-item {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section {
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.section-sub {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #999;
}

.section-more {
  font-size: 26rpx;
  color: #999;
}

.status-pill {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.status-wait {
  background: #fff7e6;
  color: #d97706;
}

.status-in {
  background: #e8f3ff;
  color: #2563eb;
}

.status-out {
  background: #ecfdf5;
  color: #059669;
}

.today-card {
  border: 1rpx solid #d7f5e2;
  background: linear-gradient(180deg, #f7fff9 0%, #ffffff 100%);
}

.today-badge {
  display: inline-flex;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e9fdf1;
  color: #05964d;
  font-size: 22rpx;
  font-weight: 600;
  margin-bottom: 14rpx;
}

.today-job {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 14rpx;
}

.today-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.meta-label {
  color: #999;
  font-size: 24rpx;
}

.meta-value {
  color: #333;
  font-size: 24rpx;
  font-weight: 500;
  text-align: right;
}

.primary-btn,
.secondary-btn {
  width: 100%;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.primary-btn {
  margin-top: 4rpx;
  background: #07c160;
  color: #fff;
}

.secondary-btn {
  margin-top: 10rpx;
  background: #fff;
  color: #07c160;
  border: 1rpx solid #b7efd0;
}

.future-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.future-top {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  align-items: flex-start;
  margin-bottom: 10rpx;
}

.future-job {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.future-time {
  font-size: 22rpx;
  color: #05964d;
  background: #ecfdf5;
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  white-space: nowrap;
}

.future-location {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 6rpx;
}

.future-status {
  font-size: 22rpx;
  color: #999;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.quick-item {
  min-height: 88rpx;
}

.quick-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.quick-desc {
  font-size: 22rpx;
  color: #999;
  line-height: 1.5;
}

.empty-card,
.empty-inline {
  margin-bottom: 24rpx;
}

.empty-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.empty-desc {
  display: block;
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
  margin-bottom: 12rpx;
}
</style>
