<template>
  <view class="schedule-page">
    <view class="top-panel">
      <view class="week-nav">
        <view class="nav-arrow" @click="prevWeek">‹</view>
        <view class="period-info"><text class="period-title">{{ periodTitle }}</text><text class="period-subtitle">{{ weekLabel }}</text></view>
        <view class="nav-arrow" @click="nextWeek">›</view>
      </view>
      <view class="stats-card">
        <view class="stat-item"><text class="stat-value">{{ scheduleStats.attendanceDays }}</text><text class="stat-label">出勤天数</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ scheduleStats.workHours }}</text><text class="stat-label">工时</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ scheduleStats.lateCount }}</text><text class="stat-label">迟到次数</text></view>
        <view class="stat-line"></view>
        <view class="stat-item"><text class="stat-value">{{ scheduleStats.absentCount }}</text><text class="stat-label">缺勤次数</text></view>
      </view>
    </view>
    <view class="week-days">
      <view v-for="(day, i) in weekDays" :key="i" class="day-cell" :class="{ today: day.isToday, selected: selectedIndex === i }" @click="selectDay(i)">
        <text class="day-name">{{ day.name }}</text>
        <text class="day-date">{{ day.date }}</text>
        <text v-if="day.hasShift" class="day-dot"></text>
      </view>
    </view>
    <uni-load-more v-if="loading" status="loading" />
    <scroll-view class="shift-scroll" scroll-y>
      <view class="section-title-row"><text class="section-title">我的排班</text><text class="section-subtitle">{{ selectedDayLabel }} · {{ shifts.length }} 个班次</text></view>
      <view v-if="shifts.length === 0 && !loading" class="empty-state"><text class="empty-title">暂无排班</text><text class="empty-text">请选择其他日期查看排班</text></view>
      <view v-for="shift in shifts" :key="shift.id" class="shift-card">
        <view class="shift-header">
          <view class="job-left"><view class="job-icon"><text>岗</text></view><view class="job-info"><text class="shift-title">{{ shift.jobTitle }}</text><text class="shift-location">{{ shift.location }}</text></view></view>
          <view class="status-badge" :class="statusClass(shift)">{{ statusText(shift) }}</view>
        </view>
        <view class="time-box"><view class="time-main"><text class="time-label">工作时间</text><text class="time-value">{{ shift.startTime }} - {{ shift.endTime }}</text></view><view class="date-box"><text class="date-day">{{ shift.date.slice(8) }}</text><text class="date-month">{{ shift.date.slice(5, 7) }}月</text></view></view>
        <view class="attendance-row">
          <view class="attendance-item"><text class="attendance-label">签到</text><text class="attendance-value">{{ shift.checkInTime || '未签到' }}</text></view>
          <view class="attendance-item"><text class="attendance-label">签退</text><text class="attendance-value">{{ shift.checkOutTime || '未签退' }}</text></view>
          <view class="attendance-item"><text class="attendance-label">工时</text><text class="attendance-value">{{ shift.workHours || calcWorkHoursText(shift) }}</text></view>
        </view>
        <view class="card-footer">
          <text class="footer-tip">{{ correctionTip(shift) }}</text>
          <view v-if="shift.correctionStatus === 'PENDING'" class="correction-tag pending">补卡审批中</view>
          <view v-else-if="shift.correctionStatus === 'APPROVED'" class="correction-tag approved">补卡已通过</view>
          <view v-else-if="shift.correctionStatus === 'REJECTED'" class="correction-tag rejected">补卡已拒绝</view>
          <view v-else-if="shift.canApplyCorrection" class="correction-btn" @click="openCorrectionDialog(shift)">申请补卡</view>
        </view>
      </view>
    </scroll-view>
    <view v-if="correctionDialogVisible" class="overlay" @click="correctionDialogVisible = false"><view class="overlay-content" @click.stop><view class="dialog-title">补卡申请</view><view class="dialog-body"><text class="dialog-label">补卡原因</text><textarea v-model="correctionReason" placeholder="请填写补卡原因" class="dialog-textarea" maxlength="500" /><text class="dialog-hint">{{ correctionReason.length }}/500</text></view><view class="dialog-footer"><view class="dialog-btn cancel" @click="correctionDialogVisible = false">取消</view><view class="dialog-btn confirm" @click="handleSubmitCorrection">{{ submitting ? '提交中...' : '提交' }}</view></view></view></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyShifts } from '@/api/schedule'
