<script setup>
import { computed, onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { request } from '@/api/request'
import { getOperationDashboard } from '@/api/operations'
import EnterpriseTabBar from '@/components/EnterpriseTabBar.vue'

const authStore = useAuthStore()
const companyName = ref('')
const dashboard = ref(null)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const stats = computed(() => {
  const overview = dashboard.value?.overview || {}
  return [
    { label: '发布中', value: overview.publishedJobCount ?? 0 },
    { label: '总报名', value: overview.totalApplicationCount ?? 0 },
    { label: '待处理', value: overview.pendingTodoCount ?? 0 }
  ]
})

const flowSteps = computed(() => (dashboard.value?.process || []).slice(0, 4).map(item => ({
  name: shortProcessName(item.name),
  value: item.count ?? 0,
  state: processState(item.status)
})))

const todos = computed(() => (dashboard.value?.todoSummary || [])
  .filter(item => Number(item.count || 0) > 0)
  .map(item => ({
    type: todoIcon(item.type),
    title: `${item.count || 0} 个${item.name}待办`,
    desc: todoDesc(item.type),
    tag: Number(item.count || 0) > 0 ? '去处理' : '已完成',
    path: todoPath(item.type)
  })))

const quickActions = [
  { name: '班次管理', icon: '班', path: '/pages/schedules/manageList' },
  { name: '招聘计划', icon: '岗', path: '/pages/jobs/jobList' },
  { name: '报名审核', icon: '审', path: '/pages/applications/applicationList' },
  { name: '考勤确认', icon: '勤', path: '/pages/schedules/scheduleList?status=SCHEDULED' },
  { name: '薪资结算', icon: '¥', path: '/pages/attendance/attendanceList?settlementStatus=UNPAID' }
]

onShow(() => {
  uni.hideTabBar({ animation: false })
  loadDashboard()
})

onMounted(async () => {
  try {
    const res = await request('GET', '')
    companyName.value = res?.companyName || ''
  } catch {}
  await loadDashboard()
})

async function loadDashboard() {
  try {
    dashboard.value = await getOperationDashboard()
  } catch {
    uni.showToast({ title: '运营数据加载失败', icon: 'none' })
  }
}

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function switchToProcess() {
  uni.switchTab({ url: '/pages/process/process' })
}

function processState(status) {
  if (status === 'WARNING') return 'warn'
  if (status === 'TODO') return 'todo'
  return 'done'
}

function shortProcessName(name) {
  if (!name) return '-'
  if (name.includes('发布')) return '发布'
  if (name.includes('报名')) return '报名'
  if (name.includes('审核')) return '审核'
  if (name.includes('薪资')) return '薪资'
  return name.slice(0, 2)
}

function todoIcon(type) {
  const map = { APPLICATION: '审', SCHEDULE: '班', ATTENDANCE: '勤', SALARY: '薪' }
  return map[type] || '办'
}

function todoDesc(type) {
  const map = {
    APPLICATION: '及时审核报名，提高招工转化',
    SCHEDULE: '跟进排班，避免班次遗漏',
    ATTENDANCE: '处理异常考勤，确认实际工时',
    SALARY: '确认工时后完成薪资结算'
  }
  return map[type] || '查看并处理待办事项'
}

function todoPath(type) {
  const map = {
    APPLICATION: '/pages/applications/applicationList?status=PENDING',
    ATTENDANCE: '/pages/schedules/scheduleList?status=SCHEDULED',
    SALARY: '/pages/attendance/attendanceList?settlementStatus=UNPAID'
  }
  return map[type] || '/pages/todos/todoList'
}
</script>

<template>
  <view class="op-page workbench-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">{{ greeting }}，{{ authStore.displayName || '企业管理员' }}</text>
      <text class="op-hero-title">今天有 {{ dashboard?.overview?.pendingTodoCount ?? 0 }} 项招聘任务待推进</text>
      <text class="op-hero-desc">{{ companyName || '企业名称' }} · 今日新增报名 {{ dashboard?.today?.newApplicationCount ?? 0 }} 条</text>
    </view>

    <view class="stats-grid">
      <view v-for="item in stats" :key="item.label" class="stat-card">
        <text class="stat-value">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="op-content">
      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">招聘流程</text>
          <text class="op-section-link" @click="switchToProcess">查看流程</text>
        </view>
        <view class="op-card flow-card">
          <view v-for="step in flowSteps" :key="step.name" class="flow-item" :class="'flow-' + step.state">
            <text class="flow-value">{{ step.value }}</text>
            <text class="flow-name">{{ step.name }}</text>
          </view>
        </view>
      </view>

      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">今日待办</text>
        </view>
        <view class="op-card todo-card">
          <view v-if="todos.length === 0" class="todo-empty">暂无待处理事项</view>
          <view v-for="item in todos" :key="item.title" class="todo-row" @click="navigateTo(item.path)">
            <view class="todo-icon">{{ item.type }}</view>
            <view class="op-row-main">
              <text class="op-row-title">{{ item.title }}</text>
              <text class="op-row-desc">{{ item.desc }}</text>
            </view>
            <text class="op-pill op-pill-warn">{{ item.tag }}</text>
          </view>
        </view>
      </view>

      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">快捷操作</text>
        </view>
        <view class="quick-grid">
          <view v-for="item in quickActions" :key="item.name" class="quick-item" @click="navigateTo(item.path)">
            <view class="quick-icon">{{ item.icon }}</view>
            <text class="quick-name">{{ item.name }}</text>
          </view>
        </view>
      </view>
    </view>
    <EnterpriseTabBar active="home" />
  </view>
</template>

<style>
.workbench-page { min-height: 100vh; padding-bottom: calc(140rpx + env(safe-area-inset-bottom)); }
.top-space { height: 24rpx; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; margin: -24rpx 28rpx 0; position: relative; z-index: 2; }
.stat-card { background: rgba(255,255,255,.96); border-radius: 24rpx; padding: 22rpx 18rpx; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); box-sizing: border-box; }
.stat-value { display: block; font-size: 40rpx; line-height: 1; font-weight: 850; color: #12834a; }
.stat-label { display: block; margin-top: 10rpx; font-size: 22rpx; color: #64748b; }
.flow-card { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14rpx; }
.flow-item { border-radius: 22rpx; padding: 20rpx 8rpx; text-align: center; background: #ecfdf5; color: #12834a; }
.flow-warn { background: #fffbeb; color: #b45309; }
.flow-todo { background: #f1f5f9; color: #64748b; }
.flow-value { display: block; font-size: 34rpx; font-weight: 850; line-height: 1; }
.flow-name { display: block; margin-top: 10rpx; font-size: 22rpx; font-weight: 750; }
.todo-card { padding-top: 8rpx; padding-bottom: 8rpx; }
.todo-empty { padding: 28rpx 0; text-align: center; color: #98a3b3; font-size: 25rpx; }
.todo-row { display: flex; align-items: center; min-width: 0; padding: 18rpx 0; border-bottom: 1rpx solid #edf0f3; }
.todo-row:last-child { border-bottom: none; }
.todo-icon { width: 72rpx; height: 72rpx; margin-right: 18rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; flex-shrink: 0; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; }
.quick-item { text-align: center; }
.quick-icon { width: 86rpx; height: 86rpx; margin: 0 auto 12rpx; border: 2rpx solid rgba(22,163,74,.28); border-radius: 30rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f0fdf4, #dcfce7); color: #16a34a; font-size: 30rpx; font-weight: 900; box-sizing: border-box; box-shadow: inset 0 0 0 6rpx rgba(255,255,255,.54); }
.quick-name { font-size: 23rpx; color: #334155; }
</style>
