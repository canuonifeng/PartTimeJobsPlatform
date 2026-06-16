<template>
  <scroll-view scroll-y class="home-page">
    <view class="header">
      <view class="greeting-row">
        <view>
          <text class="greeting">{{ greeting }}，{{ authStore.workerInfo?.name || '工人' }}</text>
          <text class="date-line">{{ todayDate }} {{ todayWeekday }}</text>
        </view>
        <view class="shift-count">
          <text class="shift-count-num">{{ todayShifts.length }}</text>
          <text class="shift-count-text">今日班次</text>
        </view>
      </view>
    </view>

    <view class="content">
      <view class="stats-card">
        <view class="stat-item"><text class="stat-value">{{ formatNumber(stats.monthHours) }}</text><text class="stat-label">本月工时</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ formatNumber(stats.monthIncome) }}</text><text class="stat-label">本月收入</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ stats.attendanceDays }}</text><text class="stat-label">出勤天数</text></view>
      </view>

      <view v-if="!authStore.token" class="card login-card">
        <text class="empty-title">请先登录</text>
        <text class="empty-desc">登录后可以看到今日排班和签到状态</text>
        <view class="btn-primary" @click="openLoginSheet({ success: refreshHome })">去登录</view>
      </view>

      <template v-else>
        <view class="section-title-row">
          <text class="section-title">今日排班</text>
          <text class="section-subtitle">{{ todayShifts.length }} 个班次待处理</text>
        </view>

        <view v-if="todayShifts.length === 0" class="card login-card">
          <text class="empty-title">今日暂无排班</text>
          <text class="empty-desc">有新的排班会在这里显示</text>
        </view>

        <view v-for="shift in todayShifts" :key="shift.id" class="card shift-card">
          <view class="shift-header">
            <view class="job-left">
              <view class="job-icon"><text>岗</text></view>
          <view class="job-info">
            <text class="job-title">{{ shift.jobTitle }}</text>
            <view v-if="shift.location" class="location-row" @click="handleOpenLocation(shift)">
              <text class="job-location">{{ shift.location }}</text>
              <text class="map-link">导航</text>
            </view>
          </view>
            </view>
              <view class="status-badge" :class="displayStatusClass(shift)">{{ displayStatusLabel(shift) }}</view>
          </view>
          <view class="time-box">
            <view>
              <text class="time-label">工作时间</text>
              <text class="time-value">{{ shift.startTime }} - {{ shift.endTime }}</text>
            </view>
            <view class="date-box">
              <text class="date-day">{{ shift.date?.slice(8) }}</text>
              <text class="date-month">{{ shift.date?.slice(5, 7) }}月</text>
            </view>
          </view>
          <view class="record-row">
            <view class="record-item">
              <text class="record-label">签到</text>
              <text class="record-value">{{ shift.checkInTime || '未签到' }}</text>
            </view>
            <view class="record-item">
              <text class="record-label">签退</text>
              <text class="record-value">{{ shift.checkOutTime || '未签退' }}</text>
            </view>
          </view>
          <view class="shift-actions">
            <button class="action-btn check-in" :class="{ disabled: isActionDisabled(shift, 'in') }" :disabled="isActionDisabled(shift, 'in')" @click="handleCheckIn(shift)">签到</button>
            <button class="action-btn check-out" :class="{ disabled: isActionDisabled(shift, 'out') }" :disabled="isActionDisabled(shift, 'out')" @click="handleCheckOut(shift)">签退</button>
          </view>
        </view>

        <view class="card future-card">
          <view class="card-header">
            <text class="section-title">未来排班</text>
            <text class="more" @click="navTo('/pages/schedule/schedule')">全部</text>
          </view>
          <view class="future-list">
            <view v-if="futureShifts.length === 0" class="future-item">
              <text class="empty-desc">暂无未来排班</text>
            </view>
            <view v-for="shift in futureShifts.slice(0, 5)" :key="shift.id" class="future-item">
              <view class="future-date-box"><text class="future-day">{{ shift.date?.slice(8) }}</text><text class="future-month">{{ shift.date?.slice(5, 7) }}月</text></view>
              <view class="future-left"><text class="future-job">{{ shift.jobTitle }}</text><text class="future-location">{{ formatAddress(shift.location) }}</text></view>
              <view class="future-right"><text class="future-time">{{ shift.startTime }}</text><text class="future-time">{{ shift.endTime }}</text></view>
            </view>
          </view>
        </view>
      </template>
    </view>
  </scroll-view>
  <LoginSheet />
  <InviteFloat />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { getHomeStats, getHomeSchedules } from '@/api/home'
