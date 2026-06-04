<template>
  <view class="auth-page">
    <view class="status-card" :class="statusClass">
      <view class="status-row">
        <text class="status-title">实名认证</text>
        <text class="status-badge">{{ statusLabel }}</text>
      </view>
      <text class="status-desc">{{ statusDesc }}</text>
      <view v-if="status === 'REJECTED' && data?.rejectReason" class="reject-reason">
        拒绝原因：{{ data.rejectReason }}
      </view>
      <view v-if="status === 'APPROVED'" class="approved-info">
        <text>{{ data?.realName }}</text>
        <text v-if="data?.idCardNoMasked">{{ data.idCardNoMasked }}</text>
      </view>
    </view>

    <view v-if="canSubmit" class="form-card">
      <text class="form-title">{{ status === 'REJECTED' ? '重新提交认证' : '提交认证资料' }}</text>
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
      <button class="submit-btn" type="primary" :loading="submitting" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中' : (status === 'REJECTED' ? '重新提交' : '提交认证') }}
      </button>
    </view>

    <view v-else class="form-card readonly-card">
      <text class="form-title">认证资料</text>
      <text class="readonly-text">{{ status === 'PENDING' ? '您的实名认证正在审核中，请耐心等待。' : '您已通过实名认证。' }}</text>
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

const statusMap = {
  NONE: '未提交',
  PENDING: '审核中',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

const statusDescMap = {
  NONE: '完成实名认证后可使用更多服务',
  PENDING: '资料已提交，请等待平台审核',
  APPROVED: '认证已通过，请确保资料真实有效',
  REJECTED: '认证未通过，请修改后重新提交'
}

const statusLabel = computed(() => statusMap[status.value] || statusMap.NONE)
const statusDesc = computed(() => statusDescMap[status.value] || statusDescMap.NONE)

const statusClass = computed(() => ({
  NONE: 'status-none',
  PENDING: 'status-pending',
  APPROVED: 'status-approved',
  REJECTED: 'status-rejected'
}[status.value] || 'status-none'))

const canSubmit = computed(() => status.value === 'NONE' || status.value === 'REJECTED')

function fillForm(source) {
  form.realName = source?.realName || ''
  form.idCardNo = source?.idCardNo || ''
  form.idCardFrontUrl = source?.idCardFrontUrl || ''
  form.idCardBackUrl = source?.idCardBackUrl || ''
}

async function loadStatus() {
  try {
    const res = await getRealNameStatus()
    data.value = res
    status.value = ['NONE', 'PENDING', 'APPROVED', 'REJECTED'].includes(res?.status) ? res.status : 'NONE'
    if (status.value === 'REJECTED') {
      fillForm(res)
    }
  } catch {
    status.value = 'NONE'
    data.value = null
  }
}

function isValidIdCard(no) {
  if (!/^\d{17}[\dXx]$/.test(no)) return false
  const year = Number(no.slice(6, 10))
  const month = Number(no.slice(10, 12))
  const day = Number(no.slice(12, 14))
  const birth = new Date(year, month - 1, day)
  return birth.getFullYear() === year && birth.getMonth() === month - 1 && birth.getDate() === day && birth.getTime() <= Date.now()
}

async function handleSubmit() {
  if (submitting.value) return
  const realName = form.realName.trim()
  const idCardNo = form.idCardNo.trim()
  if (!realName) {
    uni.showToast({ title: '请输入真实姓名', icon: 'none' })
    return
  }
  if (!idCardNo) {
    uni.showToast({ title: '请输入身份证号', icon: 'none' })
    return
  }
  if (!isValidIdCard(idCardNo)) {
    uni.showToast({ title: '身份证号格式不正确', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await submitRealName({
      realName,
      idCardNo,
      idCardFrontUrl: form.idCardFrontUrl.trim(),
      idCardBackUrl: form.idCardBackUrl.trim()
    })
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
.auth-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f5f7fa;
  box-sizing: border-box;
}
.status-card {
  padding: 34rpx;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}
.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.status-title {
  font-size: 36rpx;
  font-weight: 700;
}
.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  background: rgba(255, 255, 255, 0.22);
}
.status-desc {
  display: block;
  font-size: 28rpx;
  opacity: 0.92;
}
.status-none {
  background: linear-gradient(135deg, #909399, #6b7280);
}
.status-pending {
  background: linear-gradient(135deg, #e6a23c, #d97706);
}
.status-approved {
  background: linear-gradient(135deg, #07c160, #059d50);
}
.status-rejected {
  background: linear-gradient(135deg, #f56c6c, #dc2626);
}
.reject-reason {
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.28);
  font-size: 26rpx;
}
.approved-info {
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.28);
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
}
.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 34rpx;
  box-shadow: 0 4rpx 18rpx rgba(0, 0, 0, 0.05);
}
.form-title {
  display: block;
  margin-bottom: 30rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: #222;
}
.form-group {
  margin-bottom: 26rpx;
}
.form-label {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 12rpx;
}
.form-input {
  width: 100%;
  height: 78rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  box-sizing: border-box;
}
.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 12rpx;
  background: #07c160;
  border-radius: 44rpx;
  font-size: 32rpx;
  color: #fff;
  border: none;
}
.readonly-card {
  text-align: center;
}
.readonly-text {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 44rpx;
}
</style>
