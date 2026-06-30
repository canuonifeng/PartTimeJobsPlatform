<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>操作日志</span>
        <div>
          <el-select v-model="query.module" placeholder="筛选模块" size="small" style="width:140px;margin-right:8px" clearable>
            <el-option label="职位管理" value="职位管理" />
            <el-option label="报名审核" value="报名审核" />
            <el-option label="考勤管理" value="考勤管理" />
            <el-option label="企业管理" value="企业管理" />
            <el-option label="工人管理" value="工人管理" />
            <el-option label="提现审核" value="提现审核" />
          </el-select>
        </div>
      </div>
    </template>

    <el-table :data="logs" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="module" label="操作模块" width="150" />
      <el-table-column prop="action" label="操作类型" width="100" />
      <el-table-column prop="targetId" label="目标ID" width="100" />
      <el-table-column prop="detail" label="操作详情" min-width="300" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="130" />
      <el-table-column prop="createdAt" label="操作时间" width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listOperationLogs } from '../../api/operators'

const loading = ref(false)
const logs = ref([])
const query = ref({ module: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await listOperationLogs(query.value)
    logs.value = res || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
