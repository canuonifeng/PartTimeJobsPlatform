<template>
  <el-card>
    <template #header>
      <span>活动运营</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="活动配置" name="config">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="handleCreateActivity">新建活动</el-button>
        <el-table :data="activities" stripe style="width:100%">
          <el-table-column prop="activityName" label="活动名称" width="150" />
          <el-table-column prop="activityType" label="活动类型" width="120">
            <template #default="{ row }">{{ row.activityType === 'NEW_USER_BONUS' ? '新人红包' : '满单奖励' }}</template>
          </el-table-column>
          <el-table-column label="活动规则" min-width="200">
            <template #default="{ row }">
              <span v-if="row.activityType === 'NEW_USER_BONUS'">注册后{{ row.validDays }}天内，累计工作{{ row.minWorkHours }}小时奖励¥{{ row.bonusAmount }}</span>
              <span v-else>完成{{ row.orderCount }}单奖励¥{{ row.bonusAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="handleToggleActivity(row)" active-text="启用" inactive-text="禁用" />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="handleViewStats(row)">效果统计</el-button>
              <el-button type="primary" size="small" text @click="handleEditActivity(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="推送管理" name="push">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="handleCreatePush">新建推送</el-button>
        <el-table :data="pushTasks" stripe style="width:100%">
          <el-table-column prop="title" label="推送标题" min-width="200" />
          <el-table-column prop="content" label="推送内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="targetType" label="目标人群" width="120">
            <template #default="{ row }">{{ row.targetType === 'ALL_WORKERS' ? '全部工人' : '全部企业' }}</template>
          </el-table-column>
          <el-table-column prop="targetCount" label="目标数" width="100" />
          <el-table-column prop="sentCount" label="已发送" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'SENDING' ? 'warning' : 'info'">
                {{ row.status === 'COMPLETED' ? '已完成' : row.status === 'SENDING' ? '发送中' : '待发送' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivities, listPushTasks, toggleActivity } from '../../api/activities'

const activeTab = ref('config')
const activities = ref([])
const pushTasks = ref([])

async function fetchActivities() {
  try {
    const res = await listActivities({})
    activities.value = res || []
  } catch {}
}

async function fetchPushTasks() {
  try {
    const res = await listPushTasks({})
    pushTasks.value = res || []
  } catch {}
}

async function handleToggleActivity(row) {
  try {
    await toggleActivity(row.id, row.enabled)
    ElMessage.success(row.enabled ? '已启用活动' : '已禁用活动')
  } catch {
    row.enabled = !row.enabled
  }
}

function handleCreateActivity() {
  ElMessage.info('新建活动弹窗功能开发中')
}

function handleEditActivity(row) {
  ElMessage.info('编辑活动功能开发中')
}

function handleViewStats(row) {
  ElMessage.info('查看效果统计功能开发中')
}

function handleCreatePush() {
  ElMessage.info('新建推送功能开发中')
}

onMounted(() => {
  fetchActivities()
  fetchPushTasks()
})
</script>
