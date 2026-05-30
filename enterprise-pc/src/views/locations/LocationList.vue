<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLocations, createLocation, updateLocation, deleteLocation, enableLocation, disableLocation } from '../../api/location'
import regions from '../../assets/regions.json'
import LocationPicker from '../../components/LocationPicker.vue'

const loading = ref(false)
const locations = ref([])
const formDialog = ref({ visible: false, isEdit: false, form: {} })
const showLocationPicker = ref(false)

const regionSelected = computed({
  get: () => {
    const arr = []
    if (formDialog.value.form.province) arr.push(formDialog.value.form.province)
    if (formDialog.value.form.city) arr.push(formDialog.value.form.city)
    if (formDialog.value.form.district) arr.push(formDialog.value.form.district)
    return arr
  },
  set: (val) => {
    if (!val || val.length === 0) {
      formDialog.value.form.province = ''
      formDialog.value.form.city = ''
      formDialog.value.form.district = ''
      return
    }
    formDialog.value.form.province = val[0] || ''
    formDialog.value.form.city = val[1] || ''
    formDialog.value.form.district = val[2] || ''
  }
})

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

function onLocationConfirm(pos) {
  formDialog.value.form.latitude = pos.latitude
  formDialog.value.form.longitude = pos.longitude
  if (pos.province) formDialog.value.form.province = pos.province
  if (pos.city) formDialog.value.form.city = pos.city
  if (pos.district) formDialog.value.form.district = pos.district
  if (pos.address) formDialog.value.form.address = pos.address
  if (pos.name && !formDialog.value.form.name) formDialog.value.form.name = pos.name
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
      <el-form-item label="省/市/区">
        <el-cascader v-model="regionSelected" :options="regions" placeholder="选择省/市/区" style="width:100%" />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="formDialog.form.address" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="位置">
        <el-button @click="showLocationPicker = true">选择位置</el-button>
        <span v-if="formDialog.form.latitude" style="margin-left:12px;color:#999">
          {{ formDialog.form.latitude }}, {{ formDialog.form.longitude }}
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSave">保存</el-button>
    </template>
  </el-dialog>

  <LocationPicker v-model="showLocationPicker" :latitude="formDialog.form.latitude || 39.9042" :longitude="formDialog.form.longitude || 116.4074" @confirm="onLocationConfirm" />
</template>
