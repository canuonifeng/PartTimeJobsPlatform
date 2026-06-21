<script setup>
import { ref, nextTick } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const AI_PROXY_BASE = import.meta.env.VITE_AI_PROXY_BASE_URL || 'http://localhost:8000'
const STORAGE_KEY = 'ai_chat_history'
const MAX_HISTORY_TOKENS = 20000
const MAX_CONTENT_LEN = 200

const GREETING = '你好！我是 AI 助手，可以帮你创建岗位和班次。例如：「帮我发布一个保安岗位，明天开始每天 14:00-18:00，时薪 25 元」'

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

function estimateTokens(text) {
  if (!text) return 0
  let t = 0
  for (const ch of text) {
    if (ch >= '\u4e00' && ch <= '\u9fff') t += 2
    else if (/[a-zA-Z0-9]/.test(ch)) t += 0.25
    else t += 0.5
  }
  return Math.ceil(t)
}

function compressMessage(msg) {
  if (msg.role === 'assistant' && msg.content && msg.content.length > MAX_CONTENT_LEN) {
    return { role: 'assistant', content: msg.content.slice(0, MAX_CONTENT_LEN) + '...[省略]' }
  }
  return msg
}

function buildHistory(messages) {
  const candidates = messages.slice(0, -2).map(compressMessage)
  let total = 0
  const result = []
  for (let i = candidates.length - 1; i >= 0; i--) {
    const t = estimateTokens(candidates[i].content || '')
    if (total + t > MAX_HISTORY_TOKENS) break
    total += t
    result.unshift(candidates[i])
  }
  return result
}

function saveMessages() {
  try {
    uni.setStorageSync(STORAGE_KEY, JSON.stringify(messages.value))
  } catch (_) {}
}

function loadMessages() {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY)
    if (raw) {
      const m = JSON.parse(raw)
      if (Array.isArray(m) && m.length) {
        messages.value = m
        return
      }
    }
  } catch (_) {}
  messages.value = [{ role: 'assistant', content: GREETING }]
}

function clearHistory() {
  uni.showModal({
    title: '清空记录',
    content: '确定清空所有对话记录？',
    success: (r) => {
      if (r.confirm) {
        try { uni.removeStorageSync(STORAGE_KEY) } catch (_) {}
        messages.value = [{ role: 'assistant', content: GREETING }]
      }
    }
  })
}

onShow(loadMessages)

const messages = ref([{ role: 'assistant', content: GREETING }])
const inputText = ref('')
const sending = ref(false)
const messagesEnd = ref(null)
const showConfirm = ref(false)
const confirmData = ref(null)
const confirmAction = ref('')

const ACTION_LABELS = {
  create_job: '确认创建',
  update_job: '确认修改',
  add_schedule: '确认新增班次',
  update_schedule: '确认修改班次',
  copy_schedule: '确认复制班次',
  create_schedules: '确认批量创建班次',
  accept_application: '确认通过报名',
  reject_application: '确认拒绝报名',
  close_job: '确认关闭岗位',
  reopen_job: '确认重新开放岗位',
  cancel_schedule: '确认取消班次',
  update_attendance_hours: '确认修改工时',
  pay_attendance: '确认结算',
  unsettle_attendance: '确认撤回结算',
  batch_accept: '确认批量通过',
  batch_pay: '确认批量结算'
}

const ACTION_TITLE_MAP = {
  create_job: '📋 确认创建岗位',
  update_job: '📝 确认修改岗位',
  add_schedule: '📅 确认新增班次',
  update_schedule: '📝 确认修改班次',
  copy_schedule: '📋 确认复制班次',
  create_schedules: '📅 确认批量创建班次',
  accept_application: '✅ 确认通过报名',
  reject_application: '❌ 确认拒绝报名',
  close_job: '🔒 确认关闭岗位',
  reopen_job: '🔓 确认重新开放岗位',
  cancel_schedule: '🚫 确认取消班次',
  update_attendance_hours: '✏️ 确认修改工时',
  pay_attendance: '💰 确认结算',
  unsettle_attendance: '↩️ 确认撤回结算',
  batch_accept: '✅ 确认批量通过',
  batch_pay: '💰 确认批量结算'
}

