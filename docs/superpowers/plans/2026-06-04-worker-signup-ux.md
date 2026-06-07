# Worker Signup UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve worker-side schedule signup UX by hiding home distance, marking applied/full shifts, and guiding incomplete profiles before signup.

**Architecture:** Keep UI behavior in `worker-uniapp` pages and reuse existing profile completeness API. Add backend schedule application counts only if the detail payload cannot determine full schedules from existing fields.

**Tech Stack:** Vue 3 `<script setup>` in uni-app, existing request API wrappers, Spring Boot Java service, Maven.

---

### Task 1: Hide home schedule distance

**Files:**
- Modify: `worker-uniapp/src/pages/index/index.vue:50-58`

- [ ] Remove the distance meta row from today's schedule cards while keeping location navigation and check-in location logic unchanged.

Expected template change:
```vue
<view class="shift-meta">
  <view class="meta-item"><text class="meta-icon">时</text><text class="meta-text">{{ shift.date }} {{ shift.startTime }} - {{ shift.endTime }}</text></view>
  <view class="meta-item address-action" :class="{ 'address-disabled': !shift.lat || !shift.lng }" @click="openMap(shift)">
    <text class="meta-icon">地</text>
    <text class="meta-text address-text">{{ formatAddress(shift.location) }}</text>
    <text class="nav-hint">📍</text>
  </view>
</view>
```

### Task 2: Mark applied and full schedules on job detail

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/jobDetail.vue:40-168`
- If needed, modify: `c-service/src/main/java/com/parttime/cservice/pojo/vo/JobScheduleInfoVO.java`
- If needed, modify: `c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java`

- [ ] Add helper functions:
```ts
function isScheduleFull(slot: any) {
  if (isScheduleApplied(slot?.id)) return false
  const remaining = Number(slot?.remainingSlots ?? slot?.slotsAvailable)
  return Number.isFinite(remaining) && remaining <= 0
}

function scheduleBlockClass(slot: any) {
  const id = Number(slot?.id)
  if (appliedScheduleIds.value.includes(id)) return 'block-applied'
  if (isScheduleFull(slot)) return 'block-full'
  if (pendingScheduleIds.value.includes(id)) return 'block-selected'
  return ''
}
```

- [ ] Update template:
```vue
<view v-for="slot in schedules" :key="slot.id" class="schedule-block" :class="scheduleBlockClass(slot)" @click="toggleSchedule(slot)">
  <text v-if="isScheduleApplied(slot.id)" class="block-badge">已报名</text>
  <text v-else-if="isScheduleFull(slot)" class="block-badge full">已报满</text>
  <text v-else-if="pendingScheduleIds.includes(Number(slot.id))" class="block-badge selected">已选</text>
  <text class="block-date">{{ scheduleDate(slot) }}</text>
  <text class="block-time">{{ scheduleTime(slot) }}</text>
  <text class="block-duration">{{ scheduleDuration(slot) }}</text>
