<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOperationProcess } from '@/api/operations'
import EnterpriseTabBar from '@/components/EnterpriseTabBar.vue'

onShow(() => {
  uni.hideTabBar({ animation: false })
  loadProcess()
})

const steps = ref([])

async function loadProcess() {
  try {
    const res = await getOperationProcess()
    steps.value = (Array.isArray(res) ? res : []).map(item => ({
      icon: processIcon(item.code),
      title: item.name,
      desc: item.description,
      tags: item.tags || [],
      path: item.routePath,
      state: processState(item.status)
    }))
  } catch {
    uni.showToast({ title: '流程数据加载失败', icon: 'none' })
  }
}

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function processIcon(code) {
  const map = { PUBLISH: '发', APPLICATION: '报', REVIEW: '审', SCHEDULE: '班', ATTENDANCE: '勤', SALARY: '薪' }
  return map[code] || '流'
}

function processState(status) {
  if (status === 'WARNING') return 'warn'
  if (status === 'TODO') return 'muted'
  return 'done'
}
</script>

<template>
  <view class="op-page process-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">招聘运营流程</text>
      <text class="op-hero-title">招聘链路实时推进</text>
      <text class="op-hero-desc">发布、报名、审核、排班、考勤、薪资节点来自后端统计</text>
    </view>

    <view class="op-content">
      <view class="timeline">
        <view v-for="step in steps" :key="step.title" class="timeline-step" @click="navigateTo(step.path)">
          <view class="timeline-node" :class="'node-' + step.state">{{ step.icon }}</view>
          <view class="timeline-card">
            <view class="op-row">
              <view class="op-row-main">
                <text class="op-row-title">{{ step.title }}</text>
                <text class="op-row-desc">{{ step.desc }}</text>
              </view>
              <text class="arrow">›</text>
            </view>
            <view class="tag-row">
              <text v-for="tag in step.tags" :key="tag" class="meta-tag">{{ tag }}</text>
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
.timeline { padding-top: 6rpx; }
.timeline-step { position: relative; display: flex; padding-bottom: 26rpx; }
.timeline-step::before { content: ''; position: absolute; left: 35rpx; top: 76rpx; bottom: 0; width: 4rpx; background: #dbe8df; border-radius: 999rpx; }
.timeline-step:last-child::before { display: none; }
.timeline-node { width: 72rpx; height: 72rpx; margin-right: 18rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; flex-shrink: 0; box-shadow: 0 10rpx 24rpx rgba(22,163,74,.18); }
.node-warn { background: #f59e0b; }
.node-muted { background: #cbd5e1; }
.timeline-card { flex: 1; min-width: 0; background: #fff; border-radius: 26rpx; padding: 24rpx; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); box-sizing: border-box; }
.arrow { margin-left: 12rpx; color: #98a3b3; font-size: 40rpx; line-height: 1; }
.tag-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 18rpx; }
.meta-tag { padding: 8rpx 14rpx; border-radius: 999rpx; background: #f1f5f9; color: #64748b; font-size: 22rpx; font-weight: 700; }
</style>
