<template>
  <view class="detail-page">
    <view class="course-header">
      <text class="course-title">{{ course.title }}</text>
      <text v-if="course.certificationName" class="course-cert">🏅 认证：{{ course.certificationName }}</text>
      <view class="progress-wrap">
        <view class="progress-bar">
          <view class="progress-inner" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <text class="progress-text">已完成 {{ completedCount }}/{{ lessons.length }} 课时</text>
      </view>
    </view>

    <uni-load-more v-if="loading" status="loading" />

    <view v-else-if="!course" class="empty-state">
      <text class="empty-text">课程不存在或已下架</text>
    </view>

    <view v-else class="lesson-list">
      <view v-if="lessons.length === 0" class="empty-state">
        <text class="empty-text">暂未发布课时</text>
      </view>
      <view
        v-for="(lesson, idx) in lessons"
        :key="lesson.id"
        class="lesson-card"
        :class="{ locked: lesson.locked }"
        @click="onLessonClick(lesson)"
      >
        <view class="lesson-icon" :class="typeClass(lesson.lessonType)">
          <text>{{ typeIcon(lesson.lessonType) }}</text>
        </view>
        <view class="lesson-main">
          <view class="lesson-top">
            <text class="lesson-title">{{ idx + 1 }}. {{ lesson.title }}</text>
            <text class="lesson-type" :class="typeClass(lesson.lessonType)">{{ typeText(lesson.lessonType) }}</text>
          </view>
          <view class="lesson-bottom">
            <text v-if="lesson.completed" class="lesson-status done">
              ✓ 已完成{{ lesson.lessonType === 'EXAM' && lesson.score != null ? '（' + lesson.score + '分）' : '' }}
            </text>
            <text v-else-if="lesson.locked" class="lesson-status locked">🔒 未解锁</text>
            <text v-else class="lesson-status todo">去学习</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { courseDetail } from '@/api/training'

const course = ref<any>(null)
const lessons = ref<any[]>([])
const loading = ref(true)
const courseId = ref(0)

const completedCount = computed(() => lessons.value.filter((l) => l.completed).length)
const progressPercent = computed(() =>
  lessons.value.length ? Math.round((completedCount.value / lessons.value.length) * 100) : 0
)

function typeIcon(t: string) {
  if (t === 'VIDEO') return '🎬'
  if (t === 'AUDIO') return '🎧'
  if (t === 'DOCUMENT') return '📄'
  if (t === 'IMAGE_TEXT') return '🖼️'
  if (t === 'EXAM') return '📝'
  return '📘'
}

function typeText(t: string) {
  if (t === 'VIDEO') return '视频'
  if (t === 'AUDIO') return '音频'
  if (t === 'DOCUMENT') return '文档'
  if (t === 'IMAGE_TEXT') return '图文'
  if (t === 'EXAM') return '考试'
  return '课时'
}

function typeClass(t: string) {
  if (t === 'VIDEO') return 't-video'
  if (t === 'AUDIO') return 't-audio'
  if (t === 'DOCUMENT') return 't-doc'
  if (t === 'IMAGE_TEXT') return 't-image'
  if (t === 'EXAM') return 't-exam'
  return 't-default'
}

function onLessonClick(lesson: any) {
  if (lesson.locked) {
    uni.showToast({ title: '请先完成上一课时', icon: 'none' })
    return
  }
  let path = ''
  if (lesson.lessonType === 'VIDEO' || lesson.lessonType === 'AUDIO') {
    path = '/pages/training/lessonMedia'
  } else if (lesson.lessonType === 'DOCUMENT' || lesson.lessonType === 'IMAGE_TEXT') {
    path = '/pages/training/lessonRead'
  } else if (lesson.lessonType === 'EXAM') {
    path = '/pages/training/lessonExam'
  }
  if (!path) {
    uni.showToast({ title: '暂不支持该类型课时', icon: 'none' })
    return
  }
  uni.navigateTo({ url: path + '?lessonId=' + lesson.id })
}

async function load() {
  loading.value = true
  try {
    const res: any = await courseDetail(courseId.value)
    course.value = res || null
    const list = Array.isArray(res?.lessons) ? res.lessons.slice() : []
    list.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
    lessons.value = list
  } catch {
    course.value = null
    lessons.value = []
    uni.showToast({ title: '课程加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((params: any) => {
  const id = Number(params.id)
  courseId.value = Number.isFinite(id) && id > 0 ? id : 0
  load()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40rpx;
}

.course-header {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  padding: 50rpx 32rpx 40rpx;
  color: #fff;
}

.course-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  margin-bottom: 12rpx;
}

.course-cert {
  font-size: 26rpx;
  opacity: 0.95;
}

.progress-wrap {
  margin-top: 28rpx;
}

.progress-bar {
  height: 16rpx;
  border-radius: 8rpx;
  background: rgba(255, 255, 255, 0.35);
  overflow: hidden;
}

.progress-inner {
  height: 100%;
  border-radius: 8rpx;
  background: #fff;
  transition: width 0.3s;
}

.progress-text {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  opacity: 0.95;
}

.lesson-list {
  padding: 24rpx;
  margin-top: -20rpx;
}

.lesson-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.lesson-card.locked {
  opacity: 0.7;
}

.lesson-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}

.t-video { background: #dbeafe; }
.t-audio { background: #ede9fe; }
.t-doc { background: #ffedd5; }
.t-image { background: #ccfbf1; }
.t-exam { background: #fee2e2; }
.t-default { background: #e5e7eb; }

.lesson-main {
  flex: 1;
  min-width: 0;
}

.lesson-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 10rpx;
}

.lesson-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.5;
}

.lesson-type {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.t-video.lesson-type { color: #2563eb; }
.t-audio.lesson-type { color: #7c3aed; }
.t-doc.lesson-type { color: #ea580c; }
.t-image.lesson-type { color: #0d9488; }
.t-exam.lesson-type { color: #dc2626; }
.t-default.lesson-type { color: #6b7280; }

.lesson-bottom {
  display: flex;
  align-items: center;
}

.lesson-status {
  font-size: 24rpx;
}

.lesson-status.done {
  color: #059669;
}

.lesson-status.locked {
  color: #9ca3af;
}

.lesson-status.todo {
  color: #10b981;
  font-weight: 600;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 160rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
