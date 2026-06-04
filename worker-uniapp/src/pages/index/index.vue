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
        <view class="stat-item"><text class="stat-value">156</text><text class="stat-label">本月工时</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">4,680</text><text class="stat-label">本月收入</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">28</text><text class="stat-label">出勤天数</text></view>
      </view>

      <view v-if="!authStore.token" class="card login-card">
        <text class="empty-title">请先登录</text>
        <text class="empty-desc">登录后可以看到今日排班和签到状态</text>
        <view class="btn-primary" @click="navTo('/pages/login/login')">去登录</view>
      </view>

      <template v-else>
        <view class="section-title-row">
          <text class="section-title">今日排班</text>
          <text class="section-subtitle">{{ todayShifts.length }} 个班次待处理</text>
        </view>

        <view v-for="shift in todayShifts" :key="shift.id" class="card shift-card">
          <view class="job-row">
            <view class="job-left">
              <view class="job-icon"><text>岗</text></view>
              <view class="job-info"><text class="job-title">{{ shift.jobTitle }}</text><text class="job-desc">{{ isFallbackShift(shift) ? '兜底排班' : '接口排班' }}</text></view>
            </view>
            <view class="status-tag" :class="statusClass(shift.status)"><text>{{ statusLabel(shift.status) }}</text></view>
          </view>
          <view class="shift-meta">
            <view class="meta-item"><text class="meta-icon">时</text><text class="meta-text">{{ shift.date }} {{ shift.startTime }} - {{ shift.endTime }}</text></view>
            <view class="meta-item" @click="openMap(shift)"><text class="meta-icon">地</text><text class="meta-text">{{ formatAddress(shift.location) }}</text></view>
            <view class="meta-item"><text class="meta-icon">距</text><text class="meta-text">{{ distanceLabel(shift) }}</text></view>
          </view>
          <view class="tip" :class="tipClass(shift)"><text>{{ tipText(shift) }}</text></view>
          <view v-if="canCheckIn(shift)" class="btn-primary" @click="handleCheckIn(shift)">签到</view>
          <view v-else-if="canCheckOut(shift)" class="btn-primary" @click="handleCheckOut(shift)">签退</view>
          <view v-else class="btn-disabled">{{ isFallbackShift(shift) ? '示例不可操作' : statusLabel(shift.status) }}</view>
        </view>

        <view class="card future-card">
          <view class="card-header">
            <text class="section-title">未来排班</text>
            <text class="more" @click="navTo('/pages/schedule/schedule')">全部</text>
          </view>
          <view class="future-list">
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
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { getMyTopShifts } from '@/api/schedule'
import { checkIn, checkOut } from '@/api/attendance'

const authStore = useAuthStore()
const shifts = ref<Shift[]>([])
const checking = ref(false)
const countdown = ref('')
let countdownTimer: any = null
let currentLocation: { lat: number; lng: number } | null = null

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

function addDays(days: number) {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return formatDateKey(d)
}

function normalizeShift(shift: any): Shift {
  return {
    id: Number(shift.id ?? shift.shiftId),
    jobTitle: shift.jobTitle || shift.title || shift.positionName || '排班',
    location: shift.jobLocation || shift.location || shift.locationName || shift.address || '',
    startTime: shift.startTime || '',
    endTime: shift.endTime || '',
    date: shift.date || shift.shiftDate || '',
    status: shift.status || 'SCHEDULED',
    lat: shift.lat ?? shift.latitude,
    lng: shift.lng ?? shift.longitude,
    distance: shift.distance || shift.distanceText
  }
}

function parseShiftDateTime(date: string, time: string) {
  const [y = 0, m = 1, d = 1] = String(date).split('-').map(Number)
  const [h = 0, min = 0] = String(time).split(':').map(Number)
  return new Date(y, m - 1, d, h, min)
}

