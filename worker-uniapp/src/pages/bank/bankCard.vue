<template>
  <view class="page">
    <view class="header">银行卡绑定</view>
    <view v-if="card.id" class="card-info">
      <view class="row"><text class="label">银行</text><text>{{ card.bankName }}</text></view>
      <view class="row"><text class="label">持卡人</text><text>{{ card.cardHolder }}</text></view>
      <view class="row"><text class="label">卡号</text><text>{{ maskCard(card.cardNumber) }}</text></view>
      <view v-if="card.bankBranch" class="row"><text class="label">支行</text><text>{{ card.bankBranch }}</text></view>
    </view>
    <view class="form">
      <view class="form-item">
        <text class="label">银行名称</text>
        <input v-model="form.bankName" placeholder="如：工商银行" />
      </view>
      <view class="form-item">
        <text class="label">持卡人姓名</text>
        <input v-model="form.cardHolder" placeholder="请输入持卡人姓名" />
      </view>
      <view class="form-item">
        <text class="label">银行卡号</text>
        <input v-model="form.cardNumber" placeholder="请输入卡号" type="number" />
      </view>
      <view class="form-item">
        <text class="label">开户支行（选填）</text>
        <input v-model="form.bankBranch" placeholder="请输入开户支行" />
      </view>
    </view>
    <button class="btn-primary" @click="onSubmit">{{ card.id ? '更新' : '绑定' }}</button>
    <button v-if="card.id" class="btn-danger" @click="onDelete">解绑</button>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBankCard, upsertBankCard, deleteBankCard } from '@/api/bankCard.js'

const card = ref({})
const form = reactive({ bankName: '', cardHolder: '', cardNumber: '', bankBranch: '' })

function maskCard(no) {
  if (!no || no.length < 8) return no
  return no.slice(0, 4) + '****' + no.slice(-4)
}

async function load() {
  try {
    const res = await getBankCard()
    if (res) {
      card.value = res
      form.bankName = res.bankName
      form.cardHolder = res.cardHolder
      form.cardNumber = res.cardNumber
      form.bankBranch = res.bankBranch || ''
    }
  } catch (e) {}
}

async function onSubmit() {
  if (!form.bankName || !form.cardHolder || !form.cardNumber) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  try {
    await upsertBankCard(form)
    uni.showToast({ title: '保存成功' })
    load()
  } catch (e) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
  }
}

async function onDelete() {
  uni.showModal({
    title: '解绑',
    content: '确认解绑银行卡？',
    success: async ({ confirm }) => {
      if (!confirm) return
      await deleteBankCard()
      card.value = {}
      form.bankName = ''
      form.cardHolder = ''
      form.cardNumber = ''
      form.bankBranch = ''
      uni.showToast({ title: '已解绑' })
    }
  })
}

onShow(load)
</script>

<style scoped>
.page { padding: 30rpx; }
.header { font-size: 36rpx; font-weight: bold; margin-bottom: 30rpx; }
.card-info { background: #f5f5f5; padding: 20rpx; border-radius: 12rpx; margin-bottom: 30rpx; }
.row { display: flex; justify-content: space-between; padding: 10rpx 0; }
.label { color: #666; }
.form-item { margin-bottom: 20rpx; }
.form-item .label { display: block; margin-bottom: 10rpx; }
.form-item input { border: 1rpx solid #ddd; padding: 16rpx; border-radius: 8rpx; }
.btn-primary { background: #007aff; color: #fff; margin-top: 30rpx; }
.btn-danger { background: #ff3b30; color: #fff; margin-top: 20rpx; }
</style>
