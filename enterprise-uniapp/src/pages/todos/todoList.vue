<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'

const currentType = ref('applications')

const types = [
  { key: 'applications', name: '报名' },
  { key: 'schedules', name: '排班' },
  { key: 'attendance', name: '考勤' },
  { key: 'salary', name: '薪资' }
]

const todoMap = {
  applications: [
    { name: '张小雨', status: '待审核', title: '周末促销员', time: '10:24', desc: '23 岁 · 女', path: '/pages/applications/applicationList' },
    { name: '李明', status: '待审核', title: '仓库分拣员', time: '09:38', desc: '28 岁 · 男 · 做过 3 次', path: '/pages/applications/applicationList' }
  ],
  schedules: [
    { name: '晚班服务员', status: '未排满', title: '今日 18:00-22:00', time: '缺 2 人', desc: '星河门店', path: '/pages/schedules/scheduleList' }
  ],
  attendance: [
    { name: '考勤异常', status: '待确认', title: '2 条记录缺少签退', time: '今天', desc: '需确认实际工时', path: '/pages/schedules/scheduleList' }
  ],
  salary: [
    { name: '薪资结算', status: '待结算', title: '5 条考勤记录', time: '预计 ¥1,240', desc: '确认后可发起结算', path: '/pages/attendance/attendanceList' }
  ]
}

const currentTodos = computed(() => todoMap[currentType.value] || [])

function switchType(type) {
  currentType.value = type
}

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

onPullDownRefresh(() => {
  setTimeout(() => uni.stopPullDownRefresh(), 300)
})
</script>

<template>
  <scroll-view scroll-y class="op-page todo-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">待办中心</text>
      <text class="op-hero-title">按业务类型处理今天的事项</text>
      <text class="op-hero-desc">报名 12 · 排班 5 · 考勤 2 · 薪资 5</text>
    </view>

    <scroll-view scroll-x class="type-tabs" scroll-with-animation>
      <view class="type-tabs-inner">
        <view v-for="type in types" :key="type.key" class="type-tab" :class="{ active: currentType === type.key }" @click="switchType(type.key)">
          {{ type.name }}
        </view>
      </view>
    </scroll-view>

    <view class="op-content">
      <view v-for="item in currentTodos" :key="item.name + item.title" class="op-card task-card" @click="navigateTo(item.path)">
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
  </scroll-view>
</template>

<style>
.todo-page { height: 100vh; }
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
