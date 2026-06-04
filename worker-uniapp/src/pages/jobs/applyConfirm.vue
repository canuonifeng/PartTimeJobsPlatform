<template>
  <view class="page">
    <view class="steps">
      <view class="step on"><text class="dot">1</text><text>选择岗位</text></view><view class="line on"></view>
      <view class="step on"><text class="dot">2</text><text>确认报名</text></view><view class="line"></view>
      <view class="step"><text class="dot">3</text><text>等待联系</text></view>
    </view>
    <uni-load-more v-if="loading" status="loading" />
    <view class="body">
      <view class="hero"><view class="logo">{{ companyInitial }}</view><view class="hero-main"><text class="title">{{ title }}</text><text class="company">{{ companyName }}</text></view><text class="salary">{{ salaryText }}</text></view>
      <view class="card"><view class="card-title">岗位摘要</view><view class="row"><text class="label">工作地点</text><text class="value">{{ locationText }}</text></view><view class="row"><text class="label">报名时段</text><text class="value">{{ selectedScheduleIds.length }}个</text></view></view>
      <view class="card"><view class="card-title">工作日期选择</view><view v-if="dateOptions.length" class="chips"><view v-for="item in dateOptions" :key="item.date" class="chip" :class="{ active: item.selected }" @click="toggleDate(item.date)"><text class="main">{{ item.text }}</text><text class="sub">{{ item.count }}个时段</text></view></view><view v-else class="empty">暂无有效工作日期</view></view>
      <view class="card"><view class="card-title">工作时段选择</view><view v-if="displaySchedules.length"><view v-for="slot in displaySchedules" :key="slot.id" class="time" :class="{ active: selectedScheduleIds.includes(slot.id) }" @click="toggleSchedule(slot.id)"><view class="time-main"><text class="main">{{ formatDate(slot.date) }}</text><text class="sub">{{ formatTime(slot) }}</text></view><text class="status">{{ selectedScheduleIds.includes(slot.id) ? '已选' : '选择' }}</text></view></view><view v-else class="empty">暂无有效工作时段</view></view>
      <view class="card"><view class="card-title">报名须知</view><view v-for="item in notices" :key="item" class="notice"><text class="notice-dot"></text><text class="notice-text">{{ item }}</text></view></view>
      <view class="card phone-card"><view><text class="phone-label">联系人电话</text><text class="phone-value">{{ phoneText }}</text></view><button class="phone-btn" :disabled="!contactPhone" @click="handlePhone">拨打</button></view>
    </view>
    <view class="bar"><button class="cancel" @click="handleCancel">取消</button><button class="confirm" :class="{ disabled: !canSubmit }" :disabled="!canSubmit" @click="handleConfirm">{{ submitting ? '提交中...' : '确认报名' }}</button></view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { applyJob, getJobDetail } from '@/api/jobs'

interface ScheduleItem { id: number; date?: string; startTime?: string; endTime?: string }

const fallbackJob = { title: '待确认岗位', companyName: '招聘企业', location: '工作地点待确认', rates: [] }
const jobId = ref(0)
const selectedScheduleIds = ref<number[]>([])
const invalidParams = ref(false)
const loading = ref(false)
const submitting = ref(false)
const job = ref<any>({ ...fallbackJob })
const notices = ['请确认报名日期和工作时段，提交后将同步给招聘方', '报名成功后可在我的排班查看，按时到岗完成工作', '如需调整安排，请及时联系招聘方沟通']

