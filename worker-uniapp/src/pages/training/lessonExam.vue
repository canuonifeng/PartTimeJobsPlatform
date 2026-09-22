<template>
  <view class="exam-page">
    <view v-if="loading" class="loading-wrap">
      <uni-load-more status="loading" />
    </view>

    <template v-else-if="phase === 'quiz'">
      <view class="exam-head">
        <view class="head-top">
          <text class="head-title">{{ title }}</text>
        </view>
        <view class="head-meta">
          <text class="meta-item">时长 {{ paper.durationMinutes }} 分钟</text>
          <text class="meta-item">总分 {{ paper.totalScore }} 分</text>
          <text class="meta-item">及格 {{ paper.passScore }} 分</text>
        </view>
        <view class="countdown" :class="{ urgent: remaining <= 60 }">
          <text>剩余 {{ fmtTime(remaining) }}</text>
        </view>
      </view>

      <scroll-view class="exam-scroll" scroll-y>
        <view v-for="(q, qi) in paper.questions" :key="q.questionId" class="q-card">
          <view class="q-top">
            <text class="q-type" :class="typeClass(q.questionType)">{{ typeLabel(q.questionType) }}</text>
            <text class="q-score">{{ q.score }}分</text>
          </view>
          <text class="q-stem">{{ qi + 1 }}. {{ q.stem }}</text>
          <view
            v-for="opt in q.options"
            :key="opt.key"
            class="opt-item"
            :class="{ selected: isSelected(q, opt.key) }"
            @click="choose(q, opt.key)"
          >
            <view class="opt-radio" :class="{ active: isSelected(q, opt.key), multi: q.questionType === 'MULTIPLE_CHOICE' }">
              <text v-if="isSelected(q, opt.key)" class="opt-check">✓</text>
            </view>
            <text class="opt-label">{{ opt.key }}. {{ opt.label }}</text>
          </view>
        </view>
        <view class="bottom-safe"></view>
      </scroll-view>

      <view class="action-bar">
        <button class="action-btn" :disabled="submitting" @click="onSubmit(false)">
          {{ submitting ? '提交中...' : '提交试卷' }}
        </button>
      </view>
    </template>

    <template v-else-if="phase === 'result' && result">
      <scroll-view class="exam-scroll" scroll-y>
        <view class="result-card" :class="result.passed ? 'pass' : 'fail'">
          <text class="result-title">{{ result.passed ? '🎉 恭喜通过' : '未通过' }}</text>
          <view class="result-score">
            <text class="result-num">{{ result.score }}</text>
            <text class="result-unit">分</text>
          </view>
          <text class="result-tip">及格分 {{ result.passScore }} / 总分 {{ result.totalScore }}</text>
        </view>

        <template v-if="result.passed && result.snapshot && result.snapshot.questions">
          <view class="review-title">答题回顾</view>
          <view v-for="(rq, ri) in result.snapshot.questions" :key="rq.questionId" class="q-card">
            <view class="q-top">
              <text class="q-type" :class="rq.correct ? 'pass' : 'fail'">{{ rq.correct ? '✓ 正确' : '✗ 错误' }}</text>
              <text class="q-score">{{ rq.score }}分</text>
            </view>
            <text class="q-stem">{{ ri + 1 }}. {{ rq.stem }}</text>
            <view class="ans-line">
              <text class="ans-label">你的答案：</text>
              <text class="ans-value" :class="rq.correct ? 'ok' : 'bad'">{{ answerText(rq, rq.myAnswer) }}</text>
            </view>
            <view class="ans-line">
              <text class="ans-label">正确答案：</text>
              <text class="ans-value ok">{{ answerText(rq, rq.correctAnswer) }}</text>
            </view>
          </view>
        </template>

        <view class="bottom-safe"></view>
      </scroll-view>

      <view class="action-bar">
        <button v-if="!result.passed" class="action-btn secondary" @click="retake">重新考试</button>
        <button class="action-btn" :class="{ secondary: !result.passed }" @click="goBack">返回课程</button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { startLesson, submitExam } from '@/api/training'

