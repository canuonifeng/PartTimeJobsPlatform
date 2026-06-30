<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>排班管理</span>
        <div>
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" style="width:280px;margin-right:10px" />
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="招聘中" value="ACTIVE" />
            <el-option label="已满员" value="FULL" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索职位/企业" size="small" style="width:200px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="schedules" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="jobTitle" label="职位名称" min-width="180" />
      <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
      <el-table-column prop="scheduleDate" label="排班日期" width="120" />
      <el-table-column prop="startTime" label="开始时间" width="100" />
      <el-table-column prop="endTime" label="结束时间" width="100" />
      <el-table-column prop="slotsAvailable" label="招聘人数" width="100" />
      <el-table-column prop="applicationCount" label="已报名" width="100" />
      <el-table-column prop="checkedInCount" label="已打卡" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'COMPLETED' ? 'info' : row.status === 'CANCELLED' ? 'danger' : 'warning'">
            {{ row.status === 'ACTIVE' ? '招聘中' : row.status === 'COMPLETED' ? '已完成' : row.status === 'CANCELLED' ? '已取消' : '已满员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleViewApplications(row)">报名详情</el-button>
          <el-button type="primary" size="small" text @click="handleViewAttendance(row)">考勤记录</el-button>
          <el-button v-if="row.status === 'ACTIVE' || row.status === 'FULL'" type="danger" size="small" text @click="handleCancel(row)">取消排班</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSchedulesByJobId, cancelSchedule } from '../../api/schedules'

const loading = ref(false)
const schedules = ref([])
const query = ref({ status: '', keyword: '' })
const dateRange = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await listSchedulesByJobId(1)
    schedules.value = res || []
  } finally {
    loading.value = false
  }
}

function handleViewApplications(row) {
  ElMessage.info(`查看排班 ${row.id} 的报名详情功能开发中`)
}

function handleViewAttendance(row) {
  ElMessage.info(`查看排班 ${row.id} 的考勤记录功能开发中`)
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消该排班？`, '提示', { type: 'warning' })
    await cancelSchedule(row.id)
    ElMessage.success('已取消排班')
    await fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>
