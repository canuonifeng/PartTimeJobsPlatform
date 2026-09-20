<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkerMappings, createWorkerMapping, deleteWorkerMapping } from '../../api/workerMapping'

const mappings = ref([])
const total = ref(0)
const loading = ref(false)
const searchForm = ref({
  page: 1,
  pageSize: 20
})

const dialogVisible = ref(false)
const formRef = ref(null)
const addForm = ref({
  externalSystemType: '',
  externalWorkerId: '',
  internalWorkerId: ''
})

const systemTypeOptions = [
  { value: 'SDK', label: 'SDK' },
  { value: 'API', label: 'API' },
  { value: 'MANUAL', label: '手动录入' }
]

async function fetchData() {
  loading.value = true
  try {
    const res = await listWorkerMappings(searchForm.value)
    const data = Array.isArray(res) ? res : (res.records || [])
    mappings.value = data
    total.value = Array.isArray(res) ? res.length : (res.total || 0)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  searchForm.value.page = page
  fetchData()
}

function openAddDialog() {
  addForm.value = { externalSystemType: '', externalWorkerId: '', internalWorkerId: '' }
  dialogVisible.value = true
}

async function handleAdd() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await createWorkerMapping({ ...addForm.value })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('添加失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该映射？', '提示', { type: 'warning' })
    await deleteWorkerMapping(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="external-worker-mapping">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openAddDialog">新增映射</el-button>
      </div>
      <el-table :data="mappings" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="externalSystemType" label="外部系统类型" width="140" />
        <el-table-column prop="externalWorkerId" label="外部工人ID" min-width="160" />
        <el-table-column prop="internalWorkerId" label="内部工人ID" min-width="140" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" title="新增工人映射" width="500px">
      <el-form ref="formRef" :model="addForm" label-width="120px">
        <el-form-item label="外部系统类型" prop="externalSystemType" :rules="[{ required: true, message: '请选择外部系统类型' }]">
          <el-select v-model="addForm.externalSystemType" style="width: 100%">
            <el-option v-for="opt in systemTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="外部工人ID" prop="externalWorkerId" :rules="[{ required: true, message: '请输入外部工人ID' }]">
          <el-input v-model="addForm.externalWorkerId" />
        </el-form-item>
        <el-form-item label="内部工人ID" prop="internalWorkerId" :rules="[{ required: true, message: '请输入内部工人ID' }]">
          <el-input v-model="addForm.internalWorkerId" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.external-worker-mapping {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
