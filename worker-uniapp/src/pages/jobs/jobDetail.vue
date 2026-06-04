<template>
  <view class="detail-page">
    <view class="top-nav">
      <view class="nav-safe"></view>
      <view class="nav-row">
        <view class="nav-back" @click="handleBack">‹</view>
        <text class="nav-title">职位详情</text>
        <button class="share-btn" open-type="share">分享</button>
      </view>
    </view>

    <uni-load-more v-if="loading" status="loading" />

    <template v-if="job && !loading">
      <view class="banner-card">
        <image v-if="job.imageUrl" class="banner-image" :src="job.imageUrl" mode="aspectFill" />
        <view v-else class="banner-emoji">{{ heroEmoji }}</view>
        <view class="banner-info">
          <text class="banner-title">{{ title }}</text>
        </view>
      </view>

      <view class="content">
        <view class="salary-card">
          <text class="salary-label">薪资待遇</text>
          <text class="salary-value">{{ salaryText }}</text>
        </view>

        <view class="card">
          <view class="card-title"><text class="card-icon">💼</text><text>基本信息</text></view>
          <view class="info-row"><text class="info-label">工作地点</text><view class="info-value-row" @click="handleOpenLocation"><text class="info-value">{{ locationText }}</text><text class="map-link">导航</text></view></view>
          <view class="info-row"><text class="info-label">招聘人数</text><text class="info-value">{{ headcountText }}</text></view>
          <view class="info-row"><text class="info-label">截止日期</text><text class="info-value">{{ deadlineText }}</text></view>
        </view>

        <view class="card">
          <view class="card-title"><text class="card-icon">📅</text><text>选择排班</text></view>
          <view v-if="schedules.length" class="schedule-grid">
            <view v-for="slot in schedules" :key="slot.id" class="schedule-block" :class="scheduleBlockClass(slot.id)" @click="toggleSchedule(slot.id)">
              <text v-if="isScheduleApplied(slot.id)" class="block-badge">已报名</text>
              <text v-else-if="pendingScheduleIds.includes(Number(slot.id))" class="block-badge selected">已选</text>
              <text class="block-date">{{ scheduleDate(slot) }}</text>
              <text class="block-time">{{ scheduleTime(slot) }}</text>
              <text class="block-duration">{{ scheduleDuration(slot) }}</text>
            </view>
          </view>
          <view v-else class="empty-hint">暂无可用排班</view>
        </view>

        <view class="card">
          <view class="card-title"><text class="card-icon">🧾</text><text>岗位职责</text></view>
          <rich-text v-if="responsibilitiesHtml" class="rich-content" :nodes="responsibilitiesHtml" />
          <view v-else v-for="(item, index) in duties" :key="index" class="list-row"><text class="list-dot"></text><text class="list-text">{{ item }}</text></view>
        </view>

        <view class="card">
          <view class="card-title"><text class="card-icon">✅</text><text>任职要求</text></view>
          <rich-text v-if="requirementsHtml" class="rich-content" :nodes="requirementsHtml" />
          <view v-else v-for="(item, index) in needs" :key="index" class="list-row"><text class="list-dot green"></text><text class="list-text">{{ item }}</text></view>
        </view>

        <view class="company-card">
          <view class="company-logo">{{ companyInitial }}</view>
          <view class="company-info">
            <text class="company-name">{{ companyName }}</text>
            <text class="company-desc">企业认证，岗位信息真实有效</text>
            <text class="company-location">{{ locationText }}</text>
          </view>
        </view>
      </view>

      <view class="action-bar">
        <button class="phone-btn" @click="handlePhone">电话</button>
        <button v-if="job.status === 'CLOSED'" class="apply-btn disabled" disabled>已关闭</button>
        <button v-else class="apply-btn" @click="handleApply">立即报名</button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { getJobDetail } from '@/api/jobs'

