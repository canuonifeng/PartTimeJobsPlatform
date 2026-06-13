# Enterprise UniApp Redesign Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the redesigned enterprise miniapp shell with four primary tabs: 工作台、流程、待办、我的.

**Architecture:** Phase 1 keeps backend APIs unchanged and focuses on `enterprise-uniapp` routing, shared visual styles, and four top-level pages. Existing business pages remain available and are linked from the new shell so the app is usable after this phase.

**Tech Stack:** UniApp, Vue 3 `<script setup>`, Pinia auth store, existing `request` API wrapper, WeChat mini program compatible CSS.

---

## Scope

This is the first implementation phase for `docs/superpowers/specs/2026-06-13-enterprise-uniapp-redesign-design.md`.

Included:

- Add tabBar with 工作台、流程、待办、我的.
- Redesign `pages/home/index` as the new 工作台.
- Create `pages/process/process` for the流程时间线.
- Create `pages/todos/todoList` for业务类型待办.
- Create `pages/profile/profile` for企业与基础管理.
- Add shared shell classes in `App.vue` for cards, badges, section headers, and bottom-safe spacing.
- Keep existing secondary pages and API calls working.

Deferred to later phases:

- Full redesign of every secondary list and form page.
- Step-by-step job publishing form refactor.
- Deep interaction changes for salary settlement, attendance correction, and account management.

## File Structure

- Modify: `enterprise-uniapp/src/pages.json`
  - Adds tabBar config and registers new tab pages.
- Modify: `enterprise-uniapp/src/App.vue`
  - Adds shared redesign utility classes and CSS variables.
- Modify: `enterprise-uniapp/src/pages/home/index.vue`
  - Replaces menu-grid homepage with redesigned workbench.
- Create: `enterprise-uniapp/src/pages/process/process.vue`
  - New process timeline tab.
- Create: `enterprise-uniapp/src/pages/todos/todoList.vue`
  - New grouped todo tab.
- Create: `enterprise-uniapp/src/pages/profile/profile.vue`
  - New profile and management tab.

## Existing Paths To Reuse

The new shell must link to these existing pages:

- `/pages/jobs/jobForm`
- `/pages/jobs/jobList`
- `/pages/applications/applicationList`
- `/pages/schedules/scheduleList`
- `/pages/attendance/attendanceList`
- `/pages/locations/locationList`
- `/pages/templates/templateList`
- `/pages/workers/workerList`
- `/pages/accounts/accountList`
- `/pages/balance/balanceList`
- `/pages/settings/companySettings`
- `/pages/auth/realName`

## Task 1: Configure Tab Routes

**Files:**
- Modify: `enterprise-uniapp/src/pages.json`

- [ ] **Step 1: Open current route config**

Run:

```bash
sed -n '1,260p' enterprise-uniapp/src/pages.json
```

Expected: current file contains `pages/home/index` but no `tabBar`.

- [ ] **Step 2: Replace route config with tabBar-enabled config**

Set `enterprise-uniapp/src/pages.json` to:

```json
{
  "pages": [
    {"path": "pages/login/login", "style": {"navigationBarTitleText": "登录"}},
    {"path": "pages/home/index", "style": {"navigationBarTitleText": "工作台"}},
    {"path": "pages/process/process", "style": {"navigationBarTitleText": "流程"}},
    {"path": "pages/todos/todoList", "style": {"navigationBarTitleText": "待办", "enablePullDownRefresh": true}},
    {"path": "pages/profile/profile", "style": {"navigationBarTitleText": "我的"}},
    {"path": "pages/jobs/jobList", "style": {"navigationBarTitleText": "职位管理", "enablePullDownRefresh": true}},
    {"path": "pages/jobs/jobForm", "style": {"navigationBarTitleText": "发布职位"}},
    {"path": "pages/jobs/jobDetail", "style": {"navigationBarTitleText": "职位详情"}},
    {"path": "pages/jobs/applicationList", "style": {"navigationBarTitleText": "报名记录", "enablePullDownRefresh": true}},
    {"path": "pages/locations/locationList", "style": {"navigationBarTitleText": "工作地点", "enablePullDownRefresh": true}},
    {"path": "pages/locations/locationForm", "style": {"navigationBarTitleText": "地点表单"}},
    {"path": "pages/templates/templateList", "style": {"navigationBarTitleText": "职位模版", "enablePullDownRefresh": true}},
    {"path": "pages/templates/templateForm", "style": {"navigationBarTitleText": "模版表单"}},
    {"path": "pages/applications/applicationList", "style": {"navigationBarTitleText": "应聘管理", "enablePullDownRefresh": true}},
    {"path": "pages/schedules/scheduleList", "style": {"navigationBarTitleText": "排班考勤", "enablePullDownRefresh": true}},
    {"path": "pages/attendance/attendanceList", "style": {"navigationBarTitleText": "薪资管理", "enablePullDownRefresh": true}},
    {"path": "pages/workers/workerList", "style": {"navigationBarTitleText": "兼职管理", "enablePullDownRefresh": true}},
    {"path": "pages/accounts/accountList", "style": {"navigationBarTitleText": "账号管理", "enablePullDownRefresh": true}},
    {"path": "pages/settings/companySettings", "style": {"navigationBarTitleText": "企业设置"}},
    {"path": "pages/balance/balanceList", "style": {"navigationBarTitleText": "账户余额", "enablePullDownRefresh": true}},
    {"path": "pages/auth/realName", "style": {"navigationBarTitleText": "企业实名认证"}}
  ],
  "globalStyle": {
    "navigationBarTextStyle": "black",
    "navigationBarTitleText": "职管",
    "navigationBarBackgroundColor": "#F6F8F7",
    "backgroundColor": "#F6F8F7"
  },
  "tabBar": {
    "color": "#98A3B3",
    "selectedColor": "#16A34A",
    "backgroundColor": "#FFFFFF",
    "borderStyle": "white",
    "list": [
      {"pagePath": "pages/home/index", "text": "工作台"},
      {"pagePath": "pages/process/process", "text": "流程"},
      {"pagePath": "pages/todos/todoList", "text": "待办"},
      {"pagePath": "pages/profile/profile", "text": "我的"}
    ]
  }
}
```

- [ ] **Step 3: Validate JSON syntax**

Run:

```bash
node -e "JSON.parse(require('fs').readFileSync('enterprise-uniapp/src/pages.json','utf8')); console.log('pages.json ok')"
```

Expected: `pages.json ok`.

- [ ] **Step 4: Commit route config**

Run:

```bash
git add enterprise-uniapp/src/pages.json
git commit -m "feat(enterprise-uniapp): add redesigned tab routes"
```

Expected: commit succeeds with only `pages.json` staged.

## Task 2: Add Shared Visual Shell Styles

**Files:**
- Modify: `enterprise-uniapp/src/App.vue`

- [ ] **Step 1: Inspect existing global styles**

Run:

```bash
sed -n '1,260p' enterprise-uniapp/src/App.vue
```

Expected: file already defines `page`, CSS variables, `e-page`, `e-header`, `e-card`, badges, and button styles.

- [ ] **Step 2: Append redesigned shell classes inside `<style>`**

Add this CSS before the closing `</style>`:

```css
.op-page {
  min-height: 100vh;
  background: #f6f8f7;
  color: #1f2933;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}

.op-hero {
  margin: 0 28rpx;
  padding: 34rpx 32rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #18c86b 0%, #0f9f54 52%, #047857 100%);
  color: #fff;
  box-shadow: 0 18rpx 40rpx rgba(4, 120, 87, 0.18);
  box-sizing: border-box;
}

.op-hero-kicker {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.82);
}

.op-hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 42rpx;
  font-weight: 850;
  line-height: 1.22;
  color: #fff;
}

.op-hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.82);
}

.op-content {
  padding: 24rpx 28rpx 48rpx;
  box-sizing: border-box;
}

.op-section {
  margin-top: 28rpx;
}

.op-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.op-section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #1f2933;
}

.op-section-link {
  font-size: 24rpx;
  font-weight: 700;
  color: #16a34a;
}

.op-card {
  background: #fff;
  border-radius: 28rpx;
  padding: 26rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
  box-sizing: border-box;
}

.op-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 800;
  color: #12834a;
  background: #ecfdf5;
}

.op-pill-warn {
  color: #b45309;
  background: #fffbeb;
}

.op-pill-danger {
  color: #dc2626;
  background: #fee2e2;
}

.op-row {
  display: flex;
  align-items: center;
  min-width: 0;
}

.op-row-main {
  flex: 1;
  min-width: 0;
}

.op-row-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1f2933;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.op-row-desc {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  line-height: 1.45;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
```

- [ ] **Step 3: Run syntax-oriented build check**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: build may show known non-blocking warnings, but exits with code 0.

- [ ] **Step 4: Commit shared styles**

Run:

```bash
git add enterprise-uniapp/src/App.vue
git commit -m "style(enterprise-uniapp): add redesigned shell styles"
```

Expected: commit succeeds with only `App.vue` staged.

## Task 3: Redesign Workbench Page

**Files:**
- Modify: `enterprise-uniapp/src/pages/home/index.vue`

- [ ] **Step 1: Replace page implementation**

Set `enterprise-uniapp/src/pages/home/index.vue` to:

