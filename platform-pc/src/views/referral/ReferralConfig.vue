<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfig, updateConfig } from '../../api/referral'

const configs = ref([])
const loading = ref(false)

async function fetchConfig() {
  loading.value = true
  try {
    configs.value = await getConfig()
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  try {
    await ElMessageBox.confirm('确定保存配置？', '提示')
    await updateConfig(configs.value)
    ElMessage.success('配置已保存')
  } catch {}
}

onMounted(() => {
  fetchConfig()
})
</script>

<template>
  <div class="config-page" v-loading="loading">
    <el-card>
      <template #header>
        <span>奖励规则配置</span>
      </template>

      <el-form label-width="180px">
        <el-form-item v-for="item in configs" :key="item.configKey" :label="item.description">
          <el-input v-if="item.configKey === 'need_audit'" v-model="item.configValue" placeholder="true/false" />
          <el-input v-else-if="item.configKey === 'release_method'" v-model="item.configValue" placeholder="auto/manual" />
          <el-input-number v-else v-model="item.configValue" :min="0" style="width: 200px" />
        </el-form-item>
      </el-form>

      <el-button type="primary" @click="handleSave">保存配置</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.config-page { padding: 20px; }
</style>
