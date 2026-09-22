<template>
  <el-card>
    <template #header>
      <span class="card-title">题目管理</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">新增题目</el-button>
    </template>

    <div class="filter-bar">
      <span class="filter-label">所属题库</span>
      <el-select v-model="currentBankId" placeholder="请选择题库" style="width:320px" @change="handleBankChange">
        <el-option v-for="b in banks" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
    </div>

    <el-table :data="questions" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="stem" label="题干" min-width="260" show-overflow-tooltip />
      <el-table-column prop="questionType" label="题型" width="110">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.questionType)">{{ typeText(row.questionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="answer" label="答案" width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ answerText(row) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== 'PUBLISHED'" type="success" size="small" text @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status === 'PUBLISHED'" type="warning" size="small" text @click="handleOffline(row)">下线</el-button>
          <el-button v-if="row.status !== 'PUBLISHED'" type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑题目' : '新增题目'" width="640px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="所属题库" required>
          <el-select v-model="dialog.form.bankId" style="width:100%" :disabled="dialog.isEdit">
            <el-option v-for="b in banks" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" required>
          <el-radio-group v-model="dialog.form.questionType" @change="handleTypeChange">
            <el-radio value="SINGLE_CHOICE">单选题</el-radio>
            <el-radio value="MULTIPLE_CHOICE">多选题</el-radio>
            <el-radio value="JUDGE">判断题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题干" required>
          <el-input v-model="dialog.form.stem" type="textarea" :rows="2" placeholder="请输入题干" />
        </el-form-item>
        <el-form-item label="选项" required v-if="dialog.form.questionType !== 'JUDGE'">
          <div style="width:100%">
            <div v-for="(opt, idx) in dialog.form.options" :key="idx" class="option-row">
              <el-tag class="option-key">{{ opt.key }}</el-tag>
              <el-input v-model="opt.label" :placeholder="'选项 ' + opt.key" style="flex:1" />
              <el-button type="danger" size="small" text :disabled="dialog.form.options.length <= 2" @click="removeOption(idx)">删除</el-button>
            </div>
            <el-button size="small" text type="primary" :disabled="dialog.form.options.length >= 6" @click="addOption">+ 添加选项</el-button>
          </div>
        </el-form-item>
        <el-form-item label="答案" required>
          <el-radio-group v-if="dialog.form.questionType !== 'MULTIPLE_CHOICE'" v-model="dialog.form.answer">
            <el-radio v-for="opt in dialog.form.options" :key="opt.key" :value="opt.key">{{ opt.label || opt.key }}</el-radio>
          </el-radio-group>
          <el-checkbox-group v-else v-model="dialog.form.answer">
            <el-checkbox v-for="opt in dialog.form.options" :key="opt.key" :value="opt.key">{{ opt.label || opt.key }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="dialog.form.analysis" type="textarea" :rows="2" placeholder="请输入解析（可选）" />
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionBankList, questionList, questionCreate, questionUpdate, questionDelete, questionPublish, questionOffline } from '../../api/training'

const route = useRoute()
const loading = ref(false)
const banks = ref([])
const questions = ref([])
const currentBankId = ref(null)
const dialog = ref({
  visible: false,
  isEdit: false,
  form: emptyForm()
})

function emptyForm() {
  return {
    id: null,
    bankId: null,
    questionType: 'SINGLE_CHOICE',
    stem: '',
    options: [{ key: 'A', label: '' }, { key: 'B', label: '' }],
    answer: '',
    analysis: '',
    sortOrder: 0
  }
}

function typeText(t) {
  return t === 'SINGLE_CHOICE' ? '单选题' : t === 'MULTIPLE_CHOICE' ? '多选题' : t === 'JUDGE' ? '判断题' : t || '-'
}
function typeTag(t) {
  return t === 'SINGLE_CHOICE' ? '' : t === 'MULTIPLE_CHOICE' ? 'warning' : 'info'
}
function statusText(s) {
  return s === 'PUBLISHED' ? '已发布' : s === 'DRAFT' ? '草稿' : s === 'OFFLINE' ? '已下线' : s || '-'
}
function statusTag(s) {
  return s === 'PUBLISHED' ? 'success' : s === 'DRAFT' ? 'info' : 'danger'
}
function answerText(row) {
  if (row.questionType === 'MULTIPLE_CHOICE') {
    try {
      const arr = JSON.parse(row.answer || '[]')
      return arr.join(', ')
    } catch {
      return row.answer || '-'
    }
  }
  if (row.questionType === 'JUDGE') {
    return row.answer === 'TRUE' ? '正确' : row.answer === 'FALSE' ? '错误' : (row.answer || '-')
  }
  return row.answer || '-'
}