const INNER_ACTION_FUNCS = ['execute_action', 'batch_action']

function resolveConfirmAction(fn) {
  if (INNER_ACTION_FUNCS.includes(fn.name)) {
    return fn.arguments?.action || ''
  }
  const map = {
    create_job_and_schedules: 'create_job',
    update_job: 'update_job',
    add_schedule_to_job: 'add_schedule',
    update_schedule: 'update_schedule',
    copy_schedule: 'copy_schedule',
    batch_create_schedules: 'create_schedules'
  }
  return map[fn.name] || ''
}

function resolveConfirmData(fn) {
  const args = parseFunctionCall(fn)
  if (INNER_ACTION_FUNCS.includes(fn.name)) {
    if (fn.arguments?.action?.startsWith('batch_')) {
      return { targetIds: args.targetIds || [], filters: args.filters }
    }
    return { targetId: args.targetId, reason: args.reason, updates: args.updates }
  }
  return args
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
  const assistantMsg = { role: 'assistant', content: '🤔 思考中...', streaming: true }
  messages.value.push(assistantMsg)

  try {
    const history = buildHistory(messages.value)
    const res = await aiRequest('/api/chat/sync', {
      messages: [{ role: 'user', content: text }],
      history
    })

    if (res.code === 200) {
      const data = res.data
      assistantMsg.content = ''
      await typewrite(assistantMsg, data.content || '')

      if (data.function_call) {
        const fn = data.function_call
        showConfirm.value = true
        confirmAction.value = resolveConfirmAction(fn)
        confirmData.value = resolveConfirmData(fn)
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
    saveMessages()
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
  saveMessages()
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
      <view class="chat-header-right">
        <text class="clear-btn" @click="clearHistory">🗑️</text>
      </view>
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
        <view class="confirm-title">{{ ACTION_TITLE_MAP[confirmAction] || '📋 确认操作' }}</view>

        <!-- 报名操作卡（accept_application / reject_application） -->
        <template v-if="['accept_application','reject_application'].includes(confirmAction)">
          <view class="confirm-field">
            <text class="confirm-label">目标</text>
            <text class="confirm-value">报名 #{{ confirmData.targetId }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.reason">
            <text class="confirm-label">原因</text>
            <text class="confirm-value">{{ confirmData.reason }}</text>
          </view>
        </template>

        <!-- 状态操作卡（close_job / reopen_job / cancel_schedule） -->
        <template v-if="['close_job','reopen_job'].includes(confirmAction)">
          <view class="confirm-field">
            <text class="confirm-label">岗位ID</text>
            <text class="confirm-value">#{{ confirmData.targetId }}</text>
          </view>
        </template>
        <template v-if="confirmAction === 'cancel_schedule'">
          <view class="confirm-field">
            <text class="confirm-label">班次ID</text>
            <text class="confirm-value">#{{ confirmData.targetId }}</text>
          </view>
        </template>

        <!-- 考勤操作卡（pay_attendance / unsettle_attendance） -->
        <template v-if="['pay_attendance','unsettle_attendance'].includes(confirmAction)">
          <view class="confirm-field">
            <text class="confirm-label">考勤ID</text>
            <text class="confirm-value">#{{ confirmData.targetId }}</text>
          </view>
        </template>

        <!-- 考勤编辑卡（update_attendance_hours） -->
        <template v-if="confirmAction === 'update_attendance_hours'">
          <view class="confirm-field">
            <text class="confirm-label">考勤ID</text>
            <text class="confirm-value">#{{ confirmData.targetId }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.updates?.totalHours !== undefined">
            <text class="confirm-label">工时</text>
            <text class="confirm-value">{{ confirmData.updates.totalHours }} 小时</text>
          </view>
          <view class="confirm-field" v-if="confirmData.updates?.payablePay !== undefined">
            <text class="confirm-label">应付</text>
            <text class="confirm-value">¥{{ confirmData.updates.payablePay }}</text>
          </view>
        </template>

        <!-- 批量操作卡（batch_accept / batch_pay） -->
        <template v-if="confirmAction.startsWith('batch_')">
          <view class="confirm-field" v-if="confirmData.targetIds?.length">
            <text class="confirm-label">数量</text>
            <text class="confirm-value">{{ confirmData.targetIds.length }} 条</text>
          </view>
          <view class="confirm-field" v-if="confirmData.filters?.jobTitle">
            <text class="confirm-label">岗位</text>
            <text class="confirm-value">{{ confirmData.filters.jobTitle }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.filters?.settlementStatus">
            <text class="confirm-label">状态</text>
            <text class="confirm-value">{{ confirmData.filters.settlementStatus }}</text>
          </view>
        </template>

        <!-- 岗位/班次创建编辑卡片（原有布局） -->
        <template v-if="['create_job','update_job','add_schedule','update_schedule','copy_schedule','create_schedules'].includes(confirmAction)">
          <view class="confirm-field" v-if="confirmData.jobId && !confirmData.scheduleId">
            <text class="confirm-label">编号</text>
            <text class="confirm-value">#{{ confirmData.jobId }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.scheduleId">
            <text class="confirm-label">班次ID</text>
            <text class="confirm-value">#{{ confirmData.scheduleId }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.sourceScheduleId">
            <text class="confirm-label">源班次</text>
            <text class="confirm-value">#{{ confirmData.sourceScheduleId }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.title">
            <text class="confirm-label">岗位</text>
            <text class="confirm-value">{{ confirmData.title }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.description">
            <text class="confirm-label">职责</text>
            <text class="confirm-value">{{ confirmData.description }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.requirements">
            <text class="confirm-label">要求</text>
            <text class="confirm-value">{{ confirmData.requirements }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.headcount">
            <text class="confirm-label">人数</text>
            <text class="confirm-value">{{ confirmData.headcount }} 人</text>
          </view>
          <view class="confirm-field" v-if="confirmData.categoryId">
            <text class="confirm-label">分类</text>
            <text class="confirm-value">ID: {{ confirmData.categoryId }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.deadline">
            <text class="confirm-label">截止</text>
            <text class="confirm-value">{{ confirmData.deadline }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.salaryAmount">
            <text class="confirm-label">薪资</text>
            <text class="confirm-value">{{ confirmData.salaryType === 'DAILY' ? '日薪' : '时薪' }} ¥{{ confirmData.salaryAmount }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.province || confirmData.city || confirmData.district || confirmData.address">
            <text class="confirm-label">地点</text>
            <text class="confirm-value">{{ [confirmData.province, confirmData.city, confirmData.district, confirmData.address].filter(Boolean).join(' ') }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.contactName">
            <text class="confirm-label">联系人</text>
            <text class="confirm-value">{{ confirmData.contactName }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.contactPhone">
            <text class="confirm-label">电话</text>
            <text class="confirm-value">{{ confirmData.contactPhone }}</text>
          </view>
          <view class="confirm-field" v-if="confirmData.latitude && confirmData.longitude">
            <text class="confirm-label">定位</text>
            <text class="confirm-value">{{ confirmData.latitude }}, {{ confirmData.longitude }}</text>
          </view>

          <view v-if="confirmData.schedules && confirmData.schedules.length" class="confirm-schedules">
            <view class="confirm-schedule-title">班次安排</view>
            <view class="confirm-schedule-item" v-for="(s, si) in confirmData.schedules" :key="si">
              <view class="confirm-schedule-line">
                <text class="confirm-schedule-date">{{ s.scheduleDate }}</text>
                <text class="confirm-schedule-time">{{ (s.startTime || '').slice(0,5) }} - {{ (s.endTime || '').slice(0,5) }}</text>
              </view>
              <view class="confirm-schedule-meta" v-if="s.scheduleName || s.slotsAvailable">
                <text v-if="s.scheduleName" class="confirm-schedule-name">{{ s.scheduleName }}</text>
                <text v-if="s.slotsAvailable" class="confirm-schedule-slots">{{ s.slotsAvailable }}人</text>
              </view>
              <view class="confirm-schedule-meta" v-if="s.contactName || s.contactPhone">
                <text v-if="s.contactName">联系人: {{ s.contactName }}</text>
                <text v-if="s.contactPhone"> {{ s.contactPhone }}</text>
              </view>
            </view>
          </view>

          <view v-if="confirmData.scheduleDate" class="confirm-schedules">
            <view class="confirm-schedule-title">班次安排</view>
            <view class="confirm-schedule-item">
              <view class="confirm-schedule-line">
                <text class="confirm-schedule-date">{{ confirmData.scheduleDate }}</text>
                <text class="confirm-schedule-time">{{ (confirmData.startTime || '').slice(0,5) }} - {{ (confirmData.endTime || '').slice(0,5) }}</text>
              </view>
              <view class="confirm-schedule-meta" v-if="confirmData.scheduleName || confirmData.slotsAvailable">
                <text v-if="confirmData.scheduleName" class="confirm-schedule-name">{{ confirmData.scheduleName }}</text>
                <text v-if="confirmData.slotsAvailable" class="confirm-schedule-slots">{{ confirmData.slotsAvailable }}人</text>
              </view>
            </view>
          </view>

          <view v-if="confirmAction === 'create_schedules' && confirmData.weekdays" class="confirm-schedules">
            <view class="confirm-schedule-title">批量班次</view>
            <view class="confirm-field">
              <text class="confirm-label">范围</text>
              <text class="confirm-value">{{ confirmData.startDate }} ~ {{ confirmData.endDate }}</text>
            </view>
            <view class="confirm-field" v-if="confirmData.weekdays">
              <text class="confirm-label">星期</text>
              <text class="confirm-value">{{ confirmData.weekdays.map(w => ['','一','二','三','四','五','六','日'][w]).join('、') }}</text>
            </view>
            <view class="confirm-field">
              <text class="confirm-label">时段</text>
              <text class="confirm-value">{{ (confirmData.startTime || '').slice(0,5) }} - {{ (confirmData.endTime || '').slice(0,5) }}</text>
            </view>
          </view>
        </template>

        <view class="confirm-actions">
          <view class="confirm-btn cancel" @click="cancelConfirm">取消</view>
          <view class="confirm-btn edit" @click="editConfirm">修改</view>
          <view class="confirm-btn confirm" @click="confirmCreate">{{ ACTION_LABELS[confirmAction] || '确认' }}</view>
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
.chat-header-right { width: 60rpx; display: flex; align-items: center; justify-content: flex-end; }
.clear-btn { font-size: 36rpx; line-height: 1; opacity: .8; }
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
.confirm-field { display: flex; align-items: flex-start; padding: 10rpx 0; border-bottom: 1rpx solid #f0f2f4; }
.confirm-field:last-of-type { border-bottom: none; }
.confirm-label { width: 100rpx; font-size: 24rpx; color: #64748b; flex-shrink: 0; padding-top: 4rpx; }
.confirm-value { font-size: 26rpx; font-weight: 700; color: #1f2933; flex: 1; }
.confirm-schedules { margin-top: 18rpx; padding-top: 14rpx; border-top: 2rpx solid #e5e7eb; }
.confirm-schedule-title { font-size: 26rpx; font-weight: 800; color: #1f2933; margin-bottom: 12rpx; }
.confirm-schedule-item { padding: 10rpx 0; border-bottom: 1rpx solid #f0f2f4; }
.confirm-schedule-item:last-child { border-bottom: none; }
.confirm-schedule-line { display: flex; align-items: center; }
.confirm-schedule-date { font-size: 24rpx; color: #64748b; width: 180rpx; flex-shrink: 0; }
.confirm-schedule-time { font-size: 26rpx; font-weight: 700; color: #16a34a; }
.confirm-schedule-meta { display: flex; align-items: center; gap: 12rpx; margin-top: 4rpx; font-size: 22rpx; color: #8896a4; padding-left: 180rpx; }
.confirm-schedule-name { font-weight: 600; color: #1f2933; }
.confirm-schedule-slots { color: #b45309; }
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
