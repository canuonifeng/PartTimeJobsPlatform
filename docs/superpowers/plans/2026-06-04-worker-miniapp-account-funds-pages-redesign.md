# 工人端微信小程序 账号与资金页面 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 改造登录、收入、提现、实名、银行卡、资料编辑页面，并新增设置页。

**Architecture:** 保留现有接口和业务逻辑，只重做页面模板和样式；新增设置页并注册路由，个人中心设置入口跳转真实设置页。

**Tech Stack:** UniApp 3、Vue 3、微信小程序、Pinia、`rpx`、现有 API 模块。

---

## File Structure

- Modify: `worker-uniapp/src/pages/login/login.vue`
- Modify: `worker-uniapp/src/pages/earnings/earnings.vue`
- Modify: `worker-uniapp/src/pages/earnings/withdraw.vue`
- Modify: `worker-uniapp/src/pages/auth/realName.vue`
- Modify: `worker-uniapp/src/pages/bank/bankCard.vue`
- Modify: `worker-uniapp/src/pages/profile/edit.vue`
- Create: `worker-uniapp/src/pages/settings/settings.vue`
- Modify: `worker-uniapp/src/pages.json`
- Modify: `worker-uniapp/src/pages/profile/profile.vue`

---

### Task 1: 登录页

**Files:**
- Modify: `worker-uniapp/src/pages/login/login.vue`

- [ ] 保留微信登录、手机号登录、验证码发送、redirect 逻辑。
- [ ] 改造为设计稿绿色登录页和卡片表单。
- [ ] 移除低兼容 CSS，如 flex gap。
- [ ] 运行 `npm run build:mp-weixin`。

### Task 2: 收入与提现

**Files:**
- Modify: `worker-uniapp/src/pages/earnings/earnings.vue`
- Modify: `worker-uniapp/src/pages/earnings/withdraw.vue`

- [ ] 收入页改为橙色头部、统计、筛选 tabs、日期分组交易列表。
- [ ] 保留收入接口、考勤接口、分页加载，失败时兜底。
- [ ] 提现页改为余额卡、gate 提示、金额输入、快捷金额、提交。
- [ ] 保留提现接口和实名/银行卡 gate。
- [ ] 运行 `npm run build:mp-weixin`。

### Task 3: 实名、银行卡、资料编辑

**Files:**
- Modify: `worker-uniapp/src/pages/auth/realName.vue`
- Modify: `worker-uniapp/src/pages/bank/bankCard.vue`
- Modify: `worker-uniapp/src/pages/profile/edit.vue`

- [ ] 实名页改为状态卡 + 表单卡，保留状态加载和提交。
- [ ] 银行卡页改为银行卡预览 + 表单卡，保留绑定/更新/解绑。
- [ ] 资料编辑页改为设计系统风格，保留资料加载和保存。
- [ ] 清理空 catch 和低兼容 CSS。
- [ ] 运行 `npm run build:mp-weixin`。

### Task 4: 设置页与入口

**Files:**
- Create: `worker-uniapp/src/pages/settings/settings.vue`
- Modify: `worker-uniapp/src/pages.json`
- Modify: `worker-uniapp/src/pages/profile/profile.vue`

- [ ] 新增设置页，包含账号与安全、通知与隐私、通用、其他、退出登录。
- [ ] 注册 `pages/settings/settings`，标题“设置”。
- [ ] 修改个人中心设置菜单跳转真实设置页。
- [ ] 已有页面正常跳转，不存在页面 toast。
- [ ] 运行 `npm run build:mp-weixin`。

### Task 5: 全量验证、提交、推送

- [ ] 运行 `npm run build:mp-weixin`。
- [ ] 运行 `git status --short` 和 `git diff --stat`。
- [ ] 提交：`feat: redesign worker account funds pages`
- [ ] 推送到 `main`。

---

## Self-Review

- Spec coverage: 覆盖账号与资金页面、设置页和入口。
- Placeholder scan: 无未完成占位项。
- Type consistency: 保留现有接口参数和页面路由。