import { checkIn, checkOut } from '@/api/attendance'
import { getCheckInRadius } from '@/api/config'
import InviteFloat from '@/components/InviteFloat.vue'
import LoginSheet from '@/components/LoginSheet.vue'
import { openLoginSheet } from '@/utils/loginSheet'

const authStore = useAuthStore()
const stats = ref({ monthHours: 0, monthIncome: 0, attendanceDays: 0 })
const shifts = ref<Shift[]>([])
const checking = ref(false)
const submittingKey = ref('')
const countdown = ref('')
let countdownTimer: any = null
const currentLocation = ref<{ lat: number; lng: number } | null>(null)

type Shift = {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
  lat?: number
  lng?: number
  distance?: string
  checkInTime?: string
  checkOutTime?: string
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayDate = computed(() => {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}月${day}日`
})

const todayWeekday = computed(() => {
  return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][new Date().getDay()]
})

function formatDateKey(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatNumber(value: number) {
  return Number(value || 0).toLocaleString('zh-CN')
}

function normalizeCoordinate(value: any) {
  const coordinate = Number(value)
  return Number.isFinite(coordinate) ? coordinate : undefined
}

function normalizeShift(shift: any): Shift {
  return {
    id: Number(shift.id ?? shift.shiftId),
    jobTitle: shift.jobTitle || shift.title || shift.positionName || '排班',
    location: shift.locationName || shift.jobLocation || shift.location || shift.address || '',
    startTime: shift.startTime || '',
    endTime: shift.endTime || '',
    date: shift.date || shift.shiftDate || '',
    status: shift.status || 'SCHEDULED',
    lat: normalizeCoordinate(shift.lat ?? shift.latitude ?? shift.locationLat),
    lng: normalizeCoordinate(shift.lng ?? shift.longitude ?? shift.locationLng),
    distance: shift.distance || shift.distanceText,
    checkInTime: normalizeTime(shift.checkInTime || shift.clockInTime),
    checkOutTime: normalizeTime(shift.checkOutTime || shift.clockOutTime)
  }
}

function normalizeTime(value: any): string {
  const str = String(value || '')
  if (!str) return ''
  const timeMatch = str.match(/(\d{2}:\d{2})/)
  return timeMatch ? timeMatch[1] : str.slice(0, 5)
}

function parseShiftDateTime(date: string, time: string) {
  const [y = 0, m = 1, d = 1] = String(date).split('-').map(Number)
  const [h = 0, min = 0] = String(time).split(':').map(Number)
  return new Date(y, m - 1, d, h, min)
}

function isCheckedOutAfterShiftEnd(shift: Shift) {
  if (!shift.checkOutTime || !shift.date || !shift.endTime) return false
  return parseShiftDateTime(shift.date, shift.checkOutTime).getTime() >= parseShiftDateTime(shift.date, shift.endTime).getTime()
}


function handleOpenLocation(shift: Shift) {
  if (!shift.lat || !shift.lng) {
    uni.showToast({ title: '暂无经纬度，无法导航', icon: 'none' })
    return
  }
  uni.openLocation({
    latitude: shift.lat,
    longitude: shift.lng,
    name: shift.location || shift.jobTitle,
    address: shift.location
  })
}

function statusLabel(status?: string) {
  if (status === 'ON_DUTY') return '工作中'
  if (status === 'COMPLETED' || status === 'OFF_DUTY') return '已完成'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
  if (status === 'EARLY_LEAVE') return '早退'
  if (status === 'LATE_EARLY_LEAVE') return '迟到并早退'
  return '待上岗'
}

function displayStatusLabel(shift: Shift) {
  if (isAbnormalStatus(shift.status)) return statusLabel(shift.status)
  if (isCheckedOutAfterShiftEnd(shift)) return '已完成'
  return statusLabel(shift.status)
}

function displayStatusClass(shift: Shift) {
  if (isAbnormalStatus(shift.status)) return 'status-red'
  if (isCheckedOutAfterShiftEnd(shift)) return 'status-gray'
  return statusClass(shift.status)
}

function statusClass(status?: string) {
  if (status === 'ON_DUTY') return 'status-green'
  if (status === 'COMPLETED' || status === 'OFF_DUTY') return 'status-gray'
  if (isAbnormalStatus(status)) return 'status-red'
  return 'status-yellow'
}

function tipClass(shift: Shift) {
  if (isAbnormalStatus(shift.status)) return 'tip-red'
  if (shift.status === 'ON_DUTY') return 'tip-green'
  if (shift.status === 'COMPLETED' || shift.status === 'OFF_DUTY') return 'tip-gray'
  return 'tip-yellow'
}

function tipText(shift: Shift) {
  if (shift.status === 'LATE_EARLY_LEAVE') return '已记录迟到并早退，请留意考勤规则'
  if (isCheckedOutAfterShiftEnd(shift)) return isAbnormalStatus(shift.status) ? '已记录考勤异常，请留意考勤规则' : '本班次已完成，辛苦了'
  if (shift.status === 'ON_DUTY' || shift.status === 'LATE') return '已签到，完成工作后可在此签退'
  if (shift.status === 'COMPLETED' || shift.status === 'OFF_DUTY') return '本班次已完成，辛苦了'
  if (shift.status === 'EARLY_LEAVE') return '已记录早退，请留意考勤规则'
  if (shift.status === 'ABSENT') return '本班次已记录缺勤'
  const now = new Date()
  const start = parseShiftDateTime(shift.date, shift.startTime)
  const diffMs = start.getTime() - now.getTime()
  if (diffMs > 3600000) return '还未到签到时间，请提前规划路线'
  if (diffMs > 0) return `距离签到还有${countdown.value || '00:00:00'}`
  const lateMins = Math.max(0, Math.floor((now.getTime() - start.getTime()) / 60000))
  return lateMins > 0 ? `迟到${lateMins}分钟，点击签到` : '已到签到时间，请及时签到'
}

function canCheckIn(shift: Shift) {
  return !['ON_DUTY', 'OFF_DUTY', 'COMPLETED', 'ABSENT', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(shift.status)
}

function canCheckOut(shift: Shift) {
  if (isCheckedOutAfterShiftEnd(shift)) return false
  return shift.status === 'ON_DUTY' || shift.status === 'LATE' || shift.status === 'EARLY_LEAVE' || shift.status === 'LATE_EARLY_LEAVE'
}

function hasCheckedIn(shift: Shift) {
  return ['ON_DUTY', 'OFF_DUTY', 'COMPLETED', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(shift.status)
}

function hasCheckedOut(shift: Shift) {
  if (isCheckedOutAfterShiftEnd(shift)) return true
  return ['COMPLETED', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(shift.status)
}

function isAbnormalStatus(status?: string) {
  return status === 'ABSENT' || status === 'LATE' || status === 'EARLY_LEAVE' || status === 'LATE_EARLY_LEAVE'
}

function isActionDisabled(shift: Shift, type: 'in' | 'out') {
  if (submittingKey.value) return true
  if (type === 'in') return hasCheckedIn(shift) || hasCheckedOut(shift) || shift.status === 'ABSENT'
  return !canCheckOut(shift) || !hasCheckedIn(shift) || shift.status === 'ABSENT' || shift.status === 'OFF_DUTY' || shift.status === 'COMPLETED'
}

const sortedShifts = computed(() => {
  return [...shifts.value].sort((a, b) => {
    const dateDiff = String(a.date).localeCompare(String(b.date))
    if (dateDiff !== 0) return dateDiff
    return String(a.startTime).localeCompare(String(b.startTime))
  })
})

const todayShifts = computed(() => {
  const today = formatDateKey(new Date())
  return sortedShifts.value.filter((shift) => shift.date === today)
})

const futureShifts = computed(() => {
  const today = formatDateKey(new Date())
  return sortedShifts.value.filter((shift) => shift.date > today)
})

function startCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    const nextShift = todayShifts.value.find((shift) => shift.id > 0 && shift.status === 'SCHEDULED')
    if (!nextShift) {
      countdown.value = ''
      return
    }
    const start = parseShiftDateTime(nextShift.date, nextShift.startTime)
    const diff = start.getTime() - Date.now()
    if (diff <= 0) {
      countdown.value = ''
      return
    }
    const h = Math.floor(diff / 3600000)
    const m = Math.floor((diff % 3600000) / 60000)
    const s = Math.floor((diff % 60000) / 1000)
    countdown.value = `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }, 1000)
}

