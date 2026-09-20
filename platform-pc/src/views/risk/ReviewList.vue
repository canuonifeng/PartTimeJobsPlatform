<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>评价管理</span>
        <el-select v-model="query.isViolation" placeholder="是否违规" size="small" style="width:120px;margin-right:8px" clearable @change="fetchData">
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>
      </div>
    </template>
    <el-table :data="reviews" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="reviewType" label="评价类型" width="120">
        <template #default="{ row }">{{ row.reviewType === 'WORKER_TO_ENTERPRISE' ? '工人评企业' : '企业评工人' }}</template>
      </el-table-column>
      <el-table-column prop="reviewerName" label="评价人" width="120" />
      <el-table-column prop="revieweeName" label="被评价方" width="120" />
      <el-table-column prop="jobTitle" label="相关职位" width="150" />
      <el-table-column prop="rating" label="评分" width="100">
        <template #default="{ row }">
          <el-rate v-model="row.rating" disabled show-score />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="isViolation" label="是否违规" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isViolation ? 'danger' : 'info'" size="small">{{ row.isViolation ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="评价时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleViewDetail(row)">详情</el-button>
          <el-button v-if="!row.isViolation" type="warning" size="small" text @click="handleMarkViolation(row)">标记违规</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReviews, markReviewViolation, deleteReview } from '../../api/reviewAdmin'

const loading = ref(false)
const reviews = ref([])
const query = ref({ isViolation: null })

async function fetchData() {
  loading.value = true
  try {
    const res = await listReviews(query.value)
    reviews.value = res || []
  } finally {
    loading.value = false
  }
}

async function handleMarkViolation(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入违规原因', '标记违规', { type: 'warning' })
    await markReviewViolation({ id: row.id, violationReason: reason, operatorName: '管理员' })
    ElMessage.success('已标记为违规')
    await fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除该评价？`, '提示', { type: 'warning' })
    await deleteReview(row.id)
    ElMessage.success('已删除')
    await fetchData()
  } catch {}
}

function handleViewDetail(row) {
  ElMessage.info('查看详情功能开发中')
}

onMounted(() => {
  fetchData()
})
</script>
