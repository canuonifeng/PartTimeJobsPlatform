<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { getNotifications } from '@/api/messages'
import EnterpriseTabBar from '@/components/EnterpriseTabBar.vue'

const authStore = useAuthStore()
const loading = ref(false)
const messages = ref([])

const stats = computed(() => [
  { label: '全部消息', value: messages.value.length, tone: 'green' },
  { label: '系统通知', value: messages.value.filter(item => messageKind(item) === 'NOTICE').length, tone: 'blue' },
  { label: '平台公告', value: messages.value.filter(item => messageKind(item) === 'ANNOUNCEMENT').length, tone: 'orange' }
])

const messageList = computed(() => messages.value.map(item => ({
  id: item.id,
  icon: typeIcon(item),
  type: typeLabel(item),
  channel: sourceLabel(item),
  title: item.title || defaultTitle(item),
  content: item.content || '暂无消息内容',
  status: typeLabel(item),
  statusClass: messageKind(item) === 'ANNOUNCEMENT' ? 'op-pill-warn' : '',
  time: formatTime(item.sentAt || item.createdAt)
})))

onShow(() => {
  uni.hideTabBar({ animation: false })
  loadMessages()
})

onPullDownRefresh(() => {
  loadMessages().finally(() => uni.stopPullDownRefresh())
})

async function loadMessages() {
  const companyId = authStore.companyId
  if (!companyId) {
    messages.value = []
    return
  }
  loading.value = true
  try {
    const res = await getNotifications(companyId, 'ENTERPRISE')
    messages.value = Array.isArray(res) ? res : []
  } catch {
    uni.showToast({ title: '消息加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function messageKind(item) {
  const raw = `${item?.type || ''} ${item?.category || ''}`.toUpperCase()
  if (raw.includes('ANNOUNCEMENT') || raw.includes('NOTICE_BOARD') || raw.includes('BULLETIN')) return 'ANNOUNCEMENT'
  return 'NOTICE'
}

function typeLabel(item) {
  return messageKind(item) === 'ANNOUNCEMENT' ? '平台公告' : '系统通知'
}

function typeIcon(item) {
  return messageKind(item) === 'ANNOUNCEMENT' ? '公' : '通'
}

function defaultTitle(item) {
  return messageKind(item) === 'ANNOUNCEMENT' ? '平台公告' : '系统通知'
}

function sourceLabel(item) {
  const map = { SMS: '短信', EMAIL: '邮件', APP_PUSH: '应用推送' }
  return map[item?.channel] || item?.channel || '站内消息'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <view class="op-page message-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">消息中心</text>
      <text class="op-hero-title">接收系统通知与平台公告</text>
      <text class="op-hero-desc">平台发布的重要通知、公告会集中展示在这里</text>
    </view>

    <view class="stats-grid message-stats">
      <view v-for="item in stats" :key="item.label" class="stat-card message-stat" :class="'stat-' + item.tone">
        <text class="stat-value">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="op-content">
      <view class="op-section-head message-section-head">
        <text class="op-section-title">最新消息</text>
        <text class="message-count">{{ messageList.length }} 条</text>
      </view>

      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>
      <view v-else-if="messageList.length === 0" class="e-empty">
        <view class="empty-illustration">消</view>
        <text class="e-empty-title">暂无消息</text>
        <text class="e-empty-desc">系统通知和平台公告会展示在这里</text>
      </view>
      <view v-else class="message-list">
        <view v-for="item in messageList" :key="item.id" class="op-card message-card">
          <view class="message-head">
            <view class="message-icon">{{ item.icon }}</view>
            <view class="op-row-main">
              <text class="op-row-title">{{ item.title }}</text>
              <view class="message-meta-row">
                <text class="message-meta">{{ item.type }}</text>
                <text class="message-dot">·</text>
                <text class="message-meta">{{ item.channel }}</text>
              </view>
            </view>
            <text class="op-pill" :class="item.statusClass">{{ item.status }}</text>
          </view>
          <text class="message-content">{{ item.content }}</text>
          <view class="message-footer">
            <text class="message-time">{{ item.time }}</text>
            <text class="message-link">{{ item.channel }}</text>
          </view>
        </view>
      </view>
    </view>
    <EnterpriseTabBar active="messages" />
  </view>
</template>

<style>
.message-page { min-height: 100vh; padding-bottom: calc(140rpx + env(safe-area-inset-bottom)); }
.top-space { height: 24rpx; }
.message-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin: 22rpx 28rpx 0; }
.message-stat { position: relative; overflow: hidden; min-height: 130rpx; padding: 22rpx 18rpx; border-radius: 26rpx; background: #fff; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); box-sizing: border-box; }
.message-stat::after { content: ''; position: absolute; right: -22rpx; top: -24rpx; width: 80rpx; height: 80rpx; border-radius: 50%; opacity: .16; }
.message-stat.stat-green::after { background: #16a34a; }
.message-stat.stat-blue::after { background: #3b82f6; }
.message-stat.stat-orange::after { background: #f59e0b; }
.message-stat .stat-value { display: block; font-size: 42rpx; font-weight: 900; line-height: 1; color: #16a34a; }
.message-stat.stat-blue .stat-value { color: #2563eb; }
.message-stat.stat-orange .stat-value { color: #d97706; }
.message-stat .stat-label { display: block; margin-top: 14rpx; font-size: 22rpx; color: #64748b; font-weight: 750; }
.message-section-head { margin-bottom: 18rpx; }
.message-count { padding: 8rpx 18rpx; border-radius: 999rpx; background: #ecfdf5; color: #16a34a; font-size: 23rpx; font-weight: 800; }
.message-list { display: flex; flex-direction: column; gap: 20rpx; }
.message-card { position: relative; overflow: hidden; padding: 28rpx; }
.message-card::before { content: ''; position: absolute; left: 0; top: 28rpx; bottom: 28rpx; width: 8rpx; border-radius: 0 999rpx 999rpx 0; background: linear-gradient(180deg, #18c86b, #047857); }
.message-head { display: flex; align-items: center; min-width: 0; }
.message-icon { width: 82rpx; height: 82rpx; margin-right: 18rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #dcfce7, #bbf7d0); color: #12834a; font-size: 30rpx; font-weight: 900; flex-shrink: 0; }
.message-meta-row { display: flex; align-items: center; min-width: 0; margin-top: 8rpx; }
.message-meta { font-size: 23rpx; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.message-dot { margin: 0 8rpx; color: #cbd5e1; font-size: 22rpx; }
.message-content { display: block; margin-top: 20rpx; padding: 20rpx; border-radius: 22rpx; background: #f8fafc; color: #334155; font-size: 25rpx; line-height: 1.65; }
.message-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 18rpx; }
.message-time { font-size: 23rpx; color: #98a3b3; }
.message-link { font-size: 23rpx; color: #16a34a; font-weight: 800; }
.empty-illustration { width: 118rpx; height: 118rpx; margin: 0 auto 24rpx; border-radius: 38rpx; display: flex; align-items: center; justify-content: center; background: #ecfdf5; color: #16a34a; font-size: 42rpx; font-weight: 900; }
</style>
