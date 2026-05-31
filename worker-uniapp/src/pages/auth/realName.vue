<template>
  <view class="auth-page">
    <view class="status-banner" :class="statusClass">
      <text>当前状态：{{ statusLabel }}</text>
      <view v-if="status === 'REJECTED' && data?.rejectReason" class="reject-reason">
        拒绝原因：{{ data.rejectReason }}
      </view>
    </view>

    <view v-if="canSubmit" class="form-card">
      <view class="form-group">
        <text class="form-label">真实姓名</text>
        <input class="form-input" v-model="form.realName" placeholder="请输入真实姓名" />
      </view>
      <view class="form-group">
        <text class="form-label">身份证号</text>
        <input class="form-input" v-model="form.idCardNo" placeholder="请输入身份证号" maxlength="18" />
      </view>
      <view class="form-group">
        <text class="form-label">身份证正面照片 URL</text>
        <input class="form-input" v-model="form.idCardFrontUrl" placeholder="请粘贴正面照片 URL" />
      </view>
      <view class="form-group">
        <text class="form-label">身份证反面照片 URL</text>
        <input class="form-input" v-model="form.idCardBackUrl" placeholder="请粘贴反面照片 URL" />
      </view>
      <button class="submit-btn" type="primary" @click="handleSubmit" :loading="submitting">
        {{ status === 'REJECTED' ? '重新提交' : '提交认证' }}
      </button>
    </view>

    <view v-else-if="status === 'PENDING'" class="hint">
      您的实名认证正在审核中，请耐心等待。
    </view>
    <view v-else-if="status === 'APPROVED'" class="hint">
      您已通过实名认证（{{ data?.realName }} / {{ data?.idCardNoMasked }}）。
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getRealNameStatus, submitRealName } from '@/api/realName'

const status = ref('NONE')
const data = ref(null)
const submitting = ref(false)

const form = reactive({
  realName: '',
  idCardNo: '',
  idCardFrontUrl: '',
  idCardBackUrl: ''
})

const statusLabel = computed(() => ({
  NONE: '未提交',
  PENDING: '审核中',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}[status.value] || status.value))

const statusClass = computed(() => ({
  NONE: 'status-none',
  PENDING: 'status-pending',
  APPROVED: 'status-approved',
  REJECTED: 'status-rejected'
}[status.value] || ''))

const canSubmit = computed(() => status.value === 'NONE' || status.value === 'REJECTED')

async function loadStatus() {
  try {
    const res = await getRealNameStatus()
    data.value = res
    status.value = res?.status || 'NONE'
  } catch {
    status.value = 'NONE'
  }
}

async function handleSubmit() {
  if (!form.realName.trim() || !form.idCardNo.trim()) {
    uni.showToast({ title: '姓名和身份证号必填', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await submitRealName({ ...form })
    uni.showToast({ title: '已提交', icon: 'success' })
    await loadStatus()
  } catch (e) {
    uni.showToast({ title: e?.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onShow(loadStatus)
</script>

<style scoped>
.auth-page { padding: 30rpx; }
.status-banner {
  padding: 24rpx; border-radius: 12rpx; margin-bottom: 24rpx;
  font-size: 28rpx; color: #fff;
}
.status-none { background: #909399; }
.status-pending { background: #e6a23c; }
.status-approved { background: #07c160; }
.status-rejected { background: #f56c6c; }
.reject-reason { font-size: 24rpx; margin-top: 8rpx; opacity: 0.9; }
.form-card {
  background: #fff; border-radius: 16rpx; padding: 30rpx;
}
.form-group { margin-bottom: 24rpx; }
.form-label { font-size: 26rpx; color: #666; display: block; margin-bottom: 12rpx; }
.form-input {
  width: 100%; height: 72rpx; font-size: 28rpx; color: #333;
  border-bottom: 1rpx solid #f0f0f0;
}
.submit-btn {
  width: 100%; height: 88rpx; line-height: 88rpx;
  background: #07c160; border-radius: 44rpx;
  font-size: 32rpx; color: #fff; border: none;
}
.hint {
  background: #fff; padding: 40rpx; border-radius: 16rpx;
  text-align: center; color: #666; font-size: 28rpx;
}
</style>
