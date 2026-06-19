<template>
  <view class="detail-page" >
    <uni-load-more v-if="loading" status="loading" />

    <view v-if="!job && !loading" class="empty-state">
      <text class="empty-text">岗位信息加载失败</text>
    </view>

    <template v-if="job && !loading">
    
      <scroll-view class="detail-content" scroll-y>
        <view class="detail-banner" :style="bannerStyle">
          <view v-if="bannerImage" class="banner-overlay"></view>
        </view>

        <view class="job-info-card">
          <text class="job-info-title">{{ title }}</text>
          <view v-if="jobTags.length > 0" class="job-info-tags">
            <text v-for="(tag, idx) in jobTags.slice(0, 5)" :key="idx" class="job-info-tag">{{ tag }}</text>
          </view>
        </view>

        <view class="detail-card">
          <view class="card-title">
            <view class="title-icon"><text>📅</text></view>
            <text class="title-text">选择班次报名</text>
          </view>

          <scroll-view class="date-filter" scroll-x :show-scrollbar="false">
            <view class="date-chips">
              <view
                v-for="(d, idx) in dateFilterOptions"
                :key="idx"
                class="date-chip"
                :class="{ active: activeDateFilter === d.key }"
                @click="setDateFilter(d.key)"
              >
                <text>{{ d.label }}</text>
              </view>
            </view>
          </scroll-view>

          <view class="schedule-grid">
            <view
              v-for="slot in filteredSchedules"
              :key="slot.id"
              class="schedule-card"
              :class="scheduleCardClass(slot)"
              @click="toggleSchedule(slot)"
            >
              <view class="sch-date-box" :class="{ active: pendingScheduleIds.includes(Number(slot.id)) }">
                <text class="sch-date-day">{{ formatSlotDay(slot.date) }}</text>
                <text class="sch-date-month">{{ formatSlotMonth(slot.date) }}月</text>
              </view>
              <view class="sch-right">
                <view class="sch-pay">{{ schedulePay(slot) }}</view>
                <view class="sch-time">{{ scheduleTime(slot) }}</view>
              </view>
              <text v-if="isScheduleApplied(slot.id)" class="sch-corner-badge applied">已报名</text>
              <text v-else-if="isScheduleFull(slot)" class="sch-corner-badge full">已报满</text>
              <text v-else-if="pendingScheduleIds.includes(Number(slot.id))" class="sch-corner-badge selected">已选</text>
              <text v-else class="sch-corner-badge available">可选</text>
            </view>
          </view>

          <view v-if="filteredSchedules.length === 0" class="empty-hint">
            <text>暂无可用班次</text>
          </view>


        </view>

        <view class="detail-card location-card" @click="handleOpenLocation">
          <view class="loc-header">
            <view class="loc-icon">📍</view>
            <view class="loc-info">
              <text class="loc-label">工作地点</text>
              <text class="loc-address">{{ locationText }}</text>
            </view>
            <text class="loc-arrow">导航 ›</text>
          </view>
        </view>


        <view class="detail-card">
          <view class="card-title">
            <view class="title-icon"><text>📋</text></view>
            <text class="title-text">岗位职责</text>
          </view>
          <rich-text v-if="responsibilitiesHtml" class="rich-content" :nodes="responsibilitiesHtml" />
          <view v-else class="duty-list">
            <view v-for="(item, index) in duties" :key="index" class="duty-item">
              <view class="duty-dot"></view>
              <text class="duty-text">{{ item }}</text>
            </view>
          </view>
        </view>

        <view class="detail-card">
          <view class="card-title">
            <view class="title-icon"><text>✅</text></view>
            <text class="title-text">任职要求</text>
          </view>
          <rich-text v-if="requirementsHtml" class="rich-content" :nodes="requirementsHtml" />
          <view v-else class="duty-list">
            <view v-for="(item, index) in needs" :key="index" class="duty-item">
              <view class="duty-dot blue"></view>
              <text class="duty-text">{{ item }}</text>
            </view>
          </view>
        </view>

        <view class="detail-card company-card">
          <view class="card-title">
            <view class="title-icon"><text>🏢</text></view>
            <text class="title-text">企业信息</text>
          </view>
          <view class="company-row">
            <view class="company-big-logo">{{ companyInitial }}</view>
            <view class="company-info-col">
              <text class="company-name">{{ companyName }}</text>
              <view class="company-auth-row">
                <text class="auth-badge" :class="companyAuthClass">{{ companyAuthText }}</text>
                <text class="company-desc">在招职位 {{ job.jobCount || '多个' }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="bottom-safe"></view>
      </scroll-view>

      <view class="action-bar">
        <view class="action-icon-btn" @click="handlePhone">
          <text class="action-icon">📞</text>
          <text class="action-label">电话</text>
        </view>
        <button
          class="apply-btn-main"
          :class="{ disabled: !canApply }"
          :disabled="!canApply"
          @click="handleApply"
        >
          {{ applyButtonText }}
        </button>
      </view>
    </template>

    <ApplyConfirmSheet
      :visible="showApplySheet"
      :job-title="title"
      :location="locationText"
      :schedules="selectedScheduleList"
      :total-text="`${pendingScheduleIds.length}个班次`"
      :total-amount="`¥${selectedTotalAmount}`"
      :submitting="applying"
      @close="showApplySheet = false"
      @confirm="confirmApply"
    />

    <SuccessOverlay
      :visible="showSuccess"
      title="报名成功！"
      description="请按时前往工作地点，企业会尽快与你联系确认"
      primary-text="查看我的排班"
      secondary-text="继续找活"
      icon="🎉"
      @primary="goToSchedule"
      @secondary="closeSuccessAndBack"
    />

    <LoginSheet />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { getJobDetail, applyJob } from '@/api/jobs'
import { getProfileCompleteness } from '@/api/profile'
import { useAuthStore } from '@/store'
import LoginSheet from '@/components/LoginSheet.vue'
import ApplyConfirmSheet from '@/components/ApplyConfirmSheet.vue'
import SuccessOverlay from '@/components/SuccessOverlay.vue'
import { openLoginSheet } from '@/utils/loginSheet'

const authStore = useAuthStore()
const job = ref<any>(null)
const loading = ref(true)
const jobId = ref(0)
const pendingScheduleIds = ref<number[]>([])
const appliedScheduleIds = ref<number[]>([])
const isFavorited = ref(false)
const activeDateFilter = ref<string>('all')
const showApplySheet = ref(false)
const showSuccess = ref(false)
const applying = ref(false)

const title = computed(() => job.value?.title || '')
const bannerImage = computed(() => job.value?.imageUrl || job.value?.coverImage || '')
const bannerStyle = computed(() => {
  if (bannerImage.value) {
    return { backgroundImage: 'url(' + bannerImage.value + ')' }
  }
  return {}
})
const companyName = computed(() => job.value?.companyName || '')
const locationText = computed(() => job.value?.location || '暂无地点')
const headcountText = computed(() => job.value?.headcount ? `${job.value.headcount}人` : '不限')
const deadlineText = computed(() => formatDateText(job.value?.deadline) || '长期有效')
const schedules = computed(() => Array.isArray(job.value?.schedules) ? job.value.schedules : [])
const heroEmoji = computed(() => {
  const t = title.value
  if (t.indexOf('外卖') >= 0 || t.indexOf('配送') >= 0) return '🛵'
  if (t.indexOf('餐饮') >= 0 || t.indexOf('服务') >= 0) return '🍽️'
  if (t.indexOf('快递') >= 0 || t.indexOf('分拣') >= 0) return '📦'
  if (t.indexOf('仓') >= 0) return '🏭'
  return '💼'
})

const bannerTags = computed(() => {
  const tags: string[] = []
  const rates = Array.isArray(job.value?.rates) ? job.value.rates : []
  if (rates.length > 0) {
    const type = rates[0]?.type
    const map: Record<string, string> = { HOURLY: '时结', DAILY: '日结', PIECEWORK: '计件', MONTHLY: '月结' }
    if (map[type]) tags.push(map[type])
  }
  if (job.value?.settlement?.length) {
    tags.push(...job.value.settlement.slice(0, 1))
  }
  if (Array.isArray(job.value?.tags) && job.value.tags.length > 0) {
    const tagNames = job.value.tags
      .filter((t: any) => t?.name)
      .map((t: any) => t.name)
      .slice(0, 2 - tags.length)
    tags.push(...tagNames)
  }
  return tags.slice(0, 2)
})

const jobTags = computed(() => {
  const tags: string[] = []
  if (Array.isArray(job.value?.tags) && job.value.tags.length > 0) {
    tags.push(...job.value.tags.filter((t: any) => t?.name).map((t: any) => t.name))
  }
  if (Array.isArray(job.value?.settlement) && job.value.settlement.length > 0) {
    tags.push(...job.value.settlement)
  }
  if (job.value?.jobType) tags.push(job.value.jobType)
  if (job.value?.experience) tags.push(job.value.experience)
  if (tags.length === 0) tags.push('日结', '包吃', '无需经验')
  return [...new Set(tags)].slice(0, 8)
})
const companyAuthStatus = computed(() => normalizeCompanyAuthStatus(job.value))
const companyAuthText = computed(() => {
  const map: Record<string, string> = { APPROVED: '已认证', PENDING: '认证中', REJECTED: '未认证', NONE: '未认证' }
  return map[companyAuthStatus.value] || '未认证'
})
const companyAuthClass = computed(() => companyAuthStatus.value === 'APPROVED' ? 'approved' : (companyAuthStatus.value === 'PENDING' ? 'pending' : ''))
const companyInitial = computed(() => '企')
const salaryText = computed(() => {
  const rates = Array.isArray(job.value?.rates) ? job.value.rates : []
  const rate = rates[0]
  if (rate?.amount) return `${rate.amount}元/${rateUnit(rate.type)}`
  if (job.value?.minRate && job.value?.maxRate) return `${job.value.minRate}-${job.value.maxRate}元/小时`
  if (job.value?.minRate) return `${job.value.minRate}元起/小时`
  return '薪资面议'
})
const settlementBadgeText = computed(() => {
  const rates = Array.isArray(job.value?.rates) ? job.value.rates : []
  const type = rates[0]?.type
  const map: Record<string, string> = { HOURLY: '时结工资', DAILY: '日结工资', PIECEWORK: '计件工资', MONTHLY: '月结工资' }
  return map[type] || '薪资面议'
})
const responsibilitiesHtml = computed(() => htmlContent(job.value?.responsibilities || job.value?.description))
const requirementsHtml = computed(() => htmlContent(job.value?.requirements))
const duties = computed(() => splitItems(job.value?.responsibilities || job.value?.description, ['按排班时间准时到岗，完成岗位工作', '服从现场安排，保障服务质量', '及时沟通异常情况']))
const needs = computed(() => splitItems(job.value?.requirements, ['身体健康，能适应岗位节奏', '责任心强，时间观念好', '会使用智能手机，沟通顺畅']))

const canApply = computed(() => {
  if (!job.value || job.value.status === 'CLOSED') return false
  return pendingScheduleIds.value.length > 0
})

const applyButtonText = computed(() => {
  if (!job.value) return '加载中'
  if (job.value.status === 'CLOSED') return '已关闭'
  if (pendingScheduleIds.value.length === 0) return '请先选择班次'
  return `立即报名（${pendingScheduleIds.value.length}班）`
})

const dateFilterOptions = computed(() => {
  const today = new Date()
  const options = [
    { key: 'all', label: '全部' },
    { key: formatDateKey(today), label: '今天' },
  ]
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  options.push({ key: formatDateKey(tomorrow), label: '明天' })
  for (let i = 2; i <= 6; i++) {
    const d = new Date(today)
    d.setDate(d.getDate() + i)
    const weekNames = ['日', '一', '二', '三', '四', '五', '六']
    options.push({ key: formatDateKey(d), label: `周${weekNames[d.getDay()]}` })
  }
  return options
})

const filteredSchedules = computed(() => {
  if (activeDateFilter.value === 'all') return schedules.value
  return schedules.value.filter((slot: any) => {
    const date = String(slot.date || '').slice(0, 10)
    return date === activeDateFilter.value
  })
})

const selectedScheduleList = computed(() => {
  return pendingScheduleIds.value.map((id) => {
    const slot = schedules.value.find((s: any) => Number(s.id) === id)
    return {
      id,
      date: scheduleDate(slot || {}),
      time: scheduleTime(slot || {}),
      pay: schedulePay(slot || {})
    }
  })
})

const selectedTotalAmount = computed(() => {
  let total = 0
  pendingScheduleIds.value.forEach((id) => {
    const slot = schedules.value.find((s: any) => Number(s.id) === id)
    if (slot) {
      const rates = Array.isArray(slot.rates) ? slot.rates : (job.value?.rates || [])
      const rate = rates[0]
      if (rate?.amount) {
        total += Number(rate.amount)
      } else if (slot.daySalary || slot.dailySalary) {
        total += Number(slot.daySalary || slot.dailySalary)
      }
    }
  })
  return total > 0 ? total : '--'
})

function formatDateKey(d: Date) {
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

function setDateFilter(key: string) {
  activeDateFilter.value = key
}

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

function scheduleCardClass(slot: any) {
  const id = Number(slot?.id)
  if (appliedScheduleIds.value.includes(id)) return 'applied'
  if (isScheduleFull(slot)) return 'full'
  if (pendingScheduleIds.value.includes(id)) return 'selected'
  return ''
}

function isScheduleApplied(id: number | string) {
  return appliedScheduleIds.value.includes(Number(id))
}

function isScheduleFull(slot: any) {
  if (isScheduleApplied(slot?.id)) return false
  const remaining = Number(slot?.remainingSlots ?? slot?.slotsAvailable)
  return Number.isFinite(remaining) && remaining <= 0
}

function toggleSchedule(slot: any) {
  const scheduleId = Number(slot?.id)
  if (!Number.isFinite(scheduleId)) return
  if (isScheduleApplied(scheduleId) || job.value?.status === 'CLOSED') return
  if (isScheduleFull(slot)) {
    uni.showToast({ title: '该班次已报满', icon: 'none' })
    return
  }
  const idx = pendingScheduleIds.value.indexOf(scheduleId)
  if (idx >= 0) {
    pendingScheduleIds.value.splice(idx, 1)
  } else {
    pendingScheduleIds.value.push(scheduleId)
  }
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

function formatSlotDay(dateStr: string) {
  if (!dateStr) return ''
  const parts = String(dateStr).split('-')
  return parts.length >= 3 ? parts[2] : dateStr.slice(-2)
}

function formatSlotMonth(dateStr: string) {
  if (!dateStr) return ''
  const parts = String(dateStr).split('-')
  return parts.length >= 2 ? String(Number(parts[1])) : ''
}

function schedulePay(slot: any) {
  const rates = Array.isArray(slot?.rates) ? slot.rates : (job.value?.rates || [])
  const rate = rates[0]
  if (rate?.amount) return `¥${rate.amount}/${rateUnit(rate.type)}`
  if (slot.daySalary || slot.dailySalary) return `¥${slot.daySalary || slot.dailySalary}/天`
  return salaryText.value
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

function normalizeCompanyAuthStatus(source: any) {
  const raw = source?.companyRealNameStatus || source?.companyAuthStatus || source?.enterpriseRealNameStatus || source?.enterpriseAuthStatus
  const status = raw ? String(raw).toUpperCase() : ''
  if (source?.companyRealNamed || source?.companyVerified || source?.enterpriseVerified || status === 'VERIFIED' || status === 'APPROVED') return 'APPROVED'
  if (status === 'PENDING' || status === 'REVIEWING') return 'PENDING'
  if (status === 'REJECTED') return 'REJECTED'
  return 'NONE'
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

function toggleFavorite() {
  if (!authStore.token) {
    openLoginSheet({ success: toggleFavorite })
    return
  }
  isFavorited.value = !isFavorited.value
  uni.showToast({ title: isFavorited.value ? '已收藏' : '已取消收藏', icon: 'none' })
}

async function handleApply() {
  if (!authStore.token) {
    openLoginSheet({ success: handleApply })
    return
  }
  if (pendingScheduleIds.value.length === 0) {
    uni.showToast({ title: '请先选择班次', icon: 'none' })
    return
  }
  if (jobId.value <= 0 || !job.value?.id) {
    uni.showToast({ title: '岗位信息无效', icon: 'none' })
    return
  }
  try {
    const completeness: any = await getProfileCompleteness()
    if (!completeness?.complete) {
      uni.showModal({
        title: '完善个人资料',
        content: '报名前需要先完善个人资料',
        confirmText: '去完善',
        success: (res) => {
          if (res.confirm) uni.navigateTo({ url: '/pages/profile/edit' })
        }
      })
      return
    }
  } catch {
    uni.showToast({ title: '资料校验失败，请稍后重试', icon: 'none' })
    return
  }
  showApplySheet.value = true
}

async function confirmApply() {
  if (applying.value) return
  applying.value = true
  try {
    await applyJob(jobId.value, { scheduleIds: pendingScheduleIds.value })
    showApplySheet.value = false
    showSuccess.value = true
    appliedScheduleIds.value = [...appliedScheduleIds.value, ...pendingScheduleIds.value]
    pendingScheduleIds.value = []
  } catch (err: any) {
    uni.showToast({ title: err?.data?.error || err?.message || '报名失败', icon: 'none' })
  } finally {
    applying.value = false
  }
}

function goToSchedule() {
  showSuccess.value = false
  uni.switchTab({ url: '/pages/index/index' })
}

function closeSuccessAndBack() {
  showSuccess.value = false
}

async function loadJob() {
  loading.value = true
  pendingScheduleIds.value = []
  if (jobId.value <= 0) {
    job.value = null
    appliedScheduleIds.value = []
    loading.value = false
    uni.showToast({ title: '岗位信息无效', icon: 'none' })
    return
  }
  try {
    const res: any = await getJobDetail(jobId.value)
    job.value = res || null
    appliedScheduleIds.value = Array.isArray(job.value?.appliedScheduleIds) ? job.value.appliedScheduleIds.map(Number) : []
  } catch {
    job.value = null
    appliedScheduleIds.value = []
    uni.showToast({ title: '岗位信息加载失败', icon: 'none' })
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
.detail-page {
  min-height: 100vh;
  background: #f5f6fa;
  position: relative;
}

.detail-banner {
  position: relative;
  height: 320rpx;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 55%, #169950 100%);
  padding: 60rpx 32rpx 32rpx;
  color: #fff;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.detail-banner::before {
  content: '';
  position: absolute;
  top: -80rpx;
  right: -40rpx;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}



.banner-info {
  position: relative;
  z-index: 5;
  padding-top: 20rpx;
}

.pay-badge {
  display: inline-block;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  background: #fff4ee;
  color: #ff6b35;
  font-size: 24rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.banner-title {
  display: block;
  font-size: 44rpx;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 8rpx;
}

.banner-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 12rpx;
}

.banner-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.25);
  font-size: 22rpx;
  font-weight: 500;
}

.banner-location {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 26rpx;
  opacity: 0.9;
}

.location-link {
  text-decoration: underline;
  font-size: 24rpx;
}

.banner-illustration {
  position: absolute;
  right: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 140rpx;
  height: 140rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
}

.job-info-card {
  background: #fff;
  margin: 30rpx 24rpx 16rpx;
  border-radius: 20rpx;
  padding: 28rpx 28rpx 24rpx;
  position: relative;
  z-index: 5;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.job-info-title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
  margin-bottom: 16rpx;
}

.job-info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.job-info-tag {
  font-size: 24rpx;
  padding: 8rpx 18rpx;
  border-radius: 16rpx;
  background: #fff3e8;
  color: #ff6b35;
  font-weight: 600;
}

.detail-content {
  height: calc(100vh - 140rpx);
}

.detail-card {
  margin: 20rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.05), 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.title-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #e6f8ee, #c6f0d8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.date-filter {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.date-chips {
  display: inline-flex;
  gap: 12rpx;
}

.date-chip {
  flex-shrink: 0;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  background: #f5f6fa;
  font-size: 24rpx;
  color: #666;
}

.date-chip.active {
  background: linear-gradient(135deg, #20c26b, #1aab5a);
  color: #fff;
  font-weight: 600;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.schedule-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 20rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 16rpx;
  background: #fff;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.schedule-card.applied {
  border-color: #e5e7eb;
  background: #f5f5f5;
  cursor: default;
}

.schedule-card.applied .sch-right {
  opacity: 0.7;
}

.schedule-card.selected {
  border-color: #20c26b;
  background: #f0fdf4;
}

.schedule-card.full {
  opacity: 0.55;
}

.sch-date-box {
  width: 88rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #ecfdf3;
  border-radius: 14rpx;
  padding: 14rpx 0;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
}

.sch-date-box.active {
  background: #20c26b;
}

.sch-date-box.active .sch-date-day,
.sch-date-box.active .sch-date-month {
  color: #fff;
}

.sch-date-day {
  font-size: 40rpx;
  font-weight: 800;
  color: #20c26b;
  line-height: 1;
}

.sch-date-month {
  font-size: 20rpx;
  color: #20c26b;
  margin-top: 6rpx;
}

.sch-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 8rpx;
  min-width: 0;
}

.sch-pay {
  font-size: 26rpx;
  font-weight: 700;
  color: #ff6b35;
  line-height: 1.2;
}

.sch-time {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 500;
}

.sch-corner-badge {
  position: absolute;
  top: 0;
  right: 0;
  font-size: 20rpx;
  padding: 6rpx 16rpx 6rpx 20rpx;
  border-radius: 0 14rpx 0 14rpx;
  background: #e5e7eb;
  color: #6b7280;
  font-weight: 500;
}

.sch-corner-badge.selected {
  background: #3b82f6;
  color: #fff;
}

.sch-corner-badge.applied {
  background: #20c26b;
  color: #fff;
}

.sch-corner-badge.full {
  background: #ef4444;
  color: #fff;
}

.sch-corner-badge.available {
  background: #f3f4f6;
  color: #9ca3af;
}

.schedule-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: linear-gradient(135deg, #eefbf3, #e8f8ee);
  border-radius: 12rpx;
  font-size: 26rpx;
}

.summary-left {
  color: #666;
}

.sum-num {
  color: #20c26b;
  font-weight: 700;
  font-size: 28rpx;
}

.summary-right {
  color: #666;
}

.sum-amount {
  color: #20c26b;
  font-weight: 800;
  font-size: 32rpx;
}

.empty-hint {
  text-align: center;
  padding: 40rpx 0;
  color: #999;
  font-size: 26rpx;
}

.location-card {
  cursor: pointer;
}

.loc-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.loc-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #e6f8ee, #c6f0d8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
}

.loc-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.loc-label {
  font-size: 24rpx;
  color: #999;
}

.loc-address {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loc-arrow {
  font-size: 26rpx;
  color: #20c26b;
  font-weight: 600;
  flex-shrink: 0;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.cloud-tag {
  padding: 10rpx 20rpx;
  border-radius: 10rpx;
  background: linear-gradient(135deg, #e6f8ee, #f0fdf4);
  font-size: 24rpx;
  color: #20c26b;
  font-weight: 500;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  font-size: 26rpx;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: #888;
}

.info-value {
  color: #333;
  font-weight: 500;
  max-width: 400rpx;
  text-align: right;
}

.duty-list {
  padding: 0;
}

.duty-item {
  display: flex;
  gap: 16rpx;
  padding: 10rpx 0;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
}

.duty-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #20c26b;
  flex-shrink: 0;
  margin-top: 16rpx;
}

.duty-dot.green {
  background: #20c26b;
}

.duty-dot.blue {
  background: #3b82f6;
}

.duty-text {
  flex: 1;
}

.rich-content {
  font-size: 26rpx;
  color: #555;
  line-height: 1.7;
}

.company-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.company-big-logo {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #20c26b, #1aab5a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.company-info-col {
  flex: 1;
  min-width: 0;
}

.company-name {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.company-auth-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.auth-badge {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: #f0f0f0;
  color: #999;
}

.auth-badge.approved {
  background: #e6f8ee;
  color: #20c26b;
}

.auth-badge.pending {
  background: #fff3e8;
  color: #f59e0b;
}

.company-desc {
  font-size: 24rpx;
  color: #999;
}

.bottom-safe {
  height: 40rpx;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 140rpx;
  padding: 20rpx 24rpx 40rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 16rpx;
  z-index: 50;
}

.action-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80rpx;
  gap: 4rpx;
}

.action-icon {
  font-size: 36rpx;
  line-height: 1;
}

.action-label {
  font-size: 20rpx;
  color: #888;
}

.apply-btn-main {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #2ad879 0%, #20c26b 40%, #1aab5a 100%);
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 10rpx 28rpx rgba(32, 194, 107, 0.45);
  margin: 0;
}

.apply-btn-main:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(32, 194, 107, 0.3);
}

.apply-btn-main::after {
  border: none;
}

.apply-btn-main.disabled {
  background: #d1d5db;
  color: #9ca3af;
  box-shadow: none;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}
</style>
