<script setup>
const props = defineProps({
  active: {
    type: String,
    required: true
  }
})

const tabs = [
  { key: 'home', text: '工作台', icon: '台', url: '/pages/home/index' },
  { key: 'process', text: '流程', icon: '流', url: '/pages/process/process' },
  { key: 'todos', text: '待办', icon: '办', url: '/pages/todos/todoList' },
  { key: 'profile', text: '我的', icon: '我', url: '/pages/profile/profile' }
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
      <view class="enterprise-tabbar-icon">{{ item.icon }}</view>
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
  width: 44rpx;
  height: 44rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 850;
  line-height: 1;
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

.enterprise-tabbar-item.active .enterprise-tabbar-icon {
  background: #dcfce7;
}
</style>
