# 工人端微信小程序 4 个 Tab 视觉改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `worker-uniapp` 的首页、找活、消息、我的 4 个 Tab 改造成设计稿风格，并保持微信小程序构建可通过。

**Architecture:** 只改造现有 4 个 Tab 页面，保留 `pages.json`、TabBar、现有 API 模块和主要业务交互。页面内维护兜底数据：接口成功展示真实数据，接口失败或空数据展示设计稿示例数据。

**Tech Stack:** UniApp 3、Vue 3 `<script setup lang="ts">`、Pinia、微信小程序、`rpx`、现有 `uni-ui`。

---

## File Structure

- Modify: `worker-uniapp/src/pages/index/index.vue` — 首页排班打卡视觉改造。
- Modify: `worker-uniapp/src/pages/jobs/jobList.vue` — 找活岗位列表视觉改造。
- Modify: `worker-uniapp/src/pages/message/message.vue` — 静态消息中心。
- Modify: `worker-uniapp/src/pages/profile/profile.vue` — 我的页个人中心视觉改造。
- Verify: `worker-uniapp/package.json` — 使用已有 `npm run build:mp-weixin`。

---

### Task 1: 首页 Tab

**Files:**
- Modify: `worker-uniapp/src/pages/index/index.vue`

- [ ] **Step 1: 保留业务依赖**

保留导入：

```ts
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store'
import { getMyTopShifts } from '@/api/schedule'
import { checkIn, checkOut } from '@/api/attendance'
```

- [ ] **Step 2: 新增首页数据模型**

```ts
interface Shift {
  id: number
  jobTitle: string
  location: string
  startTime: string
  endTime: string
  date: string
  status: string
  icon: string
  distance?: string
}
const monthlyHours = ref('156')
const monthlyIncome = ref('4,680')
const attendanceDays = ref('28')
```

- [ ] **Step 3: 新增兜底排班数据**

```ts
const fallbackToday = [
  { id: -1, jobTitle: '仓库分拣员', location: '杭州市余杭区仓前街道利尔达科技园', startTime: '08:30', endTime: '16:30', status: 'SCHEDULED', icon: '📦', distance: '2.3km' },
  { id: -2, jobTitle: '餐厅服务员', location: '杭州市西湖区文三路外婆家餐厅', startTime: '17:00', endTime: '21:00', status: 'WAITING', icon: '🍽️', distance: '5.1km' }
]
```

- [ ] **Step 4: 改造模板**

实现：绿色渐变 `.home-header`、三块统计、未登录卡片、今日排班卡片列表、未来排班列表；签到和签退点击继续调用现有 `checkIn` / `checkOut`。

- [ ] **Step 5: 改造样式**

使用这些核心类：`.home-page`、`.home-header`、`.home-stat`、`.today-card`、`.badge`、`.warn-bar`、`.checkin-row`、`.future-item`。圆角 `24rpx`，阴影 `0 4rpx 24rpx rgba(0,0,0,.06)`。

