<template>
  <el-card>
    <template #header>
      <span class="card-title">企业管理</span>
      <el-button type="primary" size="small" style="float:right;margin-right:12px" @click="handleAdd">新增企业</el-button>
      <el-select v-model="statusFilter" placeholder="筛选状态" size="small" style="float:right;width:140px;margin-right:8px" @change="fetchData">
        <el-option label="全部" value="" />
        <el-option label="已启用" value="ACTIVE" />
        <el-option label="已停用" value="SUSPENDED" />
      </el-select>
    </template>
    <el-table :data="enterprises" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="60" />
      <el-table-column label="Logo" width="80">
        <template #default="{ row }">
          <el-avatar :src="row.companyLogo" :size="36">
            {{ row.companyName?.slice(0, 1) }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="companyName" label="企业名称" min-width="160" />
      <el-table-column prop="emailSuffix" label="账号后缀" width="120">
        <template #default="{ row }">
          {{ row.emailSuffix || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="contactName" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '已启用' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text
            @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
          </el-button>
          <el-button type="primary" size="small" text @click="handleAccounts(row)">账号管理</el-button>
          <el-button type="success" size="small" text @click="handleTopUp(row)">充值</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 新增企业对话框 -->
  <el-dialog v-model="createDialog.visible" title="新增企业" width="500px">
    <el-form :model="createDialog.form" label-width="100px">
      <el-form-item label="企业名称">
        <el-input v-model="createDialog.form.companyName" />
      </el-form-item>
      <el-form-item label="账号后缀">
        <el-input v-model="createDialog.form.emailSuffix" placeholder="如 acme" />
        <div style="color:#909399;font-size:12px">企业登录账号格式：前缀@后缀</div>
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="createDialog.form.contactName" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="createDialog.form.contactPhone" />
      </el-form-item>
      <el-form-item label="企业地址">
        <el-input v-model="createDialog.form.companyAddress" type="textarea" />
      </el-form-item>
      <el-form-item label="企业logo">
        <el-input v-model="createDialog.form.companyLogo" placeholder="logo图片URL" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmCreate">确定</el-button>
    </template>
  </el-dialog>

  <!-- 编辑企业对话框 -->
  <el-dialog v-model="editDialog.visible" title="编辑企业" width="500px">
    <el-form :model="editDialog.form" label-width="100px">
      <el-form-item label="企业名称">
        <el-input v-model="editDialog.form.companyName" />
      </el-form-item>
      <el-form-item label="账号后缀">
        <el-input v-model="editDialog.form.emailSuffix" placeholder="如 acme" />
        <div style="color:#909399;font-size:12px">企业登录账号格式：前缀@后缀</div>
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="editDialog.form.contactName" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="editDialog.form.contactPhone" />
      </el-form-item>
      <el-form-item label="企业地址">
        <el-input v-model="editDialog.form.companyAddress" type="textarea" />
      </el-form-item>
      <el-form-item label="企业logo">
        <el-input v-model="editDialog.form.companyLogo" placeholder="logo图片URL" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmEdit">保存</el-button>
    </template>
  </el-dialog>

  <!-- 账号管理对话框 -->
  <el-dialog v-model="accountDialog.visible" :title="`账号管理 - ${accountDialog.companyName}`" width="700px">
    <el-button type="primary" size="small" style="margin-bottom:12px" @click="handleAddAccount">添加账号</el-button>
    <el-table :data="accountDialog.accounts" stripe style="width:100%">
      <el-table-column label="用户名" width="160">
        <template #default="{ row }">
          {{ row.username }}@{{ accountDialog.emailSuffix }}
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
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEditAccount(row)">编辑</el-button>
          <el-button type="primary" size="small" text @click="handleResetPassword(row)">重置密码</el-button>
          <el-button type="danger" size="small" text @click="handleDeleteAccount(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 添加/编辑账号对话框 -->
  <el-dialog v-model="accountFormDialog.visible" :title="accountFormDialog.isEdit ? '编辑账号' : '添加账号'" width="400px">
    <el-form :model="accountFormDialog.form" label-width="80px">
      <el-form-item label="用户名" v-if="!accountFormDialog.isEdit">
        <el-input v-model="accountFormDialog.form.username" />
      </el-form-item>
      <el-form-item label="密码" v-if="!accountFormDialog.isEdit">
        <el-input v-model="accountFormDialog.form.password" type="password" />
      </el-form-item>
      <el-form-item label="显示名">
        <el-input v-model="accountFormDialog.form.displayName" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="accountFormDialog.form.role" style="width:100%">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="人力资源" value="HR" />
          <el-option label="运营经理" value="MANAGER" />
          <el-option label="财务" value="FINANCE" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="accountFormDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSaveAccount">保存</el-button>
    </template>
  </el-dialog>

  <!-- 重置密码对话框 -->
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

  <!-- 充值对话框 -->
  <el-dialog v-model="topUpDialog.visible" title="企业充值" width="400px">
    <el-form label-width="80px">
      <el-form-item label="企业名称">
        <el-input :model-value="topUpDialog.companyName" disabled />
      </el-form-item>
      <el-form-item label="充值金额">
        <el-input-number v-model="topUpDialog.amount" :min="0.01" :precision="2" style="width:100%" />
      </el-form-item>
      <el-form-item label="操作人">
        <el-input :model-value="topUpDialog.operatorName" disabled />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="topUpDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmTopUp">确认充值</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listEnterprises, createEnterprise, updateEnterprise, suspendEnterprise, activateEnterprise,
  listAccounts, createAccount, updateAccount, resetPassword, deleteAccount, adjustEnterpriseBalance
} from '../../api/enterprises'

const roleMap = { ADMIN: '管理员', HR: '人力资源', MANAGER: '运营经理', FINANCE: '财务' }

const topUpDialog = ref({ visible: false, companyId: null, companyName: '', amount: 0, operatorName: '' })

function getCurrentUsername() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return ''
    const payload = token.split('.')[1]
    const decoded = JSON.parse(atob(payload))
    return decoded.sub || ''
  } catch { return '' }
}

