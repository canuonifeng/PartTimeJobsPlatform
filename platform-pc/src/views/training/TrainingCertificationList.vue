<template>
  <el-card>
    <template #header>
      <span class="card-title">技能认证管理</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">新增认证</el-button>
    </template>

    <el-table :data="certs" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="name" label="认证名称" min-width="160" />
      <el-table-column prop="code" label="认证编码" width="140" />
      <el-table-column prop="taskType" label="适用任务类型" width="140">
        <template #default="{ row }">
          <el-tag>{{ taskTypeText(row.taskType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="validDays" label="有效期(天)" width="120">
        <template #default="{ row }">
          {{ row.validDays ? row.validDays + '天' : '永久' }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text @click="handleToggle(row)">
            {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑认证' : '新增认证'" width="520px">
      <el-form :model="dialog.form" label-width="110px">
        <el-form-item label="认证名称" required>
          <el-input v-model="dialog.form.name" placeholder="如：数据标注技能认证" />
        </el-form-item>
        <el-form-item label="认证编码" required>
          <el-input v-model="dialog.form.code" placeholder="如：ANNOTATION_BASIC" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item label="适用任务类型" required>
          <el-select v-model="dialog.form.taskType" style="width:100%">
            <el-option label="标注任务" value="ANNOTATION" />
            <el-option label="普通工作" value="WORK" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期(天)">
          <el-input-number v-model="dialog.form.validDays" :min="1" placeholder="留空为永久有效" />
          <div class="form-tip">留空表示永久有效</div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dialog.form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTrainingCertifications, createTrainingCertification, updateTrainingCertification, toggleTrainingCertification } from '../../api/training'

const loading = ref(false)
const certs = ref([])
const dialog = ref({
  visible: false,
  isEdit: false,
  form: { name: '', code: '', taskType: 'ANNOTATION', validDays: null, description: '' }
})

function taskTypeText(type) {
  return type === 'ANNOTATION' ? '标注任务' : (type === 'WORK' ? '普通工作' : type || '-')
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getTrainingCertifications()
    certs.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialog.value = { visible: true, isEdit: false, form: { name: '', code: '', taskType: 'ANNOTATION', validDays: null, description: '' } }
}

function handleEdit(row) {
  dialog.value = { visible: true, isEdit: true, form: { ...row } }
}

async function confirmSave() {
  const d = dialog.value
  if (!d.form.name || !d.form.code || !d.form.taskType) {
    ElMessage.warning('请填写认证名称、编码和任务类型')
    return
  }
  try {
    if (d.isEdit) {
      await updateTrainingCertification(d.form)
      ElMessage.success('认证已更新')
    } else {
      await createTrainingCertification(d.form)
      ElMessage.success('认证已创建')
    }
    d.visible = false
    await fetchData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleToggle(row) {
  try {
    if (row.status === 'ACTIVE') {
      await ElMessageBox.confirm(`确认停用认证 “${row.name}”？停用后已持证工人将无法凭此认证抢单`, '确认')
      await toggleTrainingCertification(row.id, 'DISABLED')
      ElMessage.success('认证已停用')
    } else {
      await toggleTrainingCertification(row.id, 'ACTIVE')
      ElMessage.success('认证已启用')
    }
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
</style>
