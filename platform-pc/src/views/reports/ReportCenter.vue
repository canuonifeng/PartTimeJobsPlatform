<template>
  <el-card>
    <template #header>
      <span>报表中心</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="运营概览" name="overview">
        <el-row :gutter="20" style="margin-bottom:20px">
          <el-col :span="4"><el-statistic title="今日新增职位" :value="overview.todayNewJobs" /></el-col>
          <el-col :span="4"><el-statistic title="今日报名数" :value="overview.todayNewApplications" /></el-col>
          <el-col :span="4"><el-statistic title="今日完成考勤" :value="overview.todayCompletedAttendance" /></el-col>
          <el-col :span="4"><el-statistic title="今日结算金额" :value="overview.todayTotalSettlement" prefix="¥" precision="2" /></el-col>
          <el-col :span="4"><el-statistic title="今日服务费收入" :value="overview.todayServiceFee" prefix="¥" precision="2" /></el-col>
          <el-col :span="4"><el-statistic title="今日新增企业" :value="overview.todayNewEnterprises" /></el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="企业活跃度" name="enterprise">
        <el-table :data="enterpriseActivity" stripe style="width:100%">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
          <el-table-column prop="jobCount" label="发布职位数" width="120" />
          <el-table-column prop="applicationCount" label="报名总数" width="120" />
          <el-table-column prop="totalSettlement" label="累计结算" width="130">
            <template #default="{ row }">¥{{ row.totalSettlement }}</template>
          </el-table-column>
          <el-table-column prop="activityLevel" label="活跃度" width="100">
            <template #default="{ row }">
              <el-tag :type="row.activityLevel === 'HIGH' ? 'success' : row.activityLevel === 'MEDIUM' ? 'warning' : 'info'">
                {{ row.activityLevel === 'HIGH' ? '高' : row.activityLevel === 'MEDIUM' ? '中' : '低' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="工人活跃度" name="worker">
        <el-table :data="workerActivity" stripe style="width:100%">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="workerName" label="工人姓名" width="120" />
          <el-table-column prop="workerPhone" label="联系方式" width="130" />
          <el-table-column prop="completedJobs" label="完成工作数" width="120" />
          <el-table-column prop="totalEarnings" label="累计收入" width="130">
            <template #default="{ row }">¥{{ row.totalEarnings }}</template>
          </el-table-column>
          <el-table-column prop="activityLevel" label="活跃度" width="100">
            <template #default="{ row }">
              <el-tag :type="row.activityLevel === 'HIGH' ? 'success' : row.activityLevel === 'MEDIUM' ? 'warning' : 'info'">
                {{ row.activityLevel === 'HIGH' ? '高' : row.activityLevel === 'MEDIUM' ? '中' : '低' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="供需分析" name="supplyDemand">
        <el-table :data="supplyDemand.byCategory" stripe style="width:100%">
          <el-table-column prop="category" label="职位分类" width="200" />
          <el-table-column prop="supply" label="招聘人数" width="120" />
          <el-table-column prop="demand" label="报名人数" width="120" />
          <el-table-column label="供需比" width="120">
            <template #default="{ row }">{{ (row.demand / row.supply).toFixed(2) }}:1</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="转化漏斗" name="conversion">
        <el-row :gutter="20">
          <el-col :span="4" v-for="(item, index) in funnelSteps" :key="index">
            <el-card shadow="never" style="text-align:center">
              <div style="font-size:28px;font-weight:bold;color:#409eff">{{ item.count }}</div>
              <div style="color:#909399;margin-top:8px">{{ item.label }}</div>
              <div v-if="index > 0" style="color:#67c23a;margin-top:4px">
                转化率: {{ ((item.count / funnelSteps[0].count) * 100).toFixed(1) }}%
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReportOverview, getEnterpriseActivity, getWorkerActivity, getSupplyDemandAnalysis, getConversionFunnel } from '../../api/reports'

const activeTab = ref('overview')
const overview = ref({
  todayNewJobs: 0,
  todayNewApplications: 0,
  todayCompletedAttendance: 0,
  todayTotalSettlement: 0,
  todayServiceFee: 0,
  todayNewEnterprises: 0
})
const enterpriseActivity = ref([])
const workerActivity = ref([])
const supplyDemand = ref({ byCategory: [] })
const funnelSteps = ref([])

async function fetchOverview() {
  try {
    const res = await getReportOverview({})
    overview.value = res
  } catch {}
}

async function fetchEnterpriseActivity() {
  try {
    const res = await getEnterpriseActivity({})
    enterpriseActivity.value = res || []
  } catch {}
}

async function fetchWorkerActivity() {
  try {
    const res = await getWorkerActivity({})
    workerActivity.value = res || []
  } catch {}
}

async function fetchSupplyDemand() {
  try {
    const res = await getSupplyDemandAnalysis({})
    supplyDemand.value = res
  } catch {}
}

async function fetchFunnel() {
  try {
    const res = await getConversionFunnel({})
    funnelSteps.value = [
      { label: '浏览职位', count: res.browseCount },
      { label: '查看详情', count: res.viewCount },
      { label: '报名申请', count: res.applyCount },
      { label: '审核通过', count: res.approvedCount },
      { label: '实际到岗', count: res.checkinCount },
      { label: '完成结算', count: res.settlementCount }
    ]
  } catch {}
}

onMounted(() => {
  fetchOverview()
  fetchEnterpriseActivity()
  fetchWorkerActivity()
  fetchSupplyDemand()
  fetchFunnel()
})
</script>