```vue
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/store'
import request from '@/api/request'

const authStore = useAuthStore()
const companyName = ref('')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const stats = [
  { label: '发布中', value: 6 },
  { label: '总报名', value: 48 },
  { label: '待处理', value: 12 }
]

const flowSteps = [
  { name: '发布', value: '6', state: 'done' },
  { name: '报名', value: '48', state: 'done' },
  { name: '审核', value: '12', state: 'warn' },
  { name: '薪资', value: '5', state: 'todo' }
]

const todos = [
  { type: '审', title: '12 个报名等待审核', desc: '服务员、分拣员岗位报名较多', tag: '紧急', path: '/pages/applications/applicationList' },
  { type: '班', title: '5 个班次未排满', desc: '今晚 18:00 前建议处理', tag: '去处理', path: '/pages/schedules/scheduleList' }
]

const quickActions = [
  { name: '发布职位', icon: '发', path: '/pages/jobs/jobForm' },
  { name: '审核报名', icon: '审', path: '/pages/applications/applicationList' },
  { name: '创建排班', icon: '排', path: '/pages/schedules/scheduleList' },
  { name: '薪资结算', icon: '薪', path: '/pages/attendance/attendanceList' }
]

onMounted(async () => {
  try {
    const res = await request('GET', '/enterprise')
    companyName.value = res?.companyName || ''
  } catch {}
})

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function switchToProcess() {
  uni.switchTab({ url: '/pages/process/process' })
}

function switchToTodos() {
  uni.switchTab({ url: '/pages/todos/todoList' })
}
</script>

<template>
  <scroll-view scroll-y class="op-page workbench-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">{{ greeting }}，{{ authStore.displayName || '企业管理员' }}</text>
      <text class="op-hero-title">今天有 12 项招聘任务待推进</text>
      <text class="op-hero-desc">{{ companyName || '企业名称' }} · 发布中岗位报名转化较昨日更活跃</text>
    </view>

    <view class="stats-grid">
      <view v-for="item in stats" :key="item.label" class="stat-card">
        <text class="stat-value">{{ item.value }}</text>
        <text class="stat-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="op-content">
      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">招聘流程</text>
          <text class="op-section-link" @click="switchToProcess">查看流程</text>
        </view>
        <view class="op-card flow-card">
          <view v-for="step in flowSteps" :key="step.name" class="flow-item" :class="'flow-' + step.state">
            <text class="flow-value">{{ step.value }}</text>
            <text class="flow-name">{{ step.name }}</text>
          </view>
        </view>
      </view>

      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">今日待办</text>
          <text class="op-section-link" @click="switchToTodos">全部</text>
        </view>
        <view class="op-card todo-card">
          <view v-for="item in todos" :key="item.title" class="todo-row" @click="navigateTo(item.path)">
            <view class="todo-icon">{{ item.type }}</view>
            <view class="op-row-main">
              <text class="op-row-title">{{ item.title }}</text>
              <text class="op-row-desc">{{ item.desc }}</text>
            </view>
            <text class="op-pill op-pill-warn">{{ item.tag }}</text>
          </view>
        </view>
      </view>

      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">快捷操作</text>
        </view>
        <view class="quick-grid">
          <view v-for="item in quickActions" :key="item.name" class="quick-item" @click="navigateTo(item.path)">
            <view class="quick-icon">{{ item.icon }}</view>
            <text class="quick-name">{{ item.name }}</text>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<style>
.workbench-page { height: 100vh; }
.top-space { height: 24rpx; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; margin: -24rpx 28rpx 0; position: relative; z-index: 2; }
.stat-card { background: rgba(255,255,255,.96); border-radius: 24rpx; padding: 22rpx 18rpx; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); box-sizing: border-box; }
.stat-value { display: block; font-size: 40rpx; line-height: 1; font-weight: 850; color: #12834a; }
.stat-label { display: block; margin-top: 10rpx; font-size: 22rpx; color: #64748b; }
.flow-card { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14rpx; }
.flow-item { border-radius: 22rpx; padding: 20rpx 8rpx; text-align: center; background: #ecfdf5; color: #12834a; }
.flow-warn { background: #fffbeb; color: #b45309; }
.flow-todo { background: #f1f5f9; color: #64748b; }
.flow-value { display: block; font-size: 34rpx; font-weight: 850; line-height: 1; }
.flow-name { display: block; margin-top: 10rpx; font-size: 22rpx; font-weight: 750; }
.todo-card { padding-top: 8rpx; padding-bottom: 8rpx; }
.todo-row { display: flex; align-items: center; min-width: 0; padding: 18rpx 0; border-bottom: 1rpx solid #edf0f3; }
.todo-row:last-child { border-bottom: none; }
.todo-icon { width: 72rpx; height: 72rpx; margin-right: 18rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; flex-shrink: 0; }
.quick-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; }
.quick-item { text-align: center; }
.quick-icon { width: 84rpx; height: 84rpx; margin: 0 auto 12rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; background: #ecfdf5; color: #16a34a; font-size: 32rpx; font-weight: 850; }
.quick-name { font-size: 23rpx; color: #334155; }
</style>
```

- [ ] **Step 2: Build to verify workbench syntax**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: build exits with code 0.

- [ ] **Step 3: Commit workbench**

Run:

```bash
git add enterprise-uniapp/src/pages/home/index.vue
git commit -m "feat(enterprise-uniapp): redesign workbench tab"
```

Expected: commit succeeds with only the workbench page staged.

## Task 4: Add Process Timeline Tab

**Files:**
- Create: `enterprise-uniapp/src/pages/process/process.vue`

- [ ] **Step 1: Create directory**

Run:

```bash
mkdir -p enterprise-uniapp/src/pages/process
```

Expected: directory exists.

- [ ] **Step 2: Create process page**

Create `enterprise-uniapp/src/pages/process/process.vue` with:

