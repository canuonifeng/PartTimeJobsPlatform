<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listAccounts, createAccount, updateAccount, resetPassword } from '@/api/account'
import { useAuthStore } from '@/store'

const authStore = useAuthStore()
const emailSuffix = authStore.emailSuffix
const accounts = ref([])
const loading = ref(false)
const formVisible = ref(false)
const resetVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, username: '', password: '', displayName: '', role: 'ADMIN' })
const resetForm = ref({ id: null, newPassword: '' })
const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '人力资源', value: 'HR' },
  { label: '运营经理', value: 'MANAGER' },
  { label: '财务', value: 'FINANCE' }
]

onShow(loadAccounts)
onPullDownRefresh(() => loadAccounts().finally(() => uni.stopPullDownRefresh()))

async function loadAccounts() {
  loading.value = true
  try {
    const res = await listAccounts()
    accounts.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function roleLabel(role) {
  return roleOptions.find(r => r.value === role)?.label || role
}

function openAdd() {
  isEdit.value = false
  form.value = { id: null, username: '', password: '', displayName: '', role: 'ADMIN' }
  formVisible.value = true
}

function openEdit(account) {
  isEdit.value = true
  form.value = { id: account.id, username: account.username, password: '', displayName: account.displayName || '', role: account.role || 'ADMIN' }
  formVisible.value = true
}

function onRoleChange(e) {
  form.value.role = roleOptions[e.detail.value]?.value || 'ADMIN'
}

async function saveAccount() {
  if (!form.value.displayName) {
    uni.showToast({ title: '请输入显示名', icon: 'none' })
    return
  }
  if (!isEdit.value && (!form.value.username || !form.value.password)) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  try {
    if (isEdit.value) await updateAccount({ id: form.value.id, displayName: form.value.displayName, role: form.value.role })
    else await createAccount(form.value)
    formVisible.value = false
    uni.showToast({ title: '保存成功', icon: 'success' })
    loadAccounts()
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

function openReset(account) {
  resetForm.value = { id: account.id, newPassword: '' }
  resetVisible.value = true
}

async function confirmReset() {
  if (!resetForm.value.newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  try {
    await resetPassword(resetForm.value)
    resetVisible.value = false
    uni.showToast({ title: '重置成功', icon: 'success' })
  } catch {
    uni.showToast({ title: '重置失败', icon: 'none' })
  }
}

function handleDisable(account) {
  uni.showModal({
    title: '确认禁用',
    content: `确定禁用账号 ${account.username}@${emailSuffix} 吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await updateAccount({ id: account.id, status: 'DISABLED' })
        uni.showToast({ title: '账号已禁用', icon: 'success' })
        loadAccounts()
      } catch {
        uni.showToast({ title: '禁用失败', icon: 'none' })
      }
    }
  })
}
</script>

<template>
  <view class="page e-page">
    <view class="header e-header">
      <view class="e-header-row">
        <view class="header-copy">
          <text class="e-header-title">账号管理</text>
          <text class="e-header-desc">管理企业成员账号与角色权限</text>
        </view>
        <view class="header-add e-action-primary" @click="openAdd">+ 新增</view>
      </view>
    </view>

    <view class="content e-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>
      <view v-else-if="accounts.length === 0" class="e-empty">
        <text class="e-empty-title">暂无账号</text>
        <text class="e-empty-desc">点击右上角新增企业成员</text>
      </view>
      <view v-else>
        <view v-for="a in accounts" :key="a.id" class="e-card">
          <view class="e-card-title-row">
            <text class="e-card-title">{{ a.username }}@{{ emailSuffix }}</text>
            <view class="e-badge" :class="a.status === 'ACTIVE' ? 'e-badge-green' : 'e-badge-gray'">{{ a.status === 'ACTIVE' ? '正常' : '禁用' }}</view>
          </view>

          <view class="e-info-grid">
            <view class="e-info-pill">
              <text class="e-info-label">显示名</text>
              <text class="e-info-value">{{ a.displayName || '-' }}</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">角色</text>
              <text class="e-info-value">{{ roleLabel(a.role) }}</text>
            </view>
            <view class="e-info-pill info-wide">
              <text class="e-info-label">创建时间</text>
              <text class="e-info-value">{{ a.createdAt || '-' }}</text>
            </view>
          </view>

          <view class="e-action-row">
            <view class="e-action-pill e-action-blue" @click="openEdit(a)">编辑</view>
            <view class="e-action-pill e-action-blue" @click="openReset(a)">重置密码</view>
            <view v-if="a.status === 'ACTIVE'" class="e-action-pill e-action-red" @click="handleDisable(a)">禁用</view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="formVisible" class="mask" @click.self="formVisible = false">
      <view class="dialog">
        <text class="dialog-title">{{ isEdit ? '编辑账号' : '新增账号' }}</text>
        <view v-if="!isEdit" class="dialog-field">
          <text class="dialog-label">用户名</text>
          <input v-model="form.username" class="dialog-input" placeholder="请输入用户名" />
        </view>
        <view v-if="!isEdit" class="dialog-field">
          <text class="dialog-label">密码</text>
          <input v-model="form.password" class="dialog-input" placeholder="请输入密码" password />
        </view>
        <view class="dialog-field">
          <text class="dialog-label">显示名</text>
          <input v-model="form.displayName" class="dialog-input" placeholder="请输入显示名" />
        </view>
        <view class="dialog-field">
          <text class="dialog-label">角色</text>
          <picker mode="selector" :range="roleOptions" range-key="label" @change="onRoleChange">
            <view class="dialog-picker">{{ roleLabel(form.role) }}</view>
          </picker>
        </view>
        <view class="dialog-actions">
          <view class="dialog-btn cancel" @click="formVisible = false">取消</view>
          <view class="dialog-btn primary" @click="saveAccount">保存</view>
        </view>
      </view>
    </view>

    <view v-if="resetVisible" class="mask" @click.self="resetVisible = false">
      <view class="dialog">
        <text class="dialog-title">重置密码</text>
        <view class="dialog-field">
          <text class="dialog-label">新密码</text>
          <input v-model="resetForm.newPassword" class="dialog-input" placeholder="请输入新密码" password />
        </view>
        <view class="dialog-actions">
          <view class="dialog-btn cancel" @click="resetVisible = false">取消</view>
          <view class="dialog-btn primary" @click="confirmReset">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.header-copy {
  flex: 1;
  min-width: 0;
}

.header-add {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.2;
}

.info-wide {
  width: calc(100% - 14rpx);
}

.mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  z-index: 999;
  box-sizing: border-box;
}

.dialog {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 34rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
  box-sizing: border-box;
}

.dialog-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
  color: #1f2933;
}

.dialog-field {
  margin-bottom: 20rpx;
}

.dialog-label {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 8rpx;
}

.dialog-input {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  padding: 0 20rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  font-size: 28rpx;
  box-sizing: border-box;
}

.dialog-picker {
  height: 78rpx;
  line-height: 78rpx;
  padding: 0 20rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  font-size: 28rpx;
  color: #1f2933;
  box-sizing: border-box;
}

.dialog-actions {
  display: flex;
  margin-top: 24rpx;
}

.dialog-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 28rpx;
  border-radius: 44rpx;
}

.dialog-btn + .dialog-btn {
  margin-left: 16rpx;
}

.cancel {
  background: #f5f7f6;
  color: #64748b;
}

.primary {
  background: linear-gradient(135deg, #18c86b, #08a95a);
  color: #fff;
  font-weight: 700;
}
</style>
