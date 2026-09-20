<template>
  <el-card>
    <template #header>
      <span class="card-title">培训课程管理</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">新增课程</el-button>
    </template>

    <el-table :data="courses" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="title" label="课程标题" min-width="200" />
      <el-table-column prop="certificationName" label="关联认证" min-width="160">
        <template #default="{ row }">
          <el-tag type="info">{{ row.certificationName || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="passScore" label="及格分" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== 'PUBLISHED'" type="success" size="small" text @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status === 'PUBLISHED'" type="warning" size="small" text @click="handleOffline(row)">下线</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑课程' : '新增课程'" width="680px" top="6vh">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="课程标题" required>
          <el-input v-model="dialog.form.title" placeholder="如：数据标注入门培训" />
        </el-form-item>
        <el-form-item label="关联认证" required>
          <el-select v-model="dialog.form.certificationId" style="width:100%" placeholder="选择认证">
            <el-option v-for="c in certs" :key="c.id" :label="`${c.name}（${c.taskType}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程摘要">
          <el-input v-model="dialog.form.summary" type="textarea" :rows="2" placeholder="一句话介绍课程内容" />
        </el-form-item>
        <el-form-item label="课程内容">
          <el-input v-model="dialog.form.content" type="textarea" :rows="6" placeholder="课程正文，支持换行" />
        </el-form-item>
        <el-form-item label="及格分">
          <el-input-number v-model="dialog.form.passScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialog.form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="考试题目">
          <div class="exam-editor">
            <div v-for="(q, qi) in dialog.questions" :key="qi" class="question-box">
              <div class="question-head">
                <span class="question-no">第 {{ qi + 1 }} 题</span>
                <el-button type="danger" size="small" text @click="removeQuestion(qi)">删除</el-button>
              </div>
              <el-input v-model="q.question" placeholder="题干" class="mb8" />
              <div v-for="(opt, oi) in q.options" :key="oi" class="option-row">
                <el-radio v-model="q.answer" :value="oi" size="small">答案</el-radio>
                <el-input v-model="q.options[oi]" :placeholder="`选项 ${'ABCD'[oi] || oi + 1}`" size="small" />
              </div>
              <el-button size="small" text type="primary" @click="addOption(q)">+ 添加选项</el-button>
            </div>
            <el-button type="primary" plain size="small" @click="addQuestion">+ 添加题目</el-button>
          </div>
          <div class="form-tip">考试题以 JSON 保存；未设置题目时完成课程即通过</div>
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
import { getTrainingCertifications } from '../../api/training'
import { getTrainingCourses, createTrainingCourse, updateTrainingCourse, publishTrainingCourse, offlineTrainingCourse, deleteTrainingCourse } from '../../api/training'

const loading = ref(false)
const courses = ref([])
const certs = ref([])
const dialog = ref({
  visible: false,
  isEdit: false,
  form: { title: '', certificationId: null, summary: '', content: '', passScore: 60, sortOrder: 0 },
  questions: []
})

function statusText(s) {
  return { DRAFT: '草稿', PUBLISHED: '已发布', OFFLINE: '已下线' }[s] || s
}

function statusType(s) {
  return { DRAFT: 'info', PUBLISHED: 'success', OFFLINE: 'warning' }[s] || 'info'
}

function parseQuestions(examJson) {
  if (!examJson) return []
  try {
    const list = JSON.parse(examJson)
    return Array.isArray(list) ? list.map(q => ({
      question: q.question || '',
      options: Array.isArray(q.options) ? q.options.map(String) : [],
      answer: typeof q.answer === 'number' ? q.answer : -1
    })) : []
  } catch {
    return []
  }
}

function buildExamJson(questions) {
  const valid = questions.filter(q => q.question && Array.isArray(q.options) && q.options.some(o => o))
  return valid.length ? JSON.stringify(valid) : null
}

async function fetchData() {
  loading.value = true
  try {
    const [courseData, certData] = await Promise.all([
      getTrainingCourses(),
      getTrainingCertifications()
    ])
    courses.value = Array.isArray(courseData) ? courseData : (courseData.records || [])
    certs.value = Array.isArray(certData) ? certData : (certData.records || [])
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialog.value = {
    visible: true,
    isEdit: false,
    form: { title: '', certificationId: null, summary: '', content: '', passScore: 60, sortOrder: 0 },
    questions: []
  }
}

function handleEdit(row) {
  dialog.value = {
    visible: true,
    isEdit: true,
    form: {
      id: row.id,
      title: row.title,
      certificationId: row.certificationId,
      summary: row.summary,
      content: row.content,
      passScore: row.passScore,
      sortOrder: row.sortOrder
    },
    questions: parseQuestions(row.examJson)
  }
}

function addQuestion() {
  dialog.value.questions.push({ question: '', options: ['', ''], answer: -1 })
}

function removeQuestion(index) {
  dialog.value.questions.splice(index, 1)
}

function addOption(q) {
  q.options.push('')
}

async function confirmSave() {
  const d = dialog.value
  if (!d.form.title || !d.form.certificationId) {
    ElMessage.warning('请填写课程标题并选择关联认证')
    return
  }
  const payload = {
    ...d.form,
    examJson: buildExamJson(d.questions)
  }
  try {
    if (d.isEdit) {
      await updateTrainingCourse(payload)
      ElMessage.success('课程已更新')
    } else {
      await createTrainingCourse(payload)
      ElMessage.success('课程已创建')
    }
    d.visible = false
    await fetchData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确认发布课程 “${row.title}”？发布后兼职可见`, '确认')
    await publishTrainingCourse(row.id)
    ElMessage.success('课程已发布')
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleOffline(row) {
  try {
    await ElMessageBox.confirm(`确认下线课程 “${row.title}”？`, '确认')
    await offlineTrainingCourse(row.id)
    ElMessage.success('课程已下线')
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除课程 “${row.title}”？`, '确认')
    await deleteTrainingCourse(row.id)
    ElMessage.success('课程已删除')
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.exam-editor { width: 100%; }
.question-box { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.question-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.question-no { font-weight: 600; }
.option-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.option-row .el-radio { margin-right: 4px; white-space: nowrap; }
.mb8 { margin-bottom: 8px; }
.form-tip { font-size: 12px; color: #999; margin-top: 6px; }
</style>
