<template>
  <el-card>
    <template #header>
      <span class="card-title">标签管理</span>
      <div style="float:right">
        <el-button type="primary" size="small" @click="handleAddGroup">新增标签组</el-button>
        <el-button type="success" size="small" @click="handleAddTag">新增标签</el-button>
      </div>
    </template>
    <el-table :data="tagRows" v-loading="loading" stripe row-key="rowKey" default-expand-all style="width:100%">
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="code" label="编码" min-width="160" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="typeLabel" label="类型" width="100" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.rowType === 'group'" type="success" size="small" text @click="handleAddTag(row)">新增标签</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="groupDialog.visible" :title="groupDialog.isEdit ? '编辑标签组' : '新增标签组'" width="500px">
      <el-form :model="groupDialog.form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="groupDialog.form.name" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="groupDialog.form.code" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="groupDialog.form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="groupDialog.form.status" style="width:100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveGroup">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialog.visible" :title="tagDialog.isEdit ? '编辑标签' : '新增标签'" width="500px">
      <el-form :model="tagDialog.form" label-width="100px">
        <el-form-item label="标签组">
          <el-select v-model="tagDialog.form.groupId" placeholder="请选择标签组" filterable style="width:100%">
            <el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="tagDialog.form.name" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="tagDialog.form.code" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="tagDialog.form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="tagDialog.form.status" style="width:100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveTag">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobTags, createTagGroup, updateTagGroup, deleteTagGroup, createTag, updateTag, deleteTag } from '../../api/jobTags'

const loading = ref(false)
const groups = ref([])
const groupDialog = ref({
  visible: false,
  isEdit: false,
  form: { name: '', code: '', sortOrder: 0, status: 'ACTIVE' }
})
const tagDialog = ref({
  visible: false,
  isEdit: false,
  form: { groupId: null, name: '', code: '', sortOrder: 0, status: 'ACTIVE' }
})

const tagRows = computed(() => groups.value.map(group => ({
  ...group,
  rowKey: `group-${group.id}`,
  rowType: 'group',
  typeLabel: '标签组',
  children: (group.tags || group.children || []).map(tag => ({
    ...tag,
    groupId: tag.groupId ?? group.id,
    rowKey: `tag-${tag.id}`,
    rowType: 'tag',
    typeLabel: '标签'
  }))
})))

function buildPayload(item) {
  return {
    name: item.name,
    code: item.code,
    sortOrder: item.sortOrder ?? 0,
    status: item.status
  }
}

function buildTagPayload(item) {
  return {
    ...buildPayload(item),
    groupId: item.groupId
  }
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getJobTags()
    groups.value = Array.isArray(data) ? data : (data?.records || [])
  } finally {
    loading.value = false
  }
}

function handleAddGroup() {
  groupDialog.value = { visible: true, isEdit: false, form: { name: '', code: '', sortOrder: 0, status: 'ACTIVE' } }
}

function handleAddTag(group) {
  tagDialog.value = { visible: true, isEdit: false, form: { groupId: group?.id ?? null, name: '', code: '', sortOrder: 0, status: 'ACTIVE' } }
}

function handleEdit(row) {
  if (row.rowType === 'group') {
    groupDialog.value = { visible: true, isEdit: true, form: { ...row } }
  } else {
    tagDialog.value = { visible: true, isEdit: true, form: { ...row } }
  }
}

async function confirmSaveGroup() {
  const d = groupDialog.value
  if (d.isEdit) {
    await updateTagGroup(d.form.id, buildPayload(d.form))
    ElMessage.success('标签组已更新')
  } else {
    await createTagGroup(buildPayload(d.form))
    ElMessage.success('标签组已创建')
  }
  d.visible = false
  await fetchData()
}

async function confirmSaveTag() {
  const d = tagDialog.value
  if (!d.form.groupId) {
    ElMessage.warning('请选择标签组')
    return
  }
  if (d.isEdit) {
    await updateTag(d.form.id, buildTagPayload(d.form))
    ElMessage.success('标签已更新')
  } else {
    await createTag(buildTagPayload(d.form))
    ElMessage.success('标签已创建')
  }
  d.visible = false
  await fetchData()
}

async function handleToggleStatus(row) {
  try {
    if (row.status === 'ACTIVE') {
      await ElMessageBox.confirm(`确认禁用${row.rowType === 'group' ? '标签组' : '标签'} “${row.name}”？`, '确认')
      if (row.rowType === 'group') {
        await deleteTagGroup(row.id)
        ElMessage.success('标签组已禁用')
      } else {
        await deleteTag(row.id)
        ElMessage.success('标签已禁用')
      }
    } else if (row.rowType === 'group') {
      await updateTagGroup(row.id, buildPayload({ ...row, status: 'ACTIVE' }))
      ElMessage.success('标签组已启用')
    } else {
      await updateTag(row.id, buildTagPayload({ ...row, status: 'ACTIVE' }))
      ElMessage.success('标签已启用')
    }
    await fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