- [ ] **Step 6: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 2: 找活 Tab

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/jobList.vue`

- [ ] **Step 1: 保留业务依赖和分页逻辑**

保留 `getJobs`、`keyword`、`categoryId`、`jobList`、`page`、`hasMore`、`loading`、`refreshing`、`loadCurrentLocation`、`onSearch`、`onCategoryChange`、`loadMore`、`onRefresh`、`goDetail`。

- [ ] **Step 2: 更新分类**

```ts
const categories = [
  { id: undefined, name: '全部' },
  { id: 1, name: '餐饮服务' },
  { id: 2, name: '物流配送' },
  { id: 3, name: '家政保洁' },
  { id: 4, name: '活动促销' },
  { id: 5, name: '仓库分拣' },
  { id: 6, name: '零售导购' }
]
```

- [ ] **Step 3: 新增兜底岗位和展示函数**

```ts
const fallbackJobs = [
  { id: -1, title: '外卖配送员', salary: '18元/单', location: '杭州市余杭区仓前街道', distanceKm: 2.3, companyName: '杭州XX餐饮管理有限公司', icon: '🛵', settlement: '日结' },
  { id: -2, title: '仓库分拣员', salary: '28元/时', location: '杭州市余杭区顺丰转运中心', distanceKm: 5.6, companyName: '顺丰速运', icon: '📦', settlement: '日结' }
]
const displayJobs = computed(() => jobList.value.length ? jobList.value : fallbackJobs)
```

- [ ] **Step 4: 改造模板和样式**

实现搜索栏、横向分类、岗位卡片。卡片包含图标/图片、岗位名、薪资、地点距离、日结/周结标签、企业名。保留下拉刷新和上拉加载。

- [ ] **Step 5: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 3: 消息 Tab

**Files:**
- Modify: `worker-uniapp/src/pages/message/message.vue`

- [ ] **Step 1: 新增本地消息数据**

```ts
import { computed, ref } from 'vue'
const activeTab = ref('system')
const tabs = [
  { key: 'system', name: '系统通知' },
  { key: 'company', name: '企业消息' },
  { key: 'feedback', name: '报名反馈' }
]
const messages = [
  { type: 'system', icon: '📢', name: '系统通知', time: '10分钟前', preview: '您报名的「仓库分拣员」已通过审核，请按时签到', unread: true },
  { type: 'company', icon: '🏢', name: '顺丰速运', time: '昨天', preview: '明天7:00的分拣工作请准时到场', unread: false },
  { type: 'feedback', icon: '📋', name: '报名反馈', time: '05-31', preview: '您报名的「展会协助员」已被录用', unread: false }
]
const filteredMessages = computed(() => messages.filter((item) => item.type === activeTab.value))
```

- [ ] **Step 2: 改造模板和样式**

实现 `.msg-tabs`、`.msg-tab.active`、`.msg-list`、`.msg-item`、`.msg-avatar`、`.msg-unread`。

- [ ] **Step 3: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 4: 我的 Tab

**Files:**
- Modify: `worker-uniapp/src/pages/profile/profile.vue`

- [ ] **Step 1: 保留业务依赖**

保留 `getProfile()`、`authStore.setWorkerInfo(res)`、`handleLogout()`。

- [ ] **Step 2: 新增展示数据**

```ts
const displayName = computed(() => profile.value?.name || authStore.workerInfo?.name || '倪先生')
const displayPhone = computed(() => profile.value?.phone || authStore.workerInfo?.phone || '138****6789')
const stats = [
  { num: '4,680', label: '累计收入(元)' },
  { num: '156', label: '累计工时(h)' },
  { num: '28', label: '出勤天数' }
]
```

- [ ] **Step 3: 改造模板和样式**

实现绿色个人信息 Header、本月收入卡片、三组菜单、退出登录按钮。已有页面正常跳转；不存在页面调用 `uni.showToast({ title: '功能建设中', icon: 'none' })`。

- [ ] **Step 4: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 5: 全量验证

**Files:**
- Verify all modified Tab pages

- [ ] **Step 1: 删除临时备份**

确认没有 `.bak` 文件进入变更集。

- [ ] **Step 2: 微信小程序构建**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

- [ ] **Step 3: 检查变更范围**

Run: `git status --short`
Expected: 只包含 4 个 Tab 页面和文档变更。

Run: `git diff -- worker-uniapp/src/pages/index/index.vue worker-uniapp/src/pages/jobs/jobList.vue worker-uniapp/src/pages/message/message.vue worker-uniapp/src/pages/profile/profile.vue`
Expected: 没有无关路由、接口、配置变更。

---

## Self-Review

- Spec coverage: 覆盖首页、找活、消息、我的 4 个 Tab；保留接口；微信小程序构建验证。
- Placeholder scan: 无未完成占位项。
- Type consistency: 页面内字段统一使用 `id/title/jobTitle/location/startTime/endTime/status/icon`，与现有接口字段通过 normalize/展示函数兼容。