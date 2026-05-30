<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLocations, createLocation, updateLocation, deleteLocation, enableLocation, disableLocation } from '../../api/location'

const loading = ref(false)
const locations = ref([])
const formDialog = ref({ visible: false, isEdit: false, form: {} })

async function fetchData() {
  loading.value = true
  try {
    locations.value = await listLocations()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  return { name: '', province: '', city: '', district: '', address: '', latitude: null, longitude: null }
}

function handleAdd() {
  formDialog.value = { visible: true, isEdit: false, form: resetForm() }
}

function handleEdit(row) {
  formDialog.value = {
    visible: true,
    isEdit: true,
    form: {
      id: row.id,
      name: row.name,
      province: row.province,
      city: row.city,
      district: row.district,
      address: row.address,
      latitude: row.latitude,
      longitude: row.longitude
    }
  }
}

async function confirmSave() {
  const f = formDialog.value
  if (f.isEdit) {
    await updateLocation(f.form)
    ElMessage.success('修改成功')
  } else {
    await createLocation(f.form)
    ElMessage.success('新增成功')
  }
  f.visible = false
  await fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除地点 "${row.name}"？`, '确认')
    await deleteLocation(row.id)
    ElMessage.success('已删除')
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleToggle(row) {
  try {
    if (row.status === 'ENABLED') {
      await disableLocation(row.id)
      ElMessage.success('已禁用')
    } else {
      await enableLocation(row.id)
      ElMessage.success('已启用')
    }
    await fetchData()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>工作地点</span>
        <el-button type="primary" @click="handleAdd">新建地点</el-button>
      </div>
    </template>
    <el-table :data="locations" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="province" label="省" width="100" />
      <el-table-column prop="city" label="市" width="100" />
      <el-table-column prop="district" label="区" width="100" />
      <el-table-column prop="address" label="详细地址" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'ENABLED' ? 'warning' : 'success'" @click="handleToggle(row)">
            {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑地点' : '新建地点'" width="500px" :close-on-click-modal="false">
    <el-form :model="formDialog.form" label-width="80px">
      <el-form-item label="名称">
        <el-input v-model="formDialog.form.name" placeholder="如：总部、分店A" />
      </el-form-item>
      <el-form-item label="省">
        <el-input v-model="formDialog.form.province" />
      </el-form-item>
      <el-form-item label="市">
        <el-input v-model="formDialog.form.city" />
      </el-form-item>
      <el-form-item label="区">
        <el-input v-model="formDialog.form.district" />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="formDialog.form.address" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="纬度">
        <el-input-number v-model="formDialog.form.latitude" :precision="7" :step="0.01" style="width:100%" />
      </el-form-item>
      <el-form-item label="经度">
        <el-input-number v-model="formDialog.form.longitude" :precision="7" :step="0.01" style="width:100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSave">保存</el-button>
    </template>
  </el-dialog>
</template>
