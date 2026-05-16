<template>
  <el-card>
    <template #header>
      <span class="card-title">职位分类</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">添加分类</el-button>
    </template>
    <el-table :data="categories" v-loading="loading" stripe row-key="id" default-expand-all style="width:100%">
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑分类' : '添加分类'" width="500px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="dialog.form.name" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-tree-select v-model="dialog.form.parentId" :data="categoryTree" :props="{ label: 'name', value: 'id' }" placeholder="无（顶级）" clearable filterable style="width:100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialog.form.sortOrder" :min="0" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, createCategory, updateCategory, deleteCategory } from '../../api/categories'

const loading = ref(false)
const categories = ref([])
const dialog = ref({
  visible: false,
  isEdit: false,
  form: { name: '', parentId: null, sortOrder: 0 }
})

const categoryTree = computed(() => {
  function buildTree(items, parentId) {
    return items.filter(i => i.parentId === parentId).map(i => ({
      ...i,
      children: buildTree(items, i.id)
    }))
  }
  return [{ id: null, name: '无（顶级）' }, ...buildTree(categories.value, null)]
})

async function fetchData() {
  loading.value = true
  try {
    const data = await getCategories()
    categories.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialog.value = { visible: true, isEdit: false, form: { name: '', parentId: null, sortOrder: 0 } }
}

function handleEdit(row) {
  dialog.value = { visible: true, isEdit: true, form: { ...row } }
}

async function confirmSave() {
  const d = dialog.value
  if (d.isEdit) {
    await updateCategory(d.form.id, d.form)
    ElMessage.success('分类已更新')
  } else {
    await createCategory(d.form)
    ElMessage.success('分类已创建')
  }
  d.visible = false
  await fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除分类 "${row.name}"？`, '确认')
    await deleteCategory(row.id)
    ElMessage.success('分类已删除')
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
