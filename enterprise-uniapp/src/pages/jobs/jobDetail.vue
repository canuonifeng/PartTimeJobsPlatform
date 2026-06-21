<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getJob, getRates, getSchedules } from '@/api/jobs'

const job = ref(null)
const rates = ref([])
const schedules = ref([])
const loading = ref(true)

onLoad(async (params) => {
  if (params.id) {
    await loadDetail(params.id)
  } else {
    loading.value = false
    uni.showToast({ title: '参数错误', icon: 'none' })
  }
})

async function loadDetail(id) {
  loading.value = true
  try {
    job.value = await getJob(id)
    try {
      const rateRes = await getRates(id)
      rates.value = Array.isArray(rateRes) ? rateRes : []
    } catch {}
    try {
      const schedRes = await getSchedules(id)
      schedules.value = Array.isArray(schedRes) ? schedRes : []
    } catch {}
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function statusLabel(s) {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭' }
  return map[s] || s
}

function rateTypeLabel(t) {
  const map = { HOURLY: '时薪', DAILY: '日薪', PIECEWORK: '计件' }
  return map[t] || t
}
</script>

<template>
  <view class="page e-page">
    <view class="header e-header">
      <view class="e-card-title-row">
        <text class="detail-title e-header-title">{{ job?.title || '职位详情' }}</text>
        <text v-if="job" class="e-badge" :class="'e-badge-' + (job.status === 'PUBLISHED' ? 'green' : job.status === 'CLOSED' ? 'red' : 'gray')">
          {{ statusLabel(job.status) }}
        </text>
      </view>
      <text class="e-header-desc">查看职位信息、薪资、时间与地点</text>
    </view>

    <scroll-view scroll-y class="detail-scroll">
      <view class="content e-content">
        <view v-if="loading" class="e-empty">
          <text class="e-empty-title">加载中...</text>
        </view>

        <view v-else-if="job" class="detail-content">
          <view class="e-card">
            <text class="e-section-title">基本信息</text>
            <view class="info-row">
              <text class="info-label">职位名称</text>
              <text class="info-value">{{ job.title || '暂无' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">职位描述</text>
              <text class="info-value">{{ job.description || '暂无' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">截止日期</text>
              <text class="info-value">{{ job.deadline || '不限' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">创建时间</text>
              <text class="info-value">{{ job.createdAt || job.createTime || '暂无' }}</text>
            </view>
          </view>

          <view class="e-card">
            <text class="e-section-title">薪资与人数</text>
            <view class="info-row">
              <text class="info-label">招聘人数</text>
              <text class="info-value">{{ job.headcount }}</text>
            </view>
            <view v-if="rates.length === 0" class="info-row">
              <text class="info-label">薪资标准</text>
              <text class="info-value">暂无</text>
            </view>
            <view v-for="(rate, i) in rates" :key="i" class="info-row">
              <text class="info-label">{{ rateTypeLabel(rate.type) }}</text>
              <text class="info-value">{{ rate.amount }} {{ rate.currency || 'CNY' }}</text>
            </view>
          </view>

          <view class="e-card">
            <text class="e-section-title">工作时间</text>
            <view v-if="schedules.length === 0" class="info-row">
              <text class="info-label">排班时段</text>
              <text class="info-value">暂无</text>
            </view>
            <view v-for="(sched, i) in schedules" :key="i" class="info-row">
              <text class="info-label">{{ sched.scheduleDate || sched.date || '日期' }}</text>
              <text class="info-value">
                {{ sched.startTime || '开始时间' }} - {{ sched.endTime || '结束时间' }}
              </text>
            </view>
          </view>

          <view class="e-card">
            <text class="e-section-title">工作地点</text>
            <view class="info-row">
              <text class="info-label">地点名称</text>
              <text class="info-value">{{ job.location || '暂无' }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">详细地址</text>
              <text class="info-value">{{ job.address || '暂无' }}</text>
            </view>
          </view>

          <view class="e-card">
            <text class="e-section-title">岗位要求</text>
            <view class="info-row">
              <text class="info-label">要求说明</text>
              <text class="info-value">{{ job.requirements || '暂无' }}</text>
            </view>
          </view>
        </view>

        <view v-else class="e-empty">
          <text class="e-empty-title">暂无数据</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<style scoped>
.page {
  box-sizing: border-box;
}

.header {
  box-sizing: border-box;
}

.detail-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-scroll {
  height: calc(100vh - 178rpx);
  box-sizing: border-box;
}

.content {
  padding-bottom: 56rpx;
}

.detail-content {
  box-sizing: border-box;
}

.info-row {
  display: flex;
  padding: 16rpx 0;
  border-bottom: 2rpx solid #f1f5f3;
  box-sizing: border-box;
}

.info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-label {
  width: 150rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  line-height: 1.5;
  color: #98a3b3;
}

.info-value {
  flex: 1;
  min-width: 0;
  font-size: 26rpx;
  line-height: 1.5;
  color: #1f2933;
  word-break: break-all;
}
</style>
