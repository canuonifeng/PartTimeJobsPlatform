<template>
  <scroll-view class="home-page" scroll-y>
    <view class="header-section">
      <view class="greeting-bar">
        <view class="greeting-col">
          <text class="greeting-text">{{ greeting }}，{{ authStore.workerInfo?.name || '工人' }}</text>
          <text class="greeting-sub">{{ todayDate }} {{ todayWeekday }} · 今日 {{ todayShifts.length }} 个班次</text>
        </view>
        <view class="msg-icon" @click="goMessage">
          <text>💬</text>
        </view>
      </view>

      <view v-if="!authStore.token" class="login-hint">
        <text class="hint-title">登录后查看排班</text>
        <button class="hint-btn" @click="openLoginSheet({ success: refreshHome })">去登录</button>
      </view>
    </view>

    <view class="content-section">
      <view v-if="authStore.token && currentShift" class="shift-clock-card">
        <view class="scc-top" @click="openShiftDetail(currentShift)">
          <text class="scc-title">{{ currentShift.jobTitle }}</text>
          <view class="scc-meta">
            <text class="scc-time">🕐 {{ currentShift.startTime }}-{{ currentShift.endTime }}</text>
            <text class="scc-badge" :class="buttonState.type">{{ shiftStatusLabel(currentShift) }}</text>
          </view>
          <view class="scc-loc-row">
            <text class="scc-loc">{{ currentShift.location || '暂无地点' }}</text>
            <text v-if="currentShiftDistance" class="scc-dist">{{ currentShiftDistance }}</text>
          </view>
        </view>

        <view class="scc-divider"></view>

        <view class="scc-btn-area">
          <view
            class="scc-clock-btn"
            :class="buttonState.type"
            @click="handleClockAction"
          >
            <view class="scc-btn-pulse"></view>
            <text class="scc-btn-main">{{ buttonState.mainText }}</text>
            <text class="scc-btn-sub">{{ buttonState.subText }}</text>
            <view class="scc-btn-dist">
              <text>{{ buttonState.distText }}</text>
            </view>
          </view>
        </view>

        <view class="scc-tips">
          <text class="scc-tip">
            <text class="scc-dot" :class="positionOk ? 'ok' : 'warn'"></text>
            {{ positionOk ? '位置正常' : '位置异常' }}
          </text>
          <text class="scc-tip">
            <text class="scc-dot ok"></text>
            网络良好
          </text>
          <text class="scc-tip">
            <text class="scc-dot" :class="buttonState.timeStatus"></text>
            {{ buttonState.timeTip }}
          </text>
        </view>
      </view>

      <view v-if="!authStore.token" class="shift-clock-card">
        <view class="scc-btn-area">
          <view class="scc-clock-btn disabled-btn" @click="openLoginSheet({ success: refreshHome })">
            <text class="scc-btn-main">登录</text>
            <text class="scc-btn-sub">查看今日排班</text>
          </view>
        </view>
      </view>

      <view class="stats-row">
        <view class="stat-card">
          <text class="stat-num">{{ formatNumber(stats.monthHours) }}</text>
          <text class="stat-label">本月工时</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">¥{{ formatNumber(stats.monthIncome) }}</text>
          <text class="stat-label">本月收入</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ stats.attendanceDays }}</text>
          <text class="stat-label">出勤天数</text>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <text class="section-title">今日班次</text>
          <text class="section-more" @click="goSchedule">全部排班 →</text>
        </view>

        <view v-if="todayShifts.length === 0" class="empty-shifts">
          <text class="empty-text">今日暂无排班</text>
          <text class="empty-desc">有新的排班会在这里显示</text>
        </view>

        <view v-else class="shift-list">
          <view
            v-for="(shift, idx) in todayShifts"
            :key="shift.id"
            class="shift-item"
            :class="{ 'shift-current': isCurrentShift(shift), 'shift-done': isShiftDone(shift) }"
            @click="openShiftDetail(shift)"
          >
            <view class="shift-indicator" :class="shiftIndicatorClass(shift)"></view>
            <view class="shift-main">
              <view class="shift-top-row">
                <text class="shift-time">{{ shift.startTime }} - {{ shift.endTime }}</text>
                <text class="shift-status" :class="shiftStatusClass(shift)">{{ shiftStatusLabel(shift) }}</text>
              </view>
              <text class="shift-name">{{ shift.jobTitle }}</text>
              <view class="shift-bottom">
                <text class="shift-loc">{{ shift.location || '暂无地点' }}</text>
                <text v-if="distanceLabel(shift)" class="shift-dist">{{ distanceLabel(shift) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="authStore.token && futureShifts.length > 0" class="section-card">
        <view class="section-header">
          <text class="section-title">未来排班</text>
          <text class="section-more" @click="goSchedule">全部 →</text>
        </view>
        <view class="future-list">
          <view v-for="shift in futureShifts.slice(0, 5)" :key="shift.id" class="future-item">
            <view class="future-date-col">
              <text class="future-date-day">{{ futureDay(shift.date) }}</text>
              <text class="future-date-weekday">{{ futureWeekday(shift.date) }}</text>
            </view>
            <view class="future-info">
              <text class="future-job-title">{{ shift.jobTitle }}</text>
              <view class="future-meta-row">
                <text class="future-time-range">🕐 {{ shift.startTime }}-{{ shift.endTime }}</text>
                <text class="future-badge">待上岗</text>
              </view>
              <text v-if="shift.location" class="future-location">{{ shift.location }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="bottom-pad"></view>
    </view>

    <SuccessOverlay
      :visible="showSuccess"
      :title="successTitle"
      :description="successDesc"
      primary-text="知道了"
      icon="✅"
      @primary="showSuccess = false"
    />

    <LoginSheet />
    <InviteFloat />
  </scroll-view>
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
import SuccessOverlay from '@/components/SuccessOverlay.vue'
import { openLoginSheet } from '@/utils/loginSheet'

const authStore = useAuthStore()
const stats = ref({ monthHours: 0, monthIncome: 0, attendanceDays: 0 })
const shifts = ref<Shift[]>([])
const checking = ref(false)
const submittingKey = ref('')
const countdown = ref('')
let countdownTimer: any = null
const currentLocation = ref<{ lat: number; lng: number } | null>(null)
const showSuccess = ref(false)
const successTitle = ref('')
const successDesc = ref('')

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
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date().getDay()]
})

