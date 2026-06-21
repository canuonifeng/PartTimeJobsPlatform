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
    { label: '发布中', value: overview.publishedJobCount ?? 0, color: '#12834a' },
    { label: '总报名', value: overview.totalApplicationCount ?? 0, color: '#3b82f6' },
    { label: '待处理', value: overview.pendingTodoCount ?? 0, color: '#d28a00' }
  ]
})

const todoConfig = {
  APPLICATION: { icon: '审', color: '#d28a00', bg: '#fffbeb', border: '#fde68a', title: '报名审核', desc: '及时审核报名，提高招工转化', path: '/pages/applications/applicationList?status=PENDING', action: '去审核' },
  SCHEDULE: { icon: '班', color: '#3b82f6', bg: '#eff6ff', border: '#bfdbfe', title: '今日班次', desc: '跟进排班，避免班次遗漏', path: '/pages/schedules/manageList', action: '查看班次' },
  ATTENDANCE: { icon: '勤', color: '#8b5cf6', bg: '#f5f3ff', border: '#ddd6fe', title: '考勤确认', desc: '处理异常考勤，确认实际工时', path: '/pages/schedules/scheduleList?status=SCHEDULED', action: '去确认' },
  SALARY: { icon: '薪', color: '#16a34a', bg: '#f0fdf4', border: '#bbf7d0', title: '薪资结算', desc: '确认工时后完成薪资结算', path: '/pages/attendance/attendanceList?settlementStatus=UNPAID', action: '去结算' }
}

const todoCards = computed(() => (dashboard.value?.todoSummary || [])
  .filter(item => Number(item.count || 0) > 0)
  .map(item => {
    const cfg = todoConfig[item.type] || {}
    return {
      type: item.type,
      count: Number(item.count || 0),
      ...cfg
    }
  })
)

const hasTodos = computed(() => todoCards.value.length > 0)

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
        <text class="stat-value" :style="{ color: item.color }">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="op-content">
      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">今日待办</text>
        </view>
        <template v-if="hasTodos">
          <view
            v-for="card in todoCards"
            :key="card.type"
            class="todo-card"
            :style="{ borderLeftColor: card.color, background: card.bg }"
            @click="navigateTo(card.path)"
          >
            <view class="todo-card-main">
              <view class="todo-icon-wrap" :style="{ background: card.color }">
                <text class="todo-icon-text">{{ card.icon }}</text>
              </view>
              <view class="todo-info">
                <text class="todo-info-title">{{ card.title }}</text>
                <text class="todo-info-desc">{{ card.desc }}</text>
              </view>
              <view class="todo-count-wrap">
                <text class="todo-count-num" :style="{ color: card.color }">{{ card.count }}</text>
                <text class="todo-count-unit">项</text>
              </view>
            </view>
            <view class="todo-card-footer">
              <text class="todo-action-text" :style="{ color: card.color }">{{ card.action }}</text>
              <text class="todo-action-arrow" :style="{ color: card.color }">›</text>
            </view>
          </view>
        </template>
        <view v-else class="todo-empty-card">
          <view class="todo-empty-icon">✓</view>
          <text class="todo-empty-title">暂无待处理事项</text>
          <text class="todo-empty-desc">所有招聘流程正常推进中</text>
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
.stat-value { display: block; font-size: 40rpx; line-height: 1; font-weight: 850; }
.stat-label { display: block; margin-top: 10rpx; font-size: 22rpx; color: #64748b; }

.todo-card { border-radius: 24rpx; padding: 22rpx 24rpx; margin-bottom: 18rpx; border-left: 6rpx solid; box-shadow: 0 8rpx 24rpx rgba(23,83,53,.06); box-sizing: border-box; }
.todo-card:last-child { margin-bottom: 0; }
.todo-card-main { display: flex; align-items: center; min-width: 0; }
.todo-icon-wrap { width: 64rpx; height: 64rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.todo-icon-text { color: #fff; font-size: 28rpx; font-weight: 850; }
.todo-info { flex: 1; min-width: 0; margin-left: 18rpx; }
.todo-info-title { display: block; font-size: 28rpx; font-weight: 800; color: #1f2933; }
.todo-info-desc { display: block; margin-top: 4rpx; font-size: 22rpx; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.todo-count-wrap { flex-shrink: 0; margin-left: 12rpx; text-align: center; }
.todo-count-num { font-size: 38rpx; font-weight: 850; line-height: 1; }
.todo-count-unit { display: block; margin-top: 2rpx; font-size: 20rpx; color: #98a3b3; }
.todo-card-footer { display: flex; align-items: center; justify-content: flex-end; margin-top: 14rpx; padding-top: 14rpx; border-top: 1rpx solid rgba(0,0,0,.04); }
.todo-action-text { font-size: 24rpx; font-weight: 700; }
.todo-action-arrow { margin-left: 4rpx; font-size: 30rpx; font-weight: 700; line-height: 1; }

.todo-empty-card { background: #fff; border-radius: 28rpx; padding: 64rpx 28rpx; box-shadow: 0 12rpx 34rpx rgba(23,83,53,.08); text-align: center; }
.todo-empty-icon { width: 80rpx; height: 80rpx; margin: 0 auto 20rpx; border-radius: 50%; background: #ecfdf5; color: #16a34a; display: flex; align-items: center; justify-content: center; font-size: 36rpx; font-weight: 850; }
.todo-empty-title { display: block; font-size: 28rpx; font-weight: 800; color: #1f2933; }
.todo-empty-desc { display: block; margin-top: 8rpx; font-size: 24rpx; color: #98a3b3; }
</style>
