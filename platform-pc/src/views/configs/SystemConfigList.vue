<template>
  <el-card>
    <template #header><span class="card-title">系统配置</span></template>
    <el-table :data="configs" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="configKey" label="配置键" min-width="200" />
      <el-table-column label="配置值" min-width="300">
        <template #default="{ row, $index }">
          <template v-if="editingIndex === $index">
            <el-input v-model="editValue" size="small" style="width:200px" />
            <el-button type="primary" size="small" @click="confirmEdit(row, $index)">保存</el-button>
            <el-button size="small" @click="cancelEdit">取消</el-button>
          </template>
          <span v-else>{{ row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ $index }">
          <el-button type="primary" size="small" text @click="startEdit($index)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfig } from '../../api/configs'

const loading = ref(false)
const configs = ref([])
const editingIndex = ref(-1)
const editValue = ref('')

async function fetchData() {
  loading.value = true
  try {
    const data = await getConfigs()
    configs.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function startEdit(index) {
  editingIndex.value = index
  editValue.value = configs.value[index].configValue
}

function cancelEdit() {
  editingIndex.value = -1
  editValue.value = ''
}

async function confirmEdit(row, index) {
  await updateConfig(row.configKey, editValue.value)
  ElMessage.success('配置已更新')
  configs.value[index].configValue = editValue.value
  cancelEdit()
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
