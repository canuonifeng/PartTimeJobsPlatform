<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnterpriseInfo, updateCompanyLogo } from '../../api/enterprise'

const loading = ref(false)
const info = ref({ companyName: '', companyLogo: '' })
const logoUrl = ref('')

async function loadInfo() {
  try {
    const res = await getEnterpriseInfo()
    info.value = res
    logoUrl.value = res.companyLogo || ''
  } catch {
    ElMessage.error('加载企业信息失败')
  }
}

function handleUploadSuccess(response) {
  if (response?.url) {
    logoUrl.value = response.url
  }
}

function beforeUpload(file) {
  const isImg = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImg) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt2M) { ElMessage.error('图片大小不能超过 2MB'); return false }
  return true
}

async function handleSave() {
  loading.value = true
  try {
    await updateCompanyLogo(logoUrl.value)
    ElMessage.success('保存成功')
    info.value.companyLogo = logoUrl.value
  } catch {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadInfo)
</script>

<template>
  <div class="settings-page">
    <el-card>
      <template #header><span>企业设置</span></template>
      <el-form label-width="120px" style="max-width: 600px">
        <el-form-item label="企业名称">
          <el-input :model-value="info.companyName" disabled />
        </el-form-item>
        <el-form-item label="企业Logo">
          <div style="display:flex;gap:12px;align-items:center">
            <el-upload
              action="/api/files/upload"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
            >
              <el-button type="primary">上传Logo</el-button>
            </el-upload>
            <el-input v-model="logoUrl" placeholder="或输入图片URL" style="width:300px" clearable />
            <el-image v-if="logoUrl" :src="logoUrl" style="width:60px;height:60px;border-radius:4px" fit="cover" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.settings-page {
  padding: 20px;
}
</style>