</view>
```

- [ ] Update click handler:
```ts
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
```

- [ ] Add styles:
```css
.block-full { border-color: #d1d5db; background: #f3f4f6; opacity: 0.72; }
.block-badge.full { background: #ef4444; }
```

### Task 3: Check profile completeness before navigating to apply confirmation

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/jobDetail.vue:85-255`
- Existing API: `worker-uniapp/src/api/profile.js:18-23`

- [ ] Import completeness API:
```ts
import { getProfileCompleteness } from '@/api/profile'
```

- [ ] Make `handleApply` async and check profile completeness before `navigateTo`:
```ts
async function handleApply() {
  if (pendingScheduleIds.value.length === 0) {
    uni.showToast({ title: '请先选择排班', icon: 'none' })
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
  const scheduleIds = encodeURIComponent(pendingScheduleIds.value.join(','))
  uni.navigateTo({ url: `/pages/jobs/applyConfirm?jobId=${jobId.value}&scheduleIds=${scheduleIds}` })
}
```

### Task 4: Mark applied and full schedules on apply confirmation

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/applyConfirm.vue:12-100`

- [ ] Extend `ScheduleItem` and schedule normalization:
```ts
interface ScheduleItem { id: number; date?: string; startTime?: string; endTime?: string; remainingSlots?: number; slotsAvailable?: number }
const appliedScheduleIds = computed(() => Array.isArray(job.value?.appliedScheduleIds) ? job.value.appliedScheduleIds.map(Number) : [])
const schedules = computed<ScheduleItem[]>(() => (Array.isArray(job.value?.schedules) ? job.value.schedules : []).map((item: any) => ({ id: Number(item.id), date: item.date, startTime: item.startTime, endTime: item.endTime, remainingSlots: Number(item.remainingSlots), slotsAvailable: Number(item.slotsAvailable) })).filter((item: ScheduleItem) => Number.isInteger(item.id) && item.id > 0))
```

- [ ] Add reusable state helpers:
```ts
function isScheduleApplied(id: number) { return appliedScheduleIds.value.includes(Number(id)) }
function isScheduleFull(slot: ScheduleItem) {
  if (isScheduleApplied(slot.id)) return false
  const remaining = Number(slot.remainingSlots ?? slot.slotsAvailable)
  return Number.isFinite(remaining) && remaining <= 0
}
function isScheduleDisabled(slot: ScheduleItem) { return isScheduleApplied(slot.id) || isScheduleFull(slot) }
function scheduleStatusText(slot: ScheduleItem) {
  if (isScheduleApplied(slot.id)) return '已报名'
  if (isScheduleFull(slot)) return '已报满'
  return selectedScheduleIds.value.includes(slot.id) ? '已选' : '选择'
}
```

- [ ] Update date and schedule selection to ignore disabled schedules:
```ts
function toggleDate(date: string) {
  const ids = displaySchedules.value.filter((slot) => (slot.date || '日期待定') === date && !isScheduleDisabled(slot)).map((slot) => slot.id)
  const allSelected = ids.length > 0 && ids.every((id) => selectedScheduleIds.value.includes(id))
  selectedScheduleIds.value = allSelected ? selectedScheduleIds.value.filter((id) => !ids.includes(id)) : Array.from(new Set([...selectedScheduleIds.value, ...ids]))
}
function toggleSchedule(slot: ScheduleItem) {
  if (isScheduleApplied(slot.id)) return
  if (isScheduleFull(slot)) return uni.showToast({ title: '该班次已报满', icon: 'none' })
  const index = selectedScheduleIds.value.indexOf(slot.id)
  index >= 0 ? selectedScheduleIds.value.splice(index, 1) : selectedScheduleIds.value.push(slot.id)
}
```

- [ ] After loading job detail, remove disabled schedules from `selectedScheduleIds`:
```ts
selectedScheduleIds.value = selectedScheduleIds.value.filter((id) => {
  const slot = schedules.value.find((item) => item.id === id)
  return !slot || !isScheduleDisabled(slot)
})
```

- [ ] Update template and styles:
```vue
<view v-for="slot in displaySchedules" :key="slot.id" class="time" :class="{ active: selectedScheduleIds.includes(slot.id), disabled: isScheduleDisabled(slot) }" @click="toggleSchedule(slot)">
  <view class="time-main"><text class="main">{{ formatDate(slot.date) }}</text><text class="sub">{{ formatTime(slot) }}</text></view><text class="status" :class="{ full: isScheduleFull(slot) }">{{ scheduleStatusText(slot) }}</text>
</view>
```
```css
.time.disabled { border-color: #d1d5db; background: #f3f4f6; opacity: 0.72; }
.time.disabled .status { background: #9ca3af; color: #fff; }
.time.disabled .status.full { background: #ef4444; }
```

### Task 5: Remove schedule week/month mode switch

**Files:**
- Modify: `worker-uniapp/src/pages/schedule/schedule.vue:3-7`
- Modify: `worker-uniapp/src/pages/schedule/schedule.vue:72`
- Modify: `worker-uniapp/src/pages/schedule/schedule.vue:198-202`
- Modify: `worker-uniapp/src/pages/schedule/schedule.vue:298-300`

- [ ] Remove the top week/month mode switch from the template:
```vue
<view class="top-panel">
  <view class="week-nav">
```

- [ ] Remove unused `viewMode` state:
```ts
const correctionDialogVisible = ref(false)
```

- [ ] Keep the page weekly and make the title fixed to current week:
```ts
const periodTitle = computed(() => {
  const d = currentWeekStart.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月本周`
})
```

- [ ] Remove unused mode switch styles:
```css
.top-panel { background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%); padding: 28rpx 28rpx 34rpx; border-bottom-left-radius: 36rpx; border-bottom-right-radius: 36rpx; }
.week-nav { display: flex; justify-content: space-between; align-items: center; color: #fff; margin-bottom: 28rpx; }
```

### Task 6: Verify

**Commands:**
- Run worker build:
```bash
npm run build:mp-weixin
```
Expected: build succeeds with only known Sass/Rollup warnings.

- If backend changed, run c-service compile:
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile
```
Expected: compile succeeds.

- Run whitespace check:
```bash
git diff --check
```
Expected: no output.
