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
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" size="small" text @click="openLessons(row)">课时管理</el-button>
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
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="lessonDrawer.visible" :title="`课时管理 - ${lessonDrawer.courseTitle}`" size="640px">
      <div class="lesson-drawer-head">
        <el-button type="primary" size="small" @click="handleLessonAdd">新增课时</el-button>
      </div>
      <el-table :data="lessonDrawer.list" v-loading="lessonDrawer.loading" stripe style="width:100%" row-key="id" ref="lessonTableRef">
        <el-table-column label="" width="36" class-name="drag-handle-col">
          <template #default>
            <span class="drag-handle">⠿</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="lessonType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="lessonTypeTag(row.lessonType)">{{ lessonTypeText(row.lessonType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="lessonStatusTag(row.status)">{{ lessonStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="handleLessonEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" type="success" size="small" text @click="handleLessonPublish(row)">发布</el-button>
            <el-button v-if="row.status === 'PUBLISHED'" type="warning" size="small" text @click="handleLessonOffline(row)">下线</el-button>
            <el-button v-if="row.status !== 'PUBLISHED'" type="danger" size="small" text @click="handleLessonDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="lessonDialog.visible" :title="lessonDialog.isEdit ? '编辑课时' : '新增课时'" width="680px" top="6vh">
      <el-form :model="lessonDialog.form" label-width="110px">
        <el-form-item label="课时标题" required>
          <el-input v-model="lessonDialog.form.title" placeholder="请输入课时标题" />
        </el-form-item>
        <el-form-item label="课时类型" required>
          <el-select v-model="lessonDialog.form.lessonType" style="width:100%" @change="handleLessonTypeChange">
            <el-option label="视频" value="VIDEO" />
            <el-option label="音频" value="AUDIO" />
            <el-option label="文档" value="DOCUMENT" />
            <el-option label="图文" value="IMAGE_TEXT" />
            <el-option label="考试" value="EXAM" />
          </el-select>
        </el-form-item>
        <el-form-item label="媒体地址" required v-if="isMediaLesson">
          <div style="display:flex;gap:8px;align-items:center;width:100%">
            <el-input v-model="lessonDialog.form.mediaUrl" placeholder="上传文件后自动回填，也可手动输入" />
            <el-upload
              :action="adminUploadUrl"
              :headers="getUploadHeaders()"
              :show-file-list="false"
              :on-success="handleMediaUploadSuccess"
              :before-upload="beforeMediaUpload"
              style="flex-shrink:0"
            >
              <el-button type="primary" plain>上传文件</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="时长(分钟)" v-if="isMediaLesson">
          <el-input-number v-model="lessonDialog.form.durationMinutes" :min="0" />
        </el-form-item>
        <el-form-item label="文件上传" v-if="isDocumentLesson">
          <div style="display:flex;gap:8px;align-items:center;width:100%">
            <el-input v-model="lessonDialog.form.mediaUrl" placeholder="上传文档后自动回填，也可手动输入" />
            <el-upload
              :action="adminUploadUrl"
              :headers="getUploadHeaders()"
              :show-file-list="false"
              :on-success="handleMediaUploadSuccess"
              :before-upload="beforeMediaUpload"
              style="flex-shrink:0"
            >
              <el-button type="primary" plain>上传文件</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="内容" required v-if="isImageTextLesson">
          <div style="width:100%;border:1px solid #dcdfe6;border-radius:4px">
            <Toolbar :editor="wangEditorRef" :defaultConfig="toolbarConfig" mode="simple" style="border-bottom:1px solid #dcdfe6" />
            <Editor v-model="editorHtml" :defaultConfig="editorConfig" mode="simple" style="height:300px;overflow-y:hidden" @onCreated="handleEditorCreated" />
          </div>
        </el-form-item>
        <template v-if="isExamLesson">
          <el-form-item label="题库" required>
            <el-select v-model="lessonDialog.form.examConfig.bankId" style="width:100%" placeholder="选择题库">
              <el-option v-for="b in banks" :key="b.id" :label="b.name" :value="b.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试时长(分钟)" required>
            <el-input-number v-model="lessonDialog.form.examConfig.durationMinutes" :min="1" />
          </el-form-item>
          <el-form-item label="及格分" required>
            <el-input-number v-model="lessonDialog.form.examConfig.passScore" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="题型规则" required>
            <div style="width:100%">
              <div v-for="(rule, ri) in lessonDialog.form.examConfig.rules" :key="ri" class="rule-row">
                <el-select v-model="rule.questionType" placeholder="题型" style="width:150px">
                  <el-option label="单选题" value="SINGLE_CHOICE" />
                  <el-option label="多选题" value="MULTIPLE_CHOICE" />
                  <el-option label="判断题" value="JUDGE" />
                </el-select>
                <el-input-number v-model="rule.count" :min="1" placeholder="题量" style="width:110px" />
                <span class="rule-label">题量</span>
                <el-input-number v-model="rule.scorePer" :min="1" placeholder="每题分" style="width:120px" />
                <span class="rule-label">每题分</span>
                <el-button type="danger" size="small" text @click="removeRule(ri)">删除</el-button>
              </div>
              <el-button size="small" text type="primary" @click="addRule">+ 添加规则</el-button>
              <div class="rule-total">总分（自动）：{{ examTotalScore }} 分</div>
            </div>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="lessonDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmLessonSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sortable from 'sortablejs'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { getTrainingCertifications } from '../../api/training'
