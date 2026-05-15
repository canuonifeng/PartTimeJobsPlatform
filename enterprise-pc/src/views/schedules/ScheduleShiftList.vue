<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listShifts, createShift, updateShift, deleteShift } from '../../api/schedule'

const shifts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  jobId: '',
  workerId: '',
  date: '',
  startTime: '',
  endTime: ''
})
const searchForm = ref({
  jobId: '',
  workerName: '',
  date: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await listShifts(searchForm.value)
    shifts.value = res.data.records || res.data || []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchData()
}

function handleReset() {
  searchForm.value = { jobId: '', workerName: '', date: '' }
  fetchData()
}

function handleAssign() {
  form.value = { jobId: '', workerId: '', date: '', startTime: '', endTime: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await createShift(form.value)
    ElMessage.success('分配成功')
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该班次？', '提示')
    await deleteShift(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="shift-list">
    <el-card>
      <el-form :model="searchForm" inline>
        <el-form-item label="职位">
          <el-input v-model="searchForm.jobId" placeholder="职位ID" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="人员">
          <el-input v-model="searchForm.workerName" placeholder="姓名" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <div class="toolbar">
        <el-button type="primary" @click="handleAssign">分配班次</el-button>
      </div>
      <el-table :data="shifts" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="jobTitle" label="职位" min-width="160" />
        <el-table-column prop="workerName" label="人员" width="120" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="时段" width="160">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="分配班次" width="500px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="职位" prop="jobId" :rules="[{ required: true, message: '请输入职位ID' }]">
          <el-input v-model="form.jobId" placeholder="职位ID" />
        </el-form-item>
        <el-form-item label="人员" prop="workerId" :rules="[{ required: true, message: '请输入人员ID' }]">
          <el-input v-model="form.workerId" placeholder="人员ID" />
        </el-form-item>
        <el-form-item label="日期" prop="date" :rules="[{ required: true, message: '请选择日期' }]">
          <el-date-picker v-model="form.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker v-model="form.startTime" value-format="HH:mm" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker v-model="form.endTime" value-format="HH:mm" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.shift-list {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
</style>