function calcDistance(lat1: number, lng1: number, lat2: number, lng2: number) {
  const R = 6371e3
  const φ1 = lat1 * Math.PI / 180
  const φ2 = lat2 * Math.PI / 180
  const Δφ = (lat2 - lat1) * Math.PI / 180
  const Δλ = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(Δφ / 2) ** 2 + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

function formatDistance(meters: number) {
  if (meters < 1000) return `${Math.round(meters)}米`
  return `${(meters / 1000).toFixed(meters < 10000 ? 1 : 0)}km`
}

async function getLocation(showError = true): Promise<{ lat: number; lng: number } | null> {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (pos: any) => {
        currentLocation.value = { lat: pos.latitude, lng: pos.longitude }
        resolve(currentLocation.value)
      },
      fail: () => {
        if (showError) uni.showToast({ title: '获取定位失败', icon: 'none' })
        resolve(null)
      }
    })
  })
}

async function checkDistance(shift: Shift): Promise<boolean> {
  const pos = await getLocation()
  if (!pos) return false
  const maxRadius = await getCheckInRadius()
  const workLat = shift.lat || 0
  const workLng = shift.lng || 0
  if (!workLat || !workLng) return true
  const dist = calcDistance(pos.lat, pos.lng, workLat, workLng)
  if (dist > maxRadius) {
    uni.showToast({ title: `超出签到距离${Math.round(dist)}米`, icon: 'none' })
    return false
  }
  return true
}

