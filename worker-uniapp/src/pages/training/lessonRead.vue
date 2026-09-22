<template>
  <view class="read-page">
    <view v-if="loading" class="loading-wrap">
      <uni-load-more status="loading" />
    </view>

    <template v-else>
      <scroll-view class="read-scroll" scroll-y>
        <view class="read-card">
          <view class="read-meta">{{ lessonType === 'IMAGE_TEXT' ? '图文课程' : '文档课程' }}</view>
          <rich-text v-if="content" class="read-content" :nodes="content"></rich-text>
          <view v-else class="empty-state">
            <text class="empty-text">暂无阅读内容</text>
          </view>
        </view>
        <view class="bottom-safe"></view>
      </scroll-view>

      <view class="action-bar">
        <button class="action-btn" :disabled="submitting" @click="onComplete">
          {{ submitting ? '提交中...' : '标记完成阅读' }}
        </button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { startLesson, completeLesson } from '@/api/training'

const lessonId = ref(0)
const lessonType = ref('')
const title = ref('')
const content = ref('')
const loading = ref(true)
const submitting = ref(false)

async function load() {
  loading.value = true
  try {
    const res: any = await startLesson(lessonId.value)
    lessonType.value = res?.lessonType || ''
    title.value = res?.title || ''
    content.value = res?.content || ''
    uni.setNavigationBarTitle({ title: title.value || '课时阅读' })
  } catch {
    uni.showToast({ title: '课时加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function onComplete() {
  if (submitting.value) return
  submitting.value = true
  try {
    await completeLesson(lessonId.value)
    uni.showToast({ title: '已完成阅读', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch (err: any) {
    uni.showToast({ title: err?.message || '操作失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad((options: any) => {
  const id = Number(options?.lessonId)
  lessonId.value = Number.isFinite(id) && id > 0 ? id : 0
  load()
})
</script>

<style scoped>
.read-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.loading-wrap {
  padding-top: 200rpx;
}

.read-scroll {
  height: calc(100vh - 140rpx);
}

.read-card {
  margin: 24rpx;
  padding: 32rpx 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.read-meta {
  display: inline-block;
  font-size: 22rpx;
  color: #ea580c;
  background: #ffedd5;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.read-content {
  font-size: 28rpx;
  color: #1f2937;
  line-height: 1.8;
}

.bottom-safe {
  height: 40rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 140rpx;
  padding: 20rpx 24rpx 40rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  align-items: center;
  z-index: 50;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #34d399 0%, #10b981 50%, #059669 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  border: none;
  margin: 0;
  box-shadow: 0 10rpx 28rpx rgba(16, 185, 129, 0.35);
}

.action-btn[disabled] {
  opacity: 0.7;
}

.action-btn::after {
  border: none;
}
</style>
