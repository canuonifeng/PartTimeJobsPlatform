<script setup>
const steps = [
  { icon: '发', title: '发布职位', desc: '6 个岗位正在招聘中', tags: ['草稿 2', '已发布 6'], path: '/pages/jobs/jobList', state: 'done' },
  { icon: '报', title: '收到报名', desc: '今日新增 18 人报名', tags: ['总报名 48', '转化 +18%'], path: '/pages/applications/applicationList', state: 'done' },
  { icon: '审', title: '审核报名', desc: '12 人等待审核，建议优先处理', tags: ['待审核 12', '已通过 22'], path: '/pages/applications/applicationList', state: 'warn' },
  { icon: '班', title: '创建排班', desc: '5 个班次未满员', tags: ['今日 9 班', '缺口 7 人'], path: '/pages/schedules/scheduleList', state: 'done' },
  { icon: '薪', title: '薪资结算', desc: '待结算 5 条考勤记录', tags: ['预计 ¥1,240'], path: '/pages/attendance/attendanceList', state: 'muted' }
]

function navigateTo(path) {
  uni.navigateTo({ url: path })
}
</script>

<template>
  <view class="op-page process-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">招聘运营流程</text>
      <text class="op-hero-title">报名审核阶段需要优先处理</text>
      <text class="op-hero-desc">当前链路：发布正常 · 报名充足 · 审核积压</text>
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
  </view>
</template>

<style>
.process-page { min-height: 100vh; padding-bottom: calc(40rpx + env(safe-area-inset-bottom)); }
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