function fallbackShifts(): Shift[] {
  const today = formatDateKey(new Date())
  return [
    { id: -1, jobTitle: '仓库分拣员', location: '绿地物流园3号仓', startTime: '09:00', endTime: '18:00', date: today, status: 'SCHEDULED' },
    { id: -2, jobTitle: '餐厅服务员', location: '阳光餐厅人民路店', startTime: '18:30', endTime: '22:30', date: today, status: 'ON_DUTY' },
    { id: -3, jobTitle: '展会协助员', location: '国际会展中心A馆', startTime: '09:00', endTime: '17:00', date: addDays(1), status: 'SCHEDULED' },
    { id: -4, jobTitle: '快递分拣员', location: '城北快递中转站', startTime: '08:00', endTime: '16:00', date: addDays(2), status: 'SCHEDULED' },
    { id: -5, jobTitle: '餐厅服务员', location: '湖滨商业街', startTime: '11:00', endTime: '20:00', date: addDays(3), status: 'SCHEDULED' },
    { id: -6, jobTitle: '超市理货员', location: '安心超市中心店', startTime: '10:00', endTime: '18:00', date: addDays(4), status: 'SCHEDULED' },
    { id: -7, jobTitle: '家政保洁员', location: '万家社区服务站', startTime: '09:30', endTime: '15:30', date: addDays(5), status: 'SCHEDULED' }
  ]
}

function isFallbackShift(shift: Shift) {
  return shift.id < 0
}

function statusLabel(status?: string) {
  if (status === 'ON_DUTY') return '工作中'
  if (status === 'COMPLETED' || status === 'OFF_DUTY') return '已完成'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
  if (status === 'EARLY_LEAVE') return '早退'
  return '待上岗'
}

function statusClass(status?: string) {
  if (status === 'ON_DUTY') return 'status-green'
  if (status === 'COMPLETED' || status === 'OFF_DUTY') return 'status-gray'
  if (status === 'ABSENT' || status === 'LATE' || status === 'EARLY_LEAVE') return 'status-red'
  return 'status-yellow'
}

function tipClass(shift: Shift) {
  if (shift.id < 0) return 'tip-info'
  if (shift.status === 'ABSENT' || shift.status === 'LATE' || shift.status === 'EARLY_LEAVE') return 'tip-red'
  if (shift.status === 'ON_DUTY') return 'tip-green'
  if (shift.status === 'COMPLETED' || shift.status === 'OFF_DUTY') return 'tip-gray'
  return 'tip-yellow'
}

function tipText(shift: Shift) {
  if (shift.id < 0) return '当前为兜底排班，仅用于展示首页样式'
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
  if (isFallbackShift(shift) || checking.value) return false
  return !['ON_DUTY', 'OFF_DUTY', 'COMPLETED', 'ABSENT', 'LATE', 'EARLY_LEAVE'].includes(shift.status)
}

