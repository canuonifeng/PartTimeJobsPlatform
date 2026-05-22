<script setup>
import { ref, onMounted } from 'vue'
import { listBills } from '../../api/settlement'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const searchForm = ref({
  workerName: '',
  dateFrom: '',
  dateTo: ''
})

const statusMap = {
  PAID: { label: '已结算', type: 'success' }
}

const rateTypeMap = {
  HOURLY: '元/小时',
  DAILY: '元/天',
  PIECEWORK: '元/件',
  MONTHLY: '元/月'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchForm.value.workerName) params.workerName = searchForm.value.workerName
    if (searchForm.value.dateFrom) params.dateFrom = searchForm.value.dateFrom
    if (searchForm.value.dateTo) params.dateTo = searchForm.value.dateTo
    const res = await listBills(params)
    records.value = Array.isArray(res) ? res : (res.records || [])
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.value = { workerName: '', dateFrom: '', dateTo: '' }
  page.value = 1
  fetchData()
}



onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="settlement-bill-list">
    <el-card>
      <el-form :model="searchForm" inline>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.workerName" placeholder="搜索" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="searchForm.dateFrom" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="searchForm.dateTo" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card style="margin-top: 16px">
      <el-table :data="records" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="日期" width="110">
          <template #default="{ row }">{{ row.shiftDate }}</template>
        </el-table-column>
        <el-table-column label="排班时间" width="140">
          <template #default="{ row }">{{ row.startTime }}~{{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="workerName" label="姓名" width="90" />
        <el-table-column label="工时" width="70">
          <template #default="{ row }">{{ row.totalHours ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="薪资规则" width="130">
          <template #default="{ row }">
            {{ row.rateAmount ?? '-' }}{{ rateTypeMap[row.rateType] || '' }}
          </template>
        </el-table-column>
        <el-table-column label="排班薪资" width="100">
          <template #default="{ row }">{{ row.scheduledPay ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="实付薪资" width="100">
          <template #default="{ row }">{{ row.actualPay ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="流水号" width="200">
          <template #default="{ row }">{{ row.serialNumber }}</template>
        </el-table-column>
        <el-table-column label="第三方流水号" width="200">
          <template #default="{ row }">{{ row.thirdPartySerialNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="支付平台" width="120">
          <template #default="{ row }">{{ row.thirdPartyPlatform || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.settlement-bill-list {
  padding: 20px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