const positionOk = computed(() => {
  if (!currentShift.value) return true
  if (!currentLocation.value) return true
  return true
})

const currentShift = computed<Shift | null>(() => {
  if (todayShifts.value.length === 0) return null
  const now = new Date()
  const active = todayShifts.value.find(s => 
    ['ON_DUTY', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(s.status))
  if (active) return active
  const next = todayShifts.value.find(s => {
    if (s.status !== 'SCHEDULED') return false
    const end = parseShiftDateTime(s.date, s.endTime)
    return now < end
  })
  if (next) return next
  return todayShifts.value[0]
})

const currentShiftDistance = computed(() => {
  if (!currentShift.value) return ''
  return distanceLabel(currentShift.value)
})

const buttonState = computed(() => {
  const shift = currentShift.value
  if (!shift) {
    return { type: 'idle', mainText: '今日无班', subText: '好好休息', distText: '--', timeStatus: 'ok', timeTip: '休息中' }
  }
  const now = new Date()
  const start = parseShiftDateTime(shift.date, shift.startTime)
  const end = parseShiftDateTime(shift.date, shift.endTime)
  const status = shift.status

  if (status === 'ON_DUTY' || status === 'LATE' || status === 'EARLY_LEAVE' || status === 'LATE_EARLY_LEAVE') {
    const actualStart = shift.checkInTime
      ? parseShiftDateTime(shift.date, shift.checkInTime)
      : start
    const workedMs = now.getTime() - actualStart.getTime()
    const safeMs = Math.max(0, workedMs)
    const workedH = Math.floor(safeMs / 3600000)
    const workedM = Math.floor((safeMs % 3600000) / 60000)
    return {
      type: 'working',
      mainText: '签退',
      subText: `已工作 ${workedH}小时${workedM}分`,
      distText: currentShiftDistance.value || '位置正常',
      timeStatus: 'ok',
      timeTip: '工作中'
    }
  }

  if (status === 'COMPLETED' || status === 'OFF_DUTY' || isCheckedOutAfterShiftEnd(shift)) {
    return {
      type: 'done',
      mainText: '已完成 ✓',
      subText: '辛苦了',
      distText: '本班次已结束',
      timeStatus: 'ok',
      timeTip: '已完成'
    }
  }

  if (status === 'ABSENT') {
    return {
      type: 'absent',
      mainText: '缺勤',
      subText: '本班次已记录缺勤',
      distText: '--',
      timeStatus: 'warn',
      timeTip: '缺勤'
    }
  }

  const diffMs = start.getTime() - now.getTime()
  const dist = currentShiftDistance.value || '获取中'

  if (diffMs > 0) {
    const mins = Math.floor(diffMs / 60000)
    const hours = Math.floor(mins / 60)
    const remainText = hours > 0 ? `距上班还有${hours}小时${mins % 60}分` : `距上班还有${mins}分钟`
    return {
      type: 'ready',
      mainText: '签到',
      subText: remainText,
      distText: dist,
      timeStatus: 'info',
      timeTip: '提前打卡'
    }
  }

  const lateMs = now.getTime() - start.getTime()
  const lateMins = Math.floor(lateMs / 60000)
  if (lateMins > 0 && now < end) {
    return {
      type: 'late',
      mainText: '迟到签到',
      subText: `已迟到${lateMins}分钟`,
      distText: dist,
      timeStatus: 'warn',
      timeTip: '迟到'
    }
  }

  return {
    type: 'ready',
    mainText: '签到',
    subText: '已到签到时间',
    distText: dist,
    timeStatus: 'ok',
    timeTip: '准时打卡'
  }
})

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

function formatDateKey(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function futureDay(dateStr: string) {
  if (!dateStr) return '--'
  const d = String(dateStr)
  const parts = d.split('-')
  if (parts.length < 3) return d
  return `${Number(parts[1])}/${Number(parts[2])}`
}

function futureWeekday(dateStr: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
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

function isCurrentShift(shift: Shift) {
  return currentShift.value?.id === shift.id
}

function isShiftDone(shift: Shift) {
  return shift.status === 'COMPLETED' || shift.status === 'OFF_DUTY' || isCheckedOutAfterShiftEnd(shift)
}

function shiftIndicatorClass(shift: Shift) {
  if (isShiftDone(shift)) return 'done'
  if (isCurrentShift(shift)) return 'current'
  return 'pending'
}

function shiftStatusLabel(shift: Shift) {
  if (isCheckedOutAfterShiftEnd(shift)) return '已完成'
  return statusLabel(shift.status)
}

function shiftStatusClass(shift: Shift) {
  if (isShiftDone(shift)) return 'done'
  if (isAbnormalStatus(shift.status)) return 'abnormal'
  if (shift.status === 'ON_DUTY' || shift.status === 'LATE') return 'active'
  return 'pending'
}

function statusLabel(status?: string) {
  if (status === 'ON_DUTY') return '工作中'
  if (status === 'COMPLETED' || status === 'OFF_DUTY') return '已完成'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
  if (status === 'EARLY_LEAVE') return '早退'
  if (status === 'LATE_EARLY_LEAVE') return '迟到早退'
  return '待上岗'
}

function isAbnormalStatus(status?: string) {
  return status === 'ABSENT' || status === 'LATE' || status === 'EARLY_LEAVE' || status === 'LATE_EARLY_LEAVE'
}

function hasCheckedIn(shift: Shift) {
  return ['ON_DUTY', 'OFF_DUTY', 'COMPLETED', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(shift.status)
}

function hasCheckedOut(shift: Shift) {
  if (isCheckedOutAfterShiftEnd(shift)) return true
  return ['COMPLETED', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(shift.status)
}

function canCheckIn(shift: Shift) {
  return !['ON_DUTY', 'OFF_DUTY', 'COMPLETED', 'ABSENT', 'LATE', 'EARLY_LEAVE', 'LATE_EARLY_LEAVE'].includes(shift.status)
}

function canCheckOut(shift: Shift) {
  if (isCheckedOutAfterShiftEnd(shift)) return false
  return shift.status === 'ON_DUTY' || shift.status === 'LATE' || shift.status === 'EARLY_LEAVE' || shift.status === 'LATE_EARLY_LEAVE'
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

function distanceLabel(shift: Shift) {
  if (shift.distance) return shift.distance
  if (shift.lat && shift.lng && currentLocation.value) {
    return formatDistance(calcDistance(currentLocation.value.lat, currentLocation.value.lng, shift.lat, shift.lng))
  }
  return ''
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

async function handleClockAction() {
  const shift = currentShift.value
  if (!shift) return
  if (!authStore.token) {
    openLoginSheet({ success: refreshHome })
    return
  }
  if (buttonState.value.type === 'working') {
    await doCheckOut(shift)
  } else if (buttonState.value.type === 'done' || buttonState.value.type === 'absent') {
    return
  } else {
    await doCheckIn(shift)
  }
}

async function doCheckIn(shift: Shift) {
  if (hasCheckedIn(shift)) return
  const ok = await checkDistance(shift)
  if (!ok) return
  checking.value = true
  try {
    const res: any = await checkIn({
      shiftId: shift.id,
      lat: currentLocation.value?.lat,
      lng: currentLocation.value?.lng
    })
    const target = shifts.value.find(s => s.id === shift.id)
    if (target) {
      const status = res?.status || ''
      target.status = status && status !== 'SCHEDULED' ? status : 'ON_DUTY'
      target.checkInTime = normalizeTime(res?.checkInTime || new Date().toTimeString())
    }
    successTitle.value = '签到成功'
    const checkInTime = res?.checkInTime ? normalizeTime(res.checkInTime) : normalizeTime(new Date().toTimeString())
    const late = statusLabel(res?.status) === '迟到'
    successDesc.value = late
      ? `签到时间 ${checkInTime}（迟到）\n开始工作，加油！`
      : `签到时间 ${checkInTime}\n开始工作，加油！`
    showSuccess.value = true
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签到失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}

async function doCheckOut(shift: Shift) {
  if (!canCheckOut(shift)) return
  const ok = await checkDistance(shift)
  if (!ok) return
  checking.value = true
  try {
    const res: any = await checkOut({
      shiftId: shift.id,
      lat: currentLocation.value?.lat,
      lng: currentLocation.value?.lng
    })
    const target = shifts.value.find(s => s.id === shift.id)
    if (target) {
      const backendStatus = res?.status || ''
      if (backendStatus === 'COMPLETED') target.status = 'OFF_DUTY'
      else if (backendStatus === 'EARLY_LEAVE') target.status = 'EARLY_LEAVE'
      else if (backendStatus === 'LATE_EARLY_LEAVE') target.status = 'LATE_EARLY_LEAVE'
      else target.status = backendStatus || 'OFF_DUTY'
      target.checkOutTime = normalizeTime(res?.checkOutTime || new Date().toISOString())
    }
    successTitle.value = '签退成功'
    const checkOutTime = res?.checkOutTime ? normalizeTime(res.checkOutTime) : normalizeTime(new Date().toTimeString())
    successDesc.value = `签退时间 ${checkOutTime}\n辛苦了，今天干得不错！`
    showSuccess.value = true
    const amount = Number(res?.payablePay || res?.scheduledPay || 0)
    if (amount > 0) {
      setTimeout(() => {
        const settled = res?.autoSettled === true
        uni.showModal({
          title: settled ? '薪资已结算' : '薪资已计算',
          content: settled
            ? `本次 ¥${amount.toFixed(2)} 已到账，请到收入明细查看`
            : `本次预计 ¥${amount.toFixed(2)}，待企业结算后到账`,
          confirmText: '去查看',
          cancelText: '知道了',
          success: (modalRes) => {
            if (modalRes.confirm) {
              uni.navigateTo({ url: '/pages/earnings/earnings' })
            }
          }
        })
      }, 500)
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签退失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}

function openShiftDetail(shift: Shift) {
  uni.navigateTo({ url: `/pages/attendance/clockIn?shiftId=${shift.id}` })
}

function goSchedule() {
  uni.navigateTo({ url: '/pages/schedule/schedule' })
}

function goClockInRecord() {
  uni.navigateTo({ url: '/pages/attendance/clockIn' })
}

function goMessage() {
  uni.switchTab({ url: '/pages/message/message' })
}

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

function stopCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = null
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
  height: 100vh;
  background: #f5f6fa;
}

.header-section {
  position: relative;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 55%, #169950 100%);
  padding: 80rpx 32rpx 80rpx;
  color: #fff;
  overflow: hidden;
}

.header-section::before {
  content: '';
  position: absolute;
  top: -120rpx;
  right: -60rpx;
  width: 340rpx;
  height: 340rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.header-section::after {
  content: '';
  position: absolute;
  bottom: 40rpx;
  left: -80rpx;
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.greeting-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28rpx;
}

.greeting-col {
  display: flex;
  flex-direction: column;
}

.greeting-text {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 1.3;
}

.greeting-sub {
  font-size: 26rpx;
  opacity: 0.85;
  margin-top: 6rpx;
}

.msg-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  position: relative;
}

.shift-glass-card {
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.glass-time {
  display: block;
  font-size: 44rpx;
  font-weight: 800;
  text-align: center;
  margin-bottom: 8rpx;
}

.glass-title {
  display: block;
  font-size: 28rpx;
  text-align: center;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.glass-loc {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16rpx;
  font-size: 24rpx;
  opacity: 0.9;
}

.glass-dist {
  padding: 2rpx 12rpx;
  border-radius: 12rpx;
  background: rgba(255, 255, 255, 0.25);
  font-size: 22rpx;
}

.login-hint {
  text-align: center;
  padding: 40rpx 0;
}

.hint-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.hint-btn {
  width: 240rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 28rpx;
  border: none;
  margin: 0 auto;
}

.hint-btn::after {
  border: none;
}

.content-section {
  margin-top: -40rpx;
  padding: 0 24rpx;
  position: relative;
  z-index: 10;
}

.shift-clock-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.scc-top {
  cursor: pointer;
}

.scc-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
  margin-bottom: 12rpx;
}

.scc-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.scc-time {
  font-size: 26rpx;
  color: #6b7280;
}

.scc-badge {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.scc-badge.ready,
.scc-badge.idle {
  background: #f3f4f6;
  color: #9ca3af;
}

.scc-badge.working {
  background: #e6f8ee;
  color: #20c26b;
}

.scc-badge.done {
  background: #e6f8ee;
  color: #20c26b;
}

.scc-badge.late {
  background: #fef3c7;
  color: #d97706;
}

.scc-badge.absent {
  background: #fee2e2;
  color: #ef4444;
}

.scc-loc-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.scc-loc {
  font-size: 24rpx;
  color: #999;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scc-dist {
  font-size: 22rpx;
  color: #20c26b;
  font-weight: 500;
  flex-shrink: 0;
}

.scc-divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 24rpx 0;
}

.scc-btn-area {
  display: flex;
  justify-content: center;
}

.scc-clock-btn {
  position: relative;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #2ad879 0%, #20c26b 40%, #1aab5a 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 20rpx 0 20rpx;
  box-shadow: 0 16rpx 40rpx rgba(32, 194, 107, 0.35),
              0 6rpx 16rpx rgba(0, 0, 0, 0.08),
              inset 0 -8rpx 20rpx rgba(0, 0, 0, 0.1),
              inset 0 4rpx 12rpx rgba(255, 255, 255, 0.2);
}

.scc-clock-btn::after {
  content: '';
  position: absolute;
  top: 10rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 140rpx;
  height: 10rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  filter: blur(3rpx);
}

.scc-clock-btn.ready {
  background: linear-gradient(145deg, #2ad879 0%, #20c26b 40%, #1aab5a 100%);
}

.scc-clock-btn.working {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  box-shadow: 0 16rpx 40rpx rgba(59, 130, 246, 0.35),
              inset 0 -8rpx 20rpx rgba(0, 0, 0, 0.12),
              inset 0 8rpx 20rpx rgba(255, 255, 255, 0.15);
}

.scc-clock-btn.done {
  background: linear-gradient(135deg, #8de6b0 0%, #20c26b 100%);
  opacity: 0.85;
}

.scc-clock-btn.late {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 16rpx 40rpx rgba(245, 158, 11, 0.35),
              inset 0 -8rpx 20rpx rgba(0, 0, 0, 0.12),
              inset 0 8rpx 20rpx rgba(255, 255, 255, 0.15);
}

.scc-clock-btn.absent {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.scc-clock-btn.disabled-btn {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
}

.scc-clock-btn:active {
  transform: scale(0.96);
}

.scc-btn-pulse {
  position: absolute;
  top: -16rpx;
  left: -16rpx;
  right: -16rpx;
  bottom: -16rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(32, 194, 107, 0.3);
  animation: sccPulse 2s ease-in-out infinite;
}

.scc-clock-btn.working .scc-btn-pulse {
  border-color: rgba(59, 130, 246, 0.25);
}

.scc-clock-btn.late .scc-btn-pulse {
  border-color: rgba(245, 158, 11, 0.25);
}

@keyframes sccPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.08);
    opacity: 0.3;
  }
}

.scc-btn-main {
  font-size: 44rpx;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.scc-btn-sub {
  font-size: 22rpx;
  opacity: 0.9;
  margin-bottom: 12rpx;
}

.scc-btn-dist {
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.25);
  font-size: 20rpx;
}

.scc-tips {
  display: flex;
  justify-content: center;
  gap: 24rpx;
  margin-top: 20rpx;
}

.scc-tip {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #888;
}

.scc-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #20c26b;
}

.scc-dot.ok {
  background: #20c26b;
}

.scc-dot.warn {
  background: #ef4444;
}

.scc-dot.info {
  background: #3b82f6;
}

.stats-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.06), 0 2rpx 6rpx rgba(0, 0, 0, 0.03);
}

.stat-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 22rpx;
  color: #888;
}

.section-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.05), 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.section-more {
  font-size: 24rpx;
  color: #20c26b;
}

.empty-shifts {
  text-align: center;
  padding: 40rpx 0;
}

.empty-text {
  display: block;
  font-size: 28rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #bbb;
}

.shift-list {
  display: flex;
  flex-direction: column;
}

.shift-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  position: relative;
}

.shift-item:last-child {
  border-bottom: none;
}

.shift-item.shift-done {
  opacity: 0.6;
}

.shift-indicator {
  width: 6rpx;
  border-radius: 4rpx;
  flex-shrink: 0;
  margin: 6rpx 0;
  background: #ddd;
}

.shift-indicator.current {
  background: linear-gradient(180deg, #20c26b, #1aab5a);
}

.shift-indicator.done {
  background: #20c26b;
  opacity: 0.5;
}

.shift-indicator.pending {
  background: #e5e7eb;
}

.shift-main {
  flex: 1;
  min-width: 0;
}

.shift-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.shift-time {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.shift-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.shift-status.active {
  background: #e6f8ee;
  color: #20c26b;
}

.shift-status.pending {
  background: #f3f4f6;
  color: #9ca3af;
}

.shift-status.done {
  background: #f3f4f6;
  color: #9ca3af;
}

.shift-status.abnormal {
  background: #fee2e2;
  color: #ef4444;
}

.shift-name {
  display: block;
  font-size: 26rpx;
  color: #555;
  margin-bottom: 6rpx;
}

.shift-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shift-loc {
  font-size: 24rpx;
  color: #999;
}

.shift-dist {
  font-size: 22rpx;
  color: #20c26b;
  font-weight: 500;
}

.entry-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.entry-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.entry-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.entry-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.entry-arrow {
  font-size: 36rpx;
  color: #ccc;
}

.future-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.future-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  border-radius: 14rpx;
  background: #fafbfc;
  border: 1rpx solid #f0f0f0;
}

.future-date-col {
  width: 80rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #eefbf3;
  border-radius: 12rpx;
  padding: 10rpx 0;
}

.future-date-day {
  font-size: 32rpx;
  font-weight: 800;
  color: #20c26b;
  line-height: 1.2;
}

.future-date-weekday {
  font-size: 20rpx;
  color: #20c26b;
  margin-top: 4rpx;
}

.future-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.future-job-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.future-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.future-time-range {
  font-size: 24rpx;
  color: #666;
}

.future-badge {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
  background: #f3f4f6;
  color: #9ca3af;
  font-weight: 500;
  flex-shrink: 0;
}

.future-location {
  font-size: 22rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bottom-pad {
  height: 120rpx;
}
</style>