import { submitCorrection } from '@/api/attendance'

interface DayInfo { name: string; date: string; fullDate: string; isToday: boolean; hasShift: boolean }
interface Shift { id: number; jobTitle: string; location: string; startTime: string; endTime: string; date: string; status: string; correctionStatus?: string | null; canApplyCorrection: boolean; checkInTime?: string; checkOutTime?: string; workHours?: string }

const loading = ref(false)
const shifts = ref<Shift[]>([])
const allShifts = ref<Shift[]>([])
const selectedIndex = ref(-1)
const currentWeekStart = ref(getWeekStart(new Date()))
const weekDays = ref<DayInfo[]>([])
const correctionDialogVisible = ref(false)
const correctionTarget = ref<Shift | null>(null)
const correctionReason = ref('')
const submitting = ref(false)

function getWeekStart(date: Date): Date {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

function formatDate(d: Date): string {
  return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatFullDate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function parseDateTime(date: string, time: string): Date {
  const [y = 0, m = 1, d = 1] = String(date).split('-').map(Number)
  const [h = 0, min = 0] = String(time).split(':').map(Number)
  return new Date(y, m - 1, d, h, min, 0, 0)
}

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
  if (!isNaN(parsed.getTime())) return formatFullDate(parsed)
  return formatFullDate(new Date())
}

function normalizeShift(s: any, index: number): Shift {
  return {
    id: Number(s.id ?? s.shiftId ?? index + 1),
    jobTitle: s.jobTitle || s.title || s.positionName || s.jobName || '临时岗位',
    location: s.location || s.locationName || s.jobLocation || s.address || '暂无地点',
    startTime: normalizeTime(s.startTime || s.beginTime),
    endTime: normalizeTime(s.endTime || s.finishTime),
    date: normalizeDate(s.date || s.shiftDate),
    status: s.status || s.attendanceStatus || 'SCHEDULED',
    correctionStatus: s.correctionStatus || s.applyStatus || null,
    canApplyCorrection: false,
    checkInTime: normalizeTime(s.checkInTime || s.clockInTime || s.signInTime),
    checkOutTime: normalizeTime(s.checkOutTime || s.clockOutTime || s.signOutTime),
    workHours: s.workHours || s.hours || s.duration || ''
  }
}

function addDays(start: Date, days: number): Date {
  const d = new Date(start)
  d.setDate(d.getDate() + days)
  return d
}


function isShiftEnded(shift: Shift): boolean {
  if (!shift.date || !shift.endTime) return false
  return new Date() > parseDateTime(shift.date, shift.endTime)
}

function canApplyCorrection(shift: Shift): boolean {
  if (shift.id < 0) return false
  if (!isShiftEnded(shift)) return false
  if (['COMPLETED', 'ABSENT', 'EARLY_LEAVE'].includes(shift.status)) return false
  if (shift.correctionStatus) return false
  return true
}

function statusClass(shift: Shift): string {
  if (['COMPLETED'].includes(shift.status)) return 'completed'
  if (shift.checkInTime && !shift.checkOutTime) return 'in-progress'
  if (['ON_DUTY', 'LATE'].includes(shift.status)) return 'in-progress'
  if (['ABSENT', 'EARLY_LEAVE'].includes(shift.status)) return 'warning'
  return 'pending'
}

function statusText(shift: Shift): string {
  if (shift.checkOutTime) return '已完成'
  if (shift.checkInTime) return '已签到'
  const map: Record<string, string> = { SCHEDULED: '待上岗', CHECKED_IN: '已签到', ON_DUTY: '工作中', CHECKED_OUT: '已完成', COMPLETED: '已完成', OFF_DUTY: '已完成', ABSENT: '缺勤', LATE: '迟到', EARLY_LEAVE: '早退' }
  return map[shift.status] || shift.status || '待上岗'
}

function buildWeekDays(start: Date) {
  const today = formatFullDate(new Date())
  const nameArr = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  weekDays.value = nameArr.map((name, i) => {
    const d = addDays(start, i)
    const fullDate = formatFullDate(d)
    return { name, date: formatDate(d), fullDate, isToday: fullDate === today, hasShift: allShifts.value.some((s) => s.date === fullDate) }
  })
}

function calcWorkHours(shift: Shift): number {
  if (!shift.startTime || !shift.endTime) return 0
  const diff = parseDateTime(shift.date, shift.endTime).getTime() - parseDateTime(shift.date, shift.startTime).getTime()
  return diff > 0 ? Math.round(diff / 360000) / 10 : 0
}

function calcWorkHoursText(shift: Shift): string {
  const hours = calcWorkHours(shift)
  return hours ? `${hours}h` : '0h'
}

function correctionTip(shift: Shift): string {
  if (shift.correctionStatus) return '补卡申请已记录'
  if (shift.canApplyCorrection) return '打卡异常，可申请补卡'
  return '打卡记录正常'
}

const weekLabel = computed(() => weekDays.value.length < 7 ? '' : `${weekDays.value[0].fullDate} ~ ${weekDays.value[6].fullDate}`)
const periodTitle = computed(() => {
  const d = currentWeekStart.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})
const selectedDayLabel = computed(() => {
  const day = weekDays.value[selectedIndex.value]
  return day ? `${day.name} ${day.date}` : ''
})
const scheduleStats = computed(() => {
  const completed = allShifts.value.filter((s) => ['CHECKED_OUT', 'COMPLETED', 'OFF_DUTY'].includes(s.status))
  const workHours = allShifts.value.reduce((total, shift) => total + (parseFloat(String(shift.workHours || '')) || (completed.includes(shift) ? calcWorkHours(shift) : 0)), 0)
  return { attendanceDays: new Set(completed.map((s) => s.date)).size, workHours: Math.round(workHours * 10) / 10, lateCount: allShifts.value.filter((s) => s.status === 'LATE').length, absentCount: allShifts.value.filter((s) => s.status === 'ABSENT').length }
})

async function loadShifts() {
  if (weekDays.value.length < 7) return
  loading.value = true
  try {
    const res: any = await getMyShifts({ startDate: weekDays.value[0].fullDate, endDate: weekDays.value[6].fullDate })
    const list = Array.isArray(res) ? res : (res?.records || res?.list || [])
    allShifts.value = list.map(normalizeShift)
  } catch {
    allShifts.value = []
    uni.showToast({ title: '排班加载失败', icon: 'none' })
  } finally {
    buildWeekDays(currentWeekStart.value)
    const todayIndex = weekDays.value.findIndex((d) => d.isToday)
    selectDay(selectedIndex.value >= 0 ? selectedIndex.value : todayIndex)
    loading.value = false
  }
}

function selectDay(index: number) {
  if (index < 0) index = 0
  selectedIndex.value = index
  const day = weekDays.value[index]
  if (!day) return
  shifts.value = allShifts.value.filter((s) => s.date === day.fullDate).map((s) => ({ ...s, canApplyCorrection: canApplyCorrection(s) }))
}

function prevWeek() {
  const d = addDays(currentWeekStart.value, -7)
  currentWeekStart.value = d
  selectedIndex.value = -1
  buildWeekDays(d)
  loadShifts()
}

function nextWeek() {
  const d = addDays(currentWeekStart.value, 7)
  currentWeekStart.value = d
  selectedIndex.value = -1
  buildWeekDays(d)
  loadShifts()
}

function openCorrectionDialog(shift: Shift) {
  if (shift.id <= 0 || !shift.canApplyCorrection) {
    uni.showToast({ title: '当前排班不可补卡', icon: 'none' })
    return
  }
  correctionTarget.value = shift
  correctionReason.value = ''
  correctionDialogVisible.value = true
}

async function handleSubmitCorrection() {
  if (!correctionTarget.value || correctionTarget.value.id <= 0 || !correctionTarget.value.canApplyCorrection) {
    uni.showToast({ title: '当前排班不可补卡', icon: 'none' })
    return
  }
  if (!correctionReason.value.trim()) {
    uni.showToast({ title: '请填写补卡原因', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await submitCorrection({ shiftId: correctionTarget.value.id, reason: correctionReason.value.trim() })
    uni.showToast({ title: '补卡申请已提交', icon: 'success' })
    correctionDialogVisible.value = false
    correctionTarget.value = null
    loadShifts()
  } catch (err: any) {
    uni.showToast({ title: err?.data?.error || err.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  buildWeekDays(currentWeekStart.value)
  loadShifts()
})
</script>

<style scoped>
.schedule-page { display: flex; flex-direction: column; height: 100vh; background: #f6f8f7; }
.top-panel { background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%); padding: 28rpx 28rpx 34rpx; border-bottom-left-radius: 36rpx; border-bottom-right-radius: 36rpx; }
.week-nav { display: flex; justify-content: space-between; align-items: center; color: #fff; margin-bottom: 28rpx; }
.nav-arrow { width: 70rpx; height: 70rpx; line-height: 66rpx; text-align: center; border-radius: 24rpx; background: rgba(255, 255, 255, 0.2); font-size: 42rpx; font-weight: 300; }
.period-info { flex: 1; text-align: center; }
.period-title, .period-subtitle, .stat-value, .stat-label, .section-title, .section-subtitle, .empty-title, .empty-text, .shift-title, .shift-location, .time-label, .time-value, .date-day, .date-month, .attendance-label, .attendance-value, .footer-tip { display: block; }
.period-title { font-size: 38rpx; font-weight: 800; margin-bottom: 10rpx; }
.period-subtitle { font-size: 24rpx; opacity: 0.9; }
.stats-card { display: flex; align-items: center; padding: 30rpx 8rpx; border-radius: 26rpx; background: #fff; box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.12); }
.stat-item { flex: 1; text-align: center; }
.stat-value { font-size: 34rpx; color: #14231b; font-weight: 800; margin-bottom: 8rpx; }
.stat-label { font-size: 22rpx; color: #8a9690; }
.stat-line { width: 1rpx; height: 48rpx; background: #edf1ef; }
.week-days { display: flex; padding: 24rpx 20rpx 20rpx; background: #f6f8f7; }
.day-cell { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 14rpx 0 18rpx; border-radius: 18rpx; position: relative; }
.day-cell.today { background: #eafaf1; }
.day-cell.selected { background: #08a95a; }
.day-cell.selected .day-name, .day-cell.selected .day-date { color: #fff; }
.day-name { font-size: 23rpx; color: #8a9690; margin-bottom: 8rpx; }
.day-date { font-size: 27rpx; color: #17251e; font-weight: 800; }
.day-dot { width: 8rpx; height: 8rpx; border-radius: 50%; background: #08a95a; position: absolute; bottom: 6rpx; }
.day-cell.selected .day-dot { background: #fff; }
.shift-scroll { flex: 1; padding: 0 28rpx 30rpx; box-sizing: border-box; }
.section-title-row { display: flex; justify-content: space-between; align-items: flex-end; margin: 12rpx 4rpx 20rpx; }
.section-title { font-size: 36rpx; color: #15231b; font-weight: 800; }
.section-subtitle { font-size: 24rpx; color: #8c9892; }
.empty-state { background: #fff; border-radius: 24rpx; padding: 80rpx 30rpx; text-align: center; }
.empty-title { font-size: 32rpx; color: #15231b; font-weight: 800; margin-bottom: 14rpx; }
.empty-text { font-size: 26rpx; color: #8c9892; }
.shift-card { background: #fff; border-radius: 24rpx; padding: 30rpx; margin-bottom: 28rpx; border: 1rpx solid #edf3ef; box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08); }
.shift-header, .job-left, .time-box, .attendance-row, .card-footer { display: flex; align-items: center; }
.shift-header, .time-box, .card-footer { justify-content: space-between; }
.shift-header { margin-bottom: 26rpx; }
.job-left { flex: 1; }
.job-icon { width: 76rpx; height: 76rpx; line-height: 76rpx; text-align: center; border-radius: 22rpx; background: #eafaf1; color: #07a857; font-size: 28rpx; font-weight: 700; margin-right: 20rpx; }
.job-info { flex: 1; }
.shift-title { font-size: 34rpx; color: #16251d; font-weight: 800; margin-bottom: 8rpx; }
.shift-location { font-size: 24rpx; color: #98a39d; }
.status-badge { padding: 10rpx 22rpx; border-radius: 999rpx; font-size: 24rpx; font-weight: 700; }
.status-badge.completed { background: #eef1f0; color: #7b8580; }
.status-badge.in-progress { background: #e7f8ef; color: #08a857; }
.status-badge.warning { background: #feecec; color: #df3b30; }
.status-badge.pending { background: #fff7df; color: #d28a00; }
.time-box { background: #f8faf9; border-radius: 20rpx; padding: 22rpx 24rpx; margin-bottom: 20rpx; }
.time-main { flex: 1; }
.time-label { font-size: 24rpx; color: #96a09b; margin-bottom: 8rpx; }
.time-value { font-size: 34rpx; color: #08a857; font-weight: 800; }
.date-box { width: 86rpx; height: 86rpx; border-radius: 22rpx; background: #eafaf1; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.date-day { font-size: 32rpx; line-height: 36rpx; color: #08a857; font-weight: 800; }
.date-month { font-size: 22rpx; color: #58b987; }
.attendance-row { background: #fbfcfb; border-radius: 18rpx; padding: 20rpx 0; margin-bottom: 22rpx; }
.attendance-item { flex: 1; text-align: center; border-right: 1rpx solid #edf1ef; }
.attendance-item:last-child { border-right: none; }
.attendance-label { font-size: 23rpx; color: #98a39d; margin-bottom: 8rpx; }
.attendance-value { font-size: 27rpx; color: #24332b; font-weight: 700; }
.footer-tip { flex: 1; font-size: 24rpx; color: #8c9892; }
.correction-btn, .correction-tag { font-size: 24rpx; padding: 9rpx 18rpx; border-radius: 999rpx; white-space: nowrap; }
.correction-btn { color: #07a857; background: #eafaf1; border: 1rpx solid #07a857; }
.correction-tag.pending { background: #fff7e6; color: #fa8c16; }
.correction-tag.approved { background: #f6ffed; color: #52c41a; }
.correction-tag.rejected { background: #fff1f0; color: #ff4d4f; }
.overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.overlay-content { background: #fff; border-radius: 24rpx; width: 600rpx; max-width: 85%; overflow: hidden; }
.dialog-title { font-size: 32rpx; font-weight: 800; color: #15231b; text-align: center; padding: 32rpx 30rpx 0; }
.dialog-body { padding: 30rpx; }
.dialog-label { font-size: 28rpx; color: #333; display: block; margin-bottom: 12rpx; }
.dialog-textarea { width: 100%; min-height: 160rpx; border: 1rpx solid #e0e0e0; border-radius: 12rpx; padding: 16rpx; font-size: 26rpx; box-sizing: border-box; }
.dialog-hint { font-size: 22rpx; color: #999; text-align: right; display: block; margin-top: 6rpx; }
.dialog-footer { display: flex; border-top: 1rpx solid #f0f0f0; }
.dialog-btn { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; }
.dialog-btn.cancel { color: #999; border-right: 1rpx solid #f0f0f0; }
.dialog-btn.confirm { color: #07c160; font-weight: 700; }
</style>
