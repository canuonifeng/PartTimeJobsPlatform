<template>
  <view class="withdraw-page">
    <view v-if="!ready" class="gate-card">
      <view v-if="!realNameOk" class="gate-row" @click="goRealName">
        <text>实名认证未通过，点击前往</text>
        <text class="arrow">›</text>
      </view>
      <view v-else-if="!bankCardOk" class="gate-row" @click="goBankCard">
        <text>未绑定银行卡，点击前往</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="balance-card">
      <text class="balance-label">可提现金额</text>
      <text class="balance-amount">{{ availableBalance }}元</text>
    </view>

    <view class="form-card">
      <view class="form-group">
        <text class="form-label">提现金额</text>
        <view class="amount-input-wrapper">
          <text class="amount-prefix">¥</text>
          <input
            class="amount-input"
            v-model="amount"
            type="digit"
            placeholder="请输入提现金额"
            @input="validateAmount"
          />
        </view>
        <text class="amount-tip">{{ balanceLoaded ? `最低提现10元，可提现余额${availableBalance}元` : '余额加载失败，请稍后重试' }}</text>
      </view>

      <view class="quick-amounts">
        <view
          v-for="val in quickAmounts"
          :key="val"
          class="quick-chip"
          :class="{ selected: amount === String(val) }"
          @click="setQuickAmount(val)"
        >
          {{ val }}元
        </view>
      </view>
    </view>

    <view class="submit-area">
      <button
        class="submit-btn"
        type="primary"
        :disabled="!canSubmit"
        @click="handleWithdraw"
        :loading="submitting"
      >
        申请提现
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getEarningsSummary, createWithdrawal } from '@/api/earnings'
import { getRealNameStatus } from '@/api/realName.js'
import { getBankCard } from '@/api/bankCard.js'

const amount = ref('')
const availableBalance = ref(0)
const balanceLoaded = ref(false)
const submitting = ref(false)
const realNameOk = ref(false)
const bankCardOk = ref(false)

const baseQuickAmounts = [50, 100, 200, 500]

const ready = computed(() => realNameOk.value && bankCardOk.value)
const quickAmounts = computed(() => baseQuickAmounts.filter(val => val <= availableBalance.value))

const canSubmit = computed(() => ready.value && balanceLoaded.value && !submitting.value && isValidAmount(amount.value))

function isValidAmount(value: string) {
  if (!/^\d+(\.\d{1,2})?$/.test(value)) return false
  const val = Number(value)
  return Number.isFinite(val) && val > 0 && val >= 10 && val <= availableBalance.value
}

function validateAmount() {
  if (!amount.value) return
  const val = Number(amount.value)
  if (Number.isFinite(val) && val > availableBalance.value) {
    amount.value = String(availableBalance.value)
  }
}

function setQuickAmount(val: number) {
  if (val <= availableBalance.value) {
    amount.value = String(val)
  }
}

function goRealName() {
  uni.navigateTo({ url: '/pages/auth/realName' })
}

function goBankCard() {
  uni.navigateTo({ url: '/pages/bank/bankCard' })
}

async function handleWithdraw() {
  if (submitting.value) return
  if (!ready.value) {
    if (!realNameOk.value) goRealName()
    else if (!bankCardOk.value) goBankCard()
    return
  }
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await createWithdrawal({ amount: Number(amount.value) })
    uni.showToast({ title: '提现申请已提交', icon: 'success' })
    uni.$emit('earningsRefresh')
    uni.navigateBack()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '提现失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function loadGates() {
  try {
    const auth: any = await getRealNameStatus()
    realNameOk.value = auth && auth.status === 'APPROVED'
  } catch {
    realNameOk.value = false
  }
  try {
    const card: any = await getBankCard()
    bankCardOk.value = !!(card && card.id)
  } catch {
    bankCardOk.value = false
  }
}

onMounted(async () => {
  await loadGates()
  try {
    const res = await getEarningsSummary()
    const balance = Number(res?.pendingWithdrawal ?? res?.availableBalance ?? res?.balance ?? 0)
    availableBalance.value = Number.isFinite(balance) && balance > 0 ? balance : 0
    balanceLoaded.value = true
  } catch {
    availableBalance.value = 0
    balanceLoaded.value = false
    uni.showToast({ title: '余额加载失败，请稍后重试', icon: 'none' })
  }
})
</script>

<style scoped>
.withdraw-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}

.balance-card {
  background: linear-gradient(135deg, #ff9f2d, #ff6a00);
  border-radius: 28rpx;
  padding: 42rpx 32rpx;
  margin-bottom: 24rpx;
  color: #fff;
  box-shadow: 0 12rpx 30rpx rgba(255, 106, 0, 0.18);
}

.balance-label {
  font-size: 26rpx;
  opacity: 0.8;
  margin-bottom: 12rpx;
  display: block;
}

.balance-amount {
  font-size: 64rpx;
  line-height: 76rpx;
  font-weight: 700;
}

.form-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.form-group {
  margin-bottom: 24rpx;
}

.form-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 16rpx;
  display: block;
}

.amount-input-wrapper {
  display: flex;
  align-items: center;
  border-bottom: 2rpx solid #07c160;
  padding-bottom: 16rpx;
}

.amount-prefix {
  font-size: 40rpx;
  color: #333;
  font-weight: 600;
  margin-right: 12rpx;
}

.amount-input {
  flex: 1;
  height: 72rpx;
  font-size: 48rpx;
  font-weight: 600;
  color: #333;
}

.amount-tip {
  font-size: 22rpx;
  color: #ccc;
  margin-top: 12rpx;
  display: block;
}

.quick-amounts {
  display: flex;
  flex-wrap: wrap;
  margin-right: -16rpx;
  margin-bottom: -16rpx;
}

.quick-chip {
  padding: 12rpx 28rpx;
  border-radius: 40rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
  margin-right: 16rpx;
  margin-bottom: 16rpx;
}

.quick-chip.selected {
  background: #e8f8ee;
  color: #07c160;
  font-weight: 500;
}

.submit-area {
  padding: 20rpx 0;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #07c160;
  border-radius: 44rpx;
  font-size: 32rpx;
  color: #fff;
  border: none;
}

.submit-btn[disabled] {
  background: #ccc;
}

.gate-card {
  background: #fff8e1;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.gate-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #d48806;
  font-size: 28rpx;
}
.gate-row .arrow { color: #d48806; }
</style>
