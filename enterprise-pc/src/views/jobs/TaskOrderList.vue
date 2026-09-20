<script setup>
import { ref, onMounted } from 'vue'
import { listTaskOrders } from '../../api/taskOrder'

const orders = ref([])
const total = ref(0)
const loading = ref(false)
const searchForm = ref({
  status: '',
  page: 1,
  pageSize: 20
})

const statusOptions = [
  { value: 'PENDING', label: '待处理' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'FAILED', label: '已失败' }
]

const statusMap = {
  PENDING: 'info',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  FAILED: 'danger'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listTaskOrders(searchForm.value)
    const data = Array.isArray(res) ? res : (res.records || [])
    orders.value = data
    total.value = Array.isArray(res) ? res.length : (res.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  searchForm.value.page = 1
  fetchData()
}

function handleReset() {
  searchForm.value = { status: '', page: 1, pageSize: 20 }
  fetchData()
}

function handlePageChange(page) {
  searchForm.value.page = page
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="task-order-list">
    <el-card>
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="orders" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="任务单ID" width="100" />
        <el-table-column prop="taskPackageName" label="任务包" min-width="150" />
        <el-table-column prop="batchNo" label="批次" width="120" />
        <el-table-column prop="workerName" label="工人" width="120" />
        <el-table-column prop="totalItems" label="任务量" width="100" />
        <el-table-column prop="completedItems" label="完成量" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">
              {{ statusOptions.find(o => o.value === row.status)?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column prop="settleTime" label="结算时间" width="170" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-if="total > searchForm.pageSize"
          :total="total"
          :page-size="searchForm.pageSize"
          :current-page="searchForm.page"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.task-order-list {
  padding: 20px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
