<template>
  <view class="detail-page">
    <uni-load-more v-if="loading" status="loading" />

    <template v-if="!loading && course">
      <scroll-view class="detail-content" scroll-y>
        <view class="course-header">
          <text class="course-title">{{ course.title }}</text>
          <text v-if="course.certificationName" class="course-cert">🏅 认证：{{ course.certificationName }}</text>
        </view>

        <view v-if="course.content" class="content-card">
          <view class="card-title">
            <view class="title-icon"><text>📖</text></view>
            <text class="title-text">课程内容</text>
          </view>
          <text class="content-text">{{ course.content }}</text>
        </view>

        <view v-if="examMode" class="exam-card">
          <view class="card-title">
            <view class="title-icon"><text>📝</text></view>
            <text class="title-text">结业考试（{{ course.passScore }}分及格）</text>
          </view>
          <view v-if="questions.length === 0" class="empty-hint">
            <text>本课程暂无考试题，点击完成即可获得认证</text>
          </view>
          <view v-for="(q, qi) in questions" :key="qi" class="question-item">
            <text class="question-title">{{ qi + 1 }}. {{ q.question }}</text>
            <view
              v-for="(opt, oi) in q.options"
              :key="oi"
              class="option-item"
              :class="{ selected: answers[qi] === oi }"
              @click="selectAnswer(qi, oi)"
            >
              <view class="option-radio" :class="{ active: answers[qi] === oi }">
                <text v-if="answers[qi] === oi" class="option-check">✓</text>
              </view>
              <text class="option-text">{{ opt }}</text>
            </view>
          </view>
        </view>

        <view v-else-if="course.myStatus === 'COMPLETED' && course.myScore !== null" class="score-card">
          <view class="score-title">最近得分</view>
          <view class="score-value" :class="{ pass: course.myScore >= course.passScore }">{{ course.myScore }}分</view>
          <text class="score-tip" v-if="course.certified">🎉 已获得技能认证</text>
          <text class="score-tip" v-else>考试通过，可重新学习巩固</text>
        </view>

        <view class="bottom-safe"></view>
      </scroll-view>

      <view class="action-bar">
        <button
          v-if="!examMode"
          class="action-btn"
          :class="{ done: course.certified }"
          @click="startOrExam"
        >
          {{ course.certified ? '重新学习' : (course.myStatus === 'FAILED' ? '重新考试' : '开始学习') }}
        </button>
        <template v-else>
          <button class="action-btn secondary" @click="examMode = false">返回</button>
          <button class="action-btn" :disabled="submitting" @click="handleSubmit">{{ submitting ? '提交中...' : '提交考试' }}</button>
        </template>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getTrainingCourseDetail, startTrainingCourse, submitExam } from '@/api/training'

const course = ref<any>(null)
const loading = ref(true)
const courseId = ref(0)
const examMode = ref(false)
const questions = ref<any[]>([])
const answers = ref<number[]>([])
const submitting = ref(false)

async function load() {
  loading.value = true
  try {
    const res: any = await getTrainingCourseDetail(courseId.value)
    course.value = res || null
    questions.value = Array.isArray(res?.questions) ? res.questions : []
    answers.value = questions.value.map(() => -1)
    if (course.value?.myStatus === 'FAILED') {
      examMode.value = true
    }
  } catch {
    course.value = null
    uni.showToast({ title: '课程加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function selectAnswer(qi: number, oi: number) {
  answers.value[qi] = oi
}

async function startOrExam() {
  if (!course.value) return
  if (course.value.myStatus === 'NOT_STARTED' || course.value.certified) {
    try {
      await startTrainingCourse(courseId.value)
      course.value.myStatus = 'IN_PROGRESS'
    } catch (err: any) {
      uni.showToast({ title: err?.message || '操作失败', icon: 'none' })
      return
    }
  }
  if (questions.value.length === 0) {
    // 无考试题：直接完成
    await doSubmit([])
    return
  }
  examMode.value = true
}

async function handleSubmit() {
  if (answers.value.some((a) => a < 0)) {
    uni.showToast({ title: '请完成所有题目', icon: 'none' })
    return
  }
  await doSubmit(answers.value)
}

async function doSubmit(list: number[]) {
  submitting.value = true
  try {
    const res: any = await submitExam(courseId.value, list)
    if (res?.passed) {
      uni.showModal({
        title: '考试通过',
        content: `得分 ${res.score} 分，恭喜获得技能认证！`,
        showCancel: false,
        success: () => {
          examMode.value = false
          load()
        }
      })
    } else {
      uni.showModal({
        title: '未通过',
        content: `得分 ${res.score} 分，未达到 ${res.passScore} 分，可重新考试。`,
        showCancel: false,
        success: () => {
          examMode.value = false
          load()
        }
      })
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
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
  position: relative;
}

.detail-content {
  height: calc(100vh - 140rpx);
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

.content-card,
.exam-card,
.score-card {
  margin: 20rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.title-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #e6f5ee, #c6ecd9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.content-text {
  font-size: 28rpx;
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
}

.question-item {
  margin-bottom: 28rpx;
}

.question-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16rpx;
  line-height: 1.6;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f8fafc;
  margin-bottom: 12rpx;
}

.option-item.selected {
  background: #ecfdf5;
  border: 2rpx solid #10b981;
}

.option-radio {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.option-radio.active {
  background: #10b981;
  border-color: #10b981;
}

.option-check {
  color: #fff;
  font-size: 22rpx;
}

.option-text {
  font-size: 26rpx;
  color: #374151;
}

.empty-hint {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

.score-card {
  text-align: center;
  padding: 60rpx 28rpx;
}

.score-title {
  font-size: 26rpx;
  color: #9ca3af;
  margin-bottom: 16rpx;
}

.score-value {
  font-size: 72rpx;
  font-weight: 800;
  color: #dc2626;
  margin-bottom: 16rpx;
}

.score-value.pass {
  color: #059669;
}

.score-tip {
  font-size: 28rpx;
  color: #374151;
}

.bottom-safe {
  height: 40rpx;
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
  gap: 20rpx;
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

.action-btn.secondary {
  background: #f3f4f6;
  color: #374151;
  box-shadow: none;
  flex: 0 0 200rpx;
}

.action-btn.done {
  background: #e5e7eb;
  color: #6b7280;
  box-shadow: none;
}

.action-btn:active {
  transform: scale(0.98);
}

.action-btn::after {
  border: none;
}
</style>