const title = computed(() => job.value?.title || fallbackJob.title)
const companyName = computed(() => job.value?.companyName || fallbackJob.companyName)
const locationText = computed(() => job.value?.location || job.value?.locationName || fallbackJob.location)
const contactPhone = computed(() => job.value?.phone || job.value?.contactPhone || job.value?.mobile || '')
const phoneText = computed(() => contactPhone.value ? String(contactPhone.value) : '暂无联系电话')
const companyInitial = computed(() => companyName.value.slice(0, 1))
const schedules = computed<ScheduleItem[]>(() => (Array.isArray(job.value?.schedules) ? job.value.schedules : []).map((item: any) => ({ id: Number(item.id), date: item.date, startTime: item.startTime, endTime: item.endTime })).filter((item: ScheduleItem) => Number.isInteger(item.id) && item.id > 0))
const selectedSchedules = computed(() => {
  const known = schedules.value.filter((item) => selectedScheduleIds.value.includes(item.id))
  return known.length ? known : selectedScheduleIds.value.map((id) => ({ id }))
})
const displaySchedules = computed(() => schedules.value.length ? schedules.value : selectedSchedules.value)
const dateOptions = computed(() => {
  const map: Record<string, { date: string; count: number; selected: boolean }> = {}
  displaySchedules.value.forEach((slot) => {
    const date = slot.date || '日期待定'
    if (!map[date]) map[date] = { date, count: 0, selected: false }
    map[date].count += 1
    if (selectedScheduleIds.value.includes(slot.id)) map[date].selected = true
  })
  return Object.keys(map).map((date) => ({ ...map[date], text: formatDate(date) }))
})
const salaryText = computed(() => {
  const rate = Array.isArray(job.value?.rates) ? job.value.rates[0] : null
  if (rate?.amount) return `${rate.amount}元/${rateUnit(rate.type)}`
  if (job.value?.minRate && job.value?.maxRate) return `${job.value.minRate}-${job.value.maxRate}元/小时`
  if (job.value?.minRate) return `${job.value.minRate}元起/小时`
  return '薪资面议'
})
const canSubmit = computed(() => !invalidParams.value && jobId.value > 0 && selectedScheduleIds.value.length > 0 && !submitting.value)

