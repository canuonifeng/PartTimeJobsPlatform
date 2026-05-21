<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listShifts, createShift, deleteShift } from '../../api/schedule'

const shifts = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 20
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
    const res = await listShifts({ ...searchForm.value, page: page.value, pageSize })
    const data = Array.isArray(res) ? res : (res.records || [])
    shifts.value = data
    total.value = Array.isArray(res) ? res.length : (res.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.value = { jobId: '', workerName: '', date: '' }
  page.value = 1
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

function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function handlePageChange(val) {
  page.value = val
  fetchData()
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
        <el-table-column prop="shiftDate" label="日期" width="120" />
        <el-table-column label="时段" width="160">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="签到" width="160">
          <template #default="{ row }">
            <template v-if="row.attendanceStatus === 'CHECKED_OUT'">
              <div class="att-row">签退 {{ formatTime(row.checkOutTime) }}</div>
              <div class="att-row att-sub">签到 {{ formatTime(row.checkInTime) }}</div>
            </template>
            <template v-else-if="row.attendanceStatus === 'CHECKED_IN'">
              <div class="att-row">签到 {{ formatTime(row.checkInTime) }}</div>
            </template>
            <span v-else class="att-none">未签到</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-if="total > pageSize"
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @current-change="handlePageChange"
        />
      </div>
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
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 16px 0 4px;
}
.att-row {
  font-size: 13px;
  line-height: 1.6;
}
.att-sub {
  color: #999;
  font-size: 12px;
}
.att-none {
  color: #999;
}
</style>
