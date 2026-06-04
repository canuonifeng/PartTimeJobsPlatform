<template>
  <view class="page">
    <view class="header-banner">
      <text class="header-title">银行卡</text>
      <text class="header-sub">绑定银行卡后可进行提现操作</text>
    </view>

    <view class="body-wrap">
      <view class="card-preview">
        <view class="card-bg" :class="{ empty: !card.id }">
          <view class="card-chip"></view>
          <text class="card-bank">{{ card.bankName || form.bankName || '未绑定银行卡' }}</text>
          <text class="card-number">{{ previewNumber }}</text>
          <view class="card-footer-row">
            <view class="card-holder-wrap">
              <text class="card-holder-label">持卡人</text>
              <text class="card-holder-name">{{ card.cardHolder || form.cardHolder || '请填写持卡人' }}</text>
            </view>
            <view class="card-brand">CARD</view>
          </view>
        </view>
        <view v-if="card.bankBranch || form.bankBranch" class="card-branch">{{ card.bankBranch || form.bankBranch }}</view>
      </view>

      <view class="form-card">
        <text class="form-title">{{ card.id ? '修改银行卡' : '绑定银行卡' }}</text>

        <view class="form-item">
          <text class="form-label">银行名称</text>
          <input v-model="form.bankName" placeholder="如：工商银行" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">持卡人姓名</text>
          <input v-model="form.cardHolder" placeholder="请输入持卡人姓名" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">银行卡号</text>
          <input v-model="form.cardNumber" placeholder="请输入卡号" type="number" maxlength="19" class="form-input" />
        </view>
        <view class="form-item">
          <text class="form-label">开户支行（选填）</text>
          <input v-model="form.bankBranch" placeholder="请输入开户支行" class="form-input" />
        </view>

        <button class="submit-btn" :loading="submitting" :disabled="submitting" @click="onSubmit">
          {{ submitting ? '保存中' : (card.id ? '更新' : '绑定') }}
        </button>
        <button v-if="card.id" class="unbind-btn" :loading="deleting" :disabled="deleting" @click="onDelete">解绑银行卡</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBankCard, upsertBankCard, deleteBankCard } from '@/api/bankCard.js'

const card = ref({})
const submitting = ref(false)
const deleting = ref(false)
const form = reactive({ bankName: '', cardHolder: '', cardNumber: '', bankBranch: '' })

const previewNumber = computed(() => maskCard(card.value.cardNumber || form.cardNumber) || '****  ****  ****  ****')

function resetForm() {
  card.value = {}
  form.bankName = ''
  form.cardHolder = ''
  form.cardNumber = ''
  form.bankBranch = ''
}

function maskCard(no) {
  const value = String(no || '').replace(/\s/g, '')
  if (!value) return ''
  if (value.length < 8) return value
  return value.slice(0, 4) + '  ****  ' + value.slice(-4)
}

async function load() {
  try {
    const res = await getBankCard()
    if (res && res.id) {
      card.value = res
      form.bankName = res.bankName || ''
      form.cardHolder = res.cardHolder || ''
      form.cardNumber = res.cardNumber || ''
      form.bankBranch = res.bankBranch || ''
    } else {
      resetForm()
    }
  } catch {
    resetForm()
  }
}

function validateCardNumber(no) {
  return /^\d{16,19}$/.test(no)
}

async function onSubmit() {
  if (submitting.value) return
  const bankName = form.bankName.trim()
  const cardHolder = form.cardHolder.trim()
  const cardNumber = String(form.cardNumber || '').replace(/\s/g, '')
  const bankBranch = form.bankBranch.trim()
  if (!bankName || !cardHolder || !cardNumber) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (!validateCardNumber(cardNumber)) {
    uni.showToast({ title: '银行卡号格式不正确', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await upsertBankCard({ bankName, cardHolder, cardNumber, bankBranch })
    uni.showToast({ title: '保存成功', icon: 'success' })
    await load()
  } catch (e) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function doDelete() {
  if (deleting.value) return
  deleting.value = true
  try {
    await deleteBankCard()
    resetForm()
    uni.showToast({ title: '已解绑' })
  } catch (e) {
    uni.showToast({ title: e?.message || '解绑失败', icon: 'none' })
  } finally {
    deleting.value = false
  }
}

function onDelete() {
  uni.showModal({
    title: '解绑',
    content: '确认解绑银行卡？',
    success: ({ confirm }) => {
      if (confirm) {
        doDelete()
      }
    }
  })
}

onShow(load)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 30rpx;
  padding-bottom: 40rpx;
  box-sizing: border-box;
}
.header-banner {
  background: linear-gradient(135deg, #07c160, #059d50);
  margin: -30rpx -30rpx 60rpx;
  padding: 50rpx 30rpx 200rpx;
  color: #fff;
  border-bottom-left-radius: 30rpx;
  border-bottom-right-radius: 30rpx;
}
.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}
.header-sub {
  display: block;
  font-size: 28rpx;
  opacity: 0.85;
}
.body-wrap {
  padding: 0 24rpx;
  margin-top: -120rpx;
  position: relative;
  z-index: 1;
}
.card-preview {
  margin-bottom: 24rpx;
}
.card-bg {
  background: linear-gradient(135deg, #1a73e8, #0d47a1);
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  color: #fff;
  box-shadow: 0 8rpx 32rpx rgba(26, 115, 232, 0.28);
}
.card-bg.empty {
  background: linear-gradient(135deg, #9ca3af, #6b7280);
}
.card-chip {
  width: 48rpx;
  height: 36rpx;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  border-radius: 6rpx;
  margin-bottom: 32rpx;
}
.card-bank {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
  opacity: 0.9;
}
.card-number {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  margin-bottom: 32rpx;
  font-family: monospace;
}
.card-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-holder-label {
  display: block;
  font-size: 22rpx;
  opacity: 0.7;
  margin-bottom: 6rpx;
}
.card-holder-name {
  font-size: 30rpx;
  font-weight: 600;
}
.card-brand {
  padding: 8rpx 12rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.5);
  border-radius: 8rpx;
  font-size: 22rpx;
}
.card-branch {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
  padding-left: 8rpx;
}
.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 36rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
}
.form-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 32rpx;
}
.form-item {
  margin-bottom: 28rpx;
}
.form-label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 12rpx;
}
.form-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #e8eaed;
  border-radius: 14rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #333;
  box-sizing: border-box;
  background: #fafafa;
}
.submit-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: #07c160;
  color: #fff;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 700;
  border: none;
  margin-top: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(7, 193, 96, 0.28);
}
.unbind-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #fff;
  color: #ff3b30;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: 2rpx solid #ff3b30;
  margin-top: 20rpx;
}
</style>
