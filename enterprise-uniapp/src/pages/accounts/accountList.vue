<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { listAccounts, createAccount, updateAccount, resetPassword, deleteAccount } from '@/api/account'

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
    content: `确定删除账号 ${account.username} 吗？`,
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
      <text class="add-btn" @click="openAdd">+ 新增</text>
    </view>
    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="accounts.length === 0" class="state-msg">暂无账号</view>
      <view v-else class="list">
        <view v-for="a in accounts" :key="a.id" class="card">
          <view class="card-top">
            <text class="card-name">{{ a.username }}</text>
            <text class="badge" :class="a.status === 'ACTIVE' ? 'badge-on' : 'badge-off'">{{ a.status === 'ACTIVE' ? '正常' : '禁用' }}</text>
          </view>
          <text class="info">显示名：{{ a.displayName || '-' }}</text>
          <text class="info">角色：{{ roleLabel(a.role) }}</text>
          <text class="info">创建时间：{{ a.createdAt || '-' }}</text>
          <view class="card-actions">
            <button class="action-btn edit" @click="openEdit(a)">编辑</button>
            <button class="action-btn reset" @click="openReset(a)">重置密码</button>
            <button class="action-btn delete" @click="handleDelete(a)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <view v-if="formVisible" class="mask">
      <view class="dialog">
        <text class="dialog-title">{{ isEdit ? '编辑账号' : '新增账号' }}</text>
        <input v-if="!isEdit" v-model="form.username" class="input" placeholder="用户名" />
        <input v-if="!isEdit" v-model="form.password" class="input" placeholder="密码" password />
        <input v-model="form.displayName" class="input" placeholder="显示名" />
        <picker mode="selector" :range="roleOptions" range-key="label" @change="onRoleChange">
          <view class="picker">角色：{{ roleLabel(form.role) }}</view>
        </picker>
        <view class="dialog-actions">
          <button class="dialog-btn cancel" @click="formVisible = false">取消</button>
          <button class="dialog-btn primary" @click="saveAccount">保存</button>
        </view>
      </view>
    </view>

    <view v-if="resetVisible" class="mask">
      <view class="dialog">
        <text class="dialog-title">重置密码</text>
        <input v-model="resetForm.newPassword" class="input" placeholder="新密码" password />
        <view class="dialog-actions">
          <button class="dialog-btn cancel" @click="resetVisible = false">取消</button>
          <button class="dialog-btn primary" @click="confirmReset">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.add-btn { font-size: 28rpx; color: #007aff; }
.content { padding: 24rpx 32rpx; }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.card-name { font-size: 30rpx; font-weight: 500; color: #333; }
.badge { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.badge-on { background: #e8f8e8; color: #34c759; }
.badge-off { background: #f0f0f0; color: #999; }
.info { display: block; font-size: 26rpx; color: #666; margin-top: 8rpx; }
.card-actions { display: flex; gap: 12rpx; margin-top: 20rpx; }
.action-btn { flex: 1; height: 60rpx; line-height: 60rpx; font-size: 22rpx; border-radius: 8rpx; background: #fff; }
.action-btn::after, .dialog-btn::after { border: none; }
.edit { border: 2rpx solid #007aff; color: #007aff; }
.reset { border: 2rpx solid #ff9500; color: #ff9500; }
.delete { border: 2rpx solid #ff3b30; color: #ff3b30; }
.mask { position: fixed; left: 0; right: 0; top: 0; bottom: 0; background: rgba(0,0,0,.45); display: flex; align-items: center; justify-content: center; padding: 32rpx; }
.dialog { width: 100%; background: #fff; border-radius: 16rpx; padding: 32rpx; }
.dialog-title { display: block; font-size: 32rpx; font-weight: 600; margin-bottom: 24rpx; color: #333; }
.input, .picker { height: 76rpx; line-height: 76rpx; padding: 0 20rpx; border: 2rpx solid #ddd; border-radius: 8rpx; margin-bottom: 20rpx; font-size: 28rpx; }
.dialog-actions { display: flex; gap: 16rpx; margin-top: 24rpx; }
.dialog-btn { flex: 1; height: 72rpx; line-height: 72rpx; font-size: 28rpx; border-radius: 8rpx; }
.cancel { background: #f5f5f5; color: #333; }
.primary { background: #007aff; color: #fff; }
</style>