async function handleCheckIn(shift: Shift) {
  if (isActionDisabled(shift, 'in')) return
  const ok = await checkDistance(shift)
  if (!ok) return
  submittingKey.value = `${shift.id}:in`
  const now = new Date()
  const start = parseShiftDateTime(shift.date, shift.startTime)
  const isLate = now > start
  try {
    await checkIn({
      shiftId: shift.id,
      lat: currentLocation.value?.lat,
      lng: currentLocation.value?.lng
    })
    const target = shifts.value.find(s => s.id === shift.id)
    if (target) {
      target.status = isLate ? 'LATE' : 'ON_DUTY'
      target.checkInTime = normalizeTime(new Date().toTimeString())
    }
    if (isLate) {
      const lateMins = Math.round((now.getTime() - start.getTime()) / 60000)
      uni.showModal({ title: '提示', content: `已迟到${lateMins}分钟，签到成功`, showCancel: false })
    } else {
      uni.showToast({ title: '签到成功', icon: 'success' })
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签到失败', icon: 'none' })
  } finally {
    submittingKey.value = ''
  }
}

async function handleCheckOut(shift: Shift) {
  if (isActionDisabled(shift, 'out')) return
  const ok = await checkDistance(shift)
  if (!ok) return
  submittingKey.value = `${shift.id}:out`
  const now = new Date()
  const end = parseShiftDateTime(shift.date, shift.endTime)
  try {
    const res: any = await checkOut({
      shiftId: shift.id,
      lat: currentLocation.value?.lat,
      lng: currentLocation.value?.lng
    })
    const target = shifts.value.find(s => s.id === shift.id)
    let finalStatus = ''
    if (target) {
      const backendStatus = res?.status || ''
      if (backendStatus === 'COMPLETED') {
        target.status = 'OFF_DUTY'
        finalStatus = 'OFF_DUTY'
      } else if (backendStatus === 'EARLY_LEAVE') {
        target.status = 'EARLY_LEAVE'
        finalStatus = 'EARLY_LEAVE'
      } else if (backendStatus === 'LATE_EARLY_LEAVE') {
        target.status = 'LATE_EARLY_LEAVE'
        finalStatus = 'LATE_EARLY_LEAVE'
      } else if (backendStatus === 'LATE') {
        target.status = 'LATE'
        finalStatus = 'LATE'
      } else {
        const fallback = now < end ? 'EARLY_LEAVE' : 'OFF_DUTY'
        target.status = fallback
        finalStatus = fallback
      }
      target.checkOutTime = normalizeTime(res?.checkOutTime || new Date().toISOString())
    }
    const amount = Number(res?.payablePay || res?.scheduledPay || 0)
    const isAbnormal = isAbnormalStatus(finalStatus)
    if (amount <= 0) {
      uni.showToast({ title: '签退成功', icon: 'success' })
    } else if (res?.autoSettled) {
      uni.showModal({
        title: '薪资已到账',
        content: `已收到 ¥${amount.toFixed(2)} 薪资，去提现？`,
        confirmText: '去提现',
        cancelText: '不了',
        success: (modalRes) => {
          if (modalRes.confirm) {
            uni.navigateTo({ url: '/pages/earnings/earnings' })
          }
        }
      })
    } else if (isAbnormal) {
      uni.showModal({
        title: '结算确认',
        content: `本次${statusLabel(finalStatus)}，预计结算 ¥${amount.toFixed(2)}，请到收入明细确认`,
        confirmText: '去确认',
        cancelText: '知道了',
        success: (modalRes) => {
          if (modalRes.confirm) {
            uni.navigateTo({ url: '/pages/earnings/earnings' })
          }
        }
      })
    } else {
      uni.showToast({ title: '签退成功', icon: 'success' })
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签退失败', icon: 'none' })
  } finally {
    submittingKey.value = ''
  }
}

async function loadShifts() {
  if (!authStore.token) {
    shifts.value = []
    return
  }
  try {
    const res: any = await getHomeSchedules()
    const today = Array.isArray(res?.todayShifts) ? res.todayShifts : []
    const future = Array.isArray(res?.futureShifts) ? res.futureShifts : []
    shifts.value = [...today, ...future].map(normalizeShift)
  } catch {
    shifts.value = []
  }
}

async function loadStats() {
  if (!authStore.token) {
    stats.value = { monthHours: 0, monthIncome: 0, attendanceDays: 0 }
    return
  }
  try {
    const res: any = await getHomeStats()
    stats.value = {
      monthHours: Number(res?.monthHours || 0),
      monthIncome: Number(res?.monthIncome || 0),
      attendanceDays: Number(res?.attendanceDays || 0)
    }
  } catch {
    stats.value = { monthHours: 0, monthIncome: 0, attendanceDays: 0 }
  }
}

function navTo(url: string) {
  uni.navigateTo({ url })
}

function formatAddress(addr: string) {
  if (!addr) return '暂无地点'
  return addr.replace(/^(北京市|上海市|天津市|重庆市|.*?省|.*?市|.*?区|.*?县)/, '')
}

function openMap(shift: Shift) {
  if (!shift.lat || !shift.lng) {
    uni.showToast({ title: '暂无可导航坐标', icon: 'none' })
    return
  }
  uni.openLocation({
    latitude: shift.lat,
    longitude: shift.lng,
    address: shift.location,
    name: shift.location || shift.jobTitle || '工作地点',
    fail: () => uni.showToast({ title: '打开地图失败', icon: 'none' })
  })
}

function distanceLabel(shift: Shift) {
  if (shift.distance) return shift.distance

  if (shift.lat && shift.lng && currentLocation.value) return formatDistance(calcDistance(currentLocation.value.lat, currentLocation.value.lng, shift.lat, shift.lng))
  return ''
}

function stopCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = null
}

async function refreshHome() {
  await authStore.loadSession()
  if (!authStore.token) {
    stats.value = { monthHours: 0, monthIncome: 0, attendanceDays: 0 }
    shifts.value = []
    currentLocation.value = null
  } else {
    await Promise.all([loadStats(), loadShifts()])
    if (shifts.value.some((shift) => shift.lat && shift.lng)) await getLocation(false)
  }
  startCountdown()
}

onShow(refreshHome)
onHide(stopCountdown)
onUnload(stopCountdown)
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f6f8f7;
}