```vue
<script setup>
const steps = [
  { icon: '发', title: '发布职位', desc: '6 个岗位正在招聘中', tags: ['草稿 2', '已发布 6'], path: '/pages/jobs/jobList', state: 'done' },
  { icon: '报', title: '收到报名', desc: '今日新增 18 人报名', tags: ['总报名 48', '转化 +18%'], path: '/pages/applications/applicationList', state: 'done' },
  { icon: '审', title: '审核报名', desc: '12 人等待审核，建议优先处理', tags: ['待审核 12', '已通过 22'], path: '/pages/applications/applicationList', state: 'warn' },
  { icon: '班', title: '创建排班', desc: '5 个班次未满员', tags: ['今日 9 班', '缺口 7 人'], path: '/pages/schedules/scheduleList', state: 'done' },
  { icon: '薪', title: '薪资结算', desc: '待结算 5 条考勤记录', tags: ['预计 ¥1,240'], path: '/pages/attendance/attendanceList', state: 'muted' }
]

function navigateTo(path) {
  uni.navigateTo({ url: path })
}
</script>

<template>
  <scroll-view scroll-y class="op-page process-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">招聘运营流程</text>
      <text class="op-hero-title">报名审核阶段需要优先处理</text>
      <text class="op-hero-desc">当前链路：发布正常 · 报名充足 · 审核积压</text>
    </view>

    <view class="op-content">
      <view class="timeline">
        <view v-for="step in steps" :key="step.title" class="timeline-step" @click="navigateTo(step.path)">
          <view class="timeline-node" :class="'node-' + step.state">{{ step.icon }}</view>
          <view class="timeline-card">
            <view class="op-row">
              <view class="op-row-main">
                <text class="op-row-title">{{ step.title }}</text>
                <text class="op-row-desc">{{ step.desc }}</text>
              </view>
              <text class="arrow">›</text>
            </view>
            <view class="tag-row">
              <text v-for="tag in step.tags" :key="tag" class="meta-tag">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<style>
.process-page { height: 100vh; }
.top-space { height: 24rpx; }
.timeline { padding-top: 6rpx; }
.timeline-step { position: relative; display: flex; padding-bottom: 26rpx; }
.timeline-step::before { content: ''; position: absolute; left: 35rpx; top: 76rpx; bottom: 0; width: 4rpx; background: #dbe8df; border-radius: 999rpx; }
.timeline-step:last-child::before { display: none; }
.timeline-node { width: 72rpx; height: 72rpx; margin-right: 18rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #16a34a; color: #fff; font-size: 28rpx; font-weight: 850; flex-shrink: 0; box-shadow: 0 10rpx 24rpx rgba(22,163,74,.18); }
.node-warn { background: #f59e0b; }
.node-muted { background: #cbd5e1; }
.timeline-card { flex: 1; min-width: 0; background: #fff; border-radius: 26rpx; padding: 24rpx; box-shadow: 0 12rpx 30rpx rgba(23,83,53,.08); box-sizing: border-box; }
.arrow { margin-left: 12rpx; color: #98a3b3; font-size: 40rpx; line-height: 1; }
.tag-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 18rpx; }
.meta-tag { padding: 8rpx 14rpx; border-radius: 999rpx; background: #f1f5f9; color: #64748b; font-size: 22rpx; font-weight: 700; }
</style>
```

- [ ] **Step 3: Build to verify process page**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: build exits with code 0.

- [ ] **Step 4: Commit process tab**

Run:

```bash
git add enterprise-uniapp/src/pages/process/process.vue
git commit -m "feat(enterprise-uniapp): add process timeline tab"
```

Expected: commit succeeds with only the process page staged.

## Task 5: Add Grouped Todo Tab

**Files:**
- Create: `enterprise-uniapp/src/pages/todos/todoList.vue`

- [ ] **Step 1: Create directory**

Run:

```bash
mkdir -p enterprise-uniapp/src/pages/todos
```

Expected: directory exists.

- [ ] **Step 2: Create todo page**

Create `enterprise-uniapp/src/pages/todos/todoList.vue` with:

