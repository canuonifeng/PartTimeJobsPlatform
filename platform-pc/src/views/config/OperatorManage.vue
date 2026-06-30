<template>
  <el-card>
    <template #header>
      <span>账号权限</span>
    </template>

    <el-button type="primary" size="small" style="margin-bottom:15px" @click="handleCreateOperator">新增账号</el-button>
    <el-table :data="operators" stripe style="width:100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" width="150" />
      <el-table-column prop="name" label="姓名" width="120" />
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
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" size="small" text @click="handleResetPassword(row)">重置密码</el-button>
          <el-button type="info" size="small" text @click="handleToggleStatus(row)">{{ row.status === 'ACTIVE' ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listOperators, resetOperatorPassword, toggleOperatorStatus } from '../../api/operators'

const operators = ref([])

async function fetchData() {
  try {
    const res = await listOperators({})
    operators.value = res || []
  } catch {}
}

async function handleResetPassword(row) {
  try {
    await resetOperatorPassword(row.id)
    ElMessage.success('密码已重置为默认密码')
  } catch {}
}

async function handleToggleStatus(row) {
  try {
    await toggleOperatorStatus(row.id)
    ElMessage.success('状态已更新')
    await fetchData()
  } catch {}
}

function handleCreateOperator() {
  ElMessage.info('新增账号功能开发中')
}

function handleEdit(row) {
  ElMessage.info('编辑账号功能开发中')
}

onMounted(() => {
  fetchData()
})
</script>
