<template>
  <view class="message-page">
    <view class="msg-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="msg-tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <text>{{ tab.label }}</text>
        <view v-if="hasUnreadCategory(messages, tab.key)" class="tab-unread"></view>
      </view>
    </view>

    <uni-load-more v-if="loading" status="loading" />

    <view v-else-if="filteredMessages.length === 0" class="empty-state">
      <text class="empty-text">暂无消息</text>
    </view>

    <view v-else class="msg-list">
      <view v-for="message in filteredMessages" :key="message.id" class="msg-item" @click="readMessage(message)">
        <view class="msg-avatar" :class="message.category">
          <text>{{ message.avatar }}</text>
        </view>
        <view class="msg-content">
          <view class="msg-header">
            <text class="msg-title">{{ message.title }}</text>
            <text class="msg-time">{{ message.time }}</text>
          </view>
          <view class="msg-summary-row">
            <text class="msg-summary">{{ message.summary }}</text>
            <view v-if="!message.read" class="msg-unread"></view>
          </view>
        </view>
      </view>
      <uni-load-more v-if="loadingMore" status="loading" />
    </view>
  </view>
  <InviteFloat />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { getMyNotifications, markNotificationRead } from '@/api/notifications'
import InviteFloat from '@/components/InviteFloat.vue'
import { hasUnreadCategory } from '@/utils/notificationBadges.mjs'
import { syncMessageTabBarBadge } from '@/utils/notificationBadge'

type TabKey = 'system' | 'application' | 'schedule' | 'finance'

interface MessageItem {
  id: number
  category: TabKey
  avatar: string
  title: string
  time: string
  summary: string
  read: boolean
}

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'system', label: '系统通知' },
  { key: 'application', label: '报名反馈' },
  { key: 'schedule', label: '排班提醒' },
  { key: 'finance', label: '收入提现' }
]

const activeTab = ref<TabKey>('system')
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const messages = ref<MessageItem[]>([])

const filteredMessages = computed(() => messages.value.filter((message) => message.category === activeTab.value))

function categoryOf(item: any): TabKey {
  const category = String(item?.category || '').toLowerCase()
  if (category === 'application' || category === 'schedule' || category === 'finance') return category
  const type = String(item?.type || '').toUpperCase()
  if (type.includes('APPLICATION')) return 'application'
  if (type.includes('SCHEDULE') || type.includes('SHIFT')) return 'schedule'
  if (type.includes('EARNING') || type.includes('WITHDRAW') || type.includes('PAY')) return 'finance'
  return 'system'
}

function avatarOf(category: TabKey) {
  const map: Record<TabKey, string> = {
    system: '通',
    application: '报',
    schedule: '班',
    finance: '收'
  }
  return map[category]
}

function formatTime(value: any) {
  const text = String(value || '')
  if (!text) return ''
  const normalized = text.replace('T', ' ')
  const today = new Date().toISOString().slice(0, 10)
  if (normalized.startsWith(today) && normalized.length >= 16) return normalized.slice(11, 16)
  if (normalized.length >= 10) return normalized.slice(5, 10)
  return normalized
}

function mapMessage(item: any): MessageItem {
  const category = categoryOf(item)
  const timeValue = item?.createdAt || item?.sentAt || item?.time
  return {
    id: Number(item?.id),
    category,
    avatar: avatarOf(category),
    title: item?.title || '消息通知',
    time: formatTime(timeValue),
    summary: item?.content || item?.summary || '',
    read: Boolean(item?.read)
  }
}

function recordsOf(res: any) {
  return Array.isArray(res) ? res : (Array.isArray(res?.records) ? res.records : [])
}

async function loadMessages(reset = true) {
  if (reset) {
    page.value = 1
    loading.value = true
  } else {
    if (loadingMore.value || messages.value.length >= total.value) return
    loadingMore.value = true
  }
  try {
    const res: any = await getMyNotifications({ page: page.value, pageSize })
    const records = recordsOf(res).map(mapMessage)
    total.value = Number(res?.total ?? records.length)
    messages.value = reset ? records : messages.value.concat(records)
    syncMessageTabBarBadge(messages.value)
    if (messages.value.length < total.value) page.value += 1
  } catch (e: any) {
    if (reset) {
      messages.value = []
      total.value = 0
      syncMessageTabBarBadge(messages.value)
    }
    uni.showToast({ title: e?.message || '消息加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function readMessage(message: MessageItem) {
  if (message.read) return
  try {
    await markNotificationRead(message.id)
    message.read = true
    syncMessageTabBarBadge(messages.value)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '标记已读失败', icon: 'none' })
  }
}

onMounted(() => loadMessages(true))
onReachBottom(() => loadMessages(false))
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  padding: 24rpx;
  background: #f6f7f8;
  box-sizing: border-box;
}

.msg-tabs {
  display: flex;
  align-items: center;
  padding: 8rpx;
  margin-bottom: 24rpx;
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.msg-tab {
  position: relative;
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  font-size: 26rpx;
  color: #666666;
  border-radius: 22rpx;
}

.msg-tab.active {
  color: #ffffff;
  font-weight: 600;
  background: #20c26b;
  box-shadow: 0 8rpx 18rpx rgba(32, 194, 107, 0.24);
}

.tab-unread {
  position: absolute;
  top: 14rpx;
  right: 22rpx;
  width: 12rpx;
  height: 12rpx;
  background: #ff3b30;
  border-radius: 50%;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-text {
  color: #999999;
  font-size: 28rpx;
}

.msg-list {
  display: flex;
  flex-direction: column;
}

.msg-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 28rpx 24rpx;
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.05);
}

.msg-item:last-child {
  margin-bottom: 0;
}

.msg-avatar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  margin-right: 22rpx;
  border-radius: 50%;
  color: #ffffff;
  font-size: 34rpx;
  font-weight: 600;
}

.msg-avatar.system {
  background: linear-gradient(135deg, #38d27a 0%, #17b75f 100%);
}

.msg-avatar.application {
  background: linear-gradient(135deg, #7bd88f 0%, #23c26b 100%);
}

.msg-avatar.schedule {
  background: linear-gradient(135deg, #44b8ff 0%, #1677ff 100%);
}

.msg-avatar.finance {
  background: linear-gradient(135deg, #ffc85a 0%, #ff9f2f 100%);
}

.msg-content {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14rpx;
}

.msg-title {
  flex: 1;
  min-width: 0;
  color: #222222;
  font-size: 30rpx;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-time {
  flex-shrink: 0;
  margin-left: 16rpx;
  color: #999999;
  font-size: 24rpx;
}

.msg-summary-row {
  display: flex;
  align-items: center;
}

.msg-summary {
  flex: 1;
  min-width: 0;
  color: #888888;
  font-size: 26rpx;
  line-height: 36rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-unread {
  flex-shrink: 0;
  width: 14rpx;
  height: 14rpx;
  margin-left: 16rpx;
  background: #ff3b30;
  border-radius: 50%;
}
</style>
