<template>
  <el-card>
    <template #header>
      <span class="card-title">Job Categories</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">Add Category</el-button>
    </template>
    <el-table :data="categories" v-loading="loading" stripe row-key="id" default-expand-all style="width:100%">
      <el-table-column prop="name" label="Name" min-width="200" />
      <el-table-column prop="sortOrder" label="Sort Order" width="100" />
      <el-table-column label="Actions" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">Edit</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? 'Edit Category' : 'Add Category'" width="500px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="Name">
          <el-input v-model="dialog.form.name" />
        </el-form-item>
        <el-form-item label="Parent">
          <el-tree-select v-model="dialog.form.parentId" :data="categoryTree" :props="{ label: 'name', value: 'id' }" placeholder="None (top level)" clearable filterable style="width:100%" />
        </el-form-item>
        <el-form-item label="Sort Order">
          <el-input-number v-model="dialog.form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmSave">Save</el-button>
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
  return [{ id: null, name: 'None (top level)' }, ...buildTree(categories.value, null)]
})

async function fetchData() {
  loading.value = true
  try {
    categories.value = await getCategories()
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
    ElMessage.success('Category updated')
  } else {
    await createCategory(d.form)
    ElMessage.success('Category created')
  }
  d.visible = false
  await fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`Delete category "${row.name}"?`, 'Confirm')
    await deleteCategory(row.id)
    ElMessage.success('Category deleted')
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
