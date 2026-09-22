<template>
  <el-card>
    <template #header>
      <span class="card-title">题库管理</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">新增题库</el-button>
    </template>

    <el-table :data="banks" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="题目数" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text @click="handleToggle(row)">
            {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑题库' : '新增题库'" width="520px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="dialog.form.name" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dialog.form.description" type="textarea" :rows="3" placeholder="请输入题库描述" />
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
import { ElMessage } from 'element-plus'
import { questionBankList, questionBankCreate, questionBankUpdate, questionBankToggle } from '../../api/training'

const loading = ref(false)
const banks = ref([])
const dialog = ref({
  visible: false,
  isEdit: false,
  form: { name: '', description: '' }
})

async function fetchData() {
  loading.value = true
  try {
    const data = await questionBankList()
    banks.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialog.value = { visible: true, isEdit: false, form: { name: '', description: '' } }
}

function handleEdit(row) {
  dialog.value = { visible: true, isEdit: true, form: { id: row.id, name: row.name, description: row.description } }
}

async function confirmSave() {
  const d = dialog.value
  if (!d.form.name) {
    ElMessage.warning('请填写题库名称')
    return
  }
  try {
    if (d.isEdit) {
      await questionBankUpdate(d.form)
      ElMessage.success('题库已更新')
    } else {
      await questionBankCreate(d.form)
      ElMessage.success('题库已创建')
    }
    d.visible = false
    await fetchData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleToggle(row) {
  try {
    await questionBankToggle(row.id)
    ElMessage.success(row.status === 'ACTIVE' ? '题库已停用' : '题库已启用')
    await fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
