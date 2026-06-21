<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOperationProcess } from '@/api/operations'
import EnterpriseTabBar from '@/components/EnterpriseTabBar.vue'

onShow(() => {
  uni.hideTabBar({ animation: false })
  loadProcess()
})

const processData = ref([])

const stepOrder = ['PUBLISH', 'SCHEDULE', 'REVIEW', 'ATTENDANCE', 'SALARY']

const stepConfig = {
  PUBLISH: { number: 1, title: '创建岗位', desc: '发布招聘岗位信息，设置薪资标准', icon: '岗', path: '/pages/jobs/jobList' },
  SCHEDULE: { number: 2, title: '创建班次', desc: '为岗位设置排班时间和人数', icon: '班', path: '/pages/schedules/manageList' },
  REVIEW: { number: 3, title: '审核报名', desc: '审核工人报名申请，确认录用', icon: '审', path: '/pages/applications/applicationList?status=PENDING' },
  ATTENDANCE: { number: 4, title: '考勤确认', desc: '确认工人实际出勤和工时', icon: '勤', path: '/pages/schedules/scheduleList?status=SCHEDULED' },
  SALARY: { number: 5, title: '薪资结算', desc: '确认工时后完成薪资发放', icon: '薪', path: '/pages/attendance/attendanceList?settlementStatus=UNPAID' }
}

const steps = computed(() => {
  const dataMap = {}
  ;(Array.isArray(processData.value) ? processData.value : []).forEach(item => {
    dataMap[item.code] = item
  })

  return stepOrder.map(code => {
    const cfg = stepConfig[code] || {}
    const api = dataMap[code] || {}
    const status = api.status || 'TODO'
    return {
      code,
      number: cfg.number,
      title: cfg.title,
      desc: cfg.desc,
      icon: cfg.icon,
      path: cfg.path,
      tags: api.tags || [],
      state: status === 'WARNING' ? 'warn' : status === 'TODO' ? 'todo' : 'done',
      isWarn: status === 'WARNING',
      isCurrent: status === 'TODO',
      isDone: status === 'done'
    }
  })
})

async function loadProcess() {
  try {
    processData.value = await getOperationProcess()
  } catch {
    uni.showToast({ title: '流程数据加载失败', icon: 'none' })
  }
}

function navigateTo(path) {
  uni.navigateTo({ url: path })
}
</script>

<template>
  <view class="op-page process-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">招聘运营流程</text>
      <text class="op-hero-title">5 步完成招聘到结算</text>
      <text class="op-hero-desc">从发布岗位到薪资结算，全链路数据一目了然</text>
    </view>

    <view class="op-content">
      <view class="process-steps">
        <view
          v-for="step in steps"
          :key="step.code"
          class="process-step-card"
          :class="{ 'is-warn': step.isWarn, 'is-done': step.isDone, 'is-todo': step.isCurrent }"
          @click="navigateTo(step.path)"
        >
          <view class="step-left">
            <view class="step-number" :class="'step-num-' + step.state">
              <text>{{ step.isDone ? '✓' : step.number }}</text>
            </view>
          </view>
          <view class="step-body">
            <view class="step-header">
              <text class="step-title">{{ step.title }}</text>
              <text class="step-arrow">›</text>
            </view>
            <text class="step-desc">{{ step.desc }}</text>
            <view v-if="step.tags.length > 0" class="step-tags">
              <text v-for="tag in step.tags" :key="tag" class="step-tag" :class="'tag-' + step.state">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <EnterpriseTabBar active="process" />
  </view>
</template>

<style>
.process-page { min-height: 100vh; padding-bottom: calc(140rpx + env(safe-area-inset-bottom)); }
.top-space { height: 24rpx; }

.process-steps { padding-top: 6rpx; }
.process-step-card { display: flex; background: #fff; border-radius: 26rpx; padding: 24rpx; margin-bottom: 18rpx; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); box-sizing: border-box; border: 2rpx solid transparent; }
.process-step-card:last-child { margin-bottom: 0; }
.process-step-card.is-warn { border-color: #f59e0b; background: #fffbeb; }
.process-step-card.is-done { border-color: transparent; }
.process-step-card.is-todo { border-color: #e5e7eb; }

.step-left { margin-right: 20rpx; display: flex; align-items: flex-start; padding-top: 4rpx; }
.step-number { width: 56rpx; height: 56rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 850; flex-shrink: 0; }
.step-num-done { background: #16a34a; color: #fff; }
.step-num-warn { background: #f59e0b; color: #fff; box-shadow: 0 0 0 4rpx rgba(245,158,11,.2); }
.step-num-todo { background: #e5e7eb; color: #9ca3af; }

.step-body { flex: 1; min-width: 0; }
.step-header { display: flex; align-items: center; justify-content: space-between; }
.step-title { font-size: 28rpx; font-weight: 800; color: #1f2933; }
.is-warn .step-title { color: #92400e; }
.step-arrow { font-size: 36rpx; color: #98a3b3; line-height: 1; }
.step-desc { display: block; margin-top: 6rpx; font-size: 23rpx; color: #64748b; line-height: 1.5; }
.step-tags { display: flex; flex-wrap: wrap; gap: 10rpx; margin-top: 14rpx; }
.step-tag { padding: 6rpx 16rpx; border-radius: 999rpx; font-size: 22rpx; font-weight: 700; }
.tag-done { background: #ecfdf5; color: #12834a; }
.tag-warn { background: #fef3c7; color: #92400e; }
.tag-todo { background: #f1f5f9; color: #64748b; }
</style>
