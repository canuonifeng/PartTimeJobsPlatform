<template>
  <el-card>
    <template #header><span class="card-title">System Configuration</span></template>
    <el-table :data="configs" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="configKey" label="Key" min-width="200" />
      <el-table-column label="Value" min-width="300">
        <template #default="{ row, $index }">
          <template v-if="editingIndex === $index">
            <el-input v-model="editValue" size="small" style="width:200px" />
            <el-button type="primary" size="small" @click="confirmEdit(row, $index)">Save</el-button>
            <el-button size="small" @click="cancelEdit">Cancel</el-button>
          </template>
          <span v-else>{{ row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="80">
        <template #default="{ $index }">
          <el-button type="primary" size="small" text @click="startEdit($index)">Edit</el-button>
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
    configs.value = await getConfigs()
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
  ElMessage.success('Config updated')
  configs.value[index].configValue = editValue.value
  cancelEdit()
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
