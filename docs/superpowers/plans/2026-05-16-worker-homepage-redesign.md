# Worker Homepage Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the C-end home page around today's shift, its签到状态, and a direct签到入口 while keeping the greeting banner and removing the首页找活入口.

**Architecture:** Keep the home page self-contained and data-driven from the existing schedule and attendance APIs. Reuse `getMyShifts` for today's and future shifts, derive the primary card state on the client, and send users to the existing签到页 when they need to perform the actual geolocation-based check-in. Keep the tab bar unchanged so `找活` remains available there.

**Tech Stack:** Vue 3 + uni-app + TypeScript + existing `@/api/schedule`, `@/api/attendance`, Pinia auth store.

---

### Task 1: Replace the home page data flow

**Files:**
- Modify: `worker-uniapp/src/pages/index/index.vue`
- Test: `worker-uniapp/src/pages/index/index.vue` (manual visual check in browser/devtools)

- [ ] **Step 1: Remove the old jobs-focused home state and wire shift data instead**

```ts
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store'
import { getMyShifts } from '@/api/schedule'

const authStore = useAuthStore()
const loading = ref(false)
const loadError = ref('')
const shifts = ref<any[]>([])

const today = computed(() => new Date())
const todayKey = computed(() => formatDateKey(today.value))

function formatDateKey(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatWeekday(date: Date) {
  return ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][date.getDay()]
}

function labelForStatus(status?: string) {
  if (status === 'CHECKED_IN') return '已签到'
  if (status === 'CHECKED_OUT') return '已签退'
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
  return '待签到'
}

function isCheckedIn(status?: string) {
  return status === 'CHECKED_IN' || status === 'CHECKED_OUT'
}

function isCheckedOut(status?: string) {
  return status === 'CHECKED_OUT'
}

async function loadShifts() {
  loading.value = true
  loadError.value = ''
  try {
    const startDate = formatDateKey(today.value)
    const endDate = formatDateKey(new Date(today.value.getTime() + 6 * 24 * 60 * 60 * 1000))
    const res: any = await getMyShifts({ startDate, endDate })
    shifts.value = Array.isArray(res) ? res : (res.list || [])
  } catch (err: any) {
    loadError.value = err?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function navTo(url: string) {
  uni.navigateTo({ url })
}

onMounted(loadShifts)
```

- [ ] **Step 2: Run the page in dev mode and confirm there is no `找活` shortcut in the homepage state**

Run: `npm run dev:mp-weixin`

Expected: the home page renders with shift data, and no jobs-focused card remains in the template.

### Task 2: Redesign the home layout around today and future shifts

**Files:**
- Modify: `worker-uniapp/src/pages/index/index.vue`

- [ ] **Step 1: Replace the old stat grid and hot jobs block with a greeting header, today's shift card, future shifts, and quick actions**

