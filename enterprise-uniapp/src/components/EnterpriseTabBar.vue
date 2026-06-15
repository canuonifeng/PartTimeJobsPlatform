<script setup>
const props = defineProps({
  active: {
    type: String,
    required: true
  }
})

const tabs = [
  {
    key: 'home',
    text: '工作台',
    icon: '/static/tab-home.png',
    activeIcon: '/static/tab-home-active.png',
    url: '/pages/home/index'
  },
  {
    key: 'process',
    text: '招聘',
    icon: '/static/tab-jobs.png',
    activeIcon: '/static/tab-jobs-active.png',
    url: '/pages/process/process'
  },
  {
    key: 'messages',
    text: '消息',
    icon: '/static/tab-message.png',
    activeIcon: '/static/tab-message-active.png',
    url: '/pages/messages/messageList'
  },
  {
    key: 'profile',
    text: '我的',
    icon: '/static/tab-profile.png',
    activeIcon: '/static/tab-profile-active.png',
    url: '/pages/profile/profile'
  }
]

function switchTab(item) {
  if (item.key === props.active) return
  uni.switchTab({ url: item.url })
}
</script>

<template>
  <view class="enterprise-tabbar">
    <view
      v-for="item in tabs"
      :key="item.key"
      class="enterprise-tabbar-item"
      :class="{ active: active === item.key }"
      @click="switchTab(item)"
    >
      <image
        class="enterprise-tabbar-icon"
        :src="active === item.key ? item.activeIcon : item.icon"
        mode="aspectFit"
      />
      <text class="enterprise-tabbar-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<style scoped>
.enterprise-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99999;
  display: flex;
  align-items: center;
  padding: 10rpx 16rpx calc(10rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 1rpx solid #edf0f3;
  box-shadow: 0 -8rpx 28rpx rgba(15, 23, 42, 0.08);
  box-sizing: border-box;
}

.enterprise-tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0;
  color: #98a3b3;
}

.enterprise-tabbar-icon {
  width: 48rpx;
  height: 48rpx;
}

.enterprise-tabbar-text {
  margin-top: 4rpx;
  font-size: 22rpx;
  line-height: 1.2;
  font-weight: 750;
}

.enterprise-tabbar-item.active {
  color: #16a34a;
}

</style>
