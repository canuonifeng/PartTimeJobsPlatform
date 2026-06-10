<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listAccounts, createAccount, updateAccount, resetPassword, deleteAccount } from '@/api/account'
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

function handleDelete(account) {
  uni.showModal({
    title: '确认删除',
    content: `确定删除账号 ${a.username}@${emailSuffix} 吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteAccount(account.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        loadAccounts()
      } catch {
        uni.showToast({ title: '删除失败', icon: 'none' })
      }
    }
  })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">账号管理</text>
      <view class="add-btn" @click="openAdd">
        <text class="add-text">+ 新增</text>
      </view>
    </view>
    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="accounts.length === 0" class="state-msg">暂无账号</view>
      <view v-else class="list">
        <view v-for="a in accounts" :key="a.id" class="card">
          <view class="card-top">
            <text class="card-name">{{ a.username }}@{{ emailSuffix }}</text>
            <view class="badge" :class="a.status === 'ACTIVE' ? 'badge-on' : 'badge-off'">
              <text class="badge-text">{{ a.status === 'ACTIVE' ? '正常' : '禁用' }}</text>
            </view>
          </view>
          <text class="info">显示名：{{ a.displayName || '-' }}</text>
          <text class="info">角色：{{ roleLabel(a.role) }}</text>
          <text class="info">创建时间：{{ a.createdAt || '-' }}</text>
          <view class="card-actions">
            <view class="action-btn edit" @click="openEdit(a)">
              <text class="action-text">编辑</text>
            </view>
            <view class="action-btn reset" @click="openReset(a)">
              <text class="action-text">重置密码</text>
            </view>
            <view class="action-btn delete" @click="handleDelete(a)">
              <text class="action-text">删除</text>
            </view>
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
          <view class="dialog-btn cancel" @click="formVisible = false">
            <text>取消</text>
          </view>
          <view class="dialog-btn primary" @click="saveAccount">
            <text>保存</text>
          </view>
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
          <view class="dialog-btn cancel" @click="resetVisible = false">
            <text>取消</text>
          </view>
          <view class="dialog-btn primary" @click="confirmReset">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page {
  min-height: 100vh;
  background: #f6f8f7;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%);
  padding: 48rpx 32rpx 28rpx;
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}
.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}
.add-btn {
  background: linear-gradient(135deg, #18c86b, #08a95a);
  border-radius: 999rpx;
  padding: 12rpx 28rpx;
}
.add-text {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
}
.content {
  padding: 24rpx 32rpx;
}
.state-msg {
  text-align: center;
  padding: 80rpx 0;
  color: #999;
  font-size: 28rpx;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.card-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #1f2933;
}
.badge {
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}
.badge-text {
  font-size: 22rpx;
}
.badge-on {
  background: #eafaf1;
}
.badge-on .badge-text {
  color: #07c160;
}
.badge-off {
  background: #f0f0f0;
}
.badge-off .badge-text {
  color: #999;
}
.info {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-top: 8rpx;
}
.card-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;
}
.action-btn {
  flex: 1;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: #fff;
}
.action-text {
  font-size: 22rpx;
}
.edit {
  border: 2rpx solid #07c160;
}
.edit .action-text {
  color: #07c160;
}
.reset {
  border: 2rpx solid #ff9500;
}
.reset .action-text {
  color: #ff9500;
}
.delete {
  border: 2rpx solid #ff3b30;
}
.delete .action-text {
  color: #ff3b30;
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
}
.dialog {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 34rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
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
}
.dialog-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}
.dialog-btn {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  border-radius: 44rpx;
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
