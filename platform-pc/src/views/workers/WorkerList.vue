<template>
  <el-card>
    <template #header>
      <span class="card-title">兼职管理</span>
      <el-input v-model="keyword" placeholder="搜索姓名/电话" size="small" style="float:right;width:200px;margin-right:8px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-select v-model="statusFilter" placeholder="筛选状态" size="small" style="float:right;width:120px;margin-right:8px" @change="fetchData">
        <el-option label="全部" value="" />
        <el-option label="正常" value="ACTIVE" />
        <el-option label="已封禁" value="DISABLED" />
      </el-select>
    </template>
    <el-table :data="workers" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="60" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="wechatCode" label="微信标识" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text
            @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '封禁' : '解封' }}
          </el-button>
          <el-button type="info" size="small" text @click="handleCredit(row)">信用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="editDialog.visible" title="编辑兼职信息" width="400px">
    <el-form :model="editDialog.form" label-width="80px">
      <el-form-item label="姓名">
        <el-input v-model="editDialog.form.name" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="editDialog.form.phone" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="creditDialog.visible" title="信用信息" width="440px">
    <el-form label-width="80px">
      <el-form-item label="兼职">
        <el-input :model-value="creditDialog.name" disabled />
      </el-form-item>
      <el-form-item label="当前信用分">
        <el-input :model-value="creditDialog.creditScore" disabled />
      </el-form-item>
      <el-form-item label="调整分值">
        <el-input-number v-model="creditDialog.delta" :min="-100" :max="100" style="width:100%" />
      </el-form-item>
      <el-form-item label="调整原因">
        <el-input v-model="creditDialog.reason" type="textarea" :rows="2" placeholder="例如：违规扣分 / 申诉恢复" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="creditDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmCredit">提交调整</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkers, updateWorker, banWorker, unbanWorker } from '../../api/workers'
import { adjustCredit } from '../../api/reviews'

const loading = ref(false)
const workers = ref([])
const statusFilter = ref('')
const keyword = ref('')
const editDialog = ref({ visible: false, form: {} })
const creditDialog = ref({ visible: false, id: null, name: '', creditScore: 100, delta: 0, reason: '' })

async function fetchData() {
  loading.value = true
  try {
    const data = await listWorkers({
      status: statusFilter.value || undefined,
      keyword: keyword.value || undefined
    })
    workers.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleEdit(row) {
  editDialog.value = { visible: true, form: { id: row.id, name: row.name, phone: row.phone } }
}

async function confirmEdit() {
  await updateWorker(editDialog.value.form)
  ElMessage.success('兼职信息已更新')
  editDialog.value.visible = false
  await fetchData()
}

async function handleToggleStatus(row) {
  const action = row.status === 'ACTIVE' ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确认${action}兼职 "${row.name || row.phone}"？`, '确认')
    if (row.status === 'ACTIVE') {
      await banWorker(row.id)
    } else {
      await unbanWorker(row.id)
    }
    ElMessage.success(`兼职已${action}`)
    await fetchData()
  } catch { /* cancelled */ }
}

function getCurrentUsername() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return ''
    const payload = token.split('.')[1]
    const decoded = JSON.parse(atob(payload))
    return decoded.sub || ''
  } catch { return '' }
}

function handleCredit(row) {
  creditDialog.value = { visible: true, id: row.id, name: row.name || row.phone, creditScore: row.creditScore ?? 100, delta: 0, reason: '' }
}

async function confirmCredit() {
  await adjustCredit({ targetType: 'WORKER', targetId: creditDialog.value.id, delta: creditDialog.value.delta, reason: creditDialog.value.reason, operatorName: getCurrentUsername() })
  ElMessage.success('信用分已调整')
  creditDialog.value.visible = false
  await fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
