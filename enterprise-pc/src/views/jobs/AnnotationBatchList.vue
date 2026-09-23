<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBatches, createBatch, updateBatch, toggleBatch } from '../../api/annotationBatch'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const router = useRouter()
const jobId = computed(() => props.id)

const batches = ref([])
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await listBatches(jobId.value)
    batches.value = Array.isArray(res) ? res : (res.records || res.list || [])
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push('/jobs/annotation')
}

const statusOptions = [
  { value: 'ACTIVE', label: '已上线' },
  { value: 'CANCELLED', label: '已下线' }
]

const statusMap = {
  ACTIVE: 'success',
  CANCELLED: 'info'
}

function progressPercent(row) {
  const total = Number(row.totalItems) || 0
  if (total <= 0) return 0
  const done = Number(row.completedCount) || 0
  return Math.min(100, Math.round((done / total) * 100))
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitting = ref(false)
const formRef = ref()
const form = ref({
  id: null,
  batchCode: '',
  totalItems: undefined,
  externalBatchId: ''
})

const formRules = {
  batchCode: [{ required: true, message: '请输入批次代号', trigger: 'blur' }],
  totalItems: [
    { required: true, message: '请输入数据量', trigger: 'blur' },
    { pattern: /^[1-9]\d*$/, message: '数据量须为正整数', trigger: 'blur' }
  ]
}

function openCreate() {
  dialogMode.value = 'create'
  form.value = { id: null, batchCode: '', totalItems: undefined, externalBatchId: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  form.value = {
    id: row.id,
    batchCode: row.batchCode,
    totalItems: row.totalItems,
    externalBatchId: row.externalBatchId || ''
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createBatch({
        jobId: jobId.value,
        batchCode: form.value.batchCode,
        totalItems: Number(form.value.totalItems),
        externalBatchId: form.value.externalBatchId || undefined
      })
      ElMessage.success('新建成功')
    } else {
      await updateBatch({
        id: form.value.id,
        batchCode: form.value.batchCode,
        totalItems: Number(form.value.totalItems),
        externalBatchId: form.value.externalBatchId || undefined
      })
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
  } finally {
    submitting.value = false
  }
}

async function handleToggle(row) {
  const target = row.status === 'ACTIVE' ? 'CANCELLED' : 'ACTIVE'
  const action = target === 'ACTIVE' ? '上线' : '下线'
  try {
    await ElMessageBox.confirm(`确定${action}批次「${row.batchCode}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await toggleBatch({ id: row.id, status: target })
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="annotation-batch-list">
    <el-card>
      <div class="toolbar">
        <el-button @click="handleBack">返回标注列表</el-button>
        <el-button type="primary" @click="openCreate">新建批次</el-button>
      </div>
      <el-table :data="batches" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="batchCode" label="批次代号" min-width="140" />
        <el-table-column prop="totalItems" label="数据量" width="100" />
        <el-table-column prop="externalBatchId" label="外部批次ID" min-width="140">
          <template #default="{ row }">{{ row.externalBatchId || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">
              {{ statusOptions.find(o => o.value === row.status)?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grabbedCount" label="已抢" width="90" />
        <el-table-column prop="completedCount" label="已完成" width="90" />
        <el-table-column label="进度" min-width="160">
          <template #default="{ row }">
            <el-progress :percentage="progressPercent(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleToggle(row)"
            >
              {{ row.status === 'ACTIVE' ? '下线' : '上线' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新建批次' : '编辑批次'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="批次代号" prop="batchCode">
          <el-input v-model="form.batchCode" placeholder="请输入批次代号" />
        </el-form-item>
        <el-form-item label="数据量" prop="totalItems">
          <el-input v-model="form.totalItems" placeholder="请输入正整数" />
        </el-form-item>
        <el-form-item label="外部批次ID" prop="externalBatchId">
          <el-input v-model="form.externalBatchId" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.annotation-batch-list {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
</style>
