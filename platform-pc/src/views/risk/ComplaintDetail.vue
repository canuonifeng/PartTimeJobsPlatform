<template>
  <div v-loading="loading">
    <el-page-header @back="goBack" class="mb16">
      <template #content>投诉工单详情</template>
      <template #extra>
        <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
        <el-tag style="margin-left:8px" :type="priorityTagType(detail.priority)">{{ priorityText(detail.priority) }}</el-tag>
      </template>
    </el-page-header>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card class="mb16">
          <template #header><span>基本信息</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工单号">{{ detail.complaintNo }}</el-descriptions-item>
            <el-descriptions-item label="投诉类型">{{ complaintTypeText(detail.complaintType) }}</el-descriptions-item>
            <el-descriptions-item label="投诉标题" :span="2">{{ detail.title }}</el-descriptions-item>
            <el-descriptions-item label="投诉内容" :span="2">
              <div style="white-space:pre-wrap">{{ detail.content }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.images" label="凭证图片" :span="2">
              <el-image v-for="(img, i) in imageList" :key="i" :src="img" :preview-src-list="imageList"
                        style="width:80px;height:80px;margin-right:8px;object-fit:cover" fit="cover" />
            </el-descriptions-item>
            <el-descriptions-item label="关联职位">{{ detail.relatedJobTitle || '-' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ detail.handlerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理时间">{{ detail.handledAt || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.handleResult" label="处理/仲裁结果" :span="2">
              <div style="white-space:pre-wrap">{{ detail.handleResult }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="mb16">
          <template #header><span>投诉人</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="类型">{{ detail.complainantType === 'WORKER' ? '工人' : '企业' }}</el-descriptions-item>
            <el-descriptions-item label="名称">{{ detail.complainantName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.complainantPhone || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card class="mb16">
          <template #header><span>被投诉方</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="类型">{{ accusedTypeText(detail.accusedType) }}</el-descriptions-item>
            <el-descriptions-item label="名称">{{ detail.accusedName || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card>
          <template #header><span>操作</span></template>
          <el-space direction="vertical" style="width:100%">
            <el-button v-if="canHandle" type="primary" style="width:100%" @click="openHandle">处理工单</el-button>
            <el-button v-if="canArbitrate" type="warning" style="width:100%" @click="openArbitrate">仲裁结论</el-button>
            <el-button v-if="canClose" type="info" style="width:100%" @click="onClose">关闭工单</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="handleDialog" title="处理工单" width="500px">
      <el-input v-model="handleForm.handleResult" type="textarea" :rows="4" placeholder="填写处理结果" />
      <template #footer>
        <el-button @click="handleDialog = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="arbitrateDialog" title="仲裁结论" width="500px">
      <el-input v-model="arbitrateForm.conclusion" type="textarea" :rows="4" placeholder="填写仲裁结论" />
      <template #footer>
        <el-button @click="arbitrateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitArbitrate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getComplaintDetail, handleComplaint, arbitrateComplaint, closeComplaint } from '../../api/complaintAdmin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({})
const handleDialog = ref(false)
const arbitrateDialog = ref(false)
const handleForm = ref({ handleResult: '' })
const arbitrateForm = ref({ conclusion: '' })

const imageList = computed(() => (detail.value.images ? detail.value.images.split(',').filter(Boolean) : []))
const canHandle = computed(() => detail.value.status === 'PENDING' || detail.value.status === 'PROCESSING')
const canArbitrate = computed(() => detail.value.status !== 'CLOSED')
const canClose = computed(() => detail.value.status !== 'CLOSED')

function statusText(s) {
  return { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }[s] || s
}
function priorityText(p) {
  return { URGENT: '紧急', HIGH: '高', NORMAL: '中', LOW: '低' }[p] || p
}
function complaintTypeText(t) {
  return { PAYMENT: '薪资纠纷', SCHEDULE: '排班问题', BEHAVIOR: '行为问题', SERVICE: '服务问题', OTHER: '其他' }[t] || t
}
function accusedTypeText(t) {
  return { WORKER: '工人', ENTERPRISE: '企业', PLATFORM: '平台' }[t] || t
}
function statusTagType(s) {
  return { RESOLVED: 'success', PROCESSING: 'warning', CLOSED: 'info', PENDING: 'danger' }[s] || 'info'
}
function priorityTagType(p) {
  return (p === 'URGENT' || p === 'HIGH') ? 'danger' : p === 'NORMAL' ? 'warning' : 'info'
}

async function fetchDetail() {
  loading.value = true
  try {
    detail.value = await getComplaintDetail(route.query.id) || {}
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openHandle() {
  handleForm.value.handleResult = detail.value.handleResult || ''
  handleDialog.value = true
}
async function submitHandle() {
  await handleComplaint({ id: detail.value.id, handleResult: handleForm.value.handleResult, handlerName: '管理员' })
  ElMessage.success('已处理')
  handleDialog.value = false
  fetchDetail()
}

function openArbitrate() {
  arbitrateForm.value.conclusion = ''
  arbitrateDialog.value = true
}
async function submitArbitrate() {
  await arbitrateComplaint({ id: detail.value.id, conclusion: arbitrateForm.value.conclusion, handlerName: '管理员' })
  ElMessage.success('已仲裁')
  arbitrateDialog.value = false
  fetchDetail()
}

async function onClose() {
  await ElMessageBox.confirm('确定关闭该工单？', '提示', { type: 'warning' })
  await closeComplaint(detail.value.id)
  ElMessage.success('已关闭')
  fetchDetail()
}

function goBack() {
  router.back()
}

onMounted(fetchDetail)
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
</style>
