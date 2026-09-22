<template>
  <view class="media-page">
    <view v-if="loading" class="loading-wrap">
      <uni-load-more status="loading" />
    </view>

    <template v-else>
      <view class="title-card">
        <text class="lesson-title">{{ title }}</text>
        <text class="lesson-meta">{{ lessonType === 'VIDEO' ? '视频课程' : '音频课程' }} · 预计 {{ durationMinutes }} 分钟</text>
      </view>

      <view v-if="!mediaUrl" class="empty-state">
        <text class="empty-text">暂无媒体资源</text>
      </view>

      <video
        v-else-if="lessonType === 'VIDEO'"
        class="video-player"
        :src="mediaUrl"
        controls
        @timeupdate="onVideoTimeUpdate"
        @ended="onEnded"
      />

      <view v-else-if="lessonType === 'AUDIO'" class="audio-card">
        <view class="audio-play" @click="togglePlay">
          <text class="audio-play-icon">{{ playing ? '⏸' : '▶' }}</text>
        </view>
        <view class="audio-body">
          <view class="audio-bar">
            <view class="audio-bar-fill" :style="{ width: progressPercent + '%' }"></view>
          </view>
          <view class="audio-row">
            <text class="progress-time">{{ fmt(currentTime) }} / {{ fmt(totalDuration) }}</text>
            <text class="progress-pct">{{ progressPercent }}%</text>
          </view>
        </view>
      </view>

      <view class="progress-card">
        <view class="progress-bar">
          <view class="progress-inner" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <view class="progress-row">
          <text class="progress-time">学习进度 {{ progressPercent }}%</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { startLesson, reportProgress } from '@/api/training'

const lessonId = ref(0)
const lessonType = ref('')
const title = ref('')
const mediaUrl = ref('')
const durationMinutes = ref(0)
const loading = ref(true)
const completed = ref(false)
const playing = ref(false)

const currentTime = ref(0)
const totalDuration = ref(0)
const reportedProgress = ref(0)
const reporting = ref(false)
let audioCtx: any = null

const progressPercent = computed(() =>
  totalDuration.value ? Math.min(100, Math.round((currentTime.value / totalDuration.value) * 100)) : 0
)

function fmt(sec: number) {
  const s = Math.max(0, Math.floor(sec || 0))
  const m = Math.floor(s / 60)
  const r = s % 60
  return m + ':' + (r < 10 ? '0' + r : '' + r)
}

async function doReport(pct: number) {
  if (reporting.value) return
  reporting.value = true
  try {
    await reportProgress(lessonId.value, pct)
    reportedProgress.value = pct
  } catch {
  } finally {
    reporting.value = false
  }
}

function tick(ct: number, dur: number) {
  currentTime.value = ct || 0
  if (dur) totalDuration.value = dur
  const d = totalDuration.value
  if (!d) return
  const pct = Math.floor(((ct || 0) / d) * 100)
  if (pct > reportedProgress.value && pct < 100) {
    doReport(pct)
  }
}

function onVideoTimeUpdate(e: any) {
  const detail = e?.detail || {}
  tick(detail.currentTime, detail.duration)
}

function setupAudio() {
  const ctx = uni.createInnerAudioContext()
  ctx.src = mediaUrl.value
  ctx.onCanplay(() => {
    if (ctx.duration) totalDuration.value = ctx.duration
  })
  ctx.onPlay(() => {
    playing.value = true
  })
  ctx.onPause(() => {
    playing.value = false
  })
  ctx.onTimeUpdate(() => {
    tick(ctx.currentTime, ctx.duration)
  })
  ctx.onEnded(() => onEnded())
  audioCtx = ctx
}

function togglePlay() {
  if (!audioCtx) return
  if (playing.value) {
    audioCtx.pause()
  } else {
    audioCtx.play()
  }
}

async function onEnded() {
  if (completed.value) return
  completed.value = true
  try {
    await reportProgress(lessonId.value, 100)
  } catch {
  }
  uni.showToast({ title: '课时已完成', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 800)
}

async function load() {
  loading.value = true
  try {
    const res: any = await startLesson(lessonId.value)
    lessonType.value = res?.lessonType || ''
    title.value = res?.title || ''
    mediaUrl.value = res?.mediaUrl || ''
    durationMinutes.value = res?.durationMinutes || 0
    reportedProgress.value = Math.max(0, Math.min(100, res?.currentProgress || 0))
    uni.setNavigationBarTitle({ title: title.value || '课时学习' })
    if (lessonType.value === 'AUDIO' && mediaUrl.value) {
      setupAudio()
    }
  } catch {
    uni.showToast({ title: '课时加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((options: any) => {
  const id = Number(options?.lessonId)
  lessonId.value = Number.isFinite(id) && id > 0 ? id : 0
  load()
})

onUnload(() => {
  if (audioCtx) {
    try {
      audioCtx.destroy()
    } catch {
    }
    audioCtx = null
  }
})
</script>

<style scoped>
.media-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.loading-wrap {
  padding-top: 200rpx;
}

.title-card {
  padding: 32rpx;
}

.lesson-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.lesson-meta {
  font-size: 24rpx;
  color: #9ca3af;
}

.video-player {
  width: 100%;
  height: 420rpx;
  background: #000;
}

.audio-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin: 24rpx;
  padding: 32rpx 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.audio-play {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 10rpx 24rpx rgba(16, 185, 129, 0.35);
}

.audio-play-icon {
  color: #fff;
  font-size: 36rpx;
}

.audio-body {
  flex: 1;
}

.audio-bar {
  height: 12rpx;
  border-radius: 6rpx;
  background: #e5e7eb;
  overflow: hidden;
  margin-bottom: 14rpx;
}

.audio-bar-fill {
  height: 100%;
  border-radius: 6rpx;
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
}

.audio-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.progress-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.progress-time {
  font-size: 24rpx;
  color: #6b7280;
}

.progress-pct {
  font-size: 26rpx;
  font-weight: 700;
  color: #10b981;
}

.progress-card {
  margin: 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.progress-bar {
  height: 16rpx;
  border-radius: 8rpx;
  background: #e5e7eb;
  overflow: hidden;
}

.progress-inner {
  height: 100%;
  border-radius: 8rpx;
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  transition: width 0.3s;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
