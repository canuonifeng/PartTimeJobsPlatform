<template>
  <el-card>
    <template #header><span>FAQ 管理</span></template>

    <el-form :inline="true" :model="query" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="问题/答案" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.category" placeholder="全部分类" clearable style="width:160px">
          <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
          <el-option label="启用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div style="margin-bottom:12px">
      <el-button type="primary" @click="openDialog()">新增 FAQ</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
      </el-table-column>
      <el-table-column prop="question" label="问题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" min-width="280" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column prop="viewCount" label="浏览" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
          <el-button type="warning" link size="small" @click="handleSort(row)">排序</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑 FAQ' : '新增 FAQ'" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="问题">
          <el-input v-model="form.question" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:240px">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保 存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFaqs, createFaq, updateFaq, sortFaq, deleteFaq } from '../../api/faqAdmin'

const categoryOptions = [
  { value: 'GENERAL', label: '通用' },
  { value: 'PAYMENT', label: '支付' },
  { value: 'SCHEDULE', label: '排班' },
  { value: 'REGISTRATION', label: '注册' },
  { value: 'WITHDRAWAL', label: '提现' },
  { value: 'COMPLAINT', label: '投诉' }
]

const query = reactive({ keyword: '', category: '', status: '' })
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, question: '', answer: '', category: 'GENERAL', sortOrder: 0, status: 'ACTIVE' })

function categoryLabel(v) {
  const c = categoryOptions.find((x) => x.value === v)
  return c ? c.label : v
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listFaqs({ ...query })
    list.value = res || []
  } catch {} finally {
    loading.value = false
  }
}

function handleSearch() {
  fetchList()
}

function handleReset() {
  query.keyword = ''
  query.category = ''
  query.status = ''
  fetchList()
}

function openDialog(row) {
  if (row) {
    Object.assign(form, { id: row.id, question: row.question, answer: row.answer, category: row.category, sortOrder: row.sortOrder, status: row.status })
  } else {
    Object.assign(form, { id: null, question: '', answer: '', category: 'GENERAL', sortOrder: 0, status: 'ACTIVE' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (form.id) {
      await updateFaq({ ...form })
    } else {
      await createFaq({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch {} finally {
    saving.value = false
  }
}

async function handleSort(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入排序值', '排序', { inputValue: String(row.sortOrder) })
    await sortFaq({ id: row.id, sortOrder: Number(value) })
    ElMessage.success('已更新排序')
    fetchList()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除 FAQ「${row.question}」？`, '提示', { type: 'warning' })
    await deleteFaq(row.id)
    ElMessage.success('已删除')
    fetchList()
  } catch {}
}

onMounted(fetchList)
</script>
