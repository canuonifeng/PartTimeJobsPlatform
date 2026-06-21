<script setup>
import { ref, nextTick } from 'vue'

const AI_PROXY_BASE = import.meta.env.VITE_AI_PROXY_BASE_URL || 'http://localhost:8000'

function authHeader() {
  const raw = uni.getStorageSync('token')
  const token = (typeof raw === 'string' ? raw : '').replace(/[\r\n]/g, '').trim()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function aiRequest(path, data) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: AI_PROXY_BASE + path,
      method: 'POST',
      data,
      header: {
        'Content-Type': 'application/json',
        ...authHeader()
      },
      success: (res) => resolve(res.data),
      fail: reject
    })
  })
}

const messages = ref([
  { role: 'assistant', content: '你好！我是 AI 助手，可以帮你创建岗位和班次。例如：「帮我发布一个保安岗位，明天开始每天 14:00-18:00，时薪 25 元」' }
])
const inputText = ref('')
const sending = ref(false)
const messagesEnd = ref(null)
const showConfirm = ref(false)
const confirmData = ref(null)
const confirmAction = ref('')

const FUNCTION_ACTION_MAP = {
  create_job_and_schedules: 'create_job',
  update_job: 'update_job',
  add_schedule_to_job: 'add_schedule'
}

function scrollToBottom() {
  nextTick(() => {
    messagesEnd.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  sending.value = true
  const assistantMsg = { role: 'assistant', content: '', streaming: true }
  messages.value.push(assistantMsg)

  try {
    const history = messages.value.slice(0, -2).map(m => ({ role: m.role, content: m.content }))
    const res = await aiRequest('/api/chat/sync', {
      messages: [{ role: 'user', content: text }],
      history
    })

    if (res.code === 200) {
      const data = res.data
      await typewrite(assistantMsg, data.content || '')

      if (data.function_call) {
        const fn = data.function_call
        showConfirm.value = true
        confirmAction.value = FUNCTION_ACTION_MAP[fn.name] || 'create_job'
        confirmData.value = parseFunctionCall(fn)
      }
    } else {
      assistantMsg.content = '抱歉，请求失败，请重试'
    }
  } catch {
    assistantMsg.content = '网络异常，请检查连接后重试'
  } finally {
    assistantMsg.streaming = false
    sending.value = false
    scrollToBottom()
  }
}

async function typewrite(msg, text) {
  msg.content = ''
  const chars = text.split('')
  for (let i = 0; i < chars.length; i++) {
    msg.content += chars[i]
    if (i % 3 === 0) {
      await new Promise(r => setTimeout(r, 16))
    }
  }
}

function parseFunctionCall(func) {
  try {
    return typeof func.arguments === 'string' ? JSON.parse(func.arguments) : func.arguments
  } catch {
    return func.arguments || {}
  }
}

async function confirmCreate() {
  if (!confirmData.value) return
  const msg = messages.value[messages.value.length - 1]
  msg.content += '\n\n✅ 已确认，正在创建...'

  try {
    const res = await aiRequest('/api/chat/execute', {
      action: confirmAction.value || 'create_job',
      data: confirmData.value
    })
    if (res.code === 200) {
      msg.content += '\n✅ 操作成功！'
    } else {
      msg.content += `\n❌ 操作失败：${res.message || '未知错误'}`
    }
  } catch {
    msg.content += '\n❌ 操作失败，请稍后重试'
  }

  showConfirm.value = false
  confirmData.value = null
  scrollToBottom()
}

function cancelConfirm() {
  showConfirm.value = false
  confirmData.value = null
}

function editConfirm() {
  uni.showModal({
    title: '修改数据',
    content: '请在对话中说明需要修改的内容',
    showCancel: false
  })
}

function startRecord() {
  uni.authorize({
    scope: 'scope.record',
    success: () => {
      const recorder = uni.getRecorderManager()
      recorder.start({ format: 'mp3' })
      uni.showToast({ title: '录音中...', icon: 'none' })

      recorder.onStop(async (res) => {
        try {
          const uploadRes = await uni.uploadFile({
            url: `${AI_PROXY_BASE}/api/upload/asr`,
            filePath: res.tempFilePath,
            name: 'file'
          })
          const data = JSON.parse(uploadRes.data)
          if (data.data?.text) {
            inputText.value = data.data.text
          } else {
            uni.showToast({ title: '语音识别失败', icon: 'none' })
          }
        } catch {
          uni.showToast({ title: '上传失败', icon: 'none' })
        }
      })
    },
    fail: () => {
      uni.showToast({ title: '需要麦克风权限', icon: 'none' })
    }
  })
}

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      try {
        const uploadRes = await uni.uploadFile({
          url: `${AI_PROXY_BASE}/api/upload`,
          filePath: res.tempFilePaths[0],
          name: 'file'
        })
        const data = JSON.parse(uploadRes.data)
        if (data.code === 200) {
          messages.value.push({ role: 'user', content: '[图片已上传]' })
          inputText.value = '请识别这张图片并帮我创建对应的岗位'
          await sendMessage()
        }
      } catch {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    }
  })
}
</script>

