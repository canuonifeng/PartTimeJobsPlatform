<template>
  <div>
    <el-card>
      <template #header><span class="card-title">系统配置</span></template>
      <el-table :data="configs" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="configKey" label="配置键" min-width="200" />
        <el-table-column prop="configValue" label="配置值" min-width="300">
          <template #default="{ row }">
            <span v-if="isLongText(row.configKey)" class="long-text-preview">{{ row.configValue?.substring(0, 80) }}...</span>
            <span v-else>{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="startEdit(row)">编辑</el-button>
            <el-button v-if="isLongText(row.configKey)" type="info" size="small" text @click="preview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" :title="`编辑 ${editingConfig?.configKey}`" width="720px" destroy-on-close>
      <div v-if="editingConfig">
        <el-input
          v-model="editValue"
          type="textarea"
          :autosize="{ minRows: 16, maxRows: 32 }"
          placeholder="请输入内容"
          style="width: 100%"
        />
      </div>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" :title="`预览 ${previewConfig?.configKey}`" width="720px" destroy-on-close>
      <div class="preview-body" v-html="previewHtml"></div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfig } from '../../api/configs'

const LONG_TEXT_KEYS = new Set(['user_agreement', 'privacy_policy'])

const loading = ref(false)
const configs = ref([])
const editVisible = ref(false)
const editingConfig = ref(null)
const editValue = ref('')
const previewVisible = ref(false)
const previewConfig = ref(null)

function isLongText(key) {
  return LONG_TEXT_KEYS.has(key)
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getConfigs()
    configs.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  editingConfig.value = row
  editValue.value = row.configValue || ''
  editVisible.value = true
}

async function confirmEdit() {
  try {
    await updateConfig(editingConfig.value.configKey, editValue.value)
    ElMessage.success('配置已更新')
    const item = configs.value.find(c => c.configKey === editingConfig.value.configKey)
    if (item) item.configValue = editValue.value
    editVisible.value = false
  } catch {
    ElMessage.error('保存失败')
  }
}

function preview(row) {
  previewConfig.value = row
  previewVisible.value = true
}

const previewHtml = computed(() => {
  if (!previewConfig.value?.configValue) return ''
  return markdownToHtml(previewConfig.value.configValue)
})

function markdownToHtml(md) {
  let html = md
  html = html.replace(/^### (.*$)/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gm, '<h1>$1</h1>')
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  html = html.replace(/^\|(.*)\|$/gm, (match) => {
    const cells = match.split('|').filter(c => c.trim())
    if (cells.every(c => c.trim().match(/^-+$/))) return ''
    return '<tr>' + cells.map(c => `<td>${c.trim()}</td>`).join('') + '</tr>'
  })
  html = html.replace(/(<tr>.*<\/tr>\n?)+/g, (match) => `<table>${match}</table>`)
  html = html.replace(/^- (.*$)/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, (match) => `<ul>${match}</ul>`)
  html = html.replace(/^-{3,}$/gm, '<hr/>')
  html = html.replace(/\n{2,}/g, '</p><p>')
  html = html.replace(/\n/g, '<br/>')
  html = '<p>' + html + '</p>'
  html = html.replace(/<p><\/p>/g, '')
  html = html.replace(/<p>(<h[1-3]>)/g, '$1')
  html = html.replace(/(<\/h[1-3]>)<\/p>/g, '$1')
  html = html.replace(/<p>(<ul>)/g, '$1')
  html = html.replace(/(<\/ul>)<\/p>/g, '$1')
  html = html.replace(/<p>(<table>)/g, '$1')
  html = html.replace(/(<\/table>)<\/p>/g, '$1')
  html = html.replace(/<p>(<hr\/>)<\/p>/g, '$1')
  return html
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }

.long-text-preview {
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.preview-body {
  line-height: 1.8;
  font-size: 14px;
  color: #333;
}

.preview-body :deep(h1) {
  font-size: 22px;
  font-weight: 700;
  margin: 20px 0 12px;
}

.preview-body :deep(h2) {
  font-size: 18px;
  font-weight: 600;
  margin: 16px 0 8px;
}

.preview-body :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  margin: 12px 0 6px;
}

.preview-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
  font-size: 13px;
}

.preview-body :deep(td) {
  padding: 8px 12px;
  border: 1px solid #eee;
}

.preview-body :deep(ul) {
  padding-left: 24px;
  margin: 6px 0;
}

.preview-body :deep(hr) {
  border: none;
  border-top: 1px solid #eee;
  margin: 16px 0;
}
</style>
