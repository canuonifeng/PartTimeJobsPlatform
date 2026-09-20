<template>
  <el-card>
    <template #header>
      <span>账号权限</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="运营账号" name="operators">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="openCreate">新增账号</el-button>
        <el-table :data="operators" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="账号" width="140" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="roleName" label="角色" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginAt" label="最后登录" width="180" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openEdit(row)">编辑</el-button>
              <el-button type="warning" size="small" text @click="handleResetPassword(row)">重置密码</el-button>
              <el-button type="info" size="small" text @click="handleToggleStatus(row)">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="角色权限矩阵" name="roles">
        <el-table :data="roles" stripe style="width:100%">
          <el-table-column prop="roleCode" label="角色编码" width="180" />
          <el-table-column prop="roleName" label="角色名称" width="120" />
          <el-table-column prop="description" label="角色描述" min-width="200" />
          <el-table-column label="权限点" min-width="400">
            <template #default="{ row }">
              <el-tag v-for="p in row.permissions" :key="p" size="small" style="margin-right:4px;margin-bottom:4px">{{ p }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑账号' : '新增账号'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名" v-if="!form.id">
          <el-input v-model="form.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="初始密码" v-if="!form.id">
          <el-input v-model="form.password" placeholder="不填默认123456" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option v-for="r in roles" :key="r.roleCode" :label="r.roleName" :value="r.roleCode" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listOperators, createOperator, updateOperator, resetOperatorPassword, toggleOperatorStatus, getRoles } from '../../api/operators'

const activeTab = ref('operators')
const operators = ref([])
const roles = ref([])
const dialogVisible = ref(false)
const form = ref({})

async function fetchOperators() {
  try { operators.value = (await listOperators({})) || [] } catch {}
}

async function fetchRoles() {
  try { roles.value = (await getRoles()) || [] } catch {}
}

function openCreate() {
  form.value = {}
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { id: row.id, realName: row.realName, phone: row.phone, email: row.email, role: row.role }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (form.value.id) {
      await updateOperator(form.value)
    } else {
      await createOperator(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await fetchOperators()
  } catch {}
}

async function handleResetPassword(row) {
  try {
    await resetOperatorPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch {}
}

async function handleToggleStatus(row) {
  try {
    await toggleOperatorStatus(row.id)
    ElMessage.success('状态已更新')
    await fetchOperators()
  } catch {}
}

onMounted(() => {
  fetchOperators()
  fetchRoles()
})
</script>
