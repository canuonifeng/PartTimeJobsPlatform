<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>操作日志</span>
        <div>
          <el-select v-model="query.module" placeholder="筛选模块" size="small" style="width:150px;margin-right:8px" clearable @change="fetchData">
            <el-option label="职位" value="JOB" />
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="工人" value="WORKER" />
            <el-option label="报名" value="APPLICATION" />
            <el-option label="排班" value="SCHEDULE" />
            <el-option label="考勤" value="ATTENDANCE" />
            <el-option label="结算" value="SETTLEMENT" />
            <el-option label="提现" value="WITHDRAWAL" />
            <el-option label="内容" value="CONTENT" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="账号" value="OPERATOR_MANAGE" />
            <el-option label="风控" value="RISK" />
          </el-select>
          <el-select v-model="query.operationType" placeholder="操作类型" size="small" style="width:120px" clearable @change="fetchData">
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="审核" value="AUDIT" />
          </el-select>
        </div>
      </div>
    </template>

    <el-table :data="logs" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="module" label="模块" width="140" />
      <el-table-column prop="operationType" label="操作类型" width="100" />
      <el-table-column prop="targetType" label="目标类型" width="120" />
      <el-table-column prop="targetId" label="目标ID" width="100" />
      <el-table-column prop="requestUrl" label="请求URL" min-width="220" show-overflow-tooltip />
      <el-table-column prop="result" label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.result }}</el-tag>
        </template>
      </el-table-column>
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
const query = ref({ module: '', operationType: '' })

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
