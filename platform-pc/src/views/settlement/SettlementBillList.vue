<template>
  <el-card>
    <template #header>
      <span class="card-title">结算账单</span>
    </template>
    <el-form :model="searchForm" inline>
      <el-form-item label="企业ID">
        <el-input v-model="searchForm.companyId" placeholder="企业ID" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="兼职姓名">
        <el-input v-model="searchForm.workerName" placeholder="搜索姓名" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="已结算" value="PAID" />
          <el-option label="已撤回" value="REFUNDED" />
          <el-option label="结算中" value="PAYING" />
          <el-option label="失败" value="FAILED" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker v-model="searchForm.dateFrom" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 150px" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="searchForm.dateTo" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 150px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" v-loading="loading" stripe style="width:100%;margin-top:12px">
      <el-table-column prop="id" label="账单ID" width="90" />
      <el-table-column prop="companyName" label="企业" min-width="160">
        <template #default="{ row }">{{ row.companyName || row.companyId }}</template>
      </el-table-column>
      <el-table-column prop="workerName" label="兼职" width="100" />
      <el-table-column label="排班日期" width="110">
        <template #default="{ row }">{{ row.shiftDate }}</template>
      </el-table-column>
      <el-table-column label="排班时间" width="140">
        <template #default="{ row }">{{ row.startTime }}~{{ row.endTime }}</template>
      </el-table-column>
      <el-table-column label="工时" width="80">
        <template #default="{ row }">{{ row.totalHours ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="应付工资" width="110">
        <template #default="{ row }">¥{{ row.actualPay ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="serialNumber" label="系统流水号" min-width="200" />
      <el-table-column label="银行/第三方流水号" min-width="200">
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
      <el-table-column prop="paidAt" label="结算时间" width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listSettlementBills } from '../../api/settlement'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchForm = ref({ companyId: null, workerName: '', dateFrom: '', dateTo: '', status: '' })

const statusMap = {
  PAID: { label: '已结算', type: 'success' },
  REFUNDED: { label: '已撤回', type: 'warning' },
  PAYING: { label: '结算中', type: 'warning' },
  FAILED: { label: '失败', type: 'danger' }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchForm.value.companyId) params.companyId = searchForm.value.companyId
    if (searchForm.value.workerName) params.workerName = searchForm.value.workerName
    if (searchForm.value.dateFrom) params.dateFrom = searchForm.value.dateFrom
    if (searchForm.value.dateTo) params.dateTo = searchForm.value.dateTo
    if (searchForm.value.status) params.status = searchForm.value.status
    const res = await listSettlementBills(params)
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
  searchForm.value = { companyId: null, workerName: '', dateFrom: '', dateTo: '', status: '' }
  page.value = 1
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
