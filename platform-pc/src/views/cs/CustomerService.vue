<template>
  <el-card>
    <template #header>
      <span>客服中心</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="在线会话" name="session">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="never" style="height:600px;overflow-y:auto">
              <template #header><span>会话列表</span></template>
              <div v-for="session in sessions" :key="session.id"
                   style="padding:12px;border-bottom:1px solid #eee;cursor:pointer"
                   :style="{backgroundColor:currentSession?.id === session.id ? '#ecf5ff' : ''}"
                   @click="handleSelectSession(session)">
                <div style="display:flex;justify-content:space-between">
                  <span style="font-weight:500">{{ session.userName }}</span>
                  <el-tag :type="session.status === 'WAITING' ? 'warning' : session.status === 'PROCESSING' ? 'primary' : 'info'" size="small">
                    {{ session.status === 'WAITING' ? '等待中' : session.status === 'PROCESSING' ? '处理中' : '已关闭' }}
                  </el-tag>
                </div>
                <div style="color:#909399;font-size:12px;margin-top:4px">{{ session.lastMessage }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="16">
            <el-card shadow="never" style="height:600px">
              <template #header>
                <div style="display:flex;justify-content:space-between;align-items:center">
                  <span>{{ currentSession?.userName || '未选择会话' }}</span>
                  <el-button v-if="currentSession?.status !== 'CLOSED'" type="danger" size="small" @click="handleCloseSession">关闭会话</el-button>
                </div>
              </template>
              <div v-if="currentSession" style="height:480px;overflow-y:auto;padding:10px">
                <div v-for="msg in messages" :key="msg.id" style="margin-bottom:15px">
                  <div v-if="msg.type === 'USER'" style="text-align:left">
                    <el-tag type="info" size="small" style="margin-bottom:4px">{{ msg.senderName }}</el-tag>
                    <div style="background:#f5f7fa;padding:10px 15px;border-radius:12px;display:inline-block;max-width:80%">
                      {{ msg.content }}
                    </div>
                  </div>
                  <div v-else style="text-align:right">
                    <el-tag type="success" size="small" style="margin-bottom:4px">{{ msg.senderName }}</el-tag>
                    <div style="background:#409eff;color:#fff;padding:10px 15px;border-radius:12px;display:inline-block;max-width:80%">
                      {{ msg.content }}
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="currentSession" style="display:flex;gap:10px;margin-top:15px">
                <el-input v-model="newMessage" placeholder="输入消息..." @keyup.enter="handleSendMessage" />
                <el-button type="primary" @click="handleSendMessage">发送</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="FAQ管理" name="faq">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="handleCreateFaq">新增FAQ</el-button>
        <el-table :data="faqs" stripe style="width:100%">
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="question" label="问题" min-width="250" />
          <el-table-column prop="answer" label="答案" min-width="350" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="viewCount" label="浏览次数" width="100" />
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" active-text="启用" inactive-text="禁用" />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="handleEditFaq(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="handleDeleteFaq(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listSessions, getSessionMessages, listFaqs, sendMessage, closeSession, deleteFaq } from '../../api/cs'

const activeTab = ref('session')
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const newMessage = ref('')
const faqs = ref([])

async function fetchSessions() {
  try {
    const res = await listSessions({})
    sessions.value = res || []
  } catch {}
}

async function handleSelectSession(session) {
  currentSession.value = session
  try {
    const res = await getSessionMessages(session.id)
    messages.value = res || []
  } catch {}
}

async function handleSendMessage() {
  if (!newMessage.value.trim()) return
  try {
    await sendMessage({ sessionId: currentSession.value.id, content: newMessage.value })
    messages.value.push({
      id: Date.now(),
      type: 'AGENT',
      senderName: '客服',
      content: newMessage.value,
      createdAt: new Date().toISOString()
    })
    newMessage.value = ''
  } catch {}
}

async function handleCloseSession() {
  try {
    await closeSession({ sessionId: currentSession.value.id })
    ElMessage.success('会话已关闭')
    await fetchSessions()
    currentSession.value = null
    messages.value = []
  } catch {}
}

async function fetchFaqs() {
  try {
    const res = await listFaqs({})
    faqs.value = res || []
  } catch {}
}

function handleCreateFaq() {
  ElMessage.info('新建FAQ功能开发中')
}

function handleEditFaq(row) {
  ElMessage.info('编辑FAQ功能开发中')
}

async function handleDeleteFaq(row) {
  try {
    await deleteFaq(row.id)
    ElMessage.success('已删除')
    await fetchFaqs()
  } catch {}
}

onMounted(() => {
  fetchSessions()
  fetchFaqs()
})
</script>