<template>
  <view class="chat-page">
    <view class="chat-header">
      <view class="chat-header-left" @click="uni.navigateBack()">
        <text class="back-arrow">‹</text>
      </view>
      <view class="chat-header-center">
        <text class="chat-header-title">AI 创建</text>
        <text class="chat-header-sub">一句话创建岗位和班次</text>
      </view>
      <view class="chat-header-right"></view>
    </view>

    <scroll-view class="chat-messages" scroll-y scroll-with-animation :scroll-into-view="'msg-' + (messages.length - 1)">
      <view v-for="(msg, i) in messages" :key="i" :id="'msg-' + i" class="msg-row" :class="'msg-' + msg.role">
        <view v-if="msg.role === 'assistant'" class="msg-avatar">AI</view>
        <view class="msg-bubble">
          <text>{{ msg.content }}</text>
          <text v-if="msg.streaming" class="typing-cursor">|</text>
        </view>
      </view>

      <view v-if="showConfirm && confirmData" class="confirm-card">
        <view class="confirm-title">{{ confirmAction === 'create_job' ? '📋 确认创建' : confirmAction === 'update_job' ? '📝 确认修改' : '📅 确认新增班次' }}</view>
        <view class="confirm-field" v-if="confirmData.jobId">
          <text class="confirm-label">岗位ID</text>
          <text class="confirm-value">#{{ confirmData.jobId }}</text>
        </view>
        <view class="confirm-field" v-if="confirmData.title">
          <text class="confirm-label">岗位</text>
          <text class="confirm-value">{{ confirmData.title }}</text>
        </view>
        <view class="confirm-field" v-if="confirmData.salaryAmount">
          <text class="confirm-label">薪资</text>
          <text class="confirm-value">{{ confirmData.salaryType === 'DAILY' ? '日薪' : '时薪' }} ¥{{ confirmData.salaryAmount }}</text>
        </view>
        <view class="confirm-field" v-if="confirmData.schedules && confirmData.schedules.length">
          <text class="confirm-label">班次</text>
          <text class="confirm-value">{{ confirmData.schedules.length }} 个排班</text>
        </view>
        <view class="confirm-field" v-if="confirmData.scheduleDate">
          <text class="confirm-label">日期</text>
          <text class="confirm-value">{{ confirmData.scheduleDate }}</text>
        </view>
        <view class="confirm-field" v-if="confirmData.startTime">
          <text class="confirm-label">时间</text>
          <text class="confirm-value">{{ confirmData.startTime }} - {{ confirmData.endTime }}</text>
        </view>
        <view class="confirm-actions">
          <view class="confirm-btn cancel" @click="cancelConfirm">取消</view>
          <view class="confirm-btn edit" @click="editConfirm">修改</view>
          <view class="confirm-btn confirm" @click="confirmCreate">{{ confirmAction === 'create_job' ? '确认创建' : confirmAction === 'update_job' ? '确认修改' : '确认新增' }}</view>
        </view>
      </view>

      <view ref="messagesEnd" class="scroll-anchor"></view>
    </scroll-view>

    <view class="chat-input-bar">
      <view class="chat-input-area">
        <view class="chat-actions">
          <text class="chat-action-btn" @click="startRecord">🎤</text>
          <text class="chat-action-btn" @click="chooseImage">📷</text>
        </view>
        <input v-model="inputText" class="chat-input" confirm-type="send" placeholder="输入你的需求..." @confirm="sendMessage" />
        <view class="chat-send-btn" :class="{ active: inputText.trim() }" @click="sendMessage">
          <text>{{ sending ? '...' : '发送' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.chat-page { display: flex; flex-direction: column; height: 100vh; background: #f6f8f7; }
.chat-header { display: flex; align-items: center; padding: 24rpx 28rpx; padding-top: calc(88rpx + env(safe-area-inset-top)); background: linear-gradient(135deg, #18c86b, #08a95a); color: #fff; }
.chat-header-left { width: 60rpx; }
.back-arrow { font-size: 44rpx; color: #fff; font-weight: 300; }
.chat-header-center { flex: 1; text-align: center; }
.chat-header-title { display: block; font-size: 34rpx; font-weight: 800; }
.chat-header-sub { display: block; margin-top: 4rpx; font-size: 22rpx; opacity: .78; }
.chat-header-right { width: 60rpx; }
.chat-messages { flex: 1; overflow-y: auto; padding: 24rpx 28rpx 20rpx; box-sizing: border-box; }
.msg-row { margin-bottom: 24rpx; display: flex; align-items: flex-start; }
.msg-user { justify-content: flex-end; }
.msg-avatar { width: 56rpx; height: 56rpx; margin-right: 14rpx; border-radius: 50%; background: #16a34a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 850; flex-shrink: 0; }
.msg-bubble { max-width: 76%; padding: 20rpx 24rpx; border-radius: 20rpx; font-size: 27rpx; line-height: 1.6; word-break: break-all; white-space: pre-wrap; }
.msg-assistant .msg-bubble { background: #fff; color: #1f2933; border-bottom-left-radius: 4rpx; box-shadow: 0 8rpx 20rpx rgba(23,83,53,.06); }
.msg-user .msg-bubble { background: #16a34a; color: #fff; border-bottom-right-radius: 4rpx; }
.typing-cursor { display: inline; animation: blink 0.8s infinite; color: #16a34a; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0; } }
.scroll-anchor { height: 1rpx; }

.confirm-card { margin: 16rpx 0 24rpx; padding: 24rpx; border-radius: 24rpx; background: #fff; box-shadow: 0 8rpx 24rpx rgba(23,83,53,.08); border: 2rpx solid #16a34a; }
.confirm-title { font-size: 30rpx; font-weight: 800; color: #1f2933; margin-bottom: 18rpx; }
.confirm-field { display: flex; align-items: center; padding: 12rpx 0; border-bottom: 1rpx solid #f0f2f4; }
.confirm-field:last-of-type { border-bottom: none; }
.confirm-label { width: 100rpx; font-size: 24rpx; color: #64748b; flex-shrink: 0; }
.confirm-value { font-size: 26rpx; font-weight: 700; color: #1f2933; }
.confirm-actions { display: flex; gap: 14rpx; margin-top: 20rpx; }
.confirm-btn { flex: 1; height: 68rpx; line-height: 68rpx; border-radius: 999rpx; text-align: center; font-size: 26rpx; font-weight: 800; }
.confirm-btn.cancel { background: #f1f5f9; color: #64748b; }
.confirm-btn.edit { background: #fef3c7; color: #b45309; }
.confirm-btn.confirm { background: #16a34a; color: #fff; }

.chat-input-bar { padding: 16rpx 20rpx; padding-bottom: calc(16rpx + env(safe-area-inset-bottom)); background: #fff; border-top: 1rpx solid #edf0f3; }
.chat-input-area { display: flex; align-items: center; gap: 14rpx; }
.chat-actions { display: flex; gap: 10rpx; }
.chat-action-btn { font-size: 40rpx; line-height: 1; }
.chat-input { flex: 1; height: 72rpx; padding: 0 22rpx; border-radius: 36rpx; background: #f1f5f9; font-size: 27rpx; color: #1f2933; }
.chat-send-btn { height: 72rpx; padding: 0 28rpx; border-radius: 36rpx; background: #e5e7eb; color: #9ca3af; display: flex; align-items: center; justify-content: center; font-size: 27rpx; font-weight: 800; }
.chat-send-btn.active { background: #16a34a; color: #fff; }
</style>
