<template>
  <el-card>
    <template #header>
      <span class="card-title">提现记录</span>
    </template>
    <el-form :model="searchForm" inline>
      <el-form-item label="兼职ID">
        <el-input v-model="searchForm.workerId" placeholder="兼职ID" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="失败" value="FAILED" />
          <el-option label="待处理" value="PENDING" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="searchForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="开始时间" style="width: 190px" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="searchForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="结束时间" style="width: 190px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" v-loading="loading" stripe style="width:100%;margin-top:12px">
      <el-table-column prop="id" label="记录ID" width="90" />
      <el-table-column prop="workerId" label="兼职ID" width="90" />
      <el-table-column prop="workerName" label="姓名" width="100" />
      <el-table-column prop="workerPhone" label="手机号" width="140" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }">¥{{ row.amount ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="bankInfo" label="银行信息" min-width="180" show-overflow-tooltip />
      <el-table-column prop="thirdPartySerialNo" label="银行/第三方流水号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="thirdPartyPlatform" label="支付平台" width="140" />
      <el-table-column prop="requestedAt" label="申请时间" width="180" />
      <el-table-column prop="processedAt" label="处理时间" width="180" />
      <el-table-column prop="completedAt" label="完成时间" width="180" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
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
import { listWithdrawals } from '../../api/withdrawals'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchForm = ref({ workerId: '', status: '', startTime: '', endTime: '' })

const statusMap = {
  PENDING: { label: '待处理', type: 'info' },
  PROCESSING: { label: '处理中', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  FAILED: { label: '失败', type: 'danger' }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    if (searchForm.value.workerId) params.workerId = searchForm.value.workerId
    if (searchForm.value.status) params.status = searchForm.value.status
    if (searchForm.value.startTime) params.startTime = searchForm.value.startTime
    if (searchForm.value.endTime) params.endTime = searchForm.value.endTime
    const res = await listWithdrawals(params)
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
  searchForm.value = { workerId: '', status: '', startTime: '', endTime: '' }
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