const fallbackJob = {
  title: '外卖配送员',
  companyName: '同城生活服务',
  location: '附近商圈，就近分配',
  description: '负责周边区域外卖订单配送，按要求准时取餐并送达。保持良好服务态度，及时沟通异常订单。',
  minRate: 28,
  maxRate: 35,
  rates: [{ amount: 30, type: 'HOURLY' }],
  schedules: [
    { id: -101, date: '2026-06-05', startTime: '10:00', endTime: '14:00' },
    { id: -102, date: '2026-06-05', startTime: '17:00', endTime: '21:00' },
    { id: -103, date: '2026-06-06', startTime: '10:00', endTime: '14:00' }
  ],
  appliedScheduleIds: [],
  status: 'OPEN',
  phone: ''
}

const job = ref<any>(null)
const loading = ref(true)
const jobId = ref(0)
const isFallbackJob = ref(false)
const pendingScheduleIds = ref<number[]>([])
const appliedScheduleIds = ref<number[]>([])

const title = computed(() => job.value?.title || fallbackJob.title)
const companyName = computed(() => job.value?.companyName || fallbackJob.companyName)
const locationText = computed(() => job.value?.location || fallbackJob.location)
const headcountText = computed(() => job.value?.headcount ? `${job.value.headcount}人` : '不限')
const deadlineText = computed(() => formatDateText(job.value?.deadline) || '长期有效')
const schedules = computed(() => Array.isArray(job.value?.schedules) ? job.value.schedules : [])
const heroEmoji = computed(() => title.value.indexOf('外卖') >= 0 || title.value.indexOf('配送') >= 0 ? '🛵' : '💼')
const companyAuthStatus = computed(() => normalizeCompanyAuthStatus(job.value))
const companyAuthText = computed(() => {
  const map: Record<string, string> = { APPROVED: '已实名', PENDING: '认证中', REJECTED: '未实名', NONE: '未实名' }
  return map[companyAuthStatus.value] || '未实名'
})
const companyAuthClass = computed(() => companyAuthStatus.value === 'APPROVED' ? 'approved' : (companyAuthStatus.value === 'PENDING' ? 'pending' : ''))
const companyInitial = computed(() => companyName.value.slice(0, 1))
const salaryText = computed(() => {
  const rates = Array.isArray(job.value?.rates) ? job.value.rates : []
  const rate = rates[0]
  if (rate?.amount) return `${rate.amount}元/${rateUnit(rate.type)}`
  if (job.value?.minRate && job.value?.maxRate) return `${job.value.minRate}-${job.value.maxRate}元/小时`
  if (job.value?.minRate) return `${job.value.minRate}元起/小时`
  return '薪资面议'
})
const responsibilitiesHtml = computed(() => htmlContent(job.value?.responsibilities || job.value?.description))
const requirementsHtml = computed(() => htmlContent(job.value?.requirements))
const duties = computed(() => splitItems(job.value?.responsibilities || job.value?.description, ['按排班时间准时到岗，完成岗位工作', '服从现场安排，保障服务质量', '及时沟通异常情况']))
const needs = computed(() => splitItems(job.value?.requirements, ['身体健康，能适应岗位节奏', '责任心强，时间观念好', '会使用智能手机，沟通顺畅']))

function htmlContent(value: any) {
  if (!value) return ''
  const content = String(value).trim()
  return /<[^>]+>/.test(content) ? sanitizeHtmlContent(content) : ''
}

function sanitizeHtmlContent(content: string) {
  const allowedTags = new Set(['p', 'br', 'div', 'span', 'strong', 'b', 'em', 'i', 'u', 'ul', 'ol', 'li'])
  return content
    .replace(/<!--[\s\S]*?-->/g, '')
    .replace(/<(script|style|iframe|object|embed|link|meta|svg|math)\b[\s\S]*?<\/\1>/gi, '')
    .replace(/<(script|style|iframe|object|embed|link|meta|svg|math)\b[^>]*\/?>/gi, '')
    .replace(/<\/?([a-z][a-z0-9]*)\b[^>]*>/gi, (match, tag) => {
      const normalizedTag = String(tag).toLowerCase()
      if (!allowedTags.has(normalizedTag)) return ''
      return match.startsWith('</') ? `</${normalizedTag}>` : `<${normalizedTag}>`
    })
}

