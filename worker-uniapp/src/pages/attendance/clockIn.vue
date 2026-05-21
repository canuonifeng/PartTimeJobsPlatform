<template>
  <view class="clockin-page">
    <view class="date-header">
      <text class="date-text">{{ todayDate }}</text>
      <text class="day-text">{{ todayName }}</text>
    </view>

    <uni-load-more v-if="loading" status="loading" />

    <scroll-view class="shift-scroll" scroll-y>
      <view v-if="shifts.length === 0 && !loading" class="empty-state">
        <text class="empty-text">今日暂无排班</text>
      </view>

      <view v-for="shift in shifts" :key="shift.id" class="shift-card">
        <view class="shift-header">
          <text class="shift-title">{{ shift.jobTitle }}</text>
          <text class="shift-time">{{ shift.startTime }}-{{ shift.endTime }}</text>
        </view>

        <view class="shift-body">
          <view class="shift-location">
            <text class="location-label">工作地点：</text>
            <text class="location-value">{{ shift.location }}</text>
          </view>
        </view>

        <view class="shift-actions">
          <button
            class="action-btn check-in"
            :class="{ disabled: shift.checkedIn }"
            :disabled="shift.checkedIn"
            @click="handleCheckIn(shift)"
          >
            {{ shift.checkedIn ? '已签到' : '签到' }}
          </button>
          <button
            class="action-btn check-out"
            :class="{ disabled: !shift.checkedIn || shift.checkedOut }"
            :disabled="!shift.checkedIn || shift.checkedOut"
            @click="handleCheckOut(shift)"
          >
            {{ shift.checkedOut ? '已签退' : '签退' }}
          </button>
        </view>

        <view v-if="shift.status" class="shift-status" :class="shift.status">
          {{ getStatusText(shift.status) }}
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyShifts } from '@/api/schedule'
import { checkIn, checkOut } from '@/api/attendance'

interface Shift {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
  checkedIn: boolean
  checkedOut: boolean
  attendanceId?: number
}

const loading = ref(false)
const shifts = ref<Shift[]>([])

const weekDayNames = ['日', '一', '二', '三', '四', '五', '六']

const todayDate = computed(() => {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}月${day}日`
})

const todayName = computed(() => {
  return `星期${weekDayNames[new Date().getDay()]}`
})

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    SCHEDULED: '待上岗',
    CHECKED_IN: '已签到',
    CHECKED_OUT: '已签退',
    ABSENT: '缺勤',
    LATE: '迟到'
  }
  return map[status] || status
}

function getLocation(): Promise<{ latitude: number; longitude: number }> {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: 'wgs84',
      success: (res) => {
        resolve({ latitude: res.latitude, longitude: res.longitude })
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

async function handleCheckIn(shift: Shift) {
  try {
    const location = await getLocation()
    await checkIn({
      shiftId: shift.id,
      lat: location.latitude,
      lng: location.longitude
    })
    shift.checkedIn = true
    shift.status = 'checked_in'
    uni.showToast({ title: '签到成功', icon: 'success' })
  } catch (err: any) {
    if (err.message?.includes('距离')) {
      uni.showToast({ title: '不在打卡范围内', icon: 'none' })
    } else {
      uni.showToast({ title: err.message || '签到失败', icon: 'none' })
    }
  }
}

async function handleCheckOut(shift: Shift) {
  try {
    const location = await getLocation()
    await checkOut({
      shiftId: shift.id,
      lat: location.latitude,
      lng: location.longitude
    })
    shift.checkedOut = true
    shift.status = 'checked_out'
    uni.showToast({ title: '签退成功', icon: 'success' })
  } catch (err: any) {
    if (err.message?.includes('距离')) {
      uni.showToast({ title: '不在打卡范围内', icon: 'none' })
    } else {
      uni.showToast({ title: err.message || '签退失败', icon: 'none' })
    }
  }
}

async function loadTodayShifts() {
  loading.value = true
  try {
    const today = new Date()
    const y = today.getFullYear()
    const m = String(today.getMonth() + 1).padStart(2, '0')
    const d = String(today.getDate()).padStart(2, '0')
    const dateStr = `${y}-${m}-${d}`

    const res: any = await getMyShifts({ startDate: dateStr, endDate: dateStr })
    const list = Array.isArray(res) ? res : (res.list || [])
    shifts.value = list.map((s: any) => ({
      id: s.id,
      jobTitle: s.jobTitle,
      location: s.location || s.locationName,
      startTime: s.startTime,
      endTime: s.endTime,
      date: s.date,
      status: s.status || '',
      checkedIn: s.status === 'CHECKED_IN' || s.status === 'CHECKED_OUT',
      checkedOut: s.status === 'CHECKED_OUT'
    }))
  } catch (err: any) {
    uni.showToast({ title: err.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(loadTodayShifts)
</script>

<style scoped>
.clockin-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.date-header {
  padding: 40rpx 30rpx;
  background: #fff;
  text-align: center;
}

.date-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
  display: block;
}

.day-text {
  font-size: 26rpx;
  color: #999;
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
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.shift-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.shift-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.shift-time {
  font-size: 26rpx;
  color: #07c160;
  font-weight: 500;
}

.shift-body {
  margin-bottom: 20rpx;
}

.location-label {
  font-size: 24rpx;
  color: #999;
}

.location-value {
  font-size: 24rpx;
  color: #666;
}

.shift-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  font-size: 28rpx;
  border: none;
  text-align: center;
}

.action-btn.check-in {
  background: #07c160;
  color: #fff;
}

.action-btn.check-out {
  background: #fff;
  color: #07c160;
  border: 1rpx solid #07c160;
}

.action-btn.disabled {
  background: #f5f5f5;
  color: #ccc;
  border-color: #f0f0f0;
}

.shift-status {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.shift-status.SCHEDULED {
  background: #fff7e6;
  color: #fa8c16;
}

.shift-status.CHECKED_IN {
  background: #e6f7ff;
  color: #1890ff;
}

.shift-status.CHECKED_OUT {
  background: #f6ffed;
  color: #52c41a;
}
</style>