function handleTopUp(row) {
  topUpDialog.value = {
    visible: true,
    companyId: row.id,
    companyName: row.companyName,
    amount: 0,
    operatorName: getCurrentUsername()
  }
}

async function confirmTopUp() {
  if (!topUpDialog.value.amount || topUpDialog.value.amount <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  try {
    await adjustEnterpriseBalance({
      companyId: topUpDialog.value.companyId,
      amount: topUpDialog.value.amount,
      description: ''
    })
    ElMessage.success(`已为企业 ${topUpDialog.value.companyName} 充值 ${topUpDialog.value.amount} 元`)
    topUpDialog.value.visible = false
    await fetchData()
  } catch {}
}

const loading = ref(false)
const enterprises = ref([])
const statusFilter = ref('')

const createDialog = ref({ visible: false, form: { companyName: '', emailSuffix: '', companyLogo: '', contactName: '', contactPhone: '', companyAddress: '' } })
const editDialog = ref({ visible: false, form: {} })
const accountDialog = ref({ visible: false, companyName: '', enterpriseId: null, accounts: [] })
const accountFormDialog = ref({ visible: false, isEdit: false, form: { username: '', password: '', displayName: '', role: 'ADMIN' } })
const resetPwdDialog = ref({ visible: false, id: null, newPassword: '' })

async function fetchData() {
  loading.value = true
  try {
    const data = await listEnterprises({ status: statusFilter.value || undefined })
    enterprises.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  createDialog.value = { visible: true, form: { companyName: '', emailSuffix: '', companyLogo: '', contactName: '', contactPhone: '', companyAddress: '' } }
}

async function confirmCreate() {
  await createEnterprise(createDialog.value.form)
  ElMessage.success('企业已创建')
  createDialog.value.visible = false
  await fetchData()
}

function handleEdit(row) {
  editDialog.value = { visible: true, form: { ...row } }
}

async function confirmEdit() {
  await updateEnterprise(editDialog.value.form)
  ElMessage.success('企业信息已更新')
  editDialog.value.visible = false
  await fetchData()
}

async function handleToggleStatus(row) {
  const action = row.status === 'ACTIVE' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}企业 "${row.companyName}"？`, '确认')
    if (row.status === 'ACTIVE') {
      await suspendEnterprise(row.id)
    } else {
      await activateEnterprise(row.id)
    }
    ElMessage.success(`企业已${action}`)
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleAccounts(row) {
  accountDialog.value = { visible: true, companyName: row.companyName, enterpriseId: row.id, emailSuffix: row.emailSuffix || '', accounts: [] }
  const data = await listAccounts(row.id)
  accountDialog.value.accounts = Array.isArray(data) ? data : []
}

function handleAddAccount() {
  accountFormDialog.value = {
    visible: true, isEdit: false,
    form: { username: '', password: '', displayName: '', role: 'ADMIN' }
  }
}

function handleEditAccount(row) {
  accountFormDialog.value = {
    visible: true, isEdit: true,
    form: { id: row.id, displayName: row.displayName, role: row.role }
  }
}

async function confirmSaveAccount() {
  const d = accountFormDialog.value
  if (d.isEdit) {
    await updateAccount(d.form)
    ElMessage.success('账号已更新')
  } else {
    await createAccount({ ...d.form, enterpriseId: accountDialog.value.enterpriseId })
    ElMessage.success('账号已创建')
  }
  d.visible = false
  const data = await listAccounts(accountDialog.value.enterpriseId)
  accountDialog.value.accounts = Array.isArray(data) ? data : []
}

function handleResetPassword(row) {
  resetPwdDialog.value = { visible: true, id: row.id, newPassword: '' }
}

async function confirmResetPassword() {
  await resetPassword(resetPwdDialog.value.id, resetPwdDialog.value.newPassword)
  ElMessage.success('密码已重置')
  resetPwdDialog.value.visible = false
}

async function handleDeleteAccount(row) {
  try {
    await ElMessageBox.confirm(`确认删除账号 "${row.username}"？`, '确认')
    await deleteAccount(row.id)
    ElMessage.success('账号已删除')
    const data = await listAccounts(accountDialog.value.enterpriseId)
    accountDialog.value.accounts = Array.isArray(data) ? data : []
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