async function fetchBanks() {
  const data = await questionBankList()
  banks.value = Array.isArray(data) ? data : (data.records || [])
}

async function fetchQuestions() {
  if (!currentBankId.value) {
    questions.value = []
    return
  }
  loading.value = true
  try {
    const data = await questionList({ bankId: currentBankId.value })
    questions.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleBankChange() {
  fetchQuestions()
}

function nextKey() {
  return String.fromCharCode(65 + dialog.value.form.options.length)
}

function rekeyOptions() {
  dialog.value.form.options.forEach((o, i) => {
    o.key = String.fromCharCode(65 + i)
  })
  dialog.value.form.answer = ''
}

function addOption() {
  if (dialog.value.form.options.length >= 6) {
    ElMessage.warning('最多 6 个选项')
    return
  }
  dialog.value.form.options.push({ key: nextKey(), label: '' })
}

function removeOption(idx) {
  if (dialog.value.form.options.length <= 2) {
    ElMessage.warning('最少 2 个选项')
    return
  }
  dialog.value.form.options.splice(idx, 1)
  rekeyOptions()
}

function handleTypeChange(t) {
  const f = dialog.value.form
  if (t === 'JUDGE') {
    f.options = [{ key: 'TRUE', label: '正确' }, { key: 'FALSE', label: '错误' }]
    f.answer = 'TRUE'
  } else {
    f.options = [{ key: 'A', label: '' }, { key: 'B', label: '' }]
    f.answer = ''
  }
}

function handleAdd() {
  if (!currentBankId.value) {
    ElMessage.warning('请先选择题库')
    return
  }
  dialog.value = { visible: true, isEdit: false, form: { ...emptyForm(), bankId: currentBankId.value } }
}

function handleEdit(row) {
  const f = {
    id: row.id,
    bankId: row.bankId,
    questionType: row.questionType,
    stem: row.stem,
    options: (row.options || []).map(o => ({ key: o.key, label: o.label })),
    answer: '',
    analysis: row.analysis || '',
    sortOrder: row.sortOrder || 0
  }
  if (row.questionType === 'MULTIPLE_CHOICE') {
    try {
      f.answer = JSON.parse(row.answer || '[]')
    } catch {
      f.answer = []
    }
  } else {
    f.answer = row.answer || ''
  }
  dialog.value = { visible: true, isEdit: true, form: f }
}

function buildPayload() {
  const f = dialog.value.form
  let answer = ''
  if (f.questionType === 'MULTIPLE_CHOICE') {
    answer = JSON.stringify((f.answer || []).slice().sort())
  } else {
    answer = f.answer || ''
  }
  return {
    id: f.id,
    bankId: f.bankId,
    questionType: f.questionType,
    stem: f.stem,
    options: f.options.map(o => ({ key: o.key, label: o.label })),
    answer,
    analysis: f.analysis,
    sortOrder: f.sortOrder
  }
}

async function confirmSave() {
  const f = dialog.value.form
  if (!f.bankId) {
    ElMessage.warning('请选择题库')
    return
  }
  if (!f.stem) {
    ElMessage.warning('请填写题干')
    return
  }
  if (f.questionType !== 'JUDGE') {
    const filled = f.options.filter(o => o.label && o.label.trim())
    if (f.options.length < 2 || f.options.length > 6) {
      ElMessage.warning('选项需 2~6 个')
      return
    }
    if (filled.length !== f.options.length) {
      ElMessage.warning('请填写完整选项内容')
      return
    }
  }
  if (!f.answer || (Array.isArray(f.answer) && f.answer.length === 0)) {
    ElMessage.warning('请选择答案')
    return
  }
  try {
    const payload = buildPayload()
    if (dialog.value.isEdit) {
      await questionUpdate(payload)
      ElMessage.success('题目已更新')
    } else {
      await questionCreate(payload)
      ElMessage.success('题目已创建')
    }
    dialog.value.visible = false
    await fetchQuestions()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handlePublish(row) {
  try {
    await questionPublish(row.id)
    ElMessage.success('已发布')
    await fetchQuestions()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确认下线题目“${row.stem}”？`, '确认')
    await questionOffline(row.id)
    ElMessage.success('已下线')
    await fetchQuestions()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除题目“${row.stem}”？删除后不可恢复`, '确认', { type: 'warning' })
    await questionDelete(row.id)
    ElMessage.success('已删除')
    await fetchQuestions()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(async () => {
  await fetchBanks()
  const qb = route.query.bankId
  if (qb) {
    currentBankId.value = Number(qb)
  }
  await fetchQuestions()
})
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.filter-bar { display: flex; align-items: center; margin-bottom: 16px; }
.filter-label { margin-right: 12px; color: #606266; }
.option-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.option-key { flex: 0 0 auto; }
</style>
