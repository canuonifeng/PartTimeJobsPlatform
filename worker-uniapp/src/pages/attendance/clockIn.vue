<template>
  <view class="clockin-page">
    <view class="top-panel">
      <view class="date-header">
        <view><text class="date-title">{{ todayDate }}</text><text class="date-subtitle">{{ todayName }} · 今日打卡</text></view>
        <view class="date-pill">{{ shifts.length }} 个班次</view>
      </view>
      <view class="stats-card">
        <view class="stat-item"><text class="stat-value">{{ stats.total }}</text><text class="stat-label">今日排班</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ stats.checkedIn }}</text><text class="stat-label">已签到</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ stats.completed }}</text><text class="stat-label">已完成</text></view>
      </view>
      <view class="notice-card"><text class="notice-dot"></text><text class="notice-text">请在岗位地点附近完成签到签退，系统将获取当前位置用于打卡校验。</text></view>
    </view>
    <uni-load-more v-if="loading" status="loading" />
    <scroll-view class="shift-scroll" scroll-y>
      <view class="section-row"><text class="section-title">打卡记录</text><text class="section-subtitle">{{ sourceTip }}</text></view>
      <view v-for="shift in shifts" :key="shift.id" class="shift-card">
        <view class="shift-header">
          <view class="job-left"><view class="job-icon"><text>岗</text></view><view class="job-info"><text class="shift-title">{{ shift.jobTitle }}</text><text class="shift-location">{{ shift.location }}</text></view></view>
          <view class="status-badge" :class="statusClass(shift.status)">{{ statusText(shift.status) }}</view>
        </view>
        <view class="time-box"><view><text class="time-label">工作时间</text><text class="time-value">{{ shift.startTime }} - {{ shift.endTime }}</text></view><view class="date-box"><text class="date-day">{{ shift.date.slice(8) }}</text><text class="date-month">{{ shift.date.slice(5, 7) }}月</text></view></view>
        <view class="record-row"><view class="record-item"><text class="record-label">签到</text><text class="record-value">{{ shift.checkInTime || '未签到' }}</text></view><view class="record-item"><text class="record-label">签退</text><text class="record-value">{{ shift.checkOutTime || '未签退' }}</text></view></view>
        <view class="shift-actions">
          <button class="action-btn check-in" :class="{ disabled: isActionDisabled(shift, 'in') }" :disabled="isActionDisabled(shift, 'in')" @click="handleCheckIn(shift)">{{ submittingKey === `${shift.id}:in` ? '签到中' : (hasCheckedIn(shift) ? '已签到' : '签到') }}</button>
          <button class="action-btn check-out" :class="{ disabled: isActionDisabled(shift, 'out') }" :disabled="isActionDisabled(shift, 'out')" @click="handleCheckOut(shift)">{{ submittingKey === `${shift.id}:out` ? '签退中' : (hasCheckedOut(shift) ? '已签退' : '签退') }}</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyShifts } from '@/api/schedule'
import { checkIn, checkOut } from '@/api/attendance'

interface Shift { id: number; jobTitle: string; location: string; startTime: string; endTime: string; date: string; status: string; checkedIn: boolean; checkedOut: boolean; checkInTime?: string; checkOutTime?: string; attendanceId?: number }

const loading = ref(false)
const shifts = ref<Shift[]>([])
const usingFallback = ref(false)
const submittingKey = ref('')
const weekDayNames = ['日', '一', '二', '三', '四', '五', '六']
const checkedInStatuses = ['CHECKED_IN', 'CHECKED_OUT', 'ON_DUTY', 'OFF_DUTY', 'COMPLETED', 'LATE']
const checkedOutStatuses = ['CHECKED_OUT', 'OFF_DUTY', 'COMPLETED']

const todayDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${String(d.getMonth() + 1).padStart(2, '0')}月${String(d.getDate()).padStart(2, '0')}日`
})
const todayName = computed(() => `星期${weekDayNames[new Date().getDay()]}`)
const sourceTip = computed(() => usingFallback.value ? '示例排班' : '实时排班')
const stats = computed(() => ({ total: shifts.value.length, checkedIn: shifts.value.filter(hasCheckedIn).length, completed: shifts.value.filter(hasCheckedOut).length }))

function formatFullDate(d: Date): string { return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }
function normalizeTime(value: any): string {
  const str = String(value || '')
  if (!str) return ''
  if (str.length >= 16 && str.indexOf(' ') > -1) return str.slice(11, 16)
  if (str.length >= 16 && str.indexOf('T') > -1) return str.slice(11, 16)
  return str.slice(0, 5)
}
function normalizeDate(value: any): string {
  const str = String(value || '')
  if (!str) return formatFullDate(new Date())
  const match = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (match) return `${match[1]}-${match[2].padStart(2, '0')}-${match[3].padStart(2, '0')}`
  const parsed = new Date(str)
  return isNaN(parsed.getTime()) ? formatFullDate(new Date()) : formatFullDate(parsed)
}
function normalizeShift(s: any, index: number): Shift {
  const status = s.status || s.attendanceStatus || 'SCHEDULED'
  return { id: Number(s.id ?? s.shiftId ?? index + 1), jobTitle: s.jobTitle || s.title || s.positionName || s.jobName || '临时岗位', location: s.location || s.locationName || s.jobLocation || s.address || '暂无地点', startTime: normalizeTime(s.startTime || s.beginTime) || '09:00', endTime: normalizeTime(s.endTime || s.finishTime) || '18:00', date: normalizeDate(s.date || s.shiftDate), status, checkedIn: checkedInStatuses.includes(status), checkedOut: checkedOutStatuses.includes(status), checkInTime: normalizeTime(s.checkInTime || s.clockInTime || s.signInTime), checkOutTime: normalizeTime(s.checkOutTime || s.clockOutTime || s.signOutTime), attendanceId: s.attendanceId }
}
function fallbackShifts(): Shift[] {
  const today = formatFullDate(new Date())
  return [
    { id: -1, jobTitle: '仓库分拣员', location: '绿地物流园3号仓', startTime: '09:00', endTime: '18:00', date: today, status: 'SCHEDULED', checkedIn: false, checkedOut: false, checkInTime: '', checkOutTime: '' },
    { id: -2, jobTitle: '餐厅服务员', location: '阳光餐厅人民路店', startTime: '18:30', endTime: '22:30', date: today, status: 'ON_DUTY', checkedIn: true, checkedOut: false, checkInTime: '18:28', checkOutTime: '' }
  ]
}
function hasCheckedIn(shift: Shift): boolean { return shift.checkedIn || checkedInStatuses.includes(shift.status) }
function hasCheckedOut(shift: Shift): boolean { return shift.checkedOut || checkedOutStatuses.includes(shift.status) }
function statusText(status: string): string {
  const map: Record<string, string> = { SCHEDULED: '待上岗', CHECKED_IN: '已签到', CHECKED_OUT: '已签退', ON_DUTY: '工作中', OFF_DUTY: '已下岗', COMPLETED: '已完成', LATE: '迟到', ABSENT: '缺勤' }
  return map[status] || status || '待上岗'
}
function statusClass(status: string): string {
  if (['CHECKED_OUT', 'OFF_DUTY', 'COMPLETED'].includes(status)) return 'completed'
  if (['CHECKED_IN', 'ON_DUTY'].includes(status)) return 'active'
  if (['LATE', 'ABSENT'].includes(status)) return 'warning'
  return 'pending'
}
function isActionDisabled(shift: Shift, type: 'in' | 'out'): boolean {
  if (submittingKey.value) return true
  if (type === 'in') return hasCheckedIn(shift) || hasCheckedOut(shift) || shift.status === 'ABSENT'
  return !hasCheckedIn(shift) || hasCheckedOut(shift) || shift.status === 'ABSENT'
}
function getLocation(): Promise<{ latitude: number; longitude: number }> {
  return new Promise((resolve, reject) => { uni.getLocation({ type: 'wgs84', success: (res) => resolve({ latitude: res.latitude, longitude: res.longitude }), fail: reject }) })
}
function showFallbackToast() { uni.showToast({ title: '示例排班暂不支持打卡', icon: 'none' }) }
function isLocationError(err: any): boolean { return !!err?.errMsg || String(err?.message || '').includes('getLocation') }
function showActionError(err: any, action: string) {
  if (isLocationError(err)) {
    uni.showToast({ title: '获取定位失败，请检查定位权限', icon: 'none' })
  } else {
    uni.showToast({ title: err?.message?.includes('距离') ? '不在打卡范围内' : (err?.message || `${action}失败`), icon: 'none' })
  }
}
async function handleCheckIn(shift: Shift) {
  if (shift.id <= 0) return showFallbackToast()
  if (isActionDisabled(shift, 'in')) return
  submittingKey.value = `${shift.id}:in`
  try {
    const location = await getLocation()
    await checkIn({ shiftId: shift.id, lat: location.latitude, lng: location.longitude })
    shift.checkedIn = true
    shift.status = 'CHECKED_IN'
    shift.checkInTime = normalizeTime(new Date().toTimeString())
    uni.showToast({ title: '签到成功', icon: 'success' })
  } catch (err: any) { showActionError(err, '签到') } finally { submittingKey.value = '' }
}
async function handleCheckOut(shift: Shift) {
  if (shift.id <= 0) return showFallbackToast()
  if (isActionDisabled(shift, 'out')) return
  submittingKey.value = `${shift.id}:out`
  try {
    const location = await getLocation()
    await checkOut({ shiftId: shift.id, lat: location.latitude, lng: location.longitude })
    shift.checkedOut = true
    shift.status = 'CHECKED_OUT'
    shift.checkOutTime = normalizeTime(new Date().toTimeString())
    uni.showToast({ title: '签退成功', icon: 'success' })
  } catch (err: any) { showActionError(err, '签退') } finally { submittingKey.value = '' }
}
async function loadTodayShifts() {
  loading.value = true
  try {
    const dateStr = formatFullDate(new Date())
    const res: any = await getMyShifts({ startDate: dateStr, endDate: dateStr })
    const list = Array.isArray(res) ? res : (res?.list || [])
    shifts.value = list.length ? list.map(normalizeShift) : fallbackShifts()
    usingFallback.value = !list.length
  } catch (err: any) {
    shifts.value = fallbackShifts()
    usingFallback.value = true
    uni.showToast({ title: err?.message || '加载失败，已展示兜底排班', icon: 'none' })
  } finally { loading.value = false }
}

onMounted(loadTodayShifts)
</script>

<style scoped>
.clockin-page { display: flex; flex-direction: column; min-height: 100vh; background: #f5f7fb; }
.top-panel { padding: 28rpx 24rpx 22rpx; background: linear-gradient(180deg, #eaf8f0 0%, #f5f7fb 100%); }
.date-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 22rpx; }
.date-title { display: block; font-size: 40rpx; line-height: 56rpx; font-weight: 700; color: #1f2d3d; }
.date-subtitle { display: block; margin-top: 6rpx; font-size: 24rpx; color: #6b7785; }
.date-pill { padding: 12rpx 22rpx; border-radius: 28rpx; background: #ffffff; color: #07c160; font-size: 24rpx; font-weight: 600; box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.12); }
.stats-card { display: flex; align-items: center; padding: 28rpx 12rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 10rpx 30rpx rgba(43, 72, 99, 0.08); }
.stat-item { flex: 1; text-align: center; }
.stat-value { display: block; font-size: 42rpx; line-height: 50rpx; font-weight: 700; color: #1f2d3d; }
.stat-label { display: block; margin-top: 8rpx; font-size: 22rpx; color: #8b98a7; }
.stat-line { width: 1rpx; height: 54rpx; background: #edf0f5; }
.notice-card { display: flex; align-items: center; margin-top: 20rpx; padding: 18rpx 22rpx; border-radius: 18rpx; background: #ffffff; }
.notice-dot { width: 12rpx; height: 12rpx; margin-right: 14rpx; border-radius: 50%; background: #07c160; }
.notice-text { flex: 1; font-size: 24rpx; line-height: 34rpx; color: #637083; }
.shift-scroll { flex: 1; padding: 22rpx 24rpx 34rpx; box-sizing: border-box; }
.section-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18rpx; }
.section-title { font-size: 32rpx; font-weight: 700; color: #1f2d3d; }
.section-subtitle { font-size: 24rpx; color: #8b98a7; }
.shift-card { margin-bottom: 22rpx; padding: 28rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 24rpx rgba(43, 72, 99, 0.06); }
.shift-header { display: flex; align-items: center; justify-content: space-between; }
.job-left { flex: 1; display: flex; align-items: center; min-width: 0; }
.job-icon { width: 64rpx; height: 64rpx; line-height: 64rpx; margin-right: 18rpx; border-radius: 20rpx; text-align: center; color: #ffffff; font-size: 26rpx; font-weight: 700; background: #07c160; }
.job-info { flex: 1; min-width: 0; }
.shift-title { display: block; font-size: 32rpx; line-height: 42rpx; font-weight: 700; color: #1f2d3d; }
.shift-location { display: block; margin-top: 8rpx; font-size: 24rpx; line-height: 32rpx; color: #7b8794; }
.status-badge { margin-left: 18rpx; padding: 8rpx 18rpx; border-radius: 24rpx; font-size: 22rpx; line-height: 28rpx; white-space: nowrap; }
.status-badge.pending { background: #fff7e6; color: #fa8c16; }
.status-badge.active { background: #e6f7ff; color: #1890ff; }
.status-badge.completed { background: #f0fbf4; color: #07c160; }
.status-badge.warning { background: #fff1f0; color: #f5222d; }
.time-box { display: flex; align-items: center; justify-content: space-between; margin-top: 24rpx; padding: 22rpx 24rpx; border-radius: 18rpx; background: #f7fafc; }
.time-label { display: block; font-size: 22rpx; color: #8b98a7; }
.time-value { display: block; margin-top: 8rpx; font-size: 34rpx; line-height: 42rpx; font-weight: 700; color: #07c160; }
.date-box { width: 92rpx; height: 92rpx; border-radius: 18rpx; background: #ffffff; text-align: center; }
.date-day { display: block; margin-top: 16rpx; font-size: 32rpx; line-height: 34rpx; font-weight: 700; color: #1f2d3d; }
.date-month { display: block; margin-top: 4rpx; font-size: 20rpx; color: #8b98a7; }
.record-row { display: flex; margin-top: 22rpx; }
.record-item { flex: 1; padding: 18rpx 20rpx; border-radius: 16rpx; background: #fbfcfe; }
.record-item:first-child { margin-right: 18rpx; }
.record-label { display: block; font-size: 22rpx; color: #8b98a7; }
.record-value { display: block; margin-top: 8rpx; font-size: 28rpx; font-weight: 600; color: #1f2d3d; }
.shift-actions { display: flex; margin-top: 24rpx; }
.action-btn { flex: 1; height: 76rpx; line-height: 76rpx; border-radius: 38rpx; font-size: 28rpx; border: none; text-align: center; }
.action-btn.check-in { margin-right: 18rpx; background: #07c160; color: #ffffff; }
.action-btn.check-out { background: #ffffff; color: #07c160; border: 1rpx solid #07c160; }
.action-btn.disabled { background: #eef1f5; color: #b7c0cc; border-color: #eef1f5; }
</style>