```vue
<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'

const currentType = ref('applications')

const types = [
  { key: 'applications', name: '报名' },
  { key: 'schedules', name: '排班' },
  { key: 'attendance', name: '考勤' },
  { key: 'salary', name: '薪资' }
]

const todoMap = {
  applications: [
    { name: '张小雨', status: '待审核', title: '周末促销员', time: '10:24', desc: '23 岁 · 女', path: '/pages/applications/applicationList' },
    { name: '李明', status: '待审核', title: '仓库分拣员', time: '09:38', desc: '28 岁 · 男 · 做过 3 次', path: '/pages/applications/applicationList' }
  ],
  schedules: [
    { name: '晚班服务员', status: '未排满', title: '今日 18:00-22:00', time: '缺 2 人', desc: '星河门店', path: '/pages/schedules/scheduleList' }
  ],
  attendance: [
    { name: '考勤异常', status: '待确认', title: '2 条记录缺少签退', time: '今天', desc: '需确认实际工时', path: '/pages/schedules/scheduleList' }
  ],
  salary: [
    { name: '薪资结算', status: '待结算', title: '5 条考勤记录', time: '预计 ¥1,240', desc: '确认后可发起结算', path: '/pages/attendance/attendanceList' }
  ]
}

const currentTodos = computed(() => todoMap[currentType.value] || [])

function switchType(type) {
  currentType.value = type
}

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

onPullDownRefresh(() => {
  setTimeout(() => uni.stopPullDownRefresh(), 300)
})
</script>

<template>
  <scroll-view scroll-y class="op-page todo-page">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">待办中心</text>
      <text class="op-hero-title">按业务类型处理今天的事项</text>
      <text class="op-hero-desc">报名 12 · 排班 5 · 考勤 2 · 薪资 5</text>
    </view>

    <scroll-view scroll-x class="type-tabs" scroll-with-animation>
      <view class="type-tabs-inner">
        <view v-for="type in types" :key="type.key" class="type-tab" :class="{ active: currentType === type.key }" @click="switchType(type.key)">
          {{ type.name }}
        </view>
      </view>
    </scroll-view>

    <view class="op-content">
      <view v-for="item in currentTodos" :key="item.name + item.title" class="op-card task-card" @click="navigateTo(item.path)">
        <view class="task-head">
          <text class="task-name">{{ item.name }}</text>
          <text class="op-pill op-pill-warn">{{ item.status }}</text>
        </view>
        <view class="task-grid">
          <view class="task-info">
            <text class="task-label">事项</text>
            <text class="task-value">{{ item.title }}</text>
          </view>
          <view class="task-info">
            <text class="task-label">时间</text>
            <text class="task-value">{{ item.time }}</text>
          </view>
          <view class="task-info wide">
            <text class="task-label">说明</text>
            <text class="task-value">{{ item.desc }}</text>
          </view>
        </view>
        <view class="task-actions">
          <view class="task-btn secondary">查看详情</view>
          <view class="task-btn primary">去处理</view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<style>
.todo-page { height: 100vh; }
.top-space { height: 24rpx; }
.type-tabs { width: 100%; margin-top: 22rpx; white-space: nowrap; }
.type-tabs-inner { display: flex; padding: 0 28rpx; }
.type-tab { flex-shrink: 0; margin-right: 16rpx; padding: 14rpx 28rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 26rpx; font-weight: 800; box-shadow: 0 8rpx 20rpx rgba(23,83,53,.06); }
.type-tab.active { background: #16a34a; color: #fff; }
.task-card { margin-bottom: 20rpx; }
.task-head { display: flex; align-items: center; justify-content: space-between; }
.task-name { font-size: 30rpx; font-weight: 850; color: #1f2933; }
.task-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin-top: 20rpx; }
.task-info { padding: 16rpx; border-radius: 18rpx; background: #f8fafc; min-width: 0; }
.task-info.wide { grid-column: span 2; }
.task-label { display: block; font-size: 22rpx; color: #64748b; }
.task-value { display: block; margin-top: 8rpx; font-size: 26rpx; font-weight: 750; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.task-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx; margin-top: 20rpx; }
.task-btn { height: 72rpx; line-height: 72rpx; border-radius: 18rpx; text-align: center; font-size: 26rpx; font-weight: 850; }
.task-btn.secondary { background: #ecfdf5; color: #16a34a; }
.task-btn.primary { background: #16a34a; color: #fff; }
</style>
```

