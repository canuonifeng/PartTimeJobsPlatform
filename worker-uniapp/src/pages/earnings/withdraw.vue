<template>
  <view class="withdraw-page">
    <view v-if="!ready" class="gate-card">
      <view v-if="!realNameOk" class="gate-row" @click="goRealName">
        <text>实名认证未通过，点击前往</text>
        <text class="arrow">›</text>
      </view>
      <view v-else-if="!bankCardOk && withdrawalMethod === 'BANK_CARD'" class="gate-row" @click="goBankCard">
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
        <text class="form-label">提现方式</text>
        <view class="method-list">
          <view
            v-for="method in availableMethods"
            :key="method.code"
            class="method-item"
            :class="{ selected: withdrawalMethod === method.code, disabled: !method.available }"
            @click="selectMethod(method)"
          >
            <view class="method-icon">{{ method.code === 'WECHAT' ? '💚' : '💳' }}</view>
            <view class="method-info">
              <text class="method-name">{{ method.name }}</text>
              <text class="method-desc">{{ method.description }}</text>
            </view>
            <view v-if="withdrawalMethod === method.code" class="method-check">✓</view>
          </view>
        </view>
      </view>

      <view v-if="withdrawalMethod === 'BANK_CARD' && bankCards.length > 0" class="form-group">
        <text class="form-label">选择银行卡</text>
        <view class="bank-list">
          <view
            v-for="card in bankCards"
            :key="card.id"
            class="bank-item"
            :class="{ selected: selectedBankId === card.id }"
            @click="selectedBankId = card.id"
          >
            <view class="bank-info">
              <text class="bank-name">{{ card.bankName }}</text>
              <text class="bank-number">****{{ card.cardNumber.slice(-4) }}</text>
            </view>
            <view v-if="selectedBankId === card.id" class="bank-check">✓</view>
          </view>
        </view>
      </view>

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
        <text class="amount-tip">{{ balanceLoaded ? `最低提现1元，可提现余额${availableBalance}元` : '余额加载失败，请稍后重试' }}</text>
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
import { getEarningsSummary, createWithdrawal, getWithdrawalMethods, getBankCards } from '@/api/earnings'
import { getRealNameStatus } from '@/api/realName.js'

const amount = ref('')
const availableBalance = ref(0)
const balanceLoaded = ref(false)
const submitting = ref(false)
const realNameOk = ref(false)
const withdrawalMethod = ref('WECHAT')
const availableMethods = ref<any[]>([])
const bankCards = ref<any[]>([])
const selectedBankId = ref<number | null>(null)

const baseQuickAmounts = [50, 100, 200, 500]

const ready = computed(() => {
  if (!realNameOk.value) return false
  if (withdrawalMethod.value === 'BANK_CARD') {
    return bankCards.value.length > 0 && selectedBankId.value !== null
  }
  return true
})
const quickAmounts = computed(() => baseQuickAmounts.filter(val => val <= availableBalance.value))

const canSubmit = computed(() => ready.value && balanceLoaded.value && !submitting.value && isValidAmount(amount.value))

function isValidAmount(value: string) {
  if (!/^\d+(\.\d{1,2})?$/.test(value)) return false
  const val = Number(value)
  return Number.isFinite(val) && val > 0 && val >= 1 && val <= availableBalance.value
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

function selectMethod(method: any) {
  if (!method.available) return
  withdrawalMethod.value = method.code
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
    else if (withdrawalMethod.value === 'BANK_CARD' && bankCards.value.length === 0) goBankCard()
    return
  }
  if (!canSubmit.value) return
  submitting.value = true
  try {
    const data: any = {
      amount: Number(amount.value),
      withdrawalMethod: withdrawalMethod.value
    }
    if (withdrawalMethod.value === 'BANK_CARD' && selectedBankId.value) {
      const card = bankCards.value.find(c => c.id === selectedBankId.value)
      if (card) {
        data.bankAccountId = card.id
      }
    }
    await createWithdrawal(data)
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
    const methods: any = await getWithdrawalMethods()
    availableMethods.value = Array.isArray(methods) ? methods : []
    if (availableMethods.value.length > 0) {
      const defaultMethod = availableMethods.value.find(m => m.available)
      if (defaultMethod) {
        withdrawalMethod.value = defaultMethod.code
      }
    }
  } catch {
    availableMethods.value = [{ code: 'WECHAT', name: '微信零钱', available: true, description: '实时到账' }]
  }
  try {
    const cards: any = await getBankCards()
    bankCards.value = Array.isArray(cards) ? cards : []
    if (bankCards.value.length > 0 && !selectedBankId.value) {
      selectedBankId.value = bankCards.value[0].id
    }
  } catch {
    bankCards.value = []
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

.method-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.method-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid #e5e7eb;
  background: #f9fafb;
  transition: all 0.2s;
}

.method-item.selected {
  border-color: #07c160;
  background: #f0fdf4;
}

.method-item.disabled {
  opacity: 0.5;
}

.method-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.method-name {
  font-size: 30rpx;
  color: #111827;
  font-weight: 600;
}

.method-desc {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.method-check {
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  border-radius: 20rpx;
  background: #07c160;
  color: #fff;
  font-size: 24rpx;
}

.bank-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.bank-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid #e5e7eb;
  background: #f9fafb;
  transition: all 0.2s;
}

.bank-item.selected {
  border-color: #07c160;
  background: #f0fdf4;
}

.bank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bank-name {
  font-size: 30rpx;
  color: #111827;
  font-weight: 600;
}

.bank-number {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 4rpx;
}

.bank-check {
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  border-radius: 20rpx;
  background: #07c160;
  color: #fff;
  font-size: 24rpx;
}
</style>
