<template>
  <el-card>
    <template #header>
      <span class="card-title">企业实名认证</span>
    </template>

    <el-alert v-if="status === 'PENDING'" type="warning" :closable="false" show-icon
      title="您的实名认证正在审核中，请耐心等待" />
    <el-alert v-else-if="status === 'APPROVED'" type="success" :closable="false" show-icon
      title="实名认证已通过" />
    <el-alert v-else-if="status === 'REJECTED'" type="error" :closable="false" show-icon
      :title="`实名认证被拒绝：${data?.rejectReason || ''}`" />
    <el-alert v-else type="info" :closable="false" show-icon title="尚未提交实名认证" />

    <el-form v-if="canSubmit" :model="form" label-width="180px" style="margin-top: 24px; max-width: 700px">
      <el-form-item label="法人姓名">
        <el-input v-model="form.legalPersonName" placeholder="请输入法人姓名" />
      </el-form-item>
      <el-form-item label="法人身份证号">
        <el-input v-model="form.legalPersonIdCard" placeholder="请输入身份证号" maxlength="18" />
      </el-form-item>
      <el-form-item label="统一社会信用代码">
        <el-input v-model="form.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" />
      </el-form-item>
      <el-form-item label="营业执照URL">
        <el-input v-model="form.businessLicenseUrl" placeholder="请粘贴营业执照图片 URL" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ status === 'REJECTED' ? '重新提交' : '提交认证' }}
        </el-button>
      </el-form-item>
    </el-form>

    <div v-else-if="status === 'APPROVED'" class="info-block">
      <p>法人姓名：{{ data?.legalPersonName }}</p>
      <p>身份证号：{{ data?.legalPersonIdCardMasked }}</p>
      <p>统一社会信用代码：{{ data?.unifiedSocialCreditCode }}</p>
      <p>营业执照：
        <el-image v-if="data?.businessLicenseUrl" :src="data.businessLicenseUrl" :preview-src-list="[data.businessLicenseUrl]" style="width:240px;max-height:180px" fit="contain" />
      </p>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRealName, submitRealName } from '../../api/realName'

const status = ref('NONE')
const data = ref(null)
const submitting = ref(false)

const form = reactive({
  legalPersonName: '',
  legalPersonIdCard: '',
  unifiedSocialCreditCode: '',
  businessLicenseUrl: ''
})

const canSubmit = computed(() => status.value === 'NONE' || status.value === 'REJECTED')

async function loadStatus() {
  try {
    const res = await getRealName()
    data.value = res
    status.value = res?.status || 'NONE'
  } catch {
    status.value = 'NONE'
  }
}

async function handleSubmit() {
  if (!form.legalPersonName.trim() || !form.legalPersonIdCard.trim()
      || !form.unifiedSocialCreditCode.trim() || !form.businessLicenseUrl.trim()) {
    ElMessage.warning('请完整填写表单')
    return
  }
  submitting.value = true
  try {
    await submitRealName({ ...form })
    ElMessage.success('已提交')
    await loadStatus()
  } catch (e) {
    ElMessage.error(e?.response?.data?.error || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadStatus)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
.info-block { padding: 12px 0; line-height: 1.8; }
</style>