function parseScheduleIds(value: any) {
  try {
    const raw = decodeURIComponent(String(value || '')).split(',').map((item) => item.trim()).filter(Boolean)
    const ids = raw.map((item) => Number(item))
    const valid = ids.length > 0 && ids.every((id) => Number.isInteger(id) && id > 0)
    return { valid, ids: valid ? Array.from(new Set(ids)) : [] }
  } catch {
    return { valid: false, ids: [] }
  }
}
function rateUnit(type: string) { return ({ HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' } as Record<string, string>)[type] || '小时' }
function formatDate(value?: string) { const text = String(value || '日期待定'); return text.length > 5 ? text.slice(5) : text }
function formatTime(slot: ScheduleItem) { return slot.startTime && slot.endTime ? `${slot.startTime}-${slot.endTime}` : '时间待定' }
function toggleDate(date: string) {
  const ids = displaySchedules.value.filter((slot) => (slot.date || '日期待定') === date).map((slot) => slot.id)
  const allSelected = ids.length > 0 && ids.every((id) => selectedScheduleIds.value.includes(id))
  selectedScheduleIds.value = allSelected ? selectedScheduleIds.value.filter((id) => !ids.includes(id)) : Array.from(new Set([...selectedScheduleIds.value, ...ids]))
}
function toggleSchedule(id: number) { const index = selectedScheduleIds.value.indexOf(id); index >= 0 ? selectedScheduleIds.value.splice(index, 1) : selectedScheduleIds.value.push(id) }
function handlePhone() { contactPhone.value ? uni.makePhoneCall({ phoneNumber: String(contactPhone.value) }) : uni.showToast({ title: '暂无联系电话', icon: 'none' }) }
function handleCancel() { uni.navigateBack({ delta: 1 }) }
async function handleConfirm() {
  if (!canSubmit.value) return uni.showToast({ title: invalidParams.value ? '报名参数无效' : '请选择工作时段', icon: 'none' })
  submitting.value = true
  try {
    await applyJob(jobId.value, { scheduleIds: selectedScheduleIds.value })
    uni.showToast({ title: '报名成功', icon: 'success' })
    setTimeout(() => uni.navigateTo({ url: '/pages/schedule/schedule', fail: () => uni.navigateBack({ delta: 1 }) }), 600)
  } catch (err: any) {
    uni.showToast({ title: err?.data?.error || err?.message || '报名失败', icon: 'none' })
    submitting.value = false
  }
}
async function loadJob() {
  if (jobId.value <= 0) return
  loading.value = true
  try { job.value = { ...fallbackJob, ...await getJobDetail(jobId.value) } } catch { uni.showToast({ title: '岗位信息加载失败', icon: 'none' }) } finally { loading.value = false }
}

onLoad((params: any) => {
  const id = Number(params?.jobId)
  const parsed = parseScheduleIds(params?.scheduleIds)
  jobId.value = Number.isFinite(id) && id > 0 ? id : 0
  selectedScheduleIds.value = parsed.ids
  invalidParams.value = jobId.value <= 0 || !parsed.valid
  if (invalidParams.value) uni.showToast({ title: '报名参数无效', icon: 'none' })
  loadJob()
})
</script>

<style scoped>
.page { min-height: 100vh; padding-bottom: 180rpx; background: #f6f7fb; }
.steps { display: flex; align-items: center; padding: 34rpx 30rpx 26rpx; background: #fff; }
.step { width: 150rpx; text-align: center; color: #94a3b8; font-size: 24rpx; }
.dot { display: block; width: 48rpx; height: 48rpx; line-height: 48rpx; margin: 0 auto 10rpx; border-radius: 24rpx; background: #e5e7eb; color: #64748b; font-weight: 700; }
.step.on { color: #10b981; }
.step.on .dot, .line.on, .confirm { background: #10b981; color: #fff; }
.line { flex: 1; height: 4rpx; margin-bottom: 34rpx; background: #e5e7eb; }
.body { padding: 24rpx; }
.hero, .card { margin-bottom: 22rpx; padding: 30rpx; border-radius: 28rpx; background: #fff; box-shadow: 0 8rpx 30rpx rgba(31, 41, 55, 0.06); }
.hero { display: flex; align-items: center; background: #10b981; }
.logo { width: 88rpx; height: 88rpx; line-height: 88rpx; margin-right: 20rpx; border-radius: 44rpx; background: #eafff5; color: #0f9f5f; text-align: center; font-size: 38rpx; font-weight: 800; }
.hero-main, .time-main, .notice-text, .value, .confirm { flex: 1; }
.title, .company, .salary, .phone-label, .phone-value, .main, .sub { display: block; }
.title { color: #fff; font-size: 38rpx; font-weight: 800; }
.company { margin-top: 8rpx; color: #eafff5; font-size: 26rpx; }
.salary { color: #fff; font-size: 28rpx; font-weight: 800; }
.card-title { margin-bottom: 22rpx; color: #111827; font-size: 34rpx; font-weight: 700; }
.row, .time, .notice, .phone-card, .bar { display: flex; }
.row { padding: 16rpx 0; border-top: 2rpx solid #f1f5f9; }
.label { width: 150rpx; color: #8a94a6; font-size: 28rpx; }
.value { color: #1f2937; font-size: 28rpx; line-height: 42rpx; }
.chips { display: flex; flex-wrap: wrap; }
.chip { width: 196rpx; margin-right: 18rpx; margin-bottom: 18rpx; padding: 22rpx 10rpx; border-radius: 22rpx; border: 2rpx solid #e5e7eb; background: #f8fafc; text-align: center; }
.chip.active, .time.active { border-color: #10b981; background: #ecfdf5; }
.main { color: #111827; font-size: 30rpx; font-weight: 700; }
.sub { margin-top: 8rpx; color: #64748b; font-size: 24rpx; }
.time { align-items: center; margin-bottom: 18rpx; padding: 24rpx; border-radius: 22rpx; border: 2rpx solid #e5e7eb; background: #f8fafc; }
.status { width: 96rpx; height: 48rpx; line-height: 48rpx; border-radius: 24rpx; background: #e5e7eb; color: #64748b; text-align: center; font-size: 24rpx; }
.time.active .status { background: #10b981; color: #fff; }
.notice { margin-bottom: 18rpx; }
.notice-dot { width: 12rpx; height: 12rpx; margin-top: 14rpx; margin-right: 16rpx; border-radius: 6rpx; background: #10b981; }
.notice-text { color: #475569; font-size: 28rpx; line-height: 42rpx; }
.phone-card { align-items: center; justify-content: space-between; }
.phone-label { color: #8a94a6; font-size: 26rpx; }
.phone-value { margin-top: 8rpx; color: #111827; font-size: 32rpx; font-weight: 700; }
.phone-btn { width: 140rpx; height: 64rpx; line-height: 64rpx; margin: 0; padding: 0; border-radius: 32rpx; background: #ecfdf5; color: #0f9f5f; font-size: 28rpx; }
.empty { padding: 28rpx 0; color: #9ca3af; text-align: center; font-size: 28rpx; }
.bar { position: fixed; left: 0; right: 0; bottom: 0; padding: 18rpx 28rpx 38rpx; background: #fff; box-shadow: 0 -8rpx 28rpx rgba(31, 41, 55, 0.08); }
.cancel, .confirm { height: 92rpx; line-height: 92rpx; margin: 0; padding: 0; border-radius: 46rpx; font-size: 32rpx; font-weight: 800; }
.cancel { width: 210rpx; margin-right: 18rpx; background: #f1f5f9; color: #111827; }
.disabled { background: #d1d5db; color: #fff; }
button::after { border: none; }
</style>