- [ ] **Step 3: Build to verify todo page**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: build exits with code 0.

- [ ] **Step 4: Commit todo tab**

Run:

```bash
git add enterprise-uniapp/src/pages/todos/todoList.vue
git commit -m "feat(enterprise-uniapp): add grouped todo tab"
```

Expected: commit succeeds with only the todo page staged.

## Task 6: Add Profile Management Tab

**Files:**
- Create: `enterprise-uniapp/src/pages/profile/profile.vue`

- [ ] **Step 1: Create directory**

Run:

```bash
mkdir -p enterprise-uniapp/src/pages/profile
```

Expected: directory exists.

- [ ] **Step 2: Create profile page**

Create `enterprise-uniapp/src/pages/profile/profile.vue` with:

```vue
<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/store'
import request from '@/api/request'

const authStore = useAuthStore()
const companyName = ref('')

const managementItems = [
  { name: '工作地点', icon: '地', path: '/pages/locations/locationList' },
  { name: '职位模板', icon: '模', path: '/pages/templates/templateList' },
  { name: '兼职管理', icon: '人', path: '/pages/workers/workerList' },
  { name: '账号管理', icon: '号', path: '/pages/accounts/accountList' },
  { name: '账户余额', icon: '余', path: '/pages/balance/balanceList' },
  { name: '实名认证', icon: '认', path: '/pages/auth/realName' }
]

const settingItems = [
  { name: '企业资料', desc: '公司名称、联系人、营业信息', icon: '企', path: '/pages/settings/companySettings' },
  { name: '账号安全', desc: '登录手机号、密码和权限', icon: '安', path: '/pages/accounts/accountList' },
  { name: '资金与流水', desc: '余额、充值、支出记录', icon: '钱', path: '/pages/balance/balanceList' }
]

onMounted(async () => {
  try {
    const res = await request('GET', '/enterprise')
    companyName.value = res?.companyName || ''
  } catch {}
})

function navigateTo(path) {
  uni.navigateTo({ url: path })
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前企业账号吗？',
    success: (res) => {
      if (res.confirm) authStore.logout()
    }
  })
}
</script>

<template>
  <scroll-view scroll-y class="op-page profile-page">
    <view class="profile-card">
      <view class="avatar">企</view>
      <view class="op-row-main">
        <text class="profile-name">{{ companyName || '企业名称' }}</text>
        <text class="profile-desc">已认证 · 管理员：{{ authStore.displayName || '企业管理员' }}</text>
      </view>
    </view>

    <view class="op-content">
      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">常用管理</text>
        </view>
        <view class="menu-grid">
          <view v-for="item in managementItems" :key="item.name" class="menu-item" @click="navigateTo(item.path)">
            <view class="menu-icon">{{ item.icon }}</view>
            <text class="menu-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <view class="op-section">
        <view class="op-section-head">
          <text class="op-section-title">企业设置</text>
        </view>
        <view class="op-card setting-card">
          <view v-for="item in settingItems" :key="item.name" class="setting-row" @click="navigateTo(item.path)">
            <view class="setting-icon">{{ item.icon }}</view>
            <view class="op-row-main">
              <text class="op-row-title">{{ item.name }}</text>
              <text class="op-row-desc">{{ item.desc }}</text>
            </view>
            <text class="setting-arrow">›</text>
          </view>
          <view class="setting-row" @click="handleLogout">
            <view class="setting-icon danger">退</view>
            <view class="op-row-main">
              <text class="op-row-title">退出登录</text>
              <text class="op-row-desc">退出当前企业账号</text>
            </view>
            <text class="setting-arrow">›</text>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<style>
.profile-page { height: 100vh; }
.profile-card { display: flex; align-items: center; margin: 24rpx 28rpx 0; padding: 32rpx; border-radius: 32rpx; background: #fff; box-shadow: 0 12rpx 34rpx rgba(23,83,53,.08); box-sizing: border-box; }
.avatar { width: 104rpx; height: 104rpx; margin-right: 22rpx; border-radius: 32rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #18c86b, #047857); color: #fff; font-size: 42rpx; font-weight: 850; flex-shrink: 0; }
.profile-name { display: block; font-size: 34rpx; font-weight: 850; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.profile-desc { display: block; margin-top: 10rpx; font-size: 24rpx; color: #64748b; }
.menu-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; }
.menu-item { padding: 24rpx 8rpx; border-radius: 24rpx; background: #fff; text-align: center; box-shadow: 0 10rpx 24rpx rgba(23,83,53,.06); box-sizing: border-box; }
.menu-icon { width: 68rpx; height: 68rpx; margin: 0 auto 12rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: #ecfdf5; color: #16a34a; font-size: 28rpx; font-weight: 850; }
.menu-name { font-size: 24rpx; color: #334155; font-weight: 750; }
.setting-card { padding-top: 4rpx; padding-bottom: 4rpx; }
.setting-row { display: flex; align-items: center; min-width: 0; padding: 22rpx 0; border-bottom: 1rpx solid #edf0f3; }
.setting-row:last-child { border-bottom: none; }
.setting-icon { width: 72rpx; height: 72rpx; margin-right: 18rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: #ecfdf5; color: #16a34a; font-size: 28rpx; font-weight: 850; flex-shrink: 0; }
.setting-icon.danger { background: #fee2e2; color: #dc2626; }
.setting-arrow { margin-left: 12rpx; color: #98a3b3; font-size: 40rpx; }
</style>
```

