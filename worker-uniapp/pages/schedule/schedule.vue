<template>
  <view class="schedule-page">
    <view class="week-nav">
      <view class="nav-arrow" @click="prevWeek">&lt;</view>
      <view class="week-label">{{ weekLabel }}</view>
      <view class="nav-arrow" @click="nextWeek">&gt;</view>
    </view>

    <view class="week-days">
      <view
        v-for="(day, i) in weekDays"
        :key="i"
        class="day-cell"
        :class="{ today: day.isToday, selected: selectedIndex === i }"
        @click="selectDay(i)"
      >
        <text class="day-name">{{ day.name }}</text>
        <text class="day-date">{{ day.date }}</text>
        <text v-if="day.hasShift" class="day-dot"></text>
      </view>
    </view>

    <uni-load-more v-if="loading" status="loading" />

    <scroll-view class="shift-scroll" scroll-y>
      <view v-if="shifts.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无排班</text>
      </view>

      <view v-for="shift in shifts" :key="shift.id" class="shift-card">
        <view class="shift-time">
          <text class="shift-time-text">{{ shift.startTime }}-{{ shift.endTime }}</text>
        </view>
        <view class="shift-info">
          <text class="shift-title">{{ shift.jobTitle }}</text>
          <text class="shift-location">{{ shift.location }}</text>
        </view>
        <view class="shift-status" :class="shift.status">
          {{ shift.status === 'completed' ? '已完成' : shift.status === 'in_progress' ? '进行中' : '待上岗' }}
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyShifts } from '@/api/schedule'

interface DayInfo {
  name: string
  date: string
  fullDate: string
  isToday: boolean
  hasShift: boolean
}

interface Shift {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
}

const loading = ref(false)
const shifts = ref<Shift[]>([])
const allShifts = ref<Shift[]>([])
const selectedIndex = ref(-1)
const currentWeekStart = ref(getWeekStart(new Date()))
const weekDays = ref<DayInfo[]>([])

function getWeekStart(date: Date): Date {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

function formatDate(d: Date): string {
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}-${day}`
}

function formatFullDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function buildWeekDays(start: Date) {
  const days: DayInfo[] = []
  const today = formatFullDate(new Date())
  for (let i = 0; i < 7; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    const fullDate = formatFullDate(d)
    const nameArr = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    days.push({
      name: nameArr[i],
      date: formatDate(d),
      fullDate,
      isToday: fullDate === today,
      hasShift: allShifts.value.some((s) => s.date === fullDate)
    })
  }
  weekDays.value = days
}

const weekLabel = computed(() => {
  if (weekDays.value.length < 7) return ''
  const start = weekDays.value[0].fullDate
  const end = weekDays.value[6].fullDate
  return `${start} ~ ${end}`
})

async function loadShifts() {
  if (weekDays.value.length < 7) return
  loading.value = true
  try {
    const res: any = await getMyShifts({
      startDate: weekDays.value[0].fullDate,
      endDate: weekDays.value[6].fullDate
    })
    allShifts.value = res.list || []
    buildWeekDays(currentWeekStart.value)
    selectDay(selectedIndex.value >= 0 ? selectedIndex.value : weekDays.value.findIndex((d) => d.isToday))
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function selectDay(index: number) {
  if (index < 0) index = 0
  selectedIndex.value = index
  const day = weekDays.value[index]
  if (day) {
    shifts.value = allShifts.value.filter((s) => s.date === day.fullDate)
  }
}

function prevWeek() {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() - 7)
  currentWeekStart.value = d
  buildWeekDays(d)
  loadShifts()
}

function nextWeek() {
  const d = new Date(currentWeekStart.value)
  d.setDate(d.getDate() + 7)
  currentWeekStart.value = d
  buildWeekDays(d)
  loadShifts()
}

onMounted(() => {
  buildWeekDays(currentWeekStart.value)
  loadShifts()
})
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.week-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
}

.nav-arrow {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #666;
}

.week-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.week-days {
  display: flex;
  background: #fff;
  padding: 0 20rpx 20rpx;
}

.day-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12rpx 0;
  border-radius: 12rpx;
  position: relative;
}

.day-cell.today {
  background: #f0fdf4;
}

.day-cell.selected {
  background: #07c160;
}

.day-cell.selected .day-name,
.day-cell.selected .day-date {
  color: #fff;
}

.day-name {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 6rpx;
}

.day-date {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.day-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #07c160;
  position: absolute;
  bottom: 4rpx;
}

.shift-scroll {
  flex: 1;
  padding: 20rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #ccc;
}

.shift-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.shift-time {
  width: 140rpx;
  text-align: center;
  border-right: 1rpx solid #f0f0f0;
  padding-right: 20rpx;
  margin-right: 20rpx;
}

.shift-time-text {
  font-size: 26rpx;
  color: #07c160;
  font-weight: 500;
}

.shift-info {
  flex: 1;
}

.shift-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 6rpx;
  display: block;
}

.shift-location {
  font-size: 24rpx;
  color: #999;
}

.shift-status {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  white-space: nowrap;
}

.shift-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.shift-status.in_progress {
  background: #e6f7ff;
  color: #1890ff;
}

.shift-status.completed {
  background: #f6ffed;
  color: #52c41a;
}
</style>
