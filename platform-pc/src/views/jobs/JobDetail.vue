<template>
  <div v-loading="loading">
    <el-card style="margin-bottom:16px">
      <div style="display:flex;align-items:center;justify-content:space-between">
        <div style="display:flex;align-items:center">
          <el-button text @click="goBack">← 返回列表</el-button>
          <span style="font-weight:600;font-size:16px;margin-left:12px">职位详情</span>
        </div>
        <el-tag :type="statusType(job.status)">{{ statusText(job.status) }}</el-tag>
      </div>
    </el-card>

    <el-card style="margin-bottom:16px">
      <template #header>基本信息</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="职位ID">{{ job.id }}</el-descriptions-item>
        <el-descriptions-item label="职位名称" :span="2">{{ job.title }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ job.companyName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ job.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ job.headcount }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ job.location }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ job.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ job.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="报名截止">{{ job.deadline }}</el-descriptions-item>
        <el-descriptions-item label="是否置顶">
          <el-tag v-if="job.isTop" type="success">已置顶</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="是否推荐">
          <el-tag v-if="job.isRecommended" type="warning">已推荐</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ job.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="职位描述" :span="3">{{ job.description }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="报名记录" name="applications">
          <el-table :data="applications" stripe size="small">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="workerName" label="工人" width="120" />
            <el-table-column prop="workerPhone" label="电话" width="140" />
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACCEPTED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
                  {{ row.status === 'ACCEPTED' ? '已通过' : row.status === 'REJECTED' ? '已拒绝' : '待审核' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="appliedAt" label="报名时间" width="180" />
            <el-table-column prop="reviewedAt" label="审核时间" width="180" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="排班记录" name="schedules">
          <el-table :data="schedules" stripe size="small">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="scheduleName" label="班次" width="120" />
            <el-table-column prop="scheduleDate" label="日期" width="120" />
            <el-table-column prop="startTime" label="开始" width="90" />
            <el-table-column prop="endTime" label="结束" width="90" />
            <el-table-column prop="slotsAvailable" label="可报名" width="90" />
            <el-table-column prop="applicationCount" label="已报名" width="90" />
            <el-table-column prop="hourlyWage" label="时薪" width="90" />
            <el-table-column prop="status" label="状态" width="110" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="考勤记录" name="attendance">
          <el-table :data="attendance" stripe size="small">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="workerName" label="工人" width="120" />
            <el-table-column prop="checkInTime" label="签到时间" width="180" />
            <el-table-column prop="checkOutTime" label="签退时间" width="180" />
            <el-table-column prop="totalHours" label="工时" width="90" />
            <el-table-column prop="status" label="考勤状态" width="110" />
            <el-table-column prop="settlementStatus" label="结算状态" width="110" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="结算记录" name="settlements">
          <el-table :data="settlements" stripe size="small">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="settlementNo" label="结算单号" width="180" />
            <el-table-column prop="companyName" label="企业" min-width="160" />
            <el-table-column prop="amount" label="金额" width="110">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="180" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column prop="createdAt" label="时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getJobDetail,
  listApplicationsByJob,
  listSchedulesByJob,
  listAttendanceByJob,
  listSettlementsByCompany
} from '../../api/jobAdmin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('applications')

const jobId = route.query.id
const job = ref({})
const applications = ref([])
const schedules = ref([])
const attendance = ref([])
const settlements = ref([])

function statusType(status) {
  return status === 'PUBLISHED' ? 'success' : status === 'CLOSED' ? 'info' : 'warning'
}

function statusText(status) {
  return status === 'PUBLISHED' ? '已发布' : status === 'CLOSED' ? '已关闭' : '草稿'
}

function goBack() {
  router.push('/jobs')
}

async function fetchAll() {
  if (!jobId) return
  loading.value = true
  try {
    job.value = await getJobDetail(jobId) || {}
    const [apps, schs, atts] = await Promise.all([
      listApplicationsByJob(jobId),
      listSchedulesByJob(jobId),
      listAttendanceByJob(jobId)
    ])
    applications.value = apps || []
    schedules.value = schs || []
    attendance.value = atts || []
    if (job.value.companyId) {
      settlements.value = await listSettlementsByCompany(job.value.companyId) || []
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchAll)
</script>
