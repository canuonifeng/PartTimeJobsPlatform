<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>在线客服</span>
        <el-radio-group v-model="statusFilter" size="small" @change="fetchSessions">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="WAITING">等待中</el-radio-button>
          <el-radio-button value="PROCESSING">处理中</el-radio-button>
          <el-radio-button value="CLOSED">已关闭</el-radio-button>
        </el-radio-group>
      </div>
    </template>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" style="height:620px;overflow-y:auto">
          <template #header><span>会话列表</span></template>
          <div v-for="s in sessions" :key="s.id"
               style="padding:12px;border-bottom:1px solid #eee;cursor:pointer"
               :style="{ backgroundColor: currentSession?.id === s.id ? '#ecf5ff' : '' }"
               @click="handleSelectSession(s)">
            <div style="display:flex;justify-content:space-between">
              <span style="font-weight:500">{{ s.userName || s.sessionNo }}</span>
              <el-tag :type="statusTag(s.status)" size="small">{{ statusText(s.status) }}</el-tag>
            </div>
            <div style="color:#909399;font-size:12px;margin-top:4px">{{ s.lastMessage || '暂无消息' }}</div>
            <div style="color:#c0c4cc;font-size:12px;margin-top:2px">
              {{ s.userType === 'WORKER' ? '工人' : '企业' }} · {{ formatTime(s.lastMessageAt) }}
            </div>
          </div>
          <el-empty v-if="!sessions.length" description="暂无会话" :image-size="60" />
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" style="height:620px">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>{{ currentSession ? (currentSession.userName || currentSession.sessionNo) : '未选择会话' }}</span>
              <div v-if="currentSession">
                <el-button v-if="currentSession.status === 'WAITING'" type="success" size="small" @click="handleAccept">接起</el-button>
                <el-button v-if="currentSession.status !== 'CLOSED'" type="danger" size="small" @click="handleClose">关闭会话</el-button>
              </div>
            </div>
          </template>

          <div v-if="currentSession" style="height:460px;overflow-y:auto;padding:10px" ref="msgBoxRef">
            <div v-for="m in messages" :key="m.id" style="margin-bottom:15px">
              <div v-if="m.senderType !== 'AGENT'" style="text-align:left">
                <el-tag type="info" size="small" style="margin-bottom:4px">{{ m.senderName || '用户' }}</el-tag>
                <div style="background:#f5f7fa;padding:10px 15px;border-radius:12px;display:inline-block;max-width:80%">
                  {{ m.content }}
                </div>
                <div style="font-size:12px;color:#c0c4cc;margin-top:2px">{{ formatTime(m.createdAt) }}</div>
              </div>
              <div v-else style="text-align:right">
                <el-tag type="success" size="small" style="margin-bottom:4px">{{ m.senderName || '客服' }}</el-tag>
                <div style="background:#409eff;color:#fff;padding:10px 15px;border-radius:12px;display:inline-block;max-width:80%">
                  {{ m.content }}
                </div>
                <div style="font-size:12px;color:#c0c4cc;margin-top:2px">{{ formatTime(m.createdAt) }}</div>
              </div>
            </div>
            <el-empty v-if="!messages.length" description="暂无消息" :image-size="60" />
          </div>

          <div v-if="currentSession && currentSession.status !== 'CLOSED'" style="display:flex;gap:10px;margin-top:15px">
            <el-input v-model="newMessage" placeholder="输入消息，回车发送" @keyup.enter="handleSend" />
            <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
          </div>
          <el-alert v-else-if="currentSession" type="info" :closable="false" title="该会话已关闭" />
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { listSessions, getSessionMessages, sendMessage, acceptSession, closeSession } from '../../api/csAdmin'

const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const newMessage = ref('')
const sending = ref(false)
const statusFilter = ref('')
const msgBoxRef = ref(null)
let pollTimer = null

function statusText(s) {
  return s === 'WAITING' ? '等待中' : s === 'PROCESSING' ? '处理中' : '已关闭'
}
function statusTag(s) {
  return s === 'WAITING' ? 'warning' : s === 'PROCESSING' ? '' : 'info'
}
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function fetchSessions() {
  try {
    const res = await listSessions({ status: statusFilter.value })
    sessions.value = res || []
  } catch {}
}

async function handleSelectSession(s) {
  currentSession.value = s
  await loadMessages()
}

async function loadMessages() {
  if (!currentSession.value) return
  try {
    const res = await getSessionMessages(currentSession.value.id)
    messages.value = res || []
    await nextTick(scrollBottom)
  } catch {}
}

function scrollBottom() {
  if (msgBoxRef.value) msgBoxRef.value.scrollTop = msgBoxRef.value.scrollHeight
}

async function handleSend() {
  const content = newMessage.value.trim()
  if (!content) return
  sending.value = true
  try {
    await sendMessage({ sessionId: currentSession.value.id, content })
    newMessage.value = ''
    await loadMessages()
  } catch {} finally {
    sending.value = false
  }
}

async function handleAccept() {
  try {
    await acceptSession(currentSession.value.id)
    ElMessage.success('已接起')
    currentSession.value.status = 'PROCESSING'
    await fetchSessions()
  } catch {}
}

async function handleClose() {
  try {
    await closeSession({ sessionId: currentSession.value.id, closeReason: '客服关闭' })
    ElMessage.success('会话已关闭')
    currentSession.value.status = 'CLOSED'
    await fetchSessions()
  } catch {}
}

onMounted(() => {
  fetchSessions()
  pollTimer = setInterval(() => {
    fetchSessions()
    if (currentSession.value) loadMessages()
  }, 5000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>
