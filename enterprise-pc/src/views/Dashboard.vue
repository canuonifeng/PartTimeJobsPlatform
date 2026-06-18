<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getOperationDashboard } from '../api/operations'

const router = useRouter()

const stats = ref([
  { label: '今日班次', value: 0, color: '#16a34a', path: '/schedules' },
  { label: '待审核报名', value: 0, color: '#f59e0b', path: '/applications' },
  { label: '异常考勤', value: 0, color: '#ef4444', path: '/schedules/shifts' },
  { label: '待结算薪资', value: 0, color: '#3b82f6', path: '/attendance/hours' }
])

const quickActions = ref([
  { label: '创建招聘计划', desc: '新建岗位信息和班次', path: '/jobs/create' },
  { label: '批量创建班次', desc: '按日期范围批量开班次', path: '/schedules' },
  { label: '报名审核', desc: '处理待审核报名申请', path: '/applications' },
  { label: '考勤确认', desc: '确认打卡和异常考勤', path: '/schedules/shifts' }
])

function navigateTo(path) {
  router.push(path)
}

onMounted(async () => {
  try {
    const data = await getOperationDashboard()
    if (data) {
      stats.value[0].value = data.overview?.todayShiftCount || 0
      const reviewNode = (data.process || []).find(p => p.code === 'REVIEW')
      stats.value[1].value = reviewNode?.count || data.overview?.pendingTodoCount || 0
      stats.value[2].value = data.today?.attendanceExceptionCount || 0
      stats.value[3].value = data.overview?.unpaidSalaryCount || 0
    }
  } catch (e) {
    // silently fail, show defaults
  }
})
</script>

<template>
  <div class="dashboard">
    <div class="section-title">运营概览</div>
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card" @click="navigateTo(stat.path)">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }} →</div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="section-title">快速操作</div>
    <div class="quick-grid">
      <div v-for="action in quickActions" :key="action.label" class="quick-card" @click="navigateTo(action.path)">
        <el-card shadow="hover">
          <div class="quick-title">{{ action.label }}</div>
          <div class="quick-desc">{{ action.desc }}</div>
        </el-card>
      </div>
    </div>

    <el-card class="welcome-card">
      <h2>班次中心招聘运营</h2>
      <p>围绕每个具体班次管理报名、考勤和结算，让招聘到结算形成完整闭环。</p>
      <p class="hint">左侧导航：班次管理 → 招聘计划 → 报名审核 → 考勤确认 → 薪资结算</p>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 8px 4px 14px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  cursor: pointer;
}
.stat-card :deep(.el-card__body) {
  padding: 24px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.quick-card {
  cursor: pointer;
}
.quick-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}
.quick-desc {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}
.stat-item {
  text-align: center;
  padding: 8px 0;
}
.stat-value {
  font-size: 38px;
  font-weight: 800;
  line-height: 1.2;
}
.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 10px;
  font-weight: 500;
}
.welcome-card {
  margin-top: 4px;
}
.welcome-card h2 {
  margin: 0 0 8px;
  color: #111827;
}
.welcome-card p {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}
.welcome-card .hint {
  margin-top: 8px;
  color: #9ca3af;
  font-size: 13px;
}
</style>
