<template>
  <el-card>
    <template #header>
      <span>活动运营</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="活动配置" name="config">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="openCreate">新建活动</el-button>
        <el-table :data="activities" stripe style="width:100%">
          <el-table-column prop="title" label="活动名称" min-width="180" />
          <el-table-column prop="activityType" label="活动类型" width="120">
            <template #default="{ row }">{{ typeLabel(row.activityType) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'ENDED' ? 'info' : 'warning'">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="endTime" label="结束时间" width="180" />
          <el-table-column prop="participantCount" label="参与人数" width="100" />
          <el-table-column prop="viewCount" label="浏览次数" width="100" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="handleViewStats(row)">效果统计</el-button>
              <el-button type="primary" size="small" text @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status !== 'PUBLISHED'" type="success" size="small" text @click="handlePublish(row)">发布</el-button>
              <el-button v-else type="warning" size="small" text @click="handleEnd(row)">结束</el-button>
              <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="推送管理" name="push">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="openPushCreate">新建推送</el-button>
        <el-table :data="pushTasks" stripe style="width:100%">
          <el-table-column prop="title" label="推送标题" min-width="180" />
          <el-table-column prop="content" label="推送内容" min-width="220" show-overflow-tooltip />
          <el-table-column prop="targetType" label="目标人群" width="120">
            <template #default="{ row }">{{ targetLabel(row.targetType) }}</template>
          </el-table-column>
          <el-table-column prop="totalCount" label="目标数" width="90" />
          <el-table-column prop="successCount" label="成功" width="90" />
          <el-table-column prop="failCount" label="失败" width="90" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'">
                {{ pushStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑活动' : '新建活动'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="form.activityType" style="width:100%">
            <el-option label="新人活动" value="NEW_USER" />
            <el-option label="推荐活动" value="REFERRAL" />
            <el-option label="节日活动" value="HOLIDAY" />
            <el-option label="通用活动" value="GENERAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="横幅图"><el-input v-model="form.bannerImage" placeholder="图片URL" /></el-form-item>
        <el-form-item label="活动链接"><el-input v-model="form.linkUrl" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pushDialogVisible" title="新建推送" width="600px">
      <el-form :model="pushForm" label-width="100px">
        <el-form-item label="推送标题"><el-input v-model="pushForm.title" /></el-form-item>
        <el-form-item label="推送内容"><el-input v-model="pushForm.content" type="textarea" /></el-form-item>
        <el-form-item label="目标人群">
          <el-select v-model="pushForm.targetType" style="width:100%">
            <el-option label="全部" value="ALL" />
            <el-option label="全部工人" value="WORKER" />
            <el-option label="全部企业" value="ENTERPRISE" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="推送类型">
          <el-radio-group v-model="pushForm.pushType">
            <el-radio value="IMMEDIATE">立即推送</el-radio>
            <el-radio value="SCHEDULED">定时推送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="pushForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pushDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePushSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statsVisible" title="活动效果统计" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="参与人数">{{ stats.participantCount }}</el-descriptions-item>
        <el-descriptions-item label="浏览次数">{{ stats.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="活动带来报名">{{ stats.incrementalApplications }}</el-descriptions-item>
        <el-descriptions-item label="转化率">{{ stats.conversionRate }}%</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivities, createActivity, updateActivity, toggleActivity, deleteActivity, getActivityEffectStats, listPushTasks, createPushTask } from '../../api/activities'

const activeTab = ref('config')
const activities = ref([])
const pushTasks = ref([])
const dialogVisible = ref(false)
const form = ref({})
const pushDialogVisible = ref(false)
const pushForm = ref({})
const statsVisible = ref(false)
const stats = ref({})

function typeLabel(t) {
  return { NEW_USER: '新人活动', REFERRAL: '推荐活动', HOLIDAY: '节日活动', GENERAL: '通用活动' }[t] || t
}
function statusLabel(s) {
  return { DRAFT: '草稿', PUBLISHED: '已发布', ENDED: '已结束' }[s] || s
}
function targetLabel(t) {
  return { ALL: '全部', WORKER: '全部工人', ENTERPRISE: '全部企业', CUSTOM: '自定义' }[t] || t
}
function pushStatusLabel(s) {
  return { PENDING: '待发送', PROCESSING: '发送中', COMPLETED: '已完成', FAILED: '失败' }[s] || s
}

async function fetchActivities() {
  try { activities.value = (await listActivities({})) || [] } catch {}
}
async function fetchPushTasks() {
  try { pushTasks.value = (await listPushTasks({})) || [] } catch {}
}

function openCreate() {
  form.value = { activityType: 'GENERAL', sortOrder: 0 }
  dialogVisible.value = true
}
function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}
async function handleSubmit() {
  try {
    if (form.value.id) { await updateActivity(form.value) } else { await createActivity(form.value) }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await fetchActivities()
  } catch {}
}
async function handlePublish(row) {
  await toggleActivity(row.id, 'PUBLISHED')
  ElMessage.success('已发布')
  await fetchActivities()
}
async function handleEnd(row) {
  await toggleActivity(row.id, 'ENDED')
  ElMessage.success('已结束')
  await fetchActivities()
}
async function handleDelete(row) {
  await deleteActivity(row.id)
  ElMessage.success('已删除')
  await fetchActivities()
}
async function handleViewStats(row) {
  stats.value = await getActivityEffectStats(row.id)
  statsVisible.value = true
}
function openPushCreate() {
  pushForm.value = { targetType: 'ALL', pushType: 'IMMEDIATE' }
  pushDialogVisible.value = true
}
async function handlePushSubmit() {
  await createPushTask(pushForm.value)
  ElMessage.success('推送任务已创建')
  pushDialogVisible.value = false
  await fetchPushTasks()
}

onMounted(() => {
  fetchActivities()
  fetchPushTasks()
})
</script>
