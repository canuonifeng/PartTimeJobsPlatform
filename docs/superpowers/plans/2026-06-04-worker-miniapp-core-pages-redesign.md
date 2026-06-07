# 工人端微信小程序 核心闭环页面 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 改造岗位详情、报名确认、我的排班、打卡记录页面，补齐工人端微信小程序核心报名与排班打卡闭环。

**Architecture:** 保留现有 API 和页面结构，新增一个报名确认页并注册路由。详情页负责展示和选择排班，确认页负责提交报名，排班页和打卡页继续使用现有排班/考勤接口并加入视觉兜底数据。

**Tech Stack:** UniApp 3、Vue 3 `<script setup lang="ts">`、微信小程序、`rpx`、现有 `uni-ui`。

---

## File Structure

- Modify: `worker-uniapp/src/pages/jobs/jobDetail.vue`
- Create: `worker-uniapp/src/pages/jobs/applyConfirm.vue`
- Modify: `worker-uniapp/src/pages.json`
- Modify: `worker-uniapp/src/pages/schedule/schedule.vue`
- Modify: `worker-uniapp/src/pages/attendance/clockIn.vue`
- Verify: `worker-uniapp/package.json`

---

### Task 1: 岗位详情页

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/jobDetail.vue`

- [ ] **Step 1: 保留业务依赖**

保留 `getJobDetail`，移除直接报名提交逻辑或只保留到确认页使用。保留 `onLoad` 读取 `id`。

- [ ] **Step 2: 新增兜底详情**

定义外卖配送员兜底数据：标题、薪资、地点、企业、职责、要求、schedules。

- [ ] **Step 3: 改造模板**

实现设计稿详情结构：导航、banner、薪资卡、基本信息、职责、要求、企业卡、底部电话和立即报名。

- [ ] **Step 4: 改造立即报名**

如果未选择排班，toast 提示；已选择后跳转：`/pages/jobs/applyConfirm?jobId=<id>&scheduleIds=<ids>`。

- [ ] **Step 5: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 2: 报名确认页

**Files:**
- Create: `worker-uniapp/src/pages/jobs/applyConfirm.vue`
- Modify: `worker-uniapp/src/pages.json`

- [ ] **Step 1: 注册页面**

在 `pages.json` 中 `jobDetail` 后新增：

```json
{
  "path": "pages/jobs/applyConfirm",
  "style": { "navigationBarTitleText": "确认报名" }
}
```

- [ ] **Step 2: 创建页面脚本**

读取 `jobId`、`scheduleIds`，展示默认岗位摘要，点击确认调用 `applyJob(jobId, { scheduleIds })`。

- [ ] **Step 3: 创建页面模板和样式**

实现步骤条、岗位摘要、日期/时段选择、报名须知、联系人、底部按钮。

- [ ] **Step 4: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 3: 我的排班页

**Files:**
- Modify: `worker-uniapp/src/pages/schedule/schedule.vue`

- [ ] **Step 1: 保留业务依赖**

保留 `getMyShifts`、`submitCorrection`、周切换、日期选择、补卡弹窗。

- [ ] **Step 2: 增加兜底排班和统计**

接口失败或空数据时展示设计稿式排班；新增统计：出勤天数、工时、迟到次数、缺勤次数。

- [ ] **Step 3: 改造模板和样式**

改为设计稿打卡记录/排班卡片风格：顶部月份/周切换、统计卡、排班卡片、状态 badge、补卡入口。

- [ ] **Step 4: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 4: 打卡记录页

**Files:**
- Modify: `worker-uniapp/src/pages/attendance/clockIn.vue`

- [ ] **Step 1: 保留业务依赖**

保留 `getMyShifts`、`checkIn`、`checkOut`、定位。

- [ ] **Step 2: 增加兜底今日排班**

接口失败或空数据时展示今日排班示例；兜底负 id 不触发真实打卡接口。

- [ ] **Step 3: 改造模板和样式**

实现日期 header、排班卡片、地点、状态 badge、签到/签退按钮。

- [ ] **Step 4: 验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: 构建成功。

---

### Task 5: 全量验证、提交、推送

**Files:**
- Verify all modified files

- [ ] **Step 1: 构建验证**

Run: `npm run build:mp-weixin`
Workdir: `worker-uniapp`
Expected: `DONE Build complete.`

- [ ] **Step 2: 检查变更范围**

Run: `git status --short`
Expected: 只包含核心闭环页面和路由变更。

- [ ] **Step 3: 提交推送**

Commit message: `feat: redesign worker core miniapp pages`

---

## Self-Review

- Spec coverage: 覆盖详情、报名确认、排班、打卡记录和 pages.json。
- Placeholder scan: 无未完成占位项。
- Type consistency: 参数统一使用 `jobId`、`scheduleIds`。