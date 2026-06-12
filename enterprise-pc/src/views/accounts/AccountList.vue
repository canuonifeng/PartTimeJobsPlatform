<template>
  <el-card>
    <template #header>
      <span class="card-title">账号管理</span>
      <el-button type="primary" size="small" style="float:right" @click="handleAdd">新增账号</el-button>
    </template>
    <el-table :data="accounts" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="60" />
      <el-table-column label="用户名" width="160">
        <template #default="{ row }">
          {{ row.username }}@{{ emailSuffix }}
        </template>
      </el-table-column>
      <el-table-column prop="displayName" label="显示名" width="120" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          {{ roleMap[row.role] || row.role }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button type="primary" size="small" text @click="handleResetPassword(row)">重置密码</el-button>
          <el-button v-if="row.status === 'ACTIVE'" type="danger" size="small" text @click="handleDisable(row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top:16px;justify-content:flex-end"
      @size-change="fetchData"
      @current-change="fetchData"
    />
  </el-card>

  <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑账号' : '新增账号'" width="400px">
    <el-form :model="formDialog.form" label-width="80px">
      <el-form-item label="用户名" v-if="!formDialog.isEdit">
        <el-input v-model="formDialog.form.username" />
      </el-form-item>
      <el-form-item label="密码" v-if="!formDialog.isEdit">
        <el-input v-model="formDialog.form.password" type="password" />
      </el-form-item>
      <el-form-item label="显示名">
        <el-input v-model="formDialog.form.displayName" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="formDialog.form.role" style="width:100%">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="人力资源" value="HR" />
          <el-option label="运营经理" value="MANAGER" />
          <el-option label="财务" value="FINANCE" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSave">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="resetPwdDialog.visible" title="重置密码" width="360px">
    <el-form :model="resetPwdDialog" label-width="80px">
      <el-form-item label="新密码">
        <el-input v-model="resetPwdDialog.newPassword" type="password" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="resetPwdDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmResetPassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAccounts, createAccount, updateAccount, resetPassword } from '../../api/account'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const emailSuffix = authStore.emailSuffix
const roleMap = { ADMIN: '管理员', HR: '人力资源', MANAGER: '运营经理', FINANCE: '财务' }

const loading = ref(false)
const accounts = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const formDialog = ref({ visible: false, isEdit: false, form: {} })
const resetPwdDialog = ref({ visible: false, id: null, newPassword: '' })

async function fetchData() {
  loading.value = true
  try {
    const data = await listAccounts({ page: page.value, pageSize: pageSize.value })
    accounts.value = Array.isArray(data) ? data : (data.records || [])
    total.value = Array.isArray(data) ? data.length : (data.total || 0)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  formDialog.value = {
    visible: true, isEdit: false,
    form: { username: '', password: '', displayName: '', role: 'ADMIN' }
  }
}

function handleEdit(row) {
  formDialog.value = {
    visible: true, isEdit: true,
    form: { id: row.id, displayName: row.displayName, role: row.role }
  }
}

async function confirmSave() {
  const d = formDialog.value
  if (d.isEdit) {
    await updateAccount(d.form)
    ElMessage.success('账号已更新')
  } else {
    await createAccount(d.form)
    ElMessage.success('账号已创建')
  }
  d.visible = false
  await fetchData()
}

function handleResetPassword(row) {
  resetPwdDialog.value = { visible: true, id: row.id, newPassword: '' }
}

async function confirmResetPassword() {
  await resetPassword({ id: resetPwdDialog.value.id, newPassword: resetPwdDialog.value.newPassword })
  ElMessage.success('密码已重置')
  resetPwdDialog.value.visible = false
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm(`确认禁用账号 "${row.username}"？`, '确认')
    await updateAccount({ id: row.id, status: 'DISABLED' })
    ElMessage.success('账号已禁用')
    await fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