import { getTrainingCourses, createTrainingCourse, updateTrainingCourse, publishTrainingCourse, offlineTrainingCourse, deleteTrainingCourse } from '../../api/training'
import { questionBankList, lessonList, lessonCreate, lessonUpdate, lessonDelete, lessonPublish, lessonOffline, lessonSort, adminUploadUrl, getUploadHeaders, getUploadUrl } from '../../api/training'

const loading = ref(false)
const courses = ref([])
const certs = ref([])
const banks = ref([])
const lessonTableRef = ref(null)
let sortableInstance = null

const wangEditorRef = ref(null)
const editorHtml = ref('')
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入图文内容',
  MENU_CONF: {
    uploadImage: {
      server: adminUploadUrl,
      fieldName: 'file',
      headers: getUploadHeaders(),
      maxFileSize: 10 * 1024 * 1024,
      customInsert(res, insertFn) {
        insertFn(getUploadUrl(res), '', '')
      }
    }
  }
}
const dialog = ref({
  visible: false,
  isEdit: false,
  form: { title: '', certificationId: null, summary: '' }
})

const lessonDrawer = ref({
  visible: false,
  loading: false,
  courseId: null,
  courseTitle: '',
  list: []
})

const lessonDialog = ref({
  visible: false,
  isEdit: false,
  form: emptyLessonForm()
})

const isMediaLesson = computed(() => ['VIDEO', 'AUDIO'].includes(lessonDialog.value.form.lessonType))
const isDocumentLesson = computed(() => lessonDialog.value.form.lessonType === 'DOCUMENT')
const isImageTextLesson = computed(() => lessonDialog.value.form.lessonType === 'IMAGE_TEXT')
const isExamLesson = computed(() => lessonDialog.value.form.lessonType === 'EXAM')

const examTotalScore = computed(() => {
  const rules = lessonDialog.value.form.examConfig.rules || []
  return rules.reduce((sum, r) => sum + (Number(r.count) || 0) * (Number(r.scorePer) || 0), 0)
})

function emptyLessonForm() {
  return {
    id: null,
    courseId: null,
    lessonType: 'VIDEO',
    title: '',
    content: '',
    mediaUrl: '',
    durationMinutes: 0,
    sortOrder: 0,
    examConfig: {
      bankId: null,
      durationMinutes: 30,
      passScore: 60,
      rules: [{ questionType: 'SINGLE_CHOICE', count: 1, scorePer: 10 }]
    }
  }
}

function lessonTypeText(t) {
  return { VIDEO: '视频', AUDIO: '音频', DOCUMENT: '文档', IMAGE_TEXT: '图文', EXAM: '考试' }[t] || t || '-'
}
function lessonTypeTag(t) {
  return { VIDEO: 'primary', AUDIO: 'success', DOCUMENT: 'warning', IMAGE_TEXT: 'info', EXAM: 'danger' }[t] || 'info'
}
function lessonStatusText(s) {
  return { DRAFT: '草稿', PUBLISHED: '已发布', OFFLINE: '已下线' }[s] || s
}
function lessonStatusTag(s) {
  return { DRAFT: 'info', PUBLISHED: 'success', OFFLINE: 'warning' }[s] || 'info'
}

