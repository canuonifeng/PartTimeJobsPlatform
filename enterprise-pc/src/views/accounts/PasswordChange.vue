<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateCurrentPassword } from '../../api/account'

const loading = ref(false)
const form = reactive({ newPassword: '', confirmPassword: '' })

async function handleSave() {
  if (!form.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (form.newPassword.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await updateCurrentPassword({ newPassword: form.newPassword })
    form.newPassword = ''
    form.confirmPassword = ''
    ElMessage.success('密码已修改')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="password-page">
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <div>
            <div class="title">修改密码</div>
            <div class="desc">单独修改当前登录账号密码</div>
          </div>
        </div>
      </template>
      <el-form label-width="100px" class="password-form">
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSave">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.password-page { padding: 20px; }
.password-card { max-width: 680px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 18px; font-weight: 700; color: #303133; }
.desc { margin-top: 6px; color: #909399; font-size: 13px; }
.password-form { max-width: 480px; }
</style>
