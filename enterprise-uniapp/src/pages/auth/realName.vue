<template>
  <view class="auth-page">
    <view class="status-card" :class="statusClass">
      <view class="status-row">
        <text class="status-title">企业实名认证</text>
        <text class="status-badge">{{ statusLabel }}</text>
      </view>
      <text class="status-desc">{{ statusDesc }}</text>
      <view v-if="status === 'REJECTED' && data?.rejectReason" class="reject-reason">
        拒绝原因：{{ data.rejectReason }}
      </view>
      <view v-if="status === 'APPROVED'" class="approved-info">
        <text>{{ data?.legalPersonName }}</text>
        <text v-if="data?.legalPersonIdCardMasked">{{ data.legalPersonIdCardMasked }}</text>
      </view>
    </view>

    <view v-if="canSubmit" class="form-card">
      <text class="form-title">{{ status === 'REJECTED' ? '重新提交认证' : '提交认证资料' }}</text>
      <view class="form-group">
        <text class="form-label">法人姓名</text>
        <input class="form-input" v-model="form.legalPersonName" placeholder="请输入法人姓名" />
      </view>
      <view class="form-group">
        <text class="form-label">法人身份证号</text>
        <input class="form-input" v-model="form.legalPersonIdCard" placeholder="请输入身份证号" maxlength="18" />
      </view>
      <view class="form-group">
        <text class="form-label">统一社会信用代码</text>
        <input class="form-input" v-model="form.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" />
      </view>
      <view class="form-group">
        <text class="form-label">营业执照图片 URL</text>
        <input class="form-input" v-model="form.businessLicenseUrl" placeholder="请粘贴营业执照图片 URL" />
      </view>
      <button class="submit-btn" type="primary" :loading="submitting" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中' : (status === 'REJECTED' ? '重新提交' : '提交认证') }}
      </button>
    </view>

    <view v-else class="form-card readonly-card">
      <text class="form-title">认证资料</text>
      <text class="readonly-text">{{ status === 'PENDING' ? '认证资料已提交，请等待平台审核。' : '企业已通过实名认证。' }}</text>
      <view v-if="status === 'APPROVED'" class="detail-list">
        <view class="detail-row">
          <text class="detail-label">法人姓名</text>
          <text class="detail-value">{{ data?.legalPersonName }}</text>
        </view>
        <view class="detail-row">
          <text class="detail-label">身份证号</text>
          <text class="detail-value">{{ data?.legalPersonIdCardMasked }}</text>
        </view>
        <view class="detail-row">
          <text class="detail-label">信用代码</text>
          <text class="detail-value">{{ data?.unifiedSocialCreditCode }}</text>
        </view>
        <view v-if="data?.businessLicenseUrl" class="detail-row column">
          <text class="detail-label">营业执照</text>
          <image class="license-img" :src="data.businessLicenseUrl" mode="aspectFit" @click="previewImage" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getRealName, submitRealName } from '@/api/realName'

const status = ref('NONE')
const data = ref(null)
const submitting = ref(false)

const form = reactive({
  legalPersonName: '',
  legalPersonIdCard: '',
  unifiedSocialCreditCode: '',
  businessLicenseUrl: ''
})

const statusMap = {
  NONE: '未提交',
  PENDING: '审核中',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

const statusDescMap = {
  NONE: '完成企业实名认证后可使用更多服务',
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
  form.legalPersonName = source?.legalPersonName || ''
  form.legalPersonIdCard = source?.legalPersonIdCard || ''
  form.unifiedSocialCreditCode = source?.unifiedSocialCreditCode || ''
  form.businessLicenseUrl = source?.businessLicenseUrl || ''
}

async function loadStatus() {
  try {
    const res = await getRealName()
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

function previewImage() {
  if (data.value?.businessLicenseUrl) {
    uni.previewImage({ urls: [data.value.businessLicenseUrl] })
  }
}

async function handleSubmit() {
  if (submitting.value) return
  if (!form.legalPersonName.trim()) {
    uni.showToast({ title: '请输入法人姓名', icon: 'none' })
    return
  }
  if (!form.legalPersonIdCard.trim()) {
    uni.showToast({ title: '请输入法人身份证号', icon: 'none' })
    return
  }
  if (!form.unifiedSocialCreditCode.trim()) {
    uni.showToast({ title: '请输入统一社会信用代码', icon: 'none' })
    return
  }
  if (!form.businessLicenseUrl.trim()) {
    uni.showToast({ title: '请粘贴营业执照图片 URL', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await submitRealName({
      legalPersonName: form.legalPersonName.trim(),
      legalPersonIdCard: form.legalPersonIdCard.trim(),
      unifiedSocialCreditCode: form.unifiedSocialCreditCode.trim(),
      businessLicenseUrl: form.businessLicenseUrl.trim()
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
  margin-bottom: 24rpx;
}
.detail-list {
  text-align: left;
  margin-top: 16rpx;
  padding-top: 24rpx;
  border-top: 2rpx solid #f2f2f2;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}
.detail-row.column {
  flex-direction: column;
  align-items: flex-start;
  gap: 16rpx;
}
.detail-label {
  font-size: 26rpx;
  color: #999;
}
.detail-value {
  font-size: 26rpx;
  color: #333;
}
.license-img {
  width: 400rpx;
  height: 280rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}
</style>
