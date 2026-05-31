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
        <text class="amount-tip">最低提现10元，可提现余额{{ availableBalance }}元</text>
      </view>

      <view class="quick-amounts">
        <view
          v-for="val in quickAmounts"
          :key="val"
          class="quick-chip"
          :class="{ selected: amount === String(val) }"
          @click="amount = String(val)"
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
const submitting = ref(false)
const realNameOk = ref(false)
const bankCardOk = ref(false)

const quickAmounts = [50, 100, 200, 500]

const ready = computed(() => realNameOk.value && bankCardOk.value)

const canSubmit = computed(() => {
  const val = parseFloat(amount.value)
  return ready.value && val >= 10 && val <= availableBalance.value
})

function validateAmount() {
  const val = parseFloat(amount.value)
  if (val > availableBalance.value) {
    amount.value = String(availableBalance.value)
  }
}

function goRealName() {
  uni.navigateTo({ url: '/pages/auth/realName' })
}

function goBankCard() {
  uni.navigateTo({ url: '/pages/bank/bankCard' })
}

async function handleWithdraw() {
  if (!ready.value) {
    if (!realNameOk.value) goRealName()
    else if (!bankCardOk.value) goBankCard()
    return
  }
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await createWithdrawal({ amount: parseFloat(amount.value) })
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
    availableBalance.value = res.pendingWithdrawal || 0
  } catch {
    // ignore
  }
})
</script>

<style scoped>
.withdraw-page {
  padding: 30rpx;
}

.balance-card {
  background: linear-gradient(135deg, #07c160, #06ad56);
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 30rpx;
  color: #fff;
}

.balance-label {
  font-size: 26rpx;
  opacity: 0.8;
  margin-bottom: 12rpx;
  display: block;
}

.balance-amount {
  font-size: 56rpx;
  font-weight: 700;
}

.form-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
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
  gap: 16rpx;
  flex-wrap: wrap;
}

.quick-chip {
  padding: 12rpx 28rpx;
  border-radius: 40rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
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
