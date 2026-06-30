<template>
  <el-card>
    <template #header>
      <span>风控中心</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="黑名单" name="blacklist">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="handleAddToBlacklist">加入黑名单</el-button>
        <el-table :data="blacklist" v-loading="loading" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userType" label="用户类型" width="100">
            <template #default="{ row }">{{ row.userType === 'WORKER' ? '工人' : '企业' }}</template>
          </el-table-column>
          <el-table-column prop="userName" label="用户名称" width="150" />
          <el-table-column prop="userPhone" label="联系方式" width="130" />
          <el-table-column prop="reason" label="拉黑原因" min-width="200" />
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="createdAt" label="拉黑时间" width="180" />
          <el-table-column prop="expireAt" label="到期时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" size="small" text @click="handleRemoveFromBlacklist(row)">移出黑名单</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="白名单" name="whitelist">
        <el-button type="primary" size="small" style="margin-bottom:15px">加入白名单</el-button>
        <el-table :data="whitelist" v-loading="loading" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userType" label="用户类型" width="100">
            <template #default="{ row }">{{ row.userType === 'WORKER' ? '工人' : '企业' }}</template>
          </el-table-column>
          <el-table-column prop="userName" label="用户名称" width="150" />
          <el-table-column prop="reason" label="加入原因" min-width="200" />
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="createdAt" label="加入时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" size="small" text>移出白名单</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="风控规则" name="rules">
        <el-table :data="rules" v-loading="loading" stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="ruleName" label="规则名称" width="200" />
          <el-table-column prop="triggerCondition" label="触发条件" min-width="250" />
          <el-table-column prop="action" label="执行动作" min-width="200" />
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="handleToggleRule(row)" active-text="启用" inactive-text="禁用" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text>编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBlacklist, getWhitelist, getRiskRules, removeFromBlacklist, toggleRiskRule } from '../../api/risk'

const loading = ref(false)
const activeTab = ref('blacklist')
const blacklist = ref([])
const whitelist = ref([])
const rules = ref([])

async function fetchBlacklist() {
  try {
    const res = await getBlacklist({})
    blacklist.value = res || []
  } catch {}
}

async function fetchWhitelist() {
  try {
    const res = await getWhitelist({})
    whitelist.value = res || []
  } catch {}
}

async function fetchRules() {
  try {
    const res = await getRiskRules()
    rules.value = res || []
  } catch {}
}

async function handleRemoveFromBlacklist(row) {
  try {
    await ElMessageBox.confirm(`确定将 ${row.userName} 移出黑名单？`, '提示', { type: 'warning' })
    await removeFromBlacklist(row.id)
    ElMessage.success('已移出黑名单')
    await fetchBlacklist()
  } catch {}
}

async function handleToggleRule(row) {
  try {
    await toggleRiskRule(row.id, row.enabled)
    ElMessage.success(row.enabled ? '已启用规则' : '已禁用规则')
  } catch {
    row.enabled = !row.enabled
  }
}

function handleAddToBlacklist() {
  ElMessage.info('加入黑名单弹窗功能开发中')
}

onMounted(() => {
  fetchBlacklist()
  fetchWhitelist()
  fetchRules()
})
</script>