async function fetchBanks() {
  const data = await questionBankList()
  banks.value = Array.isArray(data) ? data : (data.records || [])
}

async function fetchLessons() {
  const d = lessonDrawer.value
  d.loading = true
  try {
    const data = await lessonList(d.courseId)
    d.list = Array.isArray(data) ? data : (data.records || [])
    await nextTick()
    initLessonSortable()
  } finally {
    d.loading = false
  }
}

function initLessonSortable() {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
  const el = lessonTableRef.value?.$el?.querySelector('.el-table__body-wrapper tbody')
  if (!el || !lessonDrawer.value.list.length) return
  sortableInstance = Sortable.create(el, {
    handle: '.drag-handle',
    animation: 150,
    onEnd: async ({ oldIndex, newIndex }) => {
      if (oldIndex === newIndex) return
      const list = lessonDrawer.value.list
      const [moved] = list.splice(oldIndex, 1)
      list.splice(newIndex, 0, moved)
      const items = list.map((item, idx) => ({ id: item.id, sortOrder: idx + 1 }))
      try {
        await lessonSort({ courseId: lessonDrawer.value.courseId, items })
        ElMessage.success('排序已保存')
      } catch (e) {
        ElMessage.error(e.message || '排序保存失败')
        await fetchLessons()
      }
    }
  })
}

async function saveLessonSort() {
  const items = lessonDrawer.value.list.map((item, idx) => ({ id: item.id, sortOrder: idx + 1 }))
  try {
    await lessonSort({ courseId: lessonDrawer.value.courseId, items })
    ElMessage.success('排序已保存')
  } catch (e) {
    ElMessage.error(e.message || '排序保存失败')
  }
}

function openLessons(row) {
  lessonDrawer.value.visible = true
  lessonDrawer.value.courseId = row.id
  lessonDrawer.value.courseTitle = row.title
  lessonDrawer.value.list = []
  fetchLessons()
}

function handleLessonTypeChange() {
  if (!isExamLesson.value) {
    lessonDialog.value.form.examConfig = { bankId: null, durationMinutes: 30, passScore: 60, rules: [] }
  }
  if (!isImageTextLesson.value) {
    editorHtml.value = ''
  } else {
    editorHtml.value = lessonDialog.value.form.content || ''
  }
}

function addRule() {
  lessonDialog.value.form.examConfig.rules.push({ questionType: 'SINGLE_CHOICE', count: 1, scorePer: 10 })
}

function removeRule(idx) {
  lessonDialog.value.form.examConfig.rules.splice(idx, 1)
}

function handleLessonAdd() {
  editorHtml.value = ''
  lessonDialog.value = {
    visible: true,
    isEdit: false,
    form: { ...emptyLessonForm(), courseId: lessonDrawer.value.courseId }
  }
}

function handleLessonEdit(row) {
  const examConfig = row.examConfig ? JSON.parse(JSON.stringify(row.examConfig)) : {
    bankId: null,
    durationMinutes: 30,
    passScore: 60,
    rules: []
  }
  editorHtml.value = row.lessonType === 'IMAGE_TEXT' ? (row.content || '') : ''
  lessonDialog.value = {
    visible: true,
    isEdit: true,
    form: {
      id: row.id,
      courseId: row.courseId,
      lessonType: row.lessonType,
      title: row.title,
      content: row.content || '',
      mediaUrl: row.mediaUrl || '',
      durationMinutes: row.durationMinutes || 0,
      examConfig
    }
  }
}

function buildLessonPayload() {
  const f = lessonDialog.value.form
  return {
    id: f.id,
    courseId: f.courseId,
    lessonType: f.lessonType,
    title: f.title,
    content: isImageTextLesson.value ? editorHtml.value : (isDocumentLesson.value ? '' : f.content),
    mediaUrl: isMediaLesson.value ? f.mediaUrl : (isDocumentLesson.value ? f.mediaUrl : ''),
    durationMinutes: isMediaLesson.value ? f.durationMinutes : (isExamLesson.value ? f.examConfig.durationMinutes : 0),
    examConfig: isExamLesson.value ? f.examConfig : null
  }
}