- [ ] **Step 3: Build to verify profile page**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: build exits with code 0.

- [ ] **Step 4: Commit profile tab**

Run:

```bash
git add enterprise-uniapp/src/pages/profile/profile.vue
git commit -m "feat(enterprise-uniapp): add profile management tab"
```

Expected: commit succeeds with only the profile page staged.

## Task 7: Final Phase 1 Verification

**Files:**
- Verify all files changed in this phase.

- [ ] **Step 1: Run mini program build**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: build exits with code 0. Known Sass or Rollup warnings can be ignored if the command succeeds.

- [ ] **Step 2: Run H5 build**

Run:

```bash
cd enterprise-uniapp && npm run build:h5
```

Expected: build exits with code 0.

- [ ] **Step 3: Check working tree**

Run:

```bash
git status --short --branch
```

Expected: only intentional files are modified or untracked. Do not stage or commit `enterprise-uniapp/dist`.

- [ ] **Step 4: Check whitespace**

Run:

```bash
git diff --check
```

Expected: no whitespace errors.

- [ ] **Step 5: Commit final verification adjustments if any**

If verification requires small fixes, stage only the Phase 1 source files:

```bash
git add enterprise-uniapp/src/pages.json enterprise-uniapp/src/App.vue enterprise-uniapp/src/pages/home/index.vue enterprise-uniapp/src/pages/process/process.vue enterprise-uniapp/src/pages/todos/todoList.vue enterprise-uniapp/src/pages/profile/profile.vue
git commit -m "chore(enterprise-uniapp): verify redesigned shell"
```

Expected: commit succeeds only if there were post-verification source fixes. Skip this commit when there are no additional changes.

## Self-Review

- Spec coverage: This plan covers the first phase of the spec: four Tab architecture, redesigned workbench, process timeline, grouped todo tab, profile management tab, and shared visual shell classes.
- Deferred requirements: Full secondary page redesign and step-by-step job form are intentionally deferred to later phase plans because they are independent subsystems.
- Placeholder scan: The plan does not contain TBD/TODO placeholders or unspecified code steps.
- Type consistency: All page paths match `pages.json`; navigation uses `uni.navigateTo` for secondary pages and `uni.switchTab` for tab pages.
