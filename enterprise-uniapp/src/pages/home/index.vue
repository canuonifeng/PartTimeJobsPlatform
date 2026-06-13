<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/store'
import { request } from '@/api/request'

const authStore = useAuthStore()
const companyName = ref('')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const stats = [
  { label: '发布中', value: 6 },
  { label: '总报名', value: 48 },
  { label: '待处理', value: 12 }
]

const flowSteps = [
  { name: '发布', value: '6', state: 'done' },
  { name: '报名', value: '48', state: 'done' },
  { name: '审核', value: '12', state: 'warn' },
  { name: '薪资', value: '5', state: 'todo' }
]

const todos = [
  { type: '审', title: '12 个报名等待审核', desc: '服务员、分拣员岗位报名较多', tag: '紧急', path: '/pages/applications/applicationList' },
  { type: '班', title: '5 个班次未排满', desc: '今晚 18:00 前建议处理', tag: '去处理', path: '/pages/schedules/scheduleList' }
]

const quickActions = [
  { name: '发布职位', icon: '发', path: '/pages/jobs/jobForm' },
  { name: '审核报名', icon: '审', path: '/pages/applications/applicationList' },
  { name: '创建排班', icon: '排', path: '/pages/schedules/scheduleList' },
  { name: '薪资结算', icon: '薪', path: '/pages/attendance/attendanceList' }
]

onMounted(async () => {
  try {
    const res = await request('GET', '/enterprise')
    companyName.value = res?.companyName || ''
  } catch {}
})

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function switchToProcess() {
  uni.switchTab({ url: '/pages/process/process' })
}

function switchToTodos() {
  uni.switchTab({ url: '/pages/todos/todoList' })
}
</script>

<template>
  <view class="op-page workbench-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">{{ greeting }}，{{ authStore.displayName || '企业管理员' }}</text>
      <text class="op-hero-title">今天有 12 项招聘任务待推进</text>
      <text class="op-hero-desc">{{ companyName || '企业名称' }} · 发布中岗位报名转化较昨日更活跃</text>
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
          <text class="op-section-link" @click="switchToTodos">全部</text>
        </view>
        <view class="op-card todo-card">
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
.todo-row { display: flex; align-items: center; min-width: 0; padding: 18rpx 0; border-bottom: 1rpx solid #edf0f3; }
.todo-row:last-child { border-bottom: none; }
.todo-icon { width: 72rpx; height: 72rpx; margin-right: 18rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; flex-shrink: 0; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; }
.quick-item { text-align: center; }
.quick-icon { width: 84rpx; height: 84rpx; margin: 0 auto 12rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; background: #ecfdf5; color: #16a34a; font-size: 32rpx; font-weight: 850; }
.quick-name { font-size: 23rpx; color: #334155; }
</style>
