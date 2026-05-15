<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBatch } from '../../api/payroll'

const route = useRoute()
const router = useRouter()
const batch = ref(null)
const items = ref([])
const loading = ref(false)

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getBatch(route.params.id)
    batch.value = res.data
    items.value = res.data.items || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="batch-detail">
    <el-card v-if="batch">
      <template #header>
        <div class="detail-header">
          <span>{{ batch.name }} - 详情</span>
          <el-button @click="router.push('/payroll')">返回</el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="批次名称">{{ batch.name }}</el-descriptions-item>
        <el-descriptions-item label="统计周期">{{ batch.periodStart }} ~ {{ batch.periodEnd }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag>{{ batch.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>
        <span>薪资明细</span>
      </template>
      <el-table :data="items" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="workerName" label="人员" width="120" />
        <el-table-column prop="jobTitle" label="职位" min-width="160" />
        <el-table-column prop="totalHours" label="总工时" width="100" />
        <el-table-column prop="hourlyRate" label="时薪" width="100">
          <template #default="{ row }">¥{{ row.hourlyRate }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.batch-detail {
  padding: 20px;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