async function confirmLessonSave() {
  const f = lessonDialog.value.form
  if (!f.title) {
    ElMessage.warning('请填写课时标题')
    return
  }
  if (isMediaLesson.value && !f.mediaUrl) {
    ElMessage.warning('请填写媒体地址')
    return
  }
  if (isDocumentLesson.value && !f.mediaUrl) {
    ElMessage.warning('请上传文档文件')
    return
  }
  if (isImageTextLesson.value && !editorHtml.value) {
    ElMessage.warning('请填写内容')
    return
  }
  if (isExamLesson.value) {
    if (!f.examConfig.bankId) {
      ElMessage.warning('请选择题库')
      return
    }
    if (!f.examConfig.rules.length) {
      ElMessage.warning('请添加题型规则')
      return
    }
  }
  try {
    const payload = buildLessonPayload()
    if (lessonDialog.value.isEdit) {
      await lessonUpdate(payload)
      ElMessage.success('课时已更新')
    } else {
      await lessonCreate(payload)
      ElMessage.success('课时已创建')
    }
    lessonDialog.value.visible = false
    await fetchLessons()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleLessonPublish(row) {
  try {
    await lessonPublish(row.id)
    ElMessage.success('课时已发布')
    await fetchLessons()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleLessonOffline(row) {
  try {
    await ElMessageBox.confirm(`确认下线课时“${row.title}”？`, '确认')
    await lessonOffline(row.id)
    ElMessage.success('课时已下线')
    await fetchLessons()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function handleLessonDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除课时“${row.title}”？删除后不可恢复`, '确认', { type: 'warning' })
    await lessonDelete(row.id)
    ElMessage.success('课时已删除')
    await fetchLessons()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

function statusText(s) {
  return { DRAFT: '草稿', PUBLISHED: '已发布', OFFLINE: '已下线' }[s] || s
}

function statusType(s) {
  return { DRAFT: 'info', PUBLISHED: 'success', OFFLINE: 'warning' }[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const [courseData, certData, bankData] = await Promise.all([
      getTrainingCourses(),
      getTrainingCertifications(),
      questionBankList()
    ])
    courses.value = Array.isArray(courseData) ? courseData : (courseData.records || [])
    certs.value = Array.isArray(certData) ? certData : (certData.records || [])
    banks.value = Array.isArray(bankData) ? bankData : (bankData.records || [])
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialog.value = {
    visible: true,
    isEdit: false,
    form: { title: '', certificationId: null, summary: '' }
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
      summary: row.summary
    }
  }
}

async function confirmSave() {
  const d = dialog.value
  if (!d.form.title || !d.form.certificationId) {
    ElMessage.warning('请填写课程标题并选择关联认证')
    return
  }
  const payload = { ...d.form }
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

function handleEditorCreated(editor) {
  wangEditorRef.value = editor
  editorHtml.value = lessonDialog.value.form.content || ''
}

function beforeMediaUpload(file) {
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件不能超过100MB')
    return false
  }
  return true
}

function handleMediaUploadSuccess(response) {
  const url = getUploadUrl(response)
  if (!url) {
    ElMessage.error('上传失败：未获取到文件地址')
    return
  }
  lessonDialog.value.form.mediaUrl = url
  ElMessage.success('上传成功，地址已回填')
}

onMounted(fetchData)

onBeforeUnmount(() => {
  if (sortableInstance) {
    sortableInstance.destroy()
    sortableInstance = null
  }
  if (wangEditorRef.value) {
    wangEditorRef.value.destroy()
    wangEditorRef.value = null
  }
})
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.lesson-drawer-head { margin-bottom: 12px; }
.rule-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.rule-label { color: #909399; font-size: 12px; }
.rule-total { margin-top: 8px; color: #409eff; font-weight: 600; }
.drag-handle { cursor: grab; color: #909399; font-size: 14px; user-select: none; }
.drag-handle:active { cursor: grabbing; }
.drag-handle-col { cursor: grab; }
</style>