const lessonId = ref(0)
const title = ref('')
const paper = ref<any>(null)
const loading = ref(true)
const phase = ref<'quiz' | 'result'>('quiz')
const answers = ref<Record<string, any>>({})
const remaining = ref(0)
const submitting = ref(false)
const result = ref<any>(null)
let timer: any = null

function fmtTime(sec: number) {
  const s = Math.max(0, Math.floor(sec))
  const m = Math.floor(s / 60)
  const r = s % 60
  return (m < 10 ? '0' + m : '' + m) + ':' + (r < 10 ? '0' + r : '' + r)
}

function typeLabel(t: string) {
  if (t === 'SINGLE_CHOICE') return '单选题'
  if (t === 'MULTIPLE_CHOICE') return '多选题'
  if (t === 'JUDGE') return '判断题'
  return '题目'
}

function typeClass(t: string) {
  if (t === 'SINGLE_CHOICE') return 't-single'
  if (t === 'MULTIPLE_CHOICE') return 't-multi'
  if (t === 'JUDGE') return 't-judge'
  return 't-default'
}

function isSelected(q: any, key: string) {
  const v = answers.value[q.questionId]
  if (q.questionType === 'MULTIPLE_CHOICE') {
    return Array.isArray(v) && v.indexOf(key) >= 0
  }
  return v === key
}

function choose(q: any, key: string) {
  if (q.questionType === 'MULTIPLE_CHOICE') {
    const cur = Array.isArray(answers.value[q.questionId]) ? [...answers.value[q.questionId]] : []
    const i = cur.indexOf(key)
    if (i >= 0) cur.splice(i, 1)
    else cur.push(key)
    answers.value[q.questionId] = cur
  } else {
    answers.value[q.questionId] = key
  }
}

function buildAnswers() {
  const list: any[] = []
  for (const q of paper.value.questions) {
    const v = answers.value[q.questionId]
    if (q.questionType === 'MULTIPLE_CHOICE') {
      const arr = Array.isArray(v) ? v.filter((k) => k) : []
      if (arr.length) list.push({ questionId: q.questionId, answer: JSON.stringify(arr) })
    } else if (v) {
      list.push({ questionId: q.questionId, answer: String(v) })
    }
  }
  return list
}

function hasAnswer(q: any) {
  const v = answers.value[q.questionId]
  if (q.questionType === 'MULTIPLE_CHOICE') return Array.isArray(v) && v.length > 0
  return !!v
}

