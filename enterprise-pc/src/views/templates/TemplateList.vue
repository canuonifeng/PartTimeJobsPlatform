<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTemplates, createTemplate, updateTemplate, deleteTemplate } from '../../api/template'
import { listLocations } from '../../api/location'
import LocationPicker from '../../components/LocationPicker.vue'

const loading = ref(false)
const templates = ref([])
const formDialog = ref({ visible: false, isEdit: false, form: {} })
const showLocationPicker = ref(false)
const availableLocations = ref([])
const locationDialogVisible = ref(false)
const categoryOptions = [
  { value: 1, label: '餐饮服务' },
  { value: 2, label: '促销导购' },
  { value: 3, label: '快递配送' },
  { value: 4, label: '教育培训' },
  { value: 5, label: '家政保洁' },
  { value: 6, label: '美容美发' },
  { value: 7, label: '其他' }
]

async function fetchData() {
  loading.value = true
  try {
    templates.value = await listTemplates()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  return { title: '', description: '', categoryId: null, imageUrl: '', province: '', city: '', district: '', address: '', latitude: null, longitude: null }
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
      title: row.title,
      description: row.description,
      categoryId: row.categoryId,
      imageUrl: row.imageUrl || '',
      province: row.province || '',
      city: row.city || '',
      district: row.district || '',
      address: row.address || '',
      latitude: row.latitude,
      longitude: row.longitude
    }
  }
}

async function openLocationPicker() {
  try {
    const data = await listLocations({ page: 1, pageSize: 100 })
    availableLocations.value = Array.isArray(data) ? data : (data.records || [])
    locationDialogVisible.value = true
  } catch {
    ElMessage.error('加载地点列表失败')
  }
}

function selectLocation(loc) {
  const f = formDialog.value.form
  f.province = loc.province || ''
  f.city = loc.city || ''
  f.district = loc.district || ''
  f.address = loc.address || ''
  f.latitude = loc.latitude
  f.longitude = loc.longitude
  locationDialogVisible.value = false
  ElMessage.success(`已选择地点：${loc.name}`)
}

function onLocationConfirm(pos) {
  const f = formDialog.value.form
  f.latitude = pos.latitude
  f.longitude = pos.longitude
  if (pos.province) f.province = pos.province
  if (pos.city) f.city = pos.city
  if (pos.district) f.district = pos.district
  if (pos.address) f.address = pos.address
}

async function confirmSave() {
  const f = formDialog.value
  if (f.isEdit) {
    await updateTemplate(f.form)
    ElMessage.success('修改成功')
  } else {
    await createTemplate(f.form)
    ElMessage.success('新增成功')
  }
  f.visible = false
  await fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除模版 "${row.title}"？`, '确认')
    await deleteTemplate(row.id)
    ElMessage.success('已删除')
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>职位模版</span>
        <el-button type="primary" @click="handleAdd">新建模版</el-button>
      </div>
    </template>
    <el-table :data="templates" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="title" label="职位名称" min-width="150" />
      <el-table-column label="工作地点" min-width="250" show-overflow-tooltip>
        <template #default="{ row }">
          {{ [row.province, row.city, row.district, row.address].filter(Boolean).join(' ') || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="类别" width="120">
        <template #default="{ row }">
          {{ categoryOptions.find(c => c.value === row.categoryId)?.label || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑模版' : '新建模版'" width="550px" :close-on-click-modal="false">
    <el-form :model="formDialog.form" label-width="80px">
      <el-form-item label="职位名称">
        <el-input v-model="formDialog.form.title" />
      </el-form-item>
      <el-form-item label="职位描述">
        <el-input v-model="formDialog.form.description" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="类别">
        <el-select v-model="formDialog.form.categoryId" placeholder="选择类别" style="width:100%" clearable>
          <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="岗位图片">
        <el-input v-model="formDialog.form.imageUrl" placeholder="图片URL" clearable />
      </el-form-item>
      <el-form-item label="工作地点">
        <el-button @click="openLocationPicker">选择已有地点</el-button>
      </el-form-item>
      <el-form-item label="省/市/区">
        <el-input v-model="formDialog.form.province" placeholder="省" style="width:110px;margin-right:6px" />
        <el-input v-model="formDialog.form.city" placeholder="市" style="width:110px;margin-right:6px" />
        <el-input v-model="formDialog.form.district" placeholder="区" style="width:110px" />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="formDialog.form.address" />
      </el-form-item>
      <el-form-item label="坐标">
        <el-button @click="showLocationPicker = true">选择位置</el-button>
        <span v-if="formDialog.form.latitude" style="margin-left:12px;color:#999">{{ formDialog.form.latitude }}, {{ formDialog.form.longitude }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSave">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="locationDialogVisible" title="选择工作地点" width="600px">
    <el-table :data="availableLocations" stripe @row-click="selectLocation" highlight-current-row>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="province" label="省" width="80" />
      <el-table-column prop="city" label="市" width="80" />
      <el-table-column prop="district" label="区" width="80" />
      <el-table-column prop="address" label="详细地址" min-width="180" />
    </el-table>
    <template #footer>
      <el-button @click="locationDialogVisible = false">取消</el-button>
    </template>
  </el-dialog>

  <LocationPicker v-model="showLocationPicker" :latitude="formDialog.form.latitude || 39.9042" :longitude="formDialog.form.longitude || 116.4074" @confirm="onLocationConfirm" />
</template>
