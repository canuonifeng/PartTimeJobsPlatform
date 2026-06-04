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
        {{ tab.label }}
      </view>
    </view>

    <view class="msg-list">
      <view v-for="message in filteredMessages" :key="message.id" class="msg-item">
        <view class="msg-avatar" :style="{ background: message.avatarBg }">
          <text>{{ message.avatar }}</text>
        </view>
        <view class="msg-content">
          <view class="msg-header">
            <text class="msg-title">{{ message.title }}</text>
            <text class="msg-time">{{ message.time }}</text>
          </view>
          <view class="msg-summary-row">
            <text class="msg-summary">{{ message.summary }}</text>
            <view v-if="message.unread" class="msg-unread"></view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

type TabKey = 'system' | 'company' | 'feedback'

interface MessageItem {
  id: number
  tab: TabKey
  avatar: string
  avatarBg: string
  title: string
  time: string
  summary: string
  unread: boolean
}

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'system', label: '系统通知' },
  { key: 'company', label: '企业消息' },
  { key: 'feedback', label: '报名反馈' },
]

const activeTab = ref<TabKey>('system')

const messages = ref<MessageItem[]>([
  {
    id: 1,
    tab: 'system',
    avatar: '通',
    avatarBg: 'linear-gradient(135deg, #38d27a 0%, #17b75f 100%)',
    title: '系统通知',
    time: '09:30',
    summary: '您的实名信息已审核通过，可继续报名心仪岗位。',
    unread: true,
  },
  {
    id: 2,
    tab: 'system',
    avatar: '收',
    avatarBg: 'linear-gradient(135deg, #ffc85a 0%, #ff9f2f 100%)',
    title: '收入到账',
    time: '昨天',
    summary: '本次兼职收入128.00元已到账，请前往钱包查看。',
    unread: true,
  },
  {
    id: 3,
    tab: 'company',
    avatar: '顺',
    avatarBg: 'linear-gradient(135deg, #44b8ff 0%, #1677ff 100%)',
    title: '顺丰速运',
    time: '周二',
    summary: '您报名的分拣员岗位已收到企业消息，请及时确认。',
    unread: true,
  },
  {
    id: 4,
    tab: 'company',
    avatar: '外',
    avatarBg: 'linear-gradient(135deg, #ff8a65 0%, #ff5a3c 100%)',
    title: '外婆家餐饮',
    time: '周一',
    summary: '门店已发送面试时间，请按约定时间到店沟通。',
    unread: false,
  },
  {
    id: 5,
    tab: 'feedback',
    avatar: '报',
    avatarBg: 'linear-gradient(135deg, #7bd88f 0%, #23c26b 100%)',
    title: '报名反馈',
    time: '06-01',
    summary: '您报名的便利店夜班理货员已通过初筛，等待企业联系。',
    unread: false,
  },
])

const filteredMessages = computed(() => messages.value.filter((message) => message.tab === activeTab.value))
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
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  font-size: 28rpx;
  color: #666666;
  border-radius: 22rpx;
}

.msg-tab.active {
  color: #ffffff;
  font-weight: 600;
  background: #20c26b;
  box-shadow: 0 8rpx 18rpx rgba(32, 194, 107, 0.24);
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