function startTimer(sec: number) {
  stopTimer()
  remaining.value = sec
  timer = setInterval(() => {
    remaining.value--
    if (remaining.value <= 0) {
      remaining.value = 0
      stopTimer()
      doSubmit(true)
    }
  }, 1000)
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function answerText(q: any, ans: any) {
  if (!ans) return '未作答'
  let keys: any
  try {
    keys = JSON.parse(ans)
  } catch {
    keys = String(ans).split(',')
  }
  if (!Array.isArray(keys)) keys = [ans]
  return keys
    .map((k: string) => {
      const opt = (q.options || []).find((o: any) => o.key === k)
      return (opt ? opt.label : k)
    })
    .join('、')
}

async function onSubmit(auto: boolean) {
  if (submitting.value) return
  if (!auto) {
    const unanswered = paper.value.questions.filter((q: any) => !hasAnswer(q)).length
    if (unanswered > 0) {
      uni.showModal({
        title: '未完成',
        content: `还有 ${unanswered} 题未作答，确定交卷吗？`,
        success: (r) => {
          if (r.confirm) doSubmit(false)
        }
      })
      return
    }
  }
  doSubmit(auto)
}

async function doSubmit(auto: boolean) {
  if (submitting.value) return
  submitting.value = true
  try {
    const res: any = await submitExam(lessonId.value, buildAnswers())
    stopTimer()
    result.value = res
    phase.value = 'result'
    if (auto) {
      uni.showToast({ title: '时间到，自动交卷', icon: 'none' })
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const res: any = await startLesson(lessonId.value)
    title.value = res?.title || ''
    paper.value = res?.paper || null
    uni.setNavigationBarTitle({ title: title.value || '课时考试' })
    if (!paper.value) {
      uni.showToast({ title: '试卷加载失败', icon: 'none' })
      return
    }
    const sec = (paper.value.durationMinutes || 0) * 60
    if (sec > 0) startTimer(sec)
  } catch {
    uni.showToast({ title: '试卷加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function retake() {
  phase.value = 'quiz'
  result.value = null
  answers.value = {}
  await load()
}

function goBack() {
  uni.navigateBack()
}

onLoad((options: any) => {
  const id = Number(options?.lessonId)
  lessonId.value = Number.isFinite(id) && id > 0 ? id : 0
  load()
})

onUnload(() => stopTimer())
</script>

<style scoped>
.exam-page {
  min-height: 100vh;
  background: #f5f6fa;
}

.loading-wrap {
  padding-top: 200rpx;
}

.exam-head {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  padding: 32rpx;
  color: #fff;
}

.head-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  margin-bottom: 16rpx;
}

.head-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  font-size: 24rpx;
  opacity: 0.95;
}

.countdown {
  font-size: 28rpx;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.2);
  display: inline-block;
  padding: 8rpx 24rpx;
  border-radius: 28rpx;
}

.countdown.urgent {
  background: rgba(220, 38, 38, 0.85);
}

.exam-scroll {
  height: calc(100vh - 260rpx);
}

.q-card {
  margin: 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.q-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.q-type {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
}

.t-single { color: #2563eb; background: #dbeafe; }
.t-multi { color: #7c3aed; background: #ede9fe; }
.t-judge { color: #0d9488; background: #ccfbf1; }
.t-default { color: #6b7280; background: #f3f4f6; }

.q-type.pass { color: #059669; background: #d1fae5; }
.q-type.fail { color: #dc2626; background: #fee2e2; }

.q-score {
  font-size: 24rpx;
  color: #9ca3af;
}

.q-stem {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.6;
  margin-bottom: 20rpx;
}

.opt-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f8fafc;
  margin-bottom: 12rpx;
}

.opt-item.selected {
  background: #ecfdf5;
  border: 2rpx solid #10b981;
}

.opt-radio {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.opt-radio.multi {
  border-radius: 8rpx;
}

.opt-radio.active {
  background: #10b981;
  border-color: #10b981;
}

.opt-check {
  color: #fff;
  font-size: 22rpx;
}

.opt-label {
  font-size: 26rpx;
  color: #374151;
}

.ans-line {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.ans-label {
  font-size: 24rpx;
  color: #9ca3af;
}

.ans-value {
  font-size: 26rpx;
  font-weight: 600;
}

.ans-value.ok { color: #059669; }
.ans-value.bad { color: #dc2626; }

.result-card {
  margin: 24rpx;
  padding: 48rpx 28rpx;
  border-radius: 20rpx;
  text-align: center;
}

.result-card.pass {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  color: #fff;
}

.result-card.fail {
  background: #fff;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.05);
}

.result-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  margin-bottom: 16rpx;
}

.result-score {
  margin-bottom: 12rpx;
}

.result-num {
  font-size: 88rpx;
  font-weight: 800;
}

.result-card.fail .result-num { color: #dc2626; }

.result-unit {
  font-size: 28rpx;
  margin-left: 8rpx;
}

.result-tip {
  font-size: 24rpx;
  opacity: 0.95;
}

.result-card.fail .result-tip {
  color: #6b7280;
}

.review-title {
  margin: 8rpx 24rpx 0;
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1a2e;
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
}

.action-btn[disabled] {
  opacity: 0.7;
}

.action-btn::after {
  border: none;
}
</style>