```vue
<template>
  <view class="home-page">
    <view class="header-banner">
      <view class="greeting">
        <text class="greeting-text">{{ greeting }},</text>
        <text class="user-name">{{ authStore.workerInfo?.name || '工人' }}</text>
      </view>
      <text class="sub-text">{{ todayLabel }} · {{ todayWeekday }} · {{ todayShiftCountText }}</text>
    </view>

    <view v-if="loadError" class="empty-card">
      <text class="empty-title">排班加载失败</text>
      <text class="empty-desc">{{ loadError }}</text>
      <button class="primary-btn" @click="loadShifts">重试</button>
    </view>

    <view v-else-if="!authStore.workerInfo" class="empty-card">
      <text class="empty-title">请先登录</text>
      <text class="empty-desc">登录后可以看到今日排班和签到状态</text>
      <button class="primary-btn" @click="navTo('/pages/login/login')">去登录</button>
    </view>

    <template v-else>
      <view class="section">
        <view class="section-hd">
          <view>
            <text class="section-title">今日排班</text>
            <text class="section-sub">签到状态和签到操作放在最前面</text>
          </view>
          <text class="status-pill" :class="todayShift ? statusClass(todayShift.status) : 'status-wait'">{{ todayShift ? labelForStatus(todayShift.status) : '无排班' }}</text>
        </view>

        <view v-if="todayShift" class="today-card">
          <view class="today-badge">今日主班次</view>
          <text class="today-job">{{ todayShift.jobTitle }}</text>
          <view class="today-meta">
            <view class="meta-item">
              <text class="meta-label">时间</text>
              <text class="meta-value">{{ todayShift.startTime }} - {{ todayShift.endTime }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">地点</text>
              <text class="meta-value">{{ todayShift.location }}</text>
            </view>
          </view>
          <button class="primary-btn" @click="navTo('/pages/attendance/clockIn')">
            {{ isCheckedOut(todayShift.status) ? '已签退' : isCheckedIn(todayShift.status) ? '已签到' : '去签到' }}
          </button>
          <button class="secondary-btn" @click="navTo('/pages/schedule/schedule')">查看今日全部排班</button>
        </view>

        <view v-else class="empty-inline">
          <text class="empty-title">今天没有排班</text>
          <text class="empty-desc">可以先看看接下来的排班</text>
          <button class="secondary-btn" @click="navTo('/pages/schedule/schedule')">查看排班</button>
        </view>
      </view>

      <view class="section">
        <view class="section-hd">
          <view>
            <text class="section-title">我的未来排班</text>
            <text class="section-sub">最近 5 条</text>
          </view>
          <text class="section-link" @click="navTo('/pages/schedule/schedule')">查看全部</text>
        </view>
        <view class="future-list" v-if="futureShifts.length">
          <view v-for="shift in futureShifts" :key="shift.id" class="future-item">
            <view class="future-top">
              <text class="future-job">{{ shift.jobTitle }}</text>
              <text class="future-time">{{ shift.date }} {{ shift.startTime }}-{{ shift.endTime }}</text>
            </view>
            <text class="future-location">{{ shift.location }}</text>
            <text class="future-status">{{ labelForStatus(shift.status) }}</text>
          </view>
        </view>
        <view v-else class="empty-inline">
          <text class="empty-title">暂无未来排班</text>
          <text class="empty-desc">排班信息会在这里展示</text>
        </view>
      </view>

      <view class="section">
        <view class="section-hd">
          <view>
            <text class="section-title">快捷入口</text>
            <text class="section-sub">只保留工作流相关入口</text>
          </view>
        </view>
        <view class="quick-grid">
          <view class="quick-item" @click="navTo('/pages/schedule/schedule')">
            <text class="quick-title">我的排班</text>
            <text class="quick-desc">查看本周和未来排班</text>
          </view>
          <view class="quick-item" @click="navTo('/pages/attendance/clockIn')">
            <text class="quick-title">打卡</text>
            <text class="quick-desc">进入今日签到/签退</text>
          </view>
          <view class="quick-item" @click="navTo('/pages/earnings/earnings')">
            <text class="quick-title">我的收入</text>
            <text class="quick-desc">查看工时与工资</text>
          </view>
          <view class="quick-item" @click="navTo('/pages/message/message')">
            <text class="quick-title">消息</text>
            <text class="quick-desc">查看通知和提醒</text>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>
```

- [ ] **Step 2: Replace the old hot-jobs styles with mobile-first cards for the new layout**

```css
.home-page {
  padding: 30rpx;
}

.header-banner {
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #07c160, #06ad56);
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  color: #fff;
}

.section {
  margin-bottom: 24rpx;
}

.section-hd {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}

.today-card,
.future-item,
.quick-item,
.empty-card,
.empty-inline {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.primary-btn {
  width: 100%;
  margin-top: 14rpx;
  background: #07c160;
  color: #fff;
  border-radius: 14rpx;
}

.secondary-btn {
  width: 100%;
  margin-top: 10rpx;
  background: #fff;
  color: #07c160;
  border: 1rpx solid #b7efd0;
  border-radius: 14rpx;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.quick-item {
  min-height: 86rpx;
}
```

- [ ] **Step 3: Build and visually verify the page on mobile width**

Run: `npm run build:mp-weixin`

Expected: build completes, and the home page shows welcome, today's shift, future shifts, and quick actions without the old `找活` block.

### Task 3: Confirm navigation and keep `找活` only in the tab bar

**Files:**
- Modify: `worker-uniapp/src/pages/index/index.vue`
- Verify: `worker-uniapp/src/pages.json`

- [ ] **Step 1: Ensure the home page uses only existing routes for navigation**

```ts
function navTo(url: string) {
  uni.navigateTo({ url })
}
```

- [ ] **Step 2: Manually confirm `pages/jobs/jobList` still exists in the tab bar and is not shown in the home shortcuts**

Run: open `worker-uniapp/src/pages.json`

Expected: `tabBar.list` still contains `pages/jobs/jobList`, while the home template does not render any `找活` shortcut.

- [ ] **Step 3: Commit the homepage redesign**

```bash
git add worker-uniapp/src/pages/index/index.vue docs/superpowers/plans/2026-05-16-worker-homepage-redesign.md
git commit -m "feat: redesign worker homepage around shifts"
```
