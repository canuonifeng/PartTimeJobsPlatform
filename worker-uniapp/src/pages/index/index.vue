<template>
  <view class="home-page">
    <view class="header">
      <view class="greeting-row">
        <text class="greeting">{{ greeting }}，{{ authStore.workerInfo?.name || '工人' }}</text>
      </view>
      <view class="date-row">
        <text class="date">{{ todayDate }}</text>
        <text class="weekday">{{ todayWeekday }}</text>
        <text class="shift-count">{{ todayShifts.length === 0 ? '今日无班次' : '今日 ' + todayShifts.length + ' 个班次' }}</text>
      </view>
    </view>

    <view v-if="!authStore.token" class="card">
      <text class="empty-title">请先登录</text>
      <text class="empty-desc">登录后可以看到今日排班和签到状态</text>
      <button class="btn-primary" @click="navTo('/pages/login/login')">去登录</button>
    </view>

    <template v-else>
      <view v-if="currentShift" class="card current-shift-card">
        <view class="job-row">
          <text class="job-title">{{ currentShift.jobTitle }}</text>
          <view class="status-tag" :class="statusClass(currentShift.status)">
            {{ statusLabel(currentShift.status) }}
          </view>
        </view>
        <view class="shift-meta">
          <view class="meta-item">
            <uni-icons type="calendar" size="18" color="#07c160"></uni-icons>
            <text class="meta-text">{{ currentShift.date }} {{ currentShift.startTime }} - {{ currentShift.endTime }}</text>
          </view>
          <view class="meta-item location-item" @click="openMap(currentShift.location)">
            <uni-icons type="location" size="18" color="#07c160"></uni-icons>
            <text class="meta-text">{{ formatAddress(currentShift.location) }}</text>
            <text class="location-arrow">›</text>
          </view>
        </view>

        <view v-if="checkTip" class="tip" :class="checkTip.type === 'warn' ? 'tip-warn' : checkTip.type === 'late' ? 'tip-late' : checkTip.type === 'countdown' ? 'tip-countdown' : 'tip-info'">
            {{ checkTip.text }}
        </view>

        <view v-if="canCheckIn" class="btn-primary" @click="handleCheckIn">签到</view>
        <view v-else-if="canCheckOut" class="btn-primary" @click="handleCheckOut">签退</view>
        <view v-else-if="!checkTip" class="btn-disabled">
          {{ statusLabel(currentShift.status) }}
        </view>
      </view>

      <view v-else class="card empty-shift-card">
        <text class="empty-icon">📅</text>
        <text class="empty-text">今日暂无排班</text>
        <button class="btn-secondary" @click="navTo('/pages/schedule/schedule')">查看排班</button>
      </view>

      <view class="card future-card">
        <view class="card-header">
          <text class="card-title">我的未来排班</text>
          <text class="more" @click="navTo('/pages/schedule/schedule')">全部 ›</text>
        </view>

        <view v-if="futureShifts.length" class="future-list">
          <view v-for="shift in futureShifts.length > 1 ? futureShifts.slice(1, 6) : futureShifts" :key="shift.id" class="future-item">
            <view class="future-left">
              <text class="future-job">{{ shift.jobTitle }}</text>
              <text class="future-location">{{ shift.location || '暂无地点' }}</text>
            </view>
            <view class="future-right">
              <text class="future-date">{{ shift.date?.slice(5) }}</text>
              <text class="future-time">{{ shift.startTime }}-{{ shift.endTime }}</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-future">
          <text class="empty-text">暂无未来排班</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { getMyShifts } from '@/api/schedule'
import { checkIn, checkOut } from '@/api/attendance'

const authStore = useAuthStore()
const shifts = ref<any[]>([])
const checking = ref(false)

