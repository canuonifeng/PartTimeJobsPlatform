<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBatches, createBatch, calculateBatch, confirmBatch, payBatch } from '../../api/payroll'

const router = useRouter()
const batches = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  name: '',
  periodStart: '',
  periodEnd: ''
})

const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'CALCULATED', label: '已计算' },
  { value: 'CONFIRMED', label: '已确认' },
  { value: 'PAID', label: '已发放' }
]

const statusMap = {
  DRAFT: 'info',
  CALCULATED: 'warning',
  CONFIRMED: 'primary',
  PAID: 'success'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listBatches()
    batches.value = res.data.records || res.data || []
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  form.value = { name: '', periodStart: '', periodEnd: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await createBatch(form.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchData()
  } catch {}
}

function handleView(row) {
  router.push(`/payroll/${row.id}`)
}

async function handleCalculate(row) {
  try {
    await ElMessageBox.confirm('确定计算该批次？', '提示')
    await calculateBatch(row.id)
    ElMessage.success('计算完成')
    fetchData()
  } catch {}
}

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm('确定确认该批次？确认后将不可修改', '提示')
    await confirmBatch(row.id)
    ElMessage.success('已确认')
    fetchData()
  } catch {}
}

async function handlePay(row) {
  try {
    await ElMessageBox.confirm('确定发放该批次薪资？', '提示')
    await payBatch(row.id)
    ElMessage.success('发放成功')
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="batch-list">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">新建批次</el-button>
      </div>
      <el-table :data="batches" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="批次名称" min-width="160" />
        <el-table-column label="统计周期" width="200">
          <template #default="{ row }">
            {{ row.periodStart }} ~ {{ row.periodEnd }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">
              {{ statusOptions.find(o => o.value === row.status)?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="warning" @click="handleCalculate(row)">计算</el-button>
            <el-button v-if="row.status === 'CALCULATED'" size="small" type="primary" @click="handleConfirm(row)">确认</el-button>
            <el-button v-if="row.status === 'CONFIRMED'" size="small" type="success" @click="handlePay(row)">发放</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建批次" width="500px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="批次名称" prop="name" :rules="[{ required: true, message: '请输入批次名称' }]">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="开始日期" prop="periodStart" :rules="[{ required: true, message: '请选择开始日期' }]">
          <el-date-picker v-model="form.periodStart" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="periodEnd" :rules="[{ required: true, message: '请选择结束日期' }]">
          <el-date-picker v-model="form.periodEnd" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.batch-list {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
</style>
