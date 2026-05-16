<template>
  <view class="message-page">
    <view class="empty-state">
      <text class="empty-icon">&#xe602;</text>
      <text class="empty-text">暂无消息</text>
      <text class="empty-hint">系统通知和聊天消息将在这里显示</text>
    </view>
  </view>
</template>

<style scoped>
.message-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-icon {
  font-size: 80rpx;
  color: #ddd;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #ccc;
}
</style>