type Shift = {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
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

function normalizeShift(shift: any): Shift {
  return {
    id: shift.id ?? shift.shiftId,
    jobTitle: shift.jobTitle || shift.title || '排班',
    location: shift.jobLocation || shift.location || shift.locationName || '',
    startTime: shift.startTime || '',
    endTime: shift.endTime || '',
    date: shift.date || shift.shiftDate || '',
    status: shift.status || 'SCHEDULED'
  }
}

function statusLabel(status?: string) {
  if (status === 'ON_DUTY') return '工作中'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
  if (status === 'EARLY_LEAVE') return '早退'
  return '待上岗'
}

function statusClass(status?: string) {
  if (status === 'ON_DUTY') return 'status-in'
  if (status === 'OFF_DUTY') return 'status-out'
  if (status === 'ABSENT' || status === 'LATE' || status === 'EARLY_LEAVE') return 'status-danger'
  return 'status-wait'
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

const currentShift = computed(() => todayShifts.value[0] || null)

const countdown = ref('')
let countdownTimer: any = null

const checkTip = computed(() => {
  if (!currentShift.value) return null
  if (currentShift.value.status === 'ON_DUTY' || currentShift.value.status === 'LATE') {
    return { type: 'info', text: '工作中，可点击签退' }
  }
  if (currentShift.value.status === 'COMPLETED' || currentShift.value.status === 'EARLY_LEAVE' || currentShift.value.status === 'ABSENT') {
    return null
  }
  // SCHEDULED状态 - 待上岗
  const now = new Date()
  const start = new Date(`${currentShift.value.date} ${currentShift.value.startTime}`)
  const diffMs = start.getTime() - now.getTime()
  if (diffMs > 3600000) return { type: 'warn', text: '还未到签到时间' }
  if (diffMs > 0) return { type: 'countdown', text: `距离签到还有${countdown.value}` }
  const lateMins = Math.floor((now.getTime() - start.getTime()) / 60000)
  return { type: 'late', text: `迟到${lateMins}分钟，点击签到` }
})

const canCheckIn = computed(() => {
  const s = currentShift.value
  if (!s) return false
  if (s.status === 'ON_DUTY' || s.status === 'OFF_DUTY' || s.status === 'ABSENT' || s.status === 'LATE' || s.status === 'EARLY_LEAVE') return false
  return true
})

const canCheckOut = computed(() => {
  return currentShift.value?.status === 'ON_DUTY'
})

function startCountdown() {
  clearInterval(countdownTimer)
  if (!currentShift.value || currentShift.value.status === 'ON_DUTY') return
  const start = new Date(`${currentShift.value.date} ${currentShift.value.startTime}`)
  countdownTimer = setInterval(() => {
    const diff = start.getTime() - Date.now()
    if (diff <= 0) {
      clearInterval(countdownTimer)
      countdown.value = ''
      return
    }
    const h = Math.floor(diff / 3600000)
    const m = Math.floor((diff % 3600000) / 60000)
    const s = Math.floor((diff % 60000) / 1000)
    countdown.value = `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }, 1000)
}

function parseTime(timeStr: string) {
  const [h, m] = timeStr.split(':').map(Number)
  return { h, m }
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

let currentLocation: { lat: number; lng: number } | null = null

async function getLocation(): Promise<{ lat: number; lng: number } | null> {
  try {
    const pos: any = await uni.getLocation({ type: 'gcj02' })
    currentLocation = { lat: pos.latitude, lng: pos.longitude }
    return currentLocation
  } catch {
    uni.showToast({ title: '获取定位失败', icon: 'none' })
    return null
  }
}

async function checkDistance(): Promise<boolean> {
  const pos = await getLocation()
  if (!pos) return false
  // 实际项目中这里应拉取企业设置的签到点和距离，这里简化
  const checkDistance = 100
  const workLat = currentShift.value?.lat || 0
  const workLng = currentShift.value?.lng || 0
  if (!workLat || !workLng) return true
  const dist = calcDistance(pos.lat, pos.lng, workLat, workLng)
  if (dist > checkDistance) {
    uni.showToast({ title: `超出签到距离${Math.round(dist)}米`, icon: 'none' })
    return false
  }
  return true
}

async function handleCheckIn() {
  if (!currentShift.value || checking.value || !canCheckIn.value) return
  const ok = await checkDistance()
  if (!ok) return
  const now = new Date()
  const start = new Date(`${currentShift.value.date} ${currentShift.value.startTime}`)
  const isLate = now > start
  checking.value = true
  try {
    await checkIn({
      shiftId: currentShift.value.id,
      lat: currentLocation?.lat,
      lng: currentLocation?.lng
    })
    if (isLate) {
      const lateMins = Math.round((now.getTime() - start.getTime()) / 60000)
      uni.showModal({ title: '提示', content: `已迟到${lateMins}分钟，签到成功`, showCancel: false })
    } else {
      uni.showToast({ title: '签到成功', icon: 'success' })
    }
    const shift = shifts.value.find(s => s.id === currentShift.value.id)
    if (shift) shift.status = isLate ? 'LATE' : 'ON_DUTY'
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签到失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}

async function handleCheckOut() {
  if (!currentShift.value || checking.value || !canCheckOut.value) return
  const ok = await checkDistance()
  if (!ok) return
  const now = new Date()
  const end = new Date(`${currentShift.value.date} ${currentShift.value.endTime}`)
  const isEarly = now < end
  checking.value = true
  try {
    await checkOut({
      shiftId: currentShift.value.id,
      lat: currentLocation?.lat,
      lng: currentLocation?.lng
    })
    if (isEarly) {
      const earlyMins = Math.round((end.getTime() - now.getTime()) / 60000)
      uni.showModal({ title: '提示', content: `提前${earlyMins}分钟签退，确认成功`, showCancel: false })
    } else {
      uni.showToast({ title: '签退成功', icon: 'success' })
    }
    const shift = shifts.value.find(s => s.id === currentShift.value.id)
    if (shift) shift.status = isEarly ? 'EARLY_LEAVE' : 'OFF_DUTY'
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签退失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}

async function loadShifts() {
  if (!authStore.token) return
  try {
    const startDate = formatDateKey(new Date())
    const endDate = formatDateKey(new Date(Date.now() + 6 * 24 * 60 * 60 * 1000))
    const res: any = await getMyShifts({ startDate, endDate })
    const list = Array.isArray(res) ? res : (res.list || [])
    shifts.value = list.map(normalizeShift)
  } catch {
  }
}

function navTo(url: string) {
  uni.navigateTo({ url })
}

function formatAddress(addr: string) {
  if (!addr) return '暂无地点'
  return addr.replace(/^(北京市|上海市|天津市|重庆市|.*?省|.*?市|.*?区|.*?县)/, '')
}

function openMap(addr: string) {
  if (!addr) return
  uni.openLocation({
    address: addr,
    name: '工作地点',
    fail: () => uni.showToast({ title: '打开地图失败', icon: 'none' })
  })
}

async function refreshHome() {
  await authStore.loadSession()
  await loadShifts()
  startCountdown()
}

onMounted(refreshHome)
onShow(refreshHome)
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
}

.header {
  background: linear-gradient(135deg, #07c160, #059d50);
  margin: -30rpx -30rpx 30rpx;
  padding: 50rpx 30rpx 40rpx;
  color: #fff;
}

.greeting-row {
  margin-bottom: 16rpx;
}

.greeting {
  font-size: 44rpx;
  font-weight: 700;
}

.date-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  font-size: 28rpx;
  opacity: 0.95;
}

.shift-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 36rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.current-shift-card {
  border-left: 8rpx solid #07c160;
}

.job-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.job-title {
  flex: 1;
  font-size: 44rpx;
  font-weight: 800;
  color: #111;
  line-height: 1.2;
  margin-right: 20rpx;
}

.status-tag {
  flex-shrink: 0;
  padding: 10rpx 24rpx;
  border-radius: 28rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.status-wait {
  background: #fff7e6;
  color: #d97706;
}

.status-in {
  background: #e8f3ff;
  color: #2563eb;
}

.status-out {
  background: #ecfdf5;
  color: #059669;
}

.status-danger {
  background: #fef2f2;
  color: #dc2626;
}

.shift-meta {
  margin-bottom: 28rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  cursor: pointer;
}

.meta-text {
  flex: 1;
  font-size: 30rpx;
  color: #555;
}

.location-item {
  background: #f0fdf4;
  margin: 0 -8rpx;
  padding: 12rpx 8rpx;
  border-radius: 12rpx;
}

.location-arrow {
  font-size: 36rpx;
  color: #07c160;
  font-weight: 500;
}

.tip {
  padding: 20rpx 24rpx;
  border-radius: 14rpx;
  font-size: 28rpx;
  margin-bottom: 28rpx;
}

.tip-warn {
  background: #fffbeb;
  color: #d97706;
}

.tip-info {
  background: #f0f9ff;
  color: #0369a1;
}

.tip-late {
  background: #fef2f2;
  color: #dc2626;
}

.tip-countdown {
  background: #f0fdf4;
  color: #16a34a;
  font-weight: 600;
}

.btn-primary,
.btn-secondary,
.btn-disabled {
  width: 100%;
  height: 100rpx;
  line-height: 100rpx;
  text-align: center;
  border-radius: 50rpx;
  font-size: 34rpx;
  font-weight: 600;
}

.btn-primary {
  background: #07c160;
  color: #fff;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
}

.btn-disabled {
  background: #e5e5e5;
  color: #999;
}

.empty-shift-card {
  text-align: center;
  padding: 60rpx 36rpx;
}

.empty-icon {
  display: block;
  font-size: 100rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  display: block;
  font-size: 30rpx;
  color: #999;
  margin-bottom: 28rpx;
}

.empty-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 16rpx;
}

.empty-desc {
  display: block;
  font-size: 28rpx;
  color: #999;
  margin-bottom: 28rpx;
}

.future-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

.more {
  font-size: 28rpx;
  color: #07c160;
}

.future-list {
  padding-top: 10rpx;
}

.future-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.future-item:last-child {
  border-bottom: none;
}

.future-left {
  flex: 1;
}

.future-job {
  display: block;
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.future-location {
  font-size: 26rpx;
  color: #999;
}

.future-right {
  text-align: right;
  flex-shrink: 0;
}

.future-date {
  display: block;
  font-size: 28rpx;
  color: #07c160;
  font-weight: 600;
  margin-bottom: 6rpx;
}

.future-time {
  font-size: 24rpx;
  color: #999;
}

.empty-future {
  text-align: center;
  padding: 20rpx 0;
}
</style>
