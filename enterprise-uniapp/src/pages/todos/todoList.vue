<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { getOperationTodos } from '@/api/operations'

onShow(() => {
  loadTodos()
})

const currentType = ref('APPLICATION')
const loading = ref(false)
const todos = ref([])

const types = [
  { key: 'APPLICATION', name: '报名' },
  { key: 'SCHEDULE', name: '排班' },
  { key: 'ATTENDANCE', name: '考勤' },
  { key: 'SALARY', name: '薪资' }
]

const heroDesc = computed(() => types.map(type => `${type.name}${type.key === currentType.value ? todos.value.length : '-'}`).join(' · '))

const currentTodos = computed(() => todos.value.map(item => ({
  id: item.id,
  name: typeName(item.type),
  status: statusLabel(item.status),
  title: item.title || '-',
  time: formatTime(item.createdAt),
  desc: item.description || '-',
  path: item.routePath || fallbackPath(item.type)
})))

function switchType(type) {
  currentType.value = type
  loadTodos()
}

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

onPullDownRefresh(() => {
  loadTodos().finally(() => uni.stopPullDownRefresh())
})

async function loadTodos() {
  loading.value = true
  try {
    const res = await getOperationTodos({ type: currentType.value, page: 1, pageSize: 50 })
    todos.value = Array.isArray(res) ? res : (res?.records || [])
  } catch {
    uni.showToast({ title: '待办数据加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function typeName(type) {
  const map = { APPLICATION: '报名待办', SCHEDULE: '排班待办', ATTENDANCE: '考勤待办', SALARY: '薪资待办' }
  return map[type] || '待办'
}

function statusLabel(status) {
  const map = { PENDING: '待审核', UNPAID: '待结算', NORMAL: '正常', WARNING: '预警' }
  return map[status] || status || '-'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function fallbackPath(type) {
  const map = {
    APPLICATION: '/pages/applications/applicationList',
    SCHEDULE: '/pages/schedules/scheduleList',
    ATTENDANCE: '/pages/schedules/scheduleList',
    SALARY: '/pages/attendance/attendanceList'
  }
  return map[type] || '/pages/home/index'
}
</script>

<template>
  <view class="op-page todo-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">待办中心</text>
      <text class="op-hero-title">按业务类型处理今天的事项</text>
      <text class="op-hero-desc">{{ heroDesc }}</text>
    </view>

    <scroll-view scroll-x class="type-tabs" scroll-with-animation>
      <view class="type-tabs-inner">
        <view v-for="type in types" :key="type.key" class="type-tab" :class="{ active: currentType === type.key }" @click="switchType(type.key)">
          {{ type.name }}
        </view>
      </view>
    </scroll-view>

    <view class="op-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>
      <view v-else-if="currentTodos.length === 0" class="e-empty">
        <text class="e-empty-title">暂无待办</text>
        <text class="e-empty-desc">当前分类没有需要处理的事项</text>
      </view>
      <view v-else>
        <view v-for="item in currentTodos" :key="item.id" class="op-card task-card" @click="navigateTo(item.path)">
          <view class="task-head">
            <text class="task-name">{{ item.name }}</text>
            <text class="op-pill op-pill-warn">{{ item.status }}</text>
          </view>
          <view class="task-grid">
            <view class="task-info">
              <text class="task-label">事项</text>
              <text class="task-value">{{ item.title }}</text>
            </view>
            <view class="task-info">
              <text class="task-label">时间</text>
              <text class="task-value">{{ item.time }}</text>
            </view>
            <view class="task-info wide">
              <text class="task-label">说明</text>
              <text class="task-value">{{ item.desc }}</text>
            </view>
          </view>
          <view class="task-actions">
            <view class="task-btn secondary">查看详情</view>
            <view class="task-btn primary">去处理</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.todo-page { min-height: 100vh; padding-bottom: 40rpx; }
.top-space { height: 24rpx; }
.type-tabs { width: 100%; margin-top: 22rpx; white-space: nowrap; }
.type-tabs-inner { display: flex; padding: 0 28rpx; }
.type-tab { flex-shrink: 0; margin-right: 16rpx; padding: 14rpx 28rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 26rpx; font-weight: 800; box-shadow: 0 8rpx 20rpx rgba(23,83,53,.06); }
.type-tab.active { background: #16a34a; color: #fff; }
.task-card { margin-bottom: 20rpx; }
.task-head { display: flex; align-items: center; justify-content: space-between; }
.task-name { font-size: 30rpx; font-weight: 850; color: #1f2933; }
.task-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin-top: 20rpx; }
.task-info { padding: 16rpx; border-radius: 18rpx; background: #f8fafc; min-width: 0; }
.task-info.wide { grid-column: span 2; }
.task-label { display: block; font-size: 22rpx; color: #64748b; }
.task-value { display: block; margin-top: 8rpx; font-size: 26rpx; font-weight: 750; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.task-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx; margin-top: 20rpx; }
.task-btn { height: 72rpx; line-height: 72rpx; border-radius: 18rpx; text-align: center; font-size: 26rpx; font-weight: 850; }
.task-btn.secondary { background: #ecfdf5; color: #16a34a; }
.task-btn.primary { background: #16a34a; color: #fff; }
</style>