.header {
  background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%);
  padding: 56rpx 32rpx 150rpx;
  color: #fff;
  border-bottom-left-radius: 36rpx;
  border-bottom-right-radius: 36rpx;
}

.greeting-row,
.job-row,
.job-left,
.meta-item,
.card-header,
.future-item {
  display: flex;
  align-items: center;
}

.greeting-row,
.job-row,
.card-header,
.future-item {
  justify-content: space-between;
}

.greeting {
  display: block;
  font-size: 44rpx;
  font-weight: 800;
  margin-bottom: 16rpx;
}

.date-line {
  display: block;
  font-size: 28rpx;
  opacity: 0.9;
}

.shift-count {
  width: 130rpx;
  height: 130rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.shift-count-num {
  font-size: 46rpx;
  font-weight: 800;
  line-height: 54rpx;
}

.shift-count-text {
  font-size: 22rpx;
}

.content {
  padding: 0 28rpx 36rpx;
  margin-top: -96rpx;
}

.card,
.stats-card {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}

.stats-card {
  display: flex;
  align-items: center;
  padding: 32rpx 12rpx;
  margin-bottom: 30rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value,
.stat-label,
.empty-title,
.empty-desc,
.job-title,
.job-desc,
.meta-text,
.future-job,
.future-location,
.future-day,
.future-month,
.future-time {
  display: block;
}

.stat-value {
  font-size: 38rpx;
  color: #14231b;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #8a9690;
}

.stat-line {
  width: 1rpx;
  height: 46rpx;
  background: #edf1ef;
}

.card {
  padding: 30rpx;
  margin-bottom: 28rpx;
}

.login-card {
  text-align: center;
}

.btn-primary {
  width: 360rpx;
  height: 88rpx;
  margin: 0 auto;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #19c876 0%, #08a657 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 700;
  line-height: 88rpx;
  text-align: center;
  box-shadow: 0 14rpx 28rpx rgba(8, 166, 87, 0.24);
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin: 8rpx 4rpx 20rpx;
}

.section-title {
  font-size: 36rpx;
  color: #15231b;
  font-weight: 800;
}

.section-subtitle {
  font-size: 24rpx;
  color: #8c9892;
}

.shift-card {
  border: none;
  padding: 28rpx;
}

.shift-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.job-left {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0;
}

.job-icon {
  width: 64rpx;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  border-radius: 20rpx;
  background: #07c160;
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;
  margin-right: 18rpx;
}

.job-info {
  flex: 1;
  min-width: 0;
}

.shift-title {
  display: block;
  font-size: 32rpx;
  line-height: 42rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.shift-location {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 32rpx;
  color: #7b8794;
}

.location-row {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
}

.job-location {
  font-size: 24rpx;
  line-height: 32rpx;
  color: #7b8794;
}

.map-link {
  margin-left: 12rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: #ecfdf5;
  color: #0f9f5f;
  font-size: 24rpx;
}

.status-badge {
  margin-left: 18rpx;
  padding: 8rpx 18rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  line-height: 28rpx;
  white-space: nowrap;
}

.status-badge.status-yellow {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.status-green {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.status-gray {
  background: #f0fbf4;
  color: #07c160;
}

.status-badge.status-red {
  background: #fff1f0;
  color: #f5222d;
}

.time-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding: 22rpx 24rpx;
  border-radius: 18rpx;
  background: #f7fafc;
}

.time-label {
  display: block;
  font-size: 22rpx;
  color: #8b98a7;
}

.time-value {
  display: block;
  margin-top: 8rpx;
  font-size: 34rpx;
  line-height: 42rpx;
  font-weight: 700;
  color: #07c160;
}

.date-box {
  width: 86rpx;
  height: 86rpx;
  border-radius: 22rpx;
  background: #eafaf1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.date-day {
  font-size: 32rpx;
  line-height: 36rpx;
  color: #08a857;
  font-weight: 800;
}

.date-month {
  font-size: 22rpx;
  color: #58b987;
}

.record-row {
  display: flex;
  margin-top: 22rpx;
}

.record-item {
  flex: 1;
  padding: 18rpx 20rpx;
  border-radius: 16rpx;
  background: #fbfcfe;
}

.record-item:first-child {
  margin-right: 18rpx;
}

.record-label {
  display: block;
  font-size: 22rpx;
  color: #8b98a7;
}

.record-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.shift-actions {
  display: flex;
  margin-top: 24rpx;
  gap: 18rpx;
}

.action-btn {
  flex: 1;
  height: 76rpx;
  line-height: 76rpx;
  border-radius: 38rpx;
  font-size: 28rpx;
  border: none;
  text-align: center;
}

.action-btn.check-in {
  background: #07c160;
  color: #ffffff;
  animation: pulse-green 2s infinite;
}

.action-btn.check-out {
  background: #ffffff;
  color: #07c160;
  border: 1rpx solid #07c160;
  animation: pulse-border 2s infinite;
}

.action-btn.disabled {
  background: #eef1f5;
  color: #b7c0cc;
  border-color: #eef1f5;
  animation: none;
}

@keyframes pulse-green {
  0%, 100% { box-shadow: 0 0 0 0 rgba(7, 193, 96, 0.4); }
  50% { box-shadow: 0 0 0 12rpx rgba(7, 193, 96, 0); }
}

@keyframes pulse-border {
  0%, 100% { box-shadow: 0 0 0 0 rgba(7, 193, 96, 0.3); }
  50% { box-shadow: 0 0 0 12rpx rgba(7, 193, 96, 0); }
}

.empty-title {
  font-size: 34rpx;
  color: #15231b;
  font-weight: 800;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 27rpx;
  color: #8c9892;
  margin-bottom: 28rpx;
}

.more {
  font-size: 28rpx;
  color: #08a857;
  font-weight: 700;
}

.future-list {
  margin-top: 16rpx;
}

.future-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f3f1;
}

.future-item:last-child {
  border-bottom: none;
}

.future-date-box {
  width: 86rpx;
  height: 86rpx;
  border-radius: 22rpx;
  background: #eafaf1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.future-day {
  font-size: 32rpx;
  font-weight: 800;
  color: #08a857;
  line-height: 36rpx;
}

.future-month {
  font-size: 22rpx;
  color: #58b987;
}

.future-left {
  flex: 1;
}

.future-job {
  font-size: 30rpx;
  color: #18251e;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.future-location {
  font-size: 24rpx;
  color: #96a09b;
}

.future-right {
  text-align: right;
}

.future-time {
  font-size: 24rpx;
  color: #7c8882;
  line-height: 34rpx;
}
</style>
