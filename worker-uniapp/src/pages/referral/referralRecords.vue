<template>
  <view class="page">
    <view class="header">
      <text class="title">邀请记录</text>
      <text class="total">共 {{ total }} 人</text>
    </view>

    <view v-if="referees.length === 0 && !loading" class="empty">
      <text>暂无邀请记录</text>
    </view>

    <view v-for="item in referees" :key="item.id" class="record-item">
      <view class="record-left">
        <text class="record-name">{{ item.name }}</text>
        <text class="record-phone">{{ item.phone }}</text>
        <text class="record-time">邀请时间: {{ item.boundAt }}</text>
      </view>
      <view class="record-right">
        <view class="work-info">
          <text class="work-count">打工 {{ item.workCount }} 次</text>
          <text class="work-hours">工时 {{ item.workHours }} h</text>
        </view>
        <text class="reward-status" :style="{ color: rewardStatusColor(item.rewardStatus) }">
          {{ rewardStatusText(item.rewardStatus) }}
        </text>
      </view>
    </view>

    <view v-if="total > pageSize" class="load-more">
      <text @click="loadMore">{{ page * pageSize >= total ? '没有更多了' : '加载更多' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getReferees } from '@/api/referral'

const referees = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

async function fetchReferees() {
  loading.value = true
  try {
    const res = await getReferees({ page: page.value, pageSize: pageSize.value })
    referees.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取邀请记录失败', e)
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (page.value * pageSize.value < total.value) {
    page.value++
    fetchReferees()
  }
}

function rewardStatusText(status) {
  const map = {
    PENDING: '待审核',
    AUDITING: '审核中',
    GRANTED: '已发放',
    REJECTED: '已拒绝',
    NOT_QUALIFIED: '未达标'
  }
  return map[status] || status
}

function rewardStatusColor(status) {
  const map = {
    PENDING: '#e6a23c',
    AUDITING: '#909399',
    GRANTED: '#67c23a',
    REJECTED: '#f56c6c',
    NOT_QUALIFIED: '#c0c4cc'
  }
  return map[status] || '#909399'
}

onShow(() => {
  page.value = 1
  fetchReferees()
})
</script>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.title { font-size: 32rpx; font-weight: 600; color: #333; }
.total { font-size: 26rpx; color: #999; }
.empty { text-align: center; padding: 100rpx 0; color: #999; }
.record-item { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 8rpx; display: flex; justify-content: space-between; }
.record-left { flex: 1; }
.record-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.record-phone { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.record-time { font-size: 22rpx; color: #ccc; margin-top: 4rpx; display: block; }
.record-right { text-align: right; }
.work-info { margin-bottom: 8rpx; }
.work-count { font-size: 24rpx; color: #666; display: block; }
.work-hours { font-size: 24rpx; color: #666; display: block; }
.reward-status { font-size: 26rpx; font-weight: 500; }
.load-more { text-align: center; padding: 24rpx; color: #999; font-size: 26rpx; }
</style>