function splitItems(value: any, fallback: string[]) {
  if (!value) return fallback
  const list = String(value).split(/\n|。|；|;/).map((item) => item.trim()).filter(Boolean)
  return list.length ? list : fallback
}

function scheduleBlockClass(scheduleId: number | string) {
  const id = Number(scheduleId)
  if (appliedScheduleIds.value.includes(id)) return 'block-applied'
  if (pendingScheduleIds.value.includes(id)) return 'block-selected'
  return ''
}

function isScheduleApplied(id: number | string) {
  return appliedScheduleIds.value.includes(Number(id))
}

function toggleSchedule(id: number | string) {
  const scheduleId = Number(id)
  if (isScheduleApplied(scheduleId) || job.value?.status === 'CLOSED') return
  const idx = pendingScheduleIds.value.indexOf(scheduleId)
  if (idx >= 0) {
    pendingScheduleIds.value.splice(idx, 1)
  } else {
    pendingScheduleIds.value.push(scheduleId)
  }
}

function rateUnit(type: string) {
  const map: Record<string, string> = { HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' }
  return map[type] || '小时'
}

function formatDateText(value: any) {
  if (!value) return ''
  const date = String(value)
  return date.length >= 10 ? date.slice(0, 10) : date
}

function scheduleDate(slot: any) {
  if (!slot?.date) return '日期待定'
  const date = String(slot.date)
  return date.length > 5 ? date.slice(5) : date
}

function scheduleTime(slot: any) {
  if (slot?.startTime && slot?.endTime) return `${slot.startTime}-${slot.endTime}`
  return '时间待定'
}

function scheduleDuration(slot: any) {
  const minutes = diffMinutes(slot?.startTime, slot?.endTime)
  if (!minutes) return '工时待定'
  const hours = minutes / 60
  return `${Number.isInteger(hours) ? hours : hours.toFixed(1)}小时`
}

function diffMinutes(start?: string, end?: string) {
  if (!start || !end) return 0
  const startParts = String(start).split(':').map(Number)
  const endParts = String(end).split(':').map(Number)
  if (startParts.length < 2 || endParts.length < 2 || startParts.some(Number.isNaN) || endParts.some(Number.isNaN)) return 0
  const startMinutes = startParts[0] * 60 + startParts[1]
  const endMinutes = endParts[0] * 60 + endParts[1]
  return endMinutes > startMinutes ? endMinutes - startMinutes : 0
}

function normalizeCompanyAuthStatus(source: any) {
  const raw = source?.companyRealNameStatus || source?.companyAuthStatus || source?.enterpriseRealNameStatus || source?.enterpriseAuthStatus
  const status = raw ? String(raw).toUpperCase() : ''
  if (source?.companyRealNamed || source?.companyVerified || source?.enterpriseVerified || status === 'VERIFIED' || status === 'APPROVED') return 'APPROVED'
  if (status === 'PENDING' || status === 'REVIEWING') return 'PENDING'
  if (status === 'REJECTED') return 'REJECTED'
  return 'NONE'
}

function handleBack() {
  uni.navigateBack({ delta: 1 })
}

function handlePhone() {
  const phone = job.value?.contactPhone || job.value?.phone || job.value?.mobile
  if (!phone) {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: String(phone) })
}

function handleOpenLocation() {
  if (!job.value?.latitude || !job.value?.longitude) {
    uni.showToast({ title: '暂无经纬度，无法导航', icon: 'none' })
    return
  }
  uni.openLocation({
    latitude: Number(job.value.latitude),
    longitude: Number(job.value.longitude),
    name: title.value,
    address: locationText.value
  })
}

function handleApply() {
  if (pendingScheduleIds.value.length === 0) {
    uni.showToast({ title: '请先选择排班', icon: 'none' })
    return
  }
  if (jobId.value <= 0 || isFallbackJob.value) {
    uni.showToast({ title: '示例岗位暂不支持报名', icon: 'none' })
    return
  }
  const scheduleIds = encodeURIComponent(pendingScheduleIds.value.join(','))
  uni.navigateTo({ url: `/pages/jobs/applyConfirm?jobId=${jobId.value}&scheduleIds=${scheduleIds}` })
}

