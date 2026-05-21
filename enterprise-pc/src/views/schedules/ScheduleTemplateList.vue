<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTemplates, getTemplate, createTemplate, updateTemplate, deleteTemplate } from '../../api/schedule'

const dayNumberMap = { MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6, SUNDAY: 7 }
const dayNameMap = { 1: 'MONDAY', 2: 'TUESDAY', 3: 'WEDNESDAY', 4: 'THURSDAY', 5: 'FRIDAY', 6: 'SATURDAY', 7: 'SUNDAY' }

const templates = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = ref({
  name: '',
  description: '',
  workDays: [],
  startTime: '',
  endTime: ''
})

const dayOptions = [
  { value: 'MONDAY', label: '周一' },
  { value: 'TUESDAY', label: '周二' },
  { value: 'WEDNESDAY', label: '周三' },
  { value: 'THURSDAY', label: '周四' },
  { value: 'FRIDAY', label: '周五' },
  { value: 'SATURDAY', label: '周六' },
  { value: 'SUNDAY', label: '周日' }
]

function formatTime(t) {
  if (!t) return ''
  return t.length > 5 ? t.substring(0, 5) : t
}

function slotSummary(slots) {
  if (!slots || slots.length === 0) return '-'
  const days = [...new Set(slots.map(s => dayOptions.find(d => d.value === dayNameMap[s.dayOfWeek])?.label || ''))]
  const time = slots.length > 0 ? `${formatTime(slots[0].startTime)}-${formatTime(slots[0].endTime)}` : ''
  return `${days.join('、')} ${time}`
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listTemplates()
    templates.value = Array.isArray(res) ? res : (res.records || [])
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  isEdit.value = false
  dialogTitle.value = '新建排班模板'
  form.value = { name: '', description: '', workDays: [], startTime: '', endTime: '' }
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑排班模板'
  const res = await getTemplate(row.id)
  const slots = res.slots || []
  form.value = {
    id: res.id,
    name: res.name,
    description: res.description,
    workDays: slots.map(s => dayNameMap[s.dayOfWeek]).filter(Boolean),
    startTime: formatTime(slots[0]?.startTime || ''),
    endTime: formatTime(slots[0]?.endTime || '')
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const slots = form.value.workDays.map(day => ({
    dayOfWeek: dayNumberMap[day],
    startTime: form.value.startTime,
    endTime: form.value.endTime
  }))
  const payload = { name: form.value.name, description: form.value.description, slots }

  try {
    if (isEdit.value) {
      await updateTemplate(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该模板？', '提示')
    await deleteTemplate(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="template-list">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">新建模板</el-button>
      </div>
      <el-table :data="templates" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="模板名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="模板名称" prop="name" :rules="[{ required: true, message: '请输入模板名称' }]">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="工作日" prop="workDays">
          <el-checkbox-group v-model="form.workDays">
            <el-checkbox v-for="opt in dayOptions" :key="opt.value" :value="opt.value" :label="opt.label" />
          </el-checkbox-group>
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
.template-list {
  padding: 20px;
}
.toolbar {
  margin-bottom: 16px;
}
</style>
