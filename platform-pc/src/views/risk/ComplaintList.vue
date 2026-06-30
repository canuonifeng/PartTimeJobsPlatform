<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>投诉工单</span>
        <div>
          <el-select v-model="query.status" placeholder="筛选状态" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="complaints" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">{{ row.type === 'WORKER_COMPLAINT' ? '工人投诉' : '企业投诉' }}</template>
      </el-table-column>
      <el-table-column prop="complainantName" label="投诉人" width="120" />
      <el-table-column prop="respondentName" label="被投诉方" width="120" />
      <el-table-column prop="title" label="投诉标题" min-width="160" />
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="row.priority === 'HIGH' ? 'danger' : row.priority === 'MEDIUM' ? 'warning' : 'info'">
            {{ row.priority === 'HIGH' ? '高' : row.priority === 'MEDIUM' ? '中' : '低' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'RESOLVED' ? 'success' : row.status === 'PROCESSING' ? 'warning' : 'info'">
            {{ row.status === 'RESOLVED' ? '已解决' : row.status === 'PROCESSING' ? '处理中' : row.status === 'CLOSED' ? '已关闭' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="primary" size="small" text @click="handleStartProcess(row)">开始处理</el-button>
          <el-button v-if="row.status === 'PROCESSING'" type="success" size="small" text @click="handleResolve(row)">标记解决</el-button>
          <el-button v-if="row.status !== 'CLOSED'" type="info" size="small" text @click="handleClose(row)">关闭</el-button>
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const complaints = ref([])
const query = ref({ status: '' })

async function fetchData() {
  complaints.value = [
    { id: 1, type: 'WORKER_COMPLAINT', complainantName: '张三', respondentName: 'XX科技', title: '拖欠工资', priority: 'HIGH', status: 'PENDING', createdAt: '2024-01-15 10:30:00' },
    { id: 2, type: 'ENTERPRISE_COMPLAINT', complainantName: 'YY公司', respondentName: '李四', title: '无故缺勤', priority: 'MEDIUM', status: 'PROCESSING', createdAt: '2024-01-15 09:20:00' },
  ]
}

function handleStartProcess(row) {
  ElMessage.success('已开始处理')
  row.status = 'PROCESSING'
}

function handleResolve(row) {
  ElMessage.success('已标记为解决')
  row.status = 'RESOLVED'
}

function handleClose(row) {
  ElMessage.success('已关闭')
  row.status = 'CLOSED'
}

function handleDetail(row) {
  ElMessage.info('详情弹窗待开发')
}

onMounted(() => {
  fetchData()
})
</script>