async function loadJob() {
  loading.value = true
  pendingScheduleIds.value = []
  if (jobId.value <= 0) {
    job.value = { ...fallbackJob, id: jobId.value }
    isFallbackJob.value = true
    appliedScheduleIds.value = []
    loading.value = false
    return
  }
  try {
    const res: any = await getJobDetail(jobId.value)
    job.value = { ...fallbackJob, ...res }
    isFallbackJob.value = false
    appliedScheduleIds.value = Array.isArray(job.value.appliedScheduleIds) ? job.value.appliedScheduleIds.map(Number) : []
  } catch {
    job.value = { ...fallbackJob, id: jobId.value }
    isFallbackJob.value = true
    appliedScheduleIds.value = []
    uni.showToast({ title: '已展示默认岗位', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad((params: any) => {
  const id = Number(params.id)
  jobId.value = Number.isFinite(id) && id > 0 ? id : 0
  loadJob()
})

onShareAppMessage(() => ({
  title: title.value,
  path: `/pages/jobs/jobDetail?id=${jobId.value}`
}))
</script>

<style scoped>
.detail-page { min-height: 100vh; padding-bottom: 180rpx; background: #f6f7fb; }
.top-nav { position: sticky; top: 0; z-index: 20; background: #fff; box-shadow: 0 4rpx 18rpx rgba(27, 37, 67, 0.06); }
.nav-safe { height: 44rpx; }
.nav-row { height: 88rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 28rpx; }
.nav-back { width: 68rpx; height: 68rpx; line-height: 62rpx; text-align: center; border-radius: 34rpx; background: #f1f5f9; color: #1f2937; font-size: 56rpx; }
.nav-title { font-size: 34rpx; color: #111827; font-weight: 700; }
.share-btn { width: 96rpx; height: 58rpx; line-height: 58rpx; padding: 0; margin: 0; border-radius: 29rpx; background: #ecfdf5; color: #0f9f5f; font-size: 26rpx; }
.share-btn::after { border: none; }
.banner-card { margin: 24rpx 24rpx 0; border-radius: 36rpx; background: linear-gradient(135deg, #16c784, #0ea66b); overflow: hidden; box-shadow: 0 18rpx 40rpx rgba(14, 166, 107, 0.22); }
.banner-image { display: block; width: 100%; height: 300rpx; border-radius: 36rpx 36rpx 0 0; }
.banner-emoji { height: 210rpx; line-height: 210rpx; text-align: center; font-size: 118rpx; border-radius: 36rpx 36rpx 0 0; }
.banner-info { margin-top: 0; padding: 30rpx 34rpx 36rpx; border-radius: 0 0 36rpx 36rpx; text-align: left; }
.banner-title { display: block; color: #fff; font-size: 46rpx; line-height: 58rpx; font-weight: 800; text-align: left; }
.content { padding: 24rpx; }
.salary-card, .card, .company-card { margin-bottom: 22rpx; padding: 30rpx; border-radius: 28rpx; background: #fff; box-shadow: 0 8rpx 30rpx rgba(31, 41, 55, 0.06); }
.salary-card { background: #fff8ef; border: 2rpx solid #ffe4bd; }
.salary-label { display: block; color: #9a5b12; font-size: 26rpx; }
.salary-value { display: block; margin-top: 8rpx; color: #ff6b00; font-size: 48rpx; font-weight: 800; }
.tag-row { margin-top: 22rpx; }
.salary-tag { display: inline-block; margin-right: 14rpx; padding: 8rpx 18rpx; border-radius: 22rpx; background: #fff; color: #9a5b12; font-size: 24rpx; }
.card-title { display: flex; align-items: center; margin-bottom: 24rpx; color: #111827; font-size: 34rpx; font-weight: 700; }
.card-icon { margin-right: 12rpx; font-size: 34rpx; }
.info-row { display: flex; padding: 20rpx 0; border-top: 2rpx solid #f1f5f9; }
.info-label { width: 150rpx; color: #8a94a6; font-size: 28rpx; }
.info-value { flex: 1; color: #1f2937; font-size: 29rpx; line-height: 42rpx; }
.info-value-row { flex: 1; display: flex; align-items: center; }
.map-link { margin-left: 12rpx; padding: 6rpx 16rpx; border-radius: 20rpx; background: #ecfdf5; color: #0f9f5f; font-size: 24rpx; }
.auth-badge { flex-shrink: 0; margin-left: 12rpx; padding: 6rpx 16rpx; border-radius: 20rpx; background: #f1f5f9; color: #64748b; font-size: 24rpx; }
.auth-badge.approved { background: #ecfdf5; color: #0f9f5f; }
.auth-badge.pending { background: #fff7ed; color: #f97316; }
.schedule-grid { display: flex; flex-wrap: wrap; }
.schedule-block { position: relative; width: 284rpx; margin-right: 18rpx; margin-bottom: 18rpx; padding: 28rpx 12rpx; border-radius: 20rpx; border: 2rpx solid #e5e7eb; background: #f8fafc; text-align: center; }
.schedule-block:nth-child(2n) { margin-right: 0; }
.block-selected { border-color: #11b981; background: #ecfdf5; }
.block-applied { border-color: #d1d5db; background: #f3f4f6; opacity: 0.72; }
.block-badge { position: absolute; top: 0; right: 0; padding: 6rpx 14rpx; border-radius: 0 18rpx 0 14rpx; background: #9ca3af; color: #fff; font-size: 22rpx; }
.block-badge.selected { background: #10b981; }
.block-date { display: block; color: #111827; font-size: 30rpx; font-weight: 700; }
.block-time { display: block; margin-top: 10rpx; color: #64748b; font-size: 26rpx; }
.block-duration { display: block; margin-top: 8rpx; color: #0f9f5f; font-size: 24rpx; font-weight: 700; }
.empty-hint { padding: 36rpx 0; text-align: center; color: #9ca3af; font-size: 28rpx; }
.list-row { display: flex; margin-bottom: 18rpx; }
.list-dot { width: 12rpx; height: 12rpx; margin-top: 14rpx; margin-right: 16rpx; border-radius: 6rpx; background: #ff8a00; }
.list-dot.green { background: #10b981; }
.list-text { flex: 1; color: #475569; font-size: 29rpx; line-height: 44rpx; }
.rich-content { display: block; color: #475569; font-size: 29rpx; line-height: 44rpx; }
.company-card { display: flex; align-items: center; }
.company-logo { width: 96rpx; height: 96rpx; line-height: 96rpx; margin-right: 22rpx; border-radius: 48rpx; text-align: center; background: #ecfdf5; color: #0f9f5f; font-size: 42rpx; font-weight: 800; }
.company-info { flex: 1; }
.company-name { display: block; color: #111827; font-size: 32rpx; font-weight: 700; }
.company-desc { display: block; margin-top: 10rpx; color: #64748b; font-size: 26rpx; }
.company-location { display: block; margin-top: 8rpx; color: #94a3b8; font-size: 24rpx; }
.action-bar { position: fixed; left: 0; right: 0; bottom: 0; z-index: 30; display: flex; padding: 18rpx 28rpx 38rpx; background: #fff; box-shadow: 0 -8rpx 28rpx rgba(31, 41, 55, 0.08); }
.phone-btn { width: 180rpx; height: 92rpx; line-height: 92rpx; margin: 0 18rpx 0 0; padding: 0; border-radius: 46rpx; background: #f1f5f9; color: #0f172a; font-size: 32rpx; font-weight: 700; }
.apply-btn { flex: 1; height: 92rpx; line-height: 92rpx; margin: 0; padding: 0; border-radius: 46rpx; background: linear-gradient(135deg, #16c784, #0ea66b); color: #fff; font-size: 34rpx; font-weight: 800; }
.disabled { background: #d1d5db; color: #fff; }
.phone-btn::after, .apply-btn::after { border: none; }
</style>
