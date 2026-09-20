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
          <el-select v-model="query.complaintType" placeholder="投诉类型" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
            <el-option label="薪资纠纷" value="PAYMENT" />
            <el-option label="排班问题" value="SCHEDULE" />
            <el-option label="行为问题" value="BEHAVIOR" />
            <el-option label="服务问题" value="SERVICE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="搜索工单/标题/当事人" size="small" style="width:200px;margin-right:8px" clearable @keyup.enter="fetchData" />
          <el-button type="primary" size="small" @click="fetchData">搜索</el-button>
        </div>
      </div>
    </template>
    <el-table :data="complaints" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="complaintNo" label="工单号" width="160" />
      <el-table-column prop="complainantType" label="投诉人类型" width="100">
        <template #default="{ row }">{{ row.complainantType === 'WORKER' ? '工人' : '企业' }}</template>
      </el-table-column>
      <el-table-column prop="complainantName" label="投诉人" width="120" />
      <el-table-column prop="accusedName" label="被投诉方" width="120" />
      <el-table-column prop="title" label="投诉标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="90">
        <template #default="{ row }">
          <el-tag :type="row.priority === 'URGENT' || row.priority === 'HIGH' ? 'danger' : row.priority === 'NORMAL' ? 'warning' : 'info'">
            {{ priorityText(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'RESOLVED' ? 'success' : row.status === 'PROCESSING' ? 'warning' : row.status === 'CLOSED' ? 'info' : 'danger'">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listComplaints } from '../../api/complaintAdmin'

const router = useRouter()
const loading = ref(false)
const complaints = ref([])
const query = ref({ status: '', complaintType: '', keyword: '' })

function priorityText(p) {
  return { URGENT: '紧急', HIGH: '高', NORMAL: '中', LOW: '低' }[p] || p
}
function statusText(s) {
  return { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }[s] || s
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listComplaints(query.value)
    complaints.value = res || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleDetail(row) {
  router.push({ path: '/risk/complaints/detail', query: { id: row.id } })
}

onMounted(() => {
  fetchData()
})
</script>