function canCheckOut(shift: Shift) {
  if (isFallbackShift(shift) || checking.value) return false
  return shift.status === 'ON_DUTY' || shift.status === 'LATE'
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

async function checkDistance(shift: Shift): Promise<boolean> {
  const pos = await getLocation()
  if (!pos) return false
  const checkDistance = 100
  const workLat = shift.lat || 0
  const workLng = shift.lng || 0
  if (!workLat || !workLng) return true
  const dist = calcDistance(pos.lat, pos.lng, workLat, workLng)
  if (dist > checkDistance) {
    uni.showToast({ title: `超出签到距离${Math.round(dist)}米`, icon: 'none' })
    return false
  }
  return true
}

async function handleCheckIn(shift: Shift) {
  if (checking.value || !canCheckIn(shift)) return
  const ok = await checkDistance(shift)
  if (!ok) return
  const now = new Date()
  const start = parseShiftDateTime(shift.date, shift.startTime)
  const isLate = now > start
  checking.value = true
  try {
    await checkIn({
      shiftId: shift.id,
      lat: currentLocation?.lat,
      lng: currentLocation?.lng
    })
    if (isLate) {
      const lateMins = Math.round((now.getTime() - start.getTime()) / 60000)
      uni.showModal({ title: '提示', content: `已迟到${lateMins}分钟，签到成功`, showCancel: false })
    } else {
      uni.showToast({ title: '签到成功', icon: 'success' })
    }
    const target = shifts.value.find(s => s.id === shift.id)
    if (target) target.status = isLate ? 'LATE' : 'ON_DUTY'
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签到失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}

async function handleCheckOut(shift: Shift) {
  if (checking.value || !canCheckOut(shift)) return
  const ok = await checkDistance(shift)
  if (!ok) return
  const now = new Date()
  const end = parseShiftDateTime(shift.date, shift.endTime)
  const isEarly = now < end
  checking.value = true
  try {
    await checkOut({
      shiftId: shift.id,
      lat: currentLocation?.lat,
      lng: currentLocation?.lng
    })
    if (isEarly) {
      const earlyMins = Math.round((end.getTime() - now.getTime()) / 60000)
      uni.showModal({ title: '提示', content: `提前${earlyMins}分钟签退，确认成功`, showCancel: false })
    } else {
      uni.showToast({ title: '签退成功', icon: 'success' })
    }
    const target = shifts.value.find(s => s.id === shift.id)
    if (target) target.status = isEarly ? 'EARLY_LEAVE' : 'OFF_DUTY'
  } catch (e: any) {
    uni.showToast({ title: e?.message || '签退失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}

async function loadShifts() {
  if (!authStore.token) return
  try {
    const res: any = await getMyTopShifts()
    const all: Shift[] = []
    if (res.currentShift) {
      all.push(normalizeShift(res.currentShift))
    }
    if (res.futureShifts) {
      all.push(...res.futureShifts.map(normalizeShift))
    }
    shifts.value = all.length ? all : fallbackShifts()
  } catch {
    shifts.value = fallbackShifts()
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
    name: shift.jobTitle || '工作地点',
    fail: () => uni.showToast({ title: '打开地图失败', icon: 'none' })
  })
}

function distanceLabel(shift: Shift) {
  if (shift.distance) return shift.distance
  if (shift.id < 0) return '约 2.3km'
  if (shift.lat && shift.lng && currentLocation) return `${Math.round(calcDistance(currentLocation.lat, currentLocation.lng, shift.lat, shift.lng))}米`
  return ''
}

function stopCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = null
}

async function refreshHome() {
  await authStore.loadSession()
  await loadShifts()
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
  border: 1rpx solid #edf3ef;
}

.job-row {
  margin-bottom: 28rpx;
}

.job-icon {
  width: 76rpx;
  height: 76rpx;
  line-height: 76rpx;
  text-align: center;
  border-radius: 22rpx;
  background: #eafaf1;
  color: #07a857;
  font-size: 28rpx;
  font-weight: 700;
  margin-right: 20rpx;
}

.job-title {
  font-size: 34rpx;
  color: #16251d;
  font-weight: 800;
  margin-bottom: 8rpx;
}

.job-desc {
  font-size: 24rpx;
  color: #98a39d;
}

.status-tag {
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
}

.status-green {
  background: #e7f8ef;
  color: #08a857;
}

.status-yellow {
  background: #fff7df;
  color: #d28a00;
}

.status-red {
  background: #feecec;
  color: #df3b30;
}

.status-gray {
  background: #eef1f0;
  color: #7b8580;
}

.shift-meta {
  background: #f8faf9;
  border-radius: 20rpx;
  padding: 22rpx 24rpx 6rpx;
  margin-bottom: 22rpx;
}

.meta-item {
  margin-bottom: 18rpx;
}

.meta-icon {
  width: 42rpx;
  height: 42rpx;
  line-height: 42rpx;
  text-align: center;
  border-radius: 14rpx;
  background: #e9f7ef;
  color: #08a857;
  font-size: 22rpx;
  margin-right: 16rpx;
}

.meta-text {
  flex: 1;
  font-size: 27rpx;
  color: #52605a;
}

.tip {
  padding: 18rpx 22rpx;
  border-radius: 18rpx;
  font-size: 26rpx;
  margin-bottom: 24rpx;
}

.tip-green {
  background: #eafaf1;
  color: #07984f;
}

.tip-yellow {
  background: #fff8e8;
  color: #bd7b00;
}

.tip-red {
  background: #fff0f0;
  color: #d93025;
}

.tip-gray,
.tip-info {
  background: #f1f4f3;
  color: #7d8883;
}

.btn-primary,
.btn-disabled {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  border-radius: 44rpx;
  font-size: 31rpx;
  font-weight: 800;
}

.btn-primary {
  background: linear-gradient(135deg, #18c86b, #08a95a);
  color: #fff;
}

.btn-disabled {
  background: #eef1f0;
  color: #9aa39f;
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
